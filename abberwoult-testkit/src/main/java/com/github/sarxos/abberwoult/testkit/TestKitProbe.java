package com.github.sarxos.abberwoult.testkit;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import akka.actor.ActorSystem;


/**
 * A testkit class used to probe messages.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class TestKitProbe extends akka.testkit.javadsl.TestKit {

	TestKitProbe(final ActorSystem system) {
		super(system);
	}

	/**
	 * Expect N message of a given class and return them as a {@link List}.
	 *
	 * @param n how many messages are expected
	 * @param clazz the message class
	 * @return {@link List} of expected messages
	 */
	public <T> List<T> expectNMsgClass(final int n, final Class<T> clazz) {
		return IntStream
			.range(0, n)
			.mapToObj(i -> expectMsgClass(clazz))
			.collect(Collectors.toList());
	}

	/**
	 * Expect only given message class but return only the one that matches given predicate.
	 *
	 * @param clazz the message class to be expected
	 * @param predicate the predicate
	 * @return Message of a given class that matches provided predicate
	 */
	@SuppressWarnings("unchecked")
	public <T> T awaitMsgClass(final Class<T> clazz, Predicate<T> predicate) {
		Object msg;
		do {
			msg = expectMsgClass(clazz);
			if (predicate.test((T) msg)) {
				return (T) msg;
			}
		} while (true);
	}

	@SuppressWarnings("unchecked")
	public <T> T awaitMsg(Predicate<Object> predicate) {
		Object msg;
		do {
			if (predicate.test(msg = receiveN(1))) {
				return (T) msg;
			}
		} while (true);
	}
}
