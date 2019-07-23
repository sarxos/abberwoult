package com.github.sarxos.abberwoult.dsl;

import static java.time.Duration.ofSeconds;

import java.util.Deque;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;

import io.vavr.Predicates;


public class BuffersTest {

	public void test() {

		@SuppressWarnings("unused")
		class TestActor extends SimpleActor implements Buffers {

			@PreStart
			public void init() {
				bufferUntilReceivedMessageOf(Integer.class)
					.onSuccess(this::doIntegerStuff)
					.onFailure(this::doErrorStuff)
					.onTimeout(ofSeconds(10), this::doTimeoutStuff);

				bufferUntilReceivedMessage(3)
					.onSuccess(this::doIntegerStuff)
					.onFailure(this::doErrorStuff)
					.onTimeout(ofSeconds(10), this::doTimeoutStuff);

				bufferUntil(Predicates.isIn(1, 2, 3, 4));

				bufferUntilDecidedBy(new Decider<Object>() {

					@Override
					public boolean test(final Object message) {

						discard(message);

						return message == "ss";
					}
				});

				// bufferUntil(m -> m == 3)
				// .onSuccess(this::doIntegerStuff)
				// .onFailure(this::doErrorStuff)
				// .onTimeout(ofSeconds(10), this::doTimeoutStuff);
			}

			private void doIntegerStuff(final Integer i) {
				// do nothing, really
			}

			private void doErrorStuff(final Throwable t) {
				// do nothing, really
			}

			private void doTimeoutStuff(final Deque<BufferMessage> buffered) {
				// do nothing, really
			}
		}

	}
}
