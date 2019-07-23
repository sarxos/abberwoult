package com.github.sarxos.abberwoult.dsl;

import static io.vavr.Predicates.is;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
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


public interface Buffers extends ActorInternal {

	interface Buffer<T> {

		Deque<BufferMessage> getMessages();

		Buffer<T> onTimeout(final Duration duration, final Consumer<Deque<BufferMessage>> callback);

		Buffer<T> onSuccess(final Consumer<T> callback);

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

	final class BufferMessage {

		final Object value;
		final ActorRef sender;

		BufferMessage(final Object value, final ActorRef sender) {
			this.value = value;
			this.sender = sender;
		}
	}

	default <T> Buffer<T> bufferUntilReceivedMessage(final T message) {
		return new BufferImpl<>(getContext(), new BufferDecider<>(is(message)));
	}

	default <T> Buffer<T> bufferUntilReceivedMessageOf(final Class<T> clazz) {
		return new BufferImpl<>(getContext(), new BufferDecider<>(clazz::isInstance));
	}

	default Buffer<Object> bufferUntil(final Predicate<Object> predicate) {
		return new BufferImpl<>(getContext(), new BufferDecider<>(predicate));
	}

	default <T> Buffer<Object> bufferUntilDecidedBy(final Decider<Object> decider) {
		return new BufferImpl<>(getContext(), decider);
	}
}

final class BufferImpl<T> implements Buffer<T> {

	private final Object btt = new Object();
	private final Deque<BufferMessage> messages = new ArrayDeque<>(1);
	private final ActorContext context;
	private final Decider<Object> decider;
	private Option<BufferTimeout<T>> timeout = Option.none();
	private Option<Consumer<T>> success = Option.none();
	private Option<Consumer<Throwable>> failure = Option.none();

	BufferImpl(final ActorContext context, final Decider<Object> decider) {
		this.context = context;
		this.context.become(processor());
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
			.matchEquals(btt, this::timeout)
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

	private void timeout(final Object trigger) {
		try {
			timeout.forEach(callback -> callback.invoke(messages));
		} finally {
			release();
		}
	}

	private void failure(final Object message) {
		try {
			failure.forEach(handler -> handler.accept((Throwable) message));
		} finally {
			release();
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

		final Object value = message.value;
		final ActorRef sender = message.sender;

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
		return new BufferTimeout<>(schedule(duration, btt), callback);
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

	public BufferDecider(final Predicate<Object> predicate) {
		this.predicate = predicate;
	}

	@Override
	public final boolean test(final Object object) {
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
