package com.github.sarxos.abberwoult.dsl;

import static akka.actor.ActorRef.noSender;
import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;
import static java.util.stream.Collectors.toList;

import java.util.Deque;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.dsl.Buffers.BufferMessage;
import com.github.sarxos.abberwoult.dsl.BuffersTesting.SomeException;
import com.github.sarxos.abberwoult.dsl.BuffersTesting.StackedBuffersTestActor;
import com.github.sarxos.abberwoult.dsl.BuffersTesting.UntilDecidedByTestActor;
import com.github.sarxos.abberwoult.dsl.BuffersTesting.UntilReceivedMessageOfTestActor;
import com.github.sarxos.abberwoult.dsl.BuffersTesting.UntilReceivedMessageTestActor;
import com.github.sarxos.abberwoult.dsl.BuffersTesting.UntilTestActor;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class BuffersTest {

	@Inject
	TestKit testkit;

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
