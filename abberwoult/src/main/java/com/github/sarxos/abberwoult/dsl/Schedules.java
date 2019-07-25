package com.github.sarxos.abberwoult.dsl;

import static com.github.sarxos.abberwoult.dsl.TimerSupport.cancel;
import static com.github.sarxos.abberwoult.dsl.TimerSupport.createSchedule;
import static com.github.sarxos.abberwoult.dsl.TimerSupport.createSendAction;
import static java.time.Duration.ZERO;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.Receives;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Scheduler;
import scala.concurrent.ExecutionContext;


/**
 * This interface can be implemented by the actor whenever it should be able to deal with a
 * scheduled tasks.
 *
 * @author Bartosz Firyn (sarxos)
 */
public interface Schedules extends ActorInternal {

	/**
	 * Return mapping between schedule name and corresponding {@link Schedule} object.
	 *
	 * @return Mapping between schedule name and corresponding {@link Schedule}
	 */
	Map<String, Schedule> getSchedules();

	default void onScheduleCancelMsgAck(@Receives final ScheduleCancelMsg.Ack ack) {
		// override if needed
	}

	/**
	 * Cancel schedule with a given name.
	 *
	 * @param msg the message with schedule name
	 */
	default void onScheduleCancelMsg(@Receives final ScheduleCancelMsg msg) {

		final ActorRef sender = sender();
		final ActorRef self = self();
		final String name = msg.getName();
		final boolean cancelled = cancelSchedule(name);

		sender.tell(new ScheduleCancelMsg.Ack(name, cancelled), self);
	}

	/**
	 * Remove schedule with a given name.
	 *
	 * @param msg the message with schedule name
	 */
	default void onScheduleRemoveMsg(@Receives final ScheduleRemoveMsg msg) {

		final Map<String, Schedule> schedules = getSchedules();
		final String name = msg.getName();

		schedules.remove(name);
	}

	/**
	 * Cancel all recorded schedules. This method is automatically invoked when actor is stopped so
	 * there is no need to invoke it manually.
	 */
	@PostStop
	default void cancelAllSchedules() {

		final Map<String, Schedule> schedules = getSchedules();
		if (schedules.isEmpty()) {
			return;
		}

		schedules
			.values()
			.forEach(Schedule::cancel);

		schedules.clear();
	}

	/**
	 * Cancel {@link Schedule} with a given name and subsequently remove it from a tracked schedules
	 * list. It will return true if and only if schedule was successfully cancelled or false
	 * otherwise, e.g. when schedule has been already cancelled or when schedule with a given name
	 * does not exist.
	 *
	 * @param name - a name of schedule to cancel
	 * @return True if schedule was cancelled or false otherwise
	 */
	default boolean cancelSchedule(final String name) {

		final Map<String, Schedule> schedules = getSchedules();
		final Schedule schedule = schedules.remove(name);

		return cancel(schedule);
	}

	/**
	 * Schedule single message delivery. Message will be delivered to {@link #self()}.
	 *
	 * @param name the schedule name
	 * @param message the scheduled message
	 * @param delay the time to wait before sending message
	 * @return Newly created {@link Schedule}
	 */
	default Schedule scheduleOnce(final String name, final Object message, final Duration delay) {
		return scheduleOnce(name, message, self(), delay);
	}

	/**
	 * Schedule single message delivery. Message will be delivered to the recipient.
	 *
	 * @param name the schedule name
	 * @param message the scheduled message
	 * @param recipient the message recipient
	 * @param delay the time to wait before sending message
	 * @return Newly created {@link Schedule}
	 */
	default Schedule scheduleOnce(final String name, final Object message, final ActorRef recipient, final Duration delay) {
		return schedule(name, message, recipient, delay, ZERO, true);
	}

	/**
	 * Schedule message delivery at fixed interval. The message will be send to {@link #self()}.
	 *
	 * @param name the schedule name
	 * @param message the message to be delivered in a fixed time intervals
	 * @param interval the time interval
	 * @return {@link Schedule} corresponding to the scheduled action
	 */
	default Schedule schedule(final String name, final Object message, final Duration interval) {
		return schedule(name, message, self(), interval);
	}

	/**
	 * Schedule message delivery at fixed interval. The message will be send to recipient.
	 *
	 * @param name the schedule name
	 * @param message the message to be delivered in a fixed time intervals
	 * @param recipient the message recipient
	 * @param interval the time interval
	 * @return {@link Schedule} corresponding to the scheduled action
	 */
	default Schedule schedule(final String name, final Object message, final ActorRef recipient, final Duration interval) {
		return schedule(name, message, recipient, interval, interval, false);
	}

	/**
	 * Schedule message delivery at fixed interval. The message will be send to recipient.
	 *
	 * @param name the schedule name
	 * @param message the message to be delivered in a fixed time intervals
	 * @param recipient the message recipient
	 * @param delay the initial delay before message is send
	 * @param interval the time interval
	 * @return {@link Schedule} corresponding to the scheduled action
	 */
	default Schedule schedule(final String name, final Object message, final ActorRef recipient, final Duration delay, final Duration interval, final boolean once) {

		final ActorRef self = self();
		final ActorContext context = getContext();
		final ActorSystem system = context.system();
		final Scheduler scheduler = system.scheduler();
		final ExecutionContext dispatcher = system.dispatcher();
		final Runnable action = createSendAction(name, self, recipient, message, once);
		final Cancellable cancellable = createSchedule(scheduler, delay, interval, action, dispatcher, once);

		final Schedule schedule = new Schedule(name, cancellable, self);
		final Map<String, Schedule> schedules = getSchedules();
		final Cancellable previous = schedules.put(name, schedule);

		cancel(previous);

		return schedule;
	}

	/**
	 * Abstraction of {@link Cancellable} action.
	 */
	public final class Schedule implements Cancellable {

		private final String name;
		private final Cancellable cancellable;
		private final ActorRef owner;

		/**
		 * @param name - the schedule name
		 * @param cancellable - the
		 * @param owner
		 */
		Schedule(final String name, final Cancellable cancellable, final ActorRef owner) {
			this.name = name;
			this.cancellable = cancellable;
			this.owner = owner;

		}

		public String getName() {
			return name;
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

			if (cancellable.isCancelled()) {
				return false;
			}

			final boolean cancelled = cancellable.cancel();

			if (cancelled) {
				owner.tell(new ScheduleRemoveMsg(name), owner);
			}

			return cancelled;
		}

		/**
		 * Returns true if and only if this task has been successfully cancelled.
		 */
		@Override
		public boolean isCancelled() {
			return cancellable.isCancelled();
		}
	}

	/**
	 * Message to cancel given timer task.
	 */
	final class ScheduleCancelMsg {

		private final String name;

		public ScheduleCancelMsg(String name) {
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

			public String getName() {
				return name;
			}

			public boolean isCanceled() {
				return canceled;
			}
		}
	}
}

/**
 * Message to cancel given timer task.
 */
final class ScheduleRemoveMsg {

	private final String name;

	public ScheduleRemoveMsg(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}

final class TimerSupport {

	static Runnable createSendAction(final String name, final ActorRef self, final ActorRef rcpt, final Object message, final boolean once) {
		return () -> {
			rcpt.tell(message, self);
			if (once) {
				self.tell(new ScheduleRemoveMsg(name), self);
			}
		};
	}

	static Cancellable createSchedule(final Scheduler scheduler, final Duration delay, final Duration interval, final Runnable action, final ExecutionContext dispatcher, final boolean once) {
		if (once) {
			return scheduler.scheduleOnce(delay, action, dispatcher);
		} else {
			return scheduler.schedule(delay, interval, action, dispatcher);
		}
	}

	/**
	 * Cancel given {@link Cancellable} or do nothing if argument is null. This method will return
	 * true if {@link Cancellable} was cancelled or false if it has already been cancelled before or
	 * null was provided.
	 *
	 * @param cancellable the {@link Cancellable} to cancel
	 * @return True if {@link Cancellable} was cancelled or false otherwise
	 */
	static boolean cancel(final Cancellable cancellable) {
		if (cancellable == null) {
			return false;
		} else {
			return cancellable.cancel();
		}
	}
}
