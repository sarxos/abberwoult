package com.github.sarxos.abberwoult.dsl;

import static akka.actor.ActorRef.noSender;
import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;
import static java.time.Duration.ofMillis;
import static java.util.stream.Collectors.toList;

import java.util.Deque;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Buffers.BufferMessage;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;
import io.vavr.control.Option;


@QuarkusTest
public class BuffersTest {

	@Inject
	TestKit testkit;

	static final class SomeException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public SomeException(Throwable cause) {
			super(cause);
		}
	}

	public static class UntilReceivedMessageOfTestActor extends SimpleActor implements Buffers, Utils {

		final ActorRef probe;

		public UntilReceivedMessageOfTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntilReceivedMessageOf(Integer.class)
				.onSuccess(this::doIntegerStuff)
				.onFailure(this::doErrorStuff)
				.onTimeout(ofMillis(500), this::doTimeoutStuff);
		}

		private void doIntegerStuff(final Integer i) {
			forward(probe, i);
		}

		private void doErrorStuff(final Throwable t) {
			forward(probe, new SomeException(t));
		}

		private void doTimeoutStuff(final Deque<BufferMessage> messages) {
			forward(probe, messages);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	public static class UntilReceivedMessageTestActor extends SimpleActor implements Buffers, Utils {

		final ActorRef probe;

		public UntilReceivedMessageTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntilReceivedMessage(333)
				.onSuccess(this::doIntegerStuff)
				.onFailure(this::doErrorStuff)
				.onTimeout(ofMillis(500), this::doTimeoutStuff);
		}

		private void doIntegerStuff(final Integer i) {
			forward(probe, i);
		}

		private void doErrorStuff(final Throwable t) {
			forward(probe, new SomeException(t));
		}

		private void doTimeoutStuff(final Deque<BufferMessage> messages) {
			forward(probe, messages);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	public static class UntilTestActor extends SimpleActor implements Buffers, Utils {

		final ActorRef probe;

		public UntilTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntil(this::isMagic)
				.onSuccess(this::doMagicStuff)
				.onFailure(this::doErrorStuff)
				.onTimeout(ofMillis(500), this::doTimeoutStuff);
		}

		private boolean isMagic(final Object message) {
			return message == "m";
		}

		private void doMagicStuff(final Object i) {
			forward(probe, i);
		}

		private void doErrorStuff(final Throwable t) {
			forward(probe, new SomeException(t));
		}

		private void doTimeoutStuff(final Deque<BufferMessage> messages) {
			forward(probe, messages);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	public static class UntilDecidedByTestActor extends SimpleActor implements Buffers, Utils {

		class CountingDecider extends Decider<Object> {

			int count = 0;

			// expect 3 integers smaller than 5, then return true to stop buffer

			@Override
			public boolean test(final Object message) {

				final boolean smallerThanFive = Option.of(message)
					.filter(Integer.class::isInstance)
					.map(Integer.class::cast)
					.filter(i -> i < 5)
					.isDefined();

				if (smallerThanFive) {
					count++;
					discard(message);
				}

				return count == 3;
			}
		}

		final ActorRef probe;

		public UntilDecidedByTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntilDecidedBy(new CountingDecider())
				.onSuccess(this::doSuccessStuff)
				.onFailure(this::doErrorStuff)
				.onTimeout(ofMillis(500), this::doTimeoutStuff);
		}

		private void doSuccessStuff(final Object i) {
			forward(probe, "DONE");
		}

		private void doErrorStuff(final Throwable t) {
			forward(probe, new SomeException(t));
		}

		private void doTimeoutStuff(final Deque<BufferMessage> messages) {
			forward(probe, messages);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	public static class StackedBuffersTestActor extends SimpleActor implements Buffers, Utils {

		final ActorRef probe;

		public StackedBuffersTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntilReceivedMessage(8).onSuccess(this::doIntegerStuff);
			bufferUntilReceivedMessage(5).onSuccess(this::doIntegerStuff);
			bufferUntilReceivedMessage(3).onSuccess(this::doIntegerStuff);
		}

		private void doIntegerStuff(final Integer i) {
			forward(probe, i);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	@Test
	public void test_bufferUntilReceivedMessageOfOnSuccess() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilReceivedMessageOfTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell("a", noSender());
		ref.tell("b", noSender());
		ref.tell("c", noSender());
		ref.tell("d", noSender());
		ref.tell(123, noSender());

		probe.expectMsg(123);
		probe.expectMsg("a");
		probe.expectMsg("b");
		probe.expectMsg("c");
		probe.expectMsg("d");

		dispose(ref);
	}

	@Test
	public void test_bufferUntilReceivedMessageOfOnFailure() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilReceivedMessageOfTestActor.class)
			.withArguments(probe)
			.create();

		final Throwable err = new IllegalStateException("bubu");

		ref.tell("a", noSender());
		ref.tell("b", noSender());
		ref.tell("c", noSender());
		ref.tell("d", noSender());
		ref.tell(err, noSender());

		probe.expectMsgClass(SomeException.class);
		probe.expectMsg("a");
		probe.expectMsg("b");
		probe.expectMsg("c");
		probe.expectMsg("d");

		dispose(ref);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_bufferUntilReceivedMessageOfOnTimeout() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilReceivedMessageOfTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell("a", noSender());
		ref.tell("b", noSender());
		ref.tell("c", noSender());
		ref.tell("d", noSender());

		final Deque<BufferMessage> messages = probe.expectMsgClass(Deque.class);

		final List<Object> letters = messages.stream()
			.map(BufferMessage::getValue)
			.collect(toList());

		Assertions
			.assertThat(letters)
			.hasSize(4)
			.containsExactly("a", "b", "c", "d");

		dispose(ref);
	}

	@Test
	public void test_bufferUntilReceivedMessageOnSuccess() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilReceivedMessageTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell("a", noSender());
		ref.tell("b", noSender());
		ref.tell("c", noSender());
		ref.tell("d", noSender());
		ref.tell(100, noSender());
		ref.tell(101, noSender());
		ref.tell(102, noSender());
		ref.tell(333, noSender());

		probe.expectMsg(333);
		probe.expectMsg("a");
		probe.expectMsg("b");
		probe.expectMsg("c");
		probe.expectMsg("d");
		probe.expectMsg(100);
		probe.expectMsg(101);
		probe.expectMsg(102);

		dispose(ref);
	}

	@Test
	public void test_bufferUntilReceivedMessageOnFailure() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilReceivedMessageTestActor.class)
			.withArguments(probe)
			.create();

		final Throwable err = new IllegalStateException("baba");

		ref.tell("a", noSender());
		ref.tell("b", noSender());
		ref.tell("c", noSender());
		ref.tell("d", noSender());
		ref.tell(100, noSender());
		ref.tell(101, noSender());
		ref.tell(102, noSender());
		ref.tell(103, noSender());
		ref.tell(err, noSender());

		probe.expectMsgClass(SomeException.class);
		probe.expectMsg("a");
		probe.expectMsg("b");
		probe.expectMsg("c");
		probe.expectMsg("d");
		probe.expectMsg(100);
		probe.expectMsg(101);
		probe.expectMsg(102);
		probe.expectMsg(103);

		dispose(ref);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_bufferUntilReceivedMessageOnTimeout() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilReceivedMessageTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell(100, noSender());
		ref.tell(101, noSender());
		ref.tell(102, noSender());
		ref.tell(103, noSender());

		final Deque<BufferMessage> messages = probe.expectMsgClass(Deque.class);

		final List<Object> letters = messages.stream()
			.map(BufferMessage::getValue)
			.collect(toList());

		Assertions
			.assertThat(letters)
			.hasSize(4)
			.containsExactly(100, 101, 102, 103);

		dispose(ref);
	}

	@Test
	public void test_bufferUntilStackedFewTimes() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(StackedBuffersTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell(1, noSender());
		ref.tell(2, noSender());
		ref.tell(3, noSender());
		ref.tell(4, noSender());
		ref.tell(5, noSender());
		ref.tell(6, noSender());
		ref.tell(7, noSender());
		ref.tell(8, noSender());

		probe.expectMsg(3);
		probe.expectMsg(5);
		probe.expectMsg(8);

		probe.expectMsg(1);
		probe.expectMsg(2);
		probe.expectMsg(4);
		probe.expectMsg(6);
		probe.expectMsg(7);

		dispose(ref);
	}

	@Test
	public void test_bufferUntilOnSuccess() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell("a", noSender());
		ref.tell("b", noSender());
		ref.tell("c", noSender());
		ref.tell("d", noSender());
		ref.tell("m", noSender());

		probe.expectMsg("m");
		probe.expectMsg("a");
		probe.expectMsg("b");
		probe.expectMsg("c");
		probe.expectMsg("d");

		dispose(ref);
	}

	@Test
	public void test_bufferUntilOnFailure() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilTestActor.class)
			.withArguments(probe)
			.create();

		final Throwable err = new IllegalStateException("koko");

		ref.tell("a", noSender());
		ref.tell("b", noSender());
		ref.tell("c", noSender());
		ref.tell("d", noSender());
		ref.tell(err, noSender());

		probe.expectMsgClass(SomeException.class);
		probe.expectMsg("a");
		probe.expectMsg("b");
		probe.expectMsg("c");
		probe.expectMsg("d");

		dispose(ref);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_bufferUntilOnTimeout() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell("a", noSender());
		ref.tell("b", noSender());
		ref.tell("c", noSender());
		ref.tell("d", noSender());

		final Deque<BufferMessage> messages = probe.expectMsgClass(Deque.class);

		final List<Object> letters = messages.stream()
			.map(BufferMessage::getValue)
			.collect(toList());

		Assertions
			.assertThat(letters)
			.hasSize(4)
			.containsExactly("a", "b", "c", "d");

		dispose(ref);
	}

	@Test
	public void test_bufferUntilDecidedByOnSuccess() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilDecidedByTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell(1, noSender()); // should be discarded and counted
		ref.tell(2, noSender()); // should be discarded and counted
		ref.tell(3, noSender()); // should be discarded and counted
		ref.tell(4, noSender()); // should be received
		ref.tell(5, noSender()); // should be received

		probe.expectMsg("DONE");
		probe.expectMsg(4);
		probe.expectMsg(5);

		dispose(ref);
	}

	@Test
	public void test_bufferUntilDecidedByOnFailure() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilDecidedByTestActor.class)
			.withArguments(probe)
			.create();

		final Throwable err = new IllegalStateException("popo");

		ref.tell(1, noSender());
		ref.tell(2, noSender());
		ref.tell(7, noSender());
		ref.tell(8, noSender());
		ref.tell(err, noSender());

		probe.expectMsgClass(SomeException.class);
		probe.expectMsg(7);
		probe.expectMsg(8);

		dispose(ref);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_bufferUntilDecidedByOnTimeout() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(UntilDecidedByTestActor.class)
			.withArguments(probe)
			.create();

		ref.tell(1, noSender());
		ref.tell(2, noSender());
		ref.tell(7, noSender());
		ref.tell(8, noSender());

		final Deque<BufferMessage> messages = probe.expectMsgClass(Deque.class);

		final List<Object> letters = messages.stream()
			.map(BufferMessage::getValue)
			.collect(toList());

		Assertions
			.assertThat(letters)
			.hasSize(2)
			.containsExactly(7, 8);

		dispose(ref);
	}
}
