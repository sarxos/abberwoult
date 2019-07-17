package com.github.sarxos.abberwoult.dsl;

import static java.time.Duration.ZERO;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import com.github.sarxos.abberwoult.ActorSystemUniverse;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.Receives;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Scheduler;
import io.vavr.control.Option;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.FiniteDuration;


public interface Timers extends ActorInternal {

	/**
	 * Get the {@link ActorRef} which points the {@link TimerActor} or create new one if there is no
	 * such actor yet.
	 *
	 * @return {@link ActorRef} pointing to the internal timer actor
	 */
	default ActorRef getTimerRef() {

		final String name = TimerActor.INTERNAL_ACTOR_NAME;
		final ActorSystemUniverse universe = getUniverse();
		final ActorContext context = getContext();

		// The timer actor reference or null if timer actor does not exist, yet.

		final ActorRef child = context
			.child(name)
			.getOrElse(() -> null);

		return Option.of(child)
			.getOrElse(() -> universe.actor()
				.of(TimerActor.class)
				.withParent(context)
				.withName(name)
				.create());
	}

	default void onTimerScheduleMsgAck(@Receives final TimerScheduleMsg.Ack ack) {
		// override if needed
	}

	default void onTimerCancelMsgAck(@Receives final TimerCancelMsg.Ack ack) {
		// override if needed
	}

	default TimerTask scheduleOnce(final String name, final Object message, final Duration delay) {
		return scheduleOnce(name, message, self(), delay);
	}

	default TimerTask scheduleOnce(final String name, final Object message, final ActorRef recipient, final Duration delay) {

		final ActorRef timer = getTimerRef();
		final ActorRef owner = self();
		final TimerScheduleMsg schedule = new TimerScheduleMsg(name, message, delay, ZERO, owner, recipient, true);

		timer.tell(schedule, owner);

		return new TimerTask(name, timer, owner);
	}

	/**
	 * Schedule message delivery at fixed interval. The message will be send to {@link #self()}.
	 *
	 * @param name the schedule name
	 * @param message the message to be delivered in a fixed time intervals
	 * @param interval the time interval
	 * @return {@link TimerTask} corresponding to the scheduled action
	 */
	default TimerTask schedule(final String name, final Object message, final Duration interval) {
		return schedule(name, message, self(), interval);
	}

	/**
	 * Schedule message delivery at fixed interval. The message will be send to recipient.
	 *
	 * @param name the schedule name
	 * @param message the message to be delivered in a fixed time intervals
	 * @param recipient the message recipient
	 * @param interval the time interval
	 * @return {@link TimerTask} corresponding to the scheduled action
	 */
	default TimerTask schedule(final String name, final Object message, final ActorRef recipient, final Duration interval) {
		return schedule(name, message, recipient, interval, interval);
	}

	/**
	 * Schedule message delivery at fixed interval. The message will be send to recipient.
	 *
	 * @param name the schedule name
	 * @param message the message to be delivered in a fixed time intervals
	 * @param recipient the message recipient
	 * @param delay the initial delay before message is send
	 * @param interval the time interval
	 * @return {@link TimerTask} corresponding to the scheduled action
	 */
	default TimerTask schedule(final String name, final Object message, final ActorRef recipient, final Duration delay, final Duration interval) {

		final ActorRef timer = getTimerRef();
		final ActorRef owner = self();
		final TimerScheduleMsg schedule = new TimerScheduleMsg(name, message, delay, interval, owner, recipient, false);

		timer.tell(schedule, owner);

		return new TimerTask(name, timer, owner);

	}

	/**
	 * Abstraction of {@link Cancellable} timer task. Please note that this class is not
	 * thread-safe. It must not be leaked outside the actor which scheduled given task. The
	 * cancellation must be done in message scope.
	 */
	public final class TimerTask implements Cancellable {

		private final String name;
		private final ActorRef timer;
		private final ActorRef scheduler;
		private boolean cancelled = false;

		/**
		 * @param name - the timer task name
		 * @param timer - timer actor reference, a child of the scheduling actor
		 * @param scheduler - scheduling actor reference
		 */
		TimerTask(final String name, final ActorRef timer, final ActorRef scheduler) {
			this.name = name;
			this.timer = timer;
			this.scheduler = scheduler;
		}

		/**
		 * Cancels this task and returns true if cancellation was successful. If this task was
		 * cancelled already, then this method will return false although {@link #isCancelled()}
		 * will return true. Please keep in mind that the state of this task is bound to the
		 * enclosing actor and thus the same actor-related encapsulation rules must be applied. For
		 * example do not call {@link #cancel()} or {@link #isCancelled()} from within the anonymous
		 * actor class or from asynchronous {@link CompletionStage} callback. This will break actor
		 * encapsulation and introduce synchronization and race conditions bugs because the code
		 * will be executed concurrently to the enclosing actor.
		 */
		@Override
		public boolean cancel() {

			if (cancelled) {
				return false;
			}

			timer.tell(new TimerCancelMsg(name), scheduler);
			cancelled = true;

			return true;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}
	}
}

/**
 * Internal actor to abstract timer. This actor manages cancellables used by its parent.
 */
final class TimerActor extends SimpleActor implements Utils, Timeouts {

	/**
	 * Internal child actor name.
	 */
	static final String INTERNAL_ACTOR_NAME = "__timer";

	/**
	 * Map to track {@link Cancellable} schedules.
	 */
	private final Map<String, Cancellable> cancellables = new HashMap<>(1);

	/**
	 * Cancel all tracked cancellables after this actor is stopped.
	 */
	@PostStop
	protected void teardown() {
		cancelAll();
	}

	/**
	 * Create new schedule for a given task.
	 *
	 * @param schedule the schedule message
	 * @return Acknowledgement
	 */
	public void onTimerScheduleMsg(@Receives final TimerScheduleMsg schedule) {

		final String name = schedule.getName();
		final FiniteDuration delay = FiniteDuration.fromNanos(schedule.getDelay().toNanos());
		final FiniteDuration interval = FiniteDuration.fromNanos(schedule.getInterval().toNanos());

		final Runnable action = deliver(schedule);
		final ActorContext context = getContext();
		final Scheduler scheduler = context.getSystem().getScheduler();
		final ExecutionContextExecutor dispatcher = context.getDispatcher();

		// First we need to cancel previous cancellable, if there was any, and
		// then replace it with a newly created one to avoid having two cancellables
		// with the same name at the same time.

		final boolean cancelled = cancel(name);

		final Cancellable cancellable;
		if (schedule.isOnce()) {
			cancellable = scheduler.scheduleOnce(delay, action, dispatcher);
		} else {
			cancellable = scheduler.schedule(delay, interval, action, dispatcher);
		}

		cancellables.put(name, cancellable);

		reply(new TimerScheduleMsg.Ack(name, cancelled));
	}

	private Runnable deliver(final TimerScheduleMsg schedule) {
		if (schedule.isOnce()) {
			return sendOnce(schedule);
		} else {
			return send(schedule);
		}
	}

	private Runnable send(final TimerScheduleMsg schedule) {

		final Object message = schedule.getMessage();
		final ActorRef from = schedule.getSender();
		final ActorRef rcpt = schedule.getRecipient();

		return () -> rcpt.tell(message, from);
	}

	private Runnable sendOnce(final TimerScheduleMsg schedule) {

		final String name = schedule.getName();
		final Object message = schedule.getMessage();
		final ActorRef from = schedule.getSender();
		final ActorRef rcpt = schedule.getRecipient();
		final ActorRef self = self();

		return () -> {
			rcpt.tell(message, from);
			self.tell(new TimerCancelMsg(name), self);
		};
	}

	/**
	 * Cancel task with a given name.
	 *
	 * @param msg the message with task name
	 */
	public void onTimerCancelMsg(@Receives final TimerCancelMsg msg) {

		final String name = msg.getName();
		final boolean cancelled = cancel(name);

		reply(new TimerCancelMsg.Ack(name, cancelled));

		if (cancellables.isEmpty()) {
			dispose();
		}
	}

	/**
	 * Internal API to be used only for unit test purpose. Return tracked cancellables.
	 *
	 * @param msg the message
	 */
	public void onTimerListMsg(@Receives final TimerListMsg msg) {
		reply(new TimerListMsg.Result(new HashMap<>(cancellables)));
	}

	/**
	 * Cancel all cancellables tracked by this actor.
	 *
	 * @param msg the message
	 */
	public void onTimerCancelAllMsg(@Receives final TimerCancelAllMsg msg) {
		cancelAll();
		reply(TimerCancelAllMsg.Ack.getInstance());
		dispose();
	}

	/**
	 * Close this {@link TimersService} and cancel all tracked {@link Cancellable} schedules. This
	 * method is invoked from a {@link TimersSupport} trait which contains necessary
	 * {@link MessageHandler} but it can also be invoked manually if there is a need to cancel all
	 * {@link Cancellable} schedules before actor termination.
	 */
	public void cancelAll() {
		cancellables.forEach((name, cancellable) -> cancellable.cancel());
		cancellables.clear();
	}

	/**
	 * Cancel {@link Cancellable} with a given name and subsequently remove it from a tracked
	 * cancellables list. It will return true if cancellable was cancelled or false otherwise, e.g.
	 * when cancellable has been already cancelled or if there is no cancellable with a given name
	 * tracked.
	 *
	 * @param name a name given to cancellable
	 * @return True if cancellable was cancelled or false otherwise
	 */
	public boolean cancel(final String name) {
		return cancel(cancellables.remove(name));
	}

	/**
	 * Cancel given {@link Cancellable} or do nothing if argument is null. This method will return
	 * true if {@link Cancellable} was cancelled or false if it has already been cancelled before or
	 * null was provided.
	 *
	 * @param cancellable the {@link Cancellable} to cancel
	 * @return True if {@link Cancellable} was cancelled or false otherwise
	 */
	private boolean cancel(final Cancellable cancellable) {
		if (cancellable != null) {
			return cancellable.cancel();
		} else {
			return false;
		}
	}

	/**
	 * Return a {@link Map} where keys are names and values are corresponding {@link Cancellable}
	 * schedules.
	 *
	 * @return Map of tracked {@link Cancellable}
	 */
	public Map<String, Cancellable> getCancellables() {
		return cancellables;
	}

	@Override
	public int getReceiveTimeout() {
		return NO_RECEIVE_TIMEOUT;
	}
}

/**
 * Message to create new timer task.
 */
final class TimerScheduleMsg {

	private final String name;
	private final Object message;
	private final Duration delay;
	private final Duration interval;
	private final ActorRef sender;
	private final ActorRef recipient;
	private final boolean once;

	TimerScheduleMsg(String name, Object message, Duration delay, Duration interval, ActorRef sender, ActorRef recipient, boolean once) {
		this.name = requireNonNull(name, "Schedule name must not be null");
		this.message = requireNonNull(message, "Scheduled message must not be null");
		this.delay = requireNonNull(delay, "Scheduled delivery delay must not be null");
		this.interval = requireNonNull(interval, "Scheduled delivery interval must not be null");
		this.sender = requireNonNull(sender, "Scheduled message sender must not be null");
		this.recipient = requireNonNull(recipient, "Scheduled message recipient must not be null");
		this.once = once;
	}

	public String getName() {
		return name;
	}

	public Object getMessage() {
		return message;
	}

	public Duration getDelay() {
		return delay;
	}

	public Duration getInterval() {
		return interval;
	}

	public ActorRef getSender() {
		return sender;
	}

	public ActorRef getRecipient() {
		return recipient;
	}

	public boolean isOnce() {
		return once;
	}

	public static final class Ack {

		private final String name;
		private final boolean cancelled;

		public Ack(String name, boolean cancelled) {
			this.name = name;
			this.cancelled = cancelled;
		}
	}
}

/**
 * Internal API for unit test purpose only. Please do not use it in implementation.
 */
final class TimerListMsg {

	private static final TimerListMsg INSTANCE = new TimerListMsg();

	public static TimerListMsg getInstance() {
		return INSTANCE;
	}

	public static final class Result {

		private final Map<String, Cancellable> cancellables;

		public Result(Map<String, Cancellable> cancellables) {
			this.cancellables = cancellables;
		}
	}
}

/**
 * Internal API for unit test purpose only. Please do not use it in implementation.
 */
final class TimerCancelAllMsg {

	private static final TimerCancelAllMsg INSTANCE = new TimerCancelAllMsg();

	public static TimerCancelAllMsg getInstance() {
		return INSTANCE;
	}

	public static final class Ack {

		private static final Ack INSTANCE = new Ack();

		public static Ack getInstance() {
			return INSTANCE;
		}
	}
}

/**
 * Message to cancel given timer task.
 */
final class TimerCancelMsg {

	private final String name;

	public TimerCancelMsg(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static final class Ack {

		private final String name;
		private final boolean canceled;

		public Ack(String name, boolean canceled) {
			this.name = name;
			this.canceled = canceled;
		}
	}
}
