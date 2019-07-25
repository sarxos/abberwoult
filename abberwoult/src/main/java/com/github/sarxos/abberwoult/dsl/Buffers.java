package com.github.sarxos.abberwoult.dsl;

import static com.github.sarxos.abberwoult.util.CollectorUtils.emptyDeque;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.Predicates.is;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.sarxos.abberwoult.dsl.Buffers.Buffer;
import com.github.sarxos.abberwoult.dsl.Buffers.BufferMessage;
import com.github.sarxos.abberwoult.dsl.Buffers.Decider;

import akka.actor.AbstractActor.ActorContext;
import akka.actor.AbstractActor.Receive;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.pf.ReceiveBuilder;
import io.vavr.control.Option;
import scala.concurrent.ExecutionContext;


/**
 * This interface can be implemented by the actor whenever it should be able to deal with a messages
 * buffering logic.
 *
 * @author Bartosz Firyn (sarxos)
 */
public interface Buffers extends ActorInternal {

	/**
	 * The buffering abstraction.
	 */
	interface Buffer<T> {

		/**
		 * Get buffered messages.
		 *
		 * @return Buffered messages
		 */
		Deque<BufferMessage> getMessages();

		/**
		 * Given callback will be invoked when buffering timed out.
		 *
		 * @param duration the timeout duration
		 * @param callback the callback to be invoked after {@link Buffer} timed out
		 * @return This {@link Buffer}
		 */
		Buffer<T> onTimeout(final Duration duration, final Consumer<Deque<BufferMessage>> callback);

		/**
		 * This callback will be invoked when buffering is completed with success (underlying
		 * decided decided to stop buffering).
		 *
		 * @param callback the callback to be invoked
		 * @return This {@link Buffer}
		 */
		Buffer<T> onSuccess(final Consumer<T> callback);

		/**
		 * This callback will be invoked when buffering failed because {@link Buffer} received some
		 * {@link Throwable} message. When failure behavior is not defined all {@link Throwable}
		 * messages will be buffered.
		 *
		 * @param callback the callback to be invoked with {@link Throwable} as the argument
		 * @return This {@link Buffer}
		 */
		Buffer<T> onFailure(final Consumer<Throwable> callback);
	}

	/**
	 * This is abstraction which decides if buffering should be completed. Decider is a
	 * {@link Predicate} which will complete buffering when resolved to true. Decider has an ability
	 * to discard processed message. Discarded message will not be buffered in a {@link Buffer}
	 * which uses given decider as a completion logic. To discard message one should use
	 * {@link #discard(Object)} method of a given decider.
	 *
	 * @author sarxos
	 * @param <T>
	 */
	abstract class Decider<T> implements Predicate<T> {

		private Object discarded;

		/**
		 * Discard message. Discarded message will not be buffered by a {@link Buffer} which use
		 * this decider.
		 *
		 * @param message the message
		 */
		public final void discard(final Object message) {
			this.discarded = message;
		}

		/**
		 * Return true if given message was discarded in current deciding context.
		 *
		 * @param message the message to test
		 * @return True if message is discarded or false othrwise
		 */
		final boolean isDiscarded(final Object message) {
			return discarded == message;
		}
	}

	/**
	 * A class to abstract message stored in a {@link Buffer}.
	 */
	final class BufferMessage {

		private final Object value;
		private final ActorRef sender;

		BufferMessage(final Object value, final ActorRef sender) {
			this.value = value;
			this.sender = sender;
		}

		/**
		 * @return The message itself
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * @return The message sender
		 */
		public ActorRef getSender() {
			return sender;
		}
	}

	/**
	 * Create {@link Buffer} and start buffering all incoming messages until given message is
	 * received by the actor. Previous {@link Receive} behavior is put on a stack from where it will
	 * be popped automatically when this {@link Buffer} is completed (when succeeded, failed or
	 * timed out).
	 *
	 * @param <T> the expected message type
	 * @param message the expected message
	 * @return New {@link Buffer} which will hold buffered messages
	 */
	default <T> Buffer<T> bufferUntilReceivedMessage(final T message) {
		return bufferUntilDecidedBy(new BufferDecider<>(is(message)));
	}

	/**
	 * Create {@link Buffer} and start buffering all incoming messages until message of a given
	 * class is received by the actor. Previous {@link Receive} behavior is put on a stack from
	 * where it will be popped automatically when this {@link Buffer} is completed (when succeeded,
	 * failed or timed out).
	 *
	 * @param <T> the expected message type
	 * @param clazz the expected message class
	 * @return New {@link Buffer} which will hold buffered messages
	 */
	default <T> Buffer<T> bufferUntilReceivedMessageOf(final Class<T> clazz) {
		return bufferUntilDecidedBy(new BufferDecider<>(instanceOf(clazz)));
	}

	/**
	 * Create {@link Buffer} and start buffering all incoming messages until given predicate is
	 * resolved by one of the received messages. Previous {@link Receive} behavior is put on a stack
	 * from where it will be popped automatically when this {@link Buffer} is completed (when
	 * succeeded, failed or timed out).
	 *
	 * @param <T> the expected message type
	 * @param clazz the expected message class
	 * @return New {@link Buffer} which will hold buffered messages
	 */
	default <T> Buffer<T> bufferUntil(final Predicate<Object> predicate) {
		return bufferUntilDecidedBy(new BufferDecider<Object>(predicate));
	}

	/**
	 * Create {@link Buffer} and start buffering all incoming messages until given {@link Decider}
	 * decide to stop. Previous {@link Receive} behavior is put on a stack from where it will be
	 * popped automatically when this {@link Buffer} is completed (when succeeded, failed or timed
	 * out).
	 *
	 * @param <T> the expected message type
	 * @param decider the buffering completion decision maker
	 * @return New {@link Buffer} which will hold buffered messages
	 */
	default <T> Buffer<T> bufferUntilDecidedBy(final Decider<Object> decider) {
		return new BufferImpl<>(getContext(), decider);
	}
}

/**
 * This is {@link Buffer} implementation.
 *
 * @author Bartosz Firyn (sarxos)
 * @param <T> the generic type used for inference
 */
final class BufferImpl<T> implements Buffer<T> {

	private final Object cancellation = new Object();
	private final Deque<BufferMessage> messages = new LinkedList<>();
	private final ActorContext context;
	private final Decider<Object> decider;
	private Option<BufferTimeout<T>> timeout = Option.none();
	private Option<Consumer<T>> success = Option.none();
	private Option<Consumer<Throwable>> failure = Option.none();

	BufferImpl(final ActorContext context, final Decider<Object> decider) {
		this.context = context;
		this.context.become(processor(), false); // false = do not discard old behavior
		this.decider = decider;
	}

	@Override
	public Deque<BufferMessage> getMessages() {
		return messages;
	}

	@Override
	public Buffer<T> onTimeout(final Duration duration, final Consumer<Deque<BufferMessage>> callback) {
		timeout = Option.some(createTimeout(duration, callback));
		return this;
	}

	@Override
	public Buffer<T> onSuccess(final Consumer<T> callback) {
		success = Option.some(callback);
		return this;
	}

	@Override
	public Buffer<T> onFailure(final Consumer<Throwable> callback) {
		failure = Option.some(callback);
		return this;
	}

	private Receive processor() {
		return ReceiveBuilder.create()
			.match(Throwable.class, this::failure)
			.matchEquals(cancellation, this::timeout)
			.matchAny(this::process)
			.build();
	}

	private void process(final Object message) {
		if (decider.test(message)) {
			success(message);
		} else {
			store(message);
		}
	}

	private void timeout(final Object cancellation) {

		final Deque<BufferMessage> data;

		if (messages.isEmpty()) {
			data = emptyDeque();
		} else {
			data = new ArrayDeque<>(messages);
		}

		try {
			timeout.forEach(callback -> callback.invoke(data));
		} finally {
			release();
		}
	}

	private void failure(final Object message) {
		if (failure.isDefined()) {
			try {
				failure.forEach(handler -> handler.accept((Throwable) message));
			} finally {
				release();
			}
		} else {
			store(message);
		}
	}

	@SuppressWarnings("unchecked")
	private void success(final Object message) {
		try {
			success.forEach(handler -> handler.accept((T) message));
		} finally {
			release();
		}
	}

	private void release() {

		context.unbecome();

		while (!messages.isEmpty()) {
			resend(messages.remove());
		}
	}

	private void resend(final BufferMessage message) {

		final Object value = message.getValue();
		final ActorRef sender = message.getSender();

		context
			.self()
			.tell(value, sender);
	}

	private void store(final Object message) {
		if (!decider.isDiscarded(message)) {
			messages.add(new BufferMessage(message, context.sender()));
		}
	}

	/**
	 * Create new {@link BufferTimeout} which encapsulates timeout behavior.
	 *
	 * @param duration the timeout duration
	 * @param callback the callback to be invoked when this {@link Buffer} timeouted
	 * @return New {@link BufferTimeout} instance
	 */
	private BufferTimeout<T> createTimeout(final Duration duration, final Consumer<Deque<BufferMessage>> callback) {
		return new BufferTimeout<>(schedule(duration, cancellation), callback);
	}

	/**
	 * Schedule message to be send to context self after specified duration.
	 *
	 * @param duration the duration interval to wait before message is send
	 * @param message the message to be send
	 * @return {@link Cancellable} instance
	 */
	private Cancellable schedule(final Duration duration, final Object message) {

		final ActorRef self = context.self();
		final ActorSystem system = context.system();
		final ExecutionContext dispatcher = system.dispatcher();

		return system
			.scheduler()
			.scheduleOnce(duration, self, message, dispatcher, self);
	}
}

/**
 * {@link Decider} which takes {@link Predicate} to decide when buffering should be completed.
 *
 * @author Bartosz Firyn (sarxos)
 * @param <T> used only to infer type
 */
class BufferDecider<T> extends Decider<T> {

	private final Predicate<Object> predicate;

	BufferDecider(final Predicate<Object> predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(final Object object) {
		return predicate.test(object);
	}
}

/**
 * This class encapsulates timeout behavior. This is {@link Cancellable} and therefore can be
 * cancelled at any time.
 *
 * @author Bartosz Firyn (sarxos)
 * @param <T>
 */
final class BufferTimeout<T> implements Cancellable {

	private final Consumer<Deque<BufferMessage>> callback;
	private final Cancellable schedule;

	BufferTimeout(final Cancellable schedule, final Consumer<Deque<BufferMessage>> callback) {
		this.schedule = schedule;
		this.callback = callback;
	}

	@Override
	public boolean cancel() {
		return schedule.cancel();
	}

	@Override
	public boolean isCancelled() {
		return schedule.isCancelled();
	}

	void invoke(final Deque<BufferMessage> messages) {
		callback.accept(messages);
	}
}
