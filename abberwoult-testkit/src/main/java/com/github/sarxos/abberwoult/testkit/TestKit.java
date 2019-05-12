package com.github.sarxos.abberwoult.testkit;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.awaitility.Awaitility;

import com.github.sarxos.abberwoult.AbstractActorBuilder.ActorBuilderRefCreator;
import com.github.sarxos.abberwoult.ActorBuilder;
import com.github.sarxos.abberwoult.ActorSystemProxy;

import akka.actor.Actor;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.actor.PoisonPill;
import akka.pattern.AskableActorRef;
import akka.pattern.AskableActorSelection;
import akka.testkit.TestActorRef;
import akka.util.Timeout;
import io.vavr.control.Option;
import scala.concurrent.Await;
import scala.concurrent.duration.FiniteDuration;


/**
 * Utility for testing actor-related integrations.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class TestKit {

	public static final ActorBuilderRefCreator CREATOR = (factory, props, name) -> {
		if (name.isDefined()) {
			return TestActorRef.create((ActorSystem) factory, props, name.get());
		} else {
			return TestActorRef.create((ActorSystem) factory, props);
		}
	};

	private final ActorSystemProxy proxy;

	@Inject
	public TestKit(final ActorSystemProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Extract {@link Actor} instance from {@link ActorRef}.
	 *
	 * @param ref {@link ActorRef} to extract {@link Actor} from
	 * @return {@link Actor} instance
	 */
	@SuppressWarnings("unchecked")
	public <T extends Actor> T extract(final ActorRef ref) {
		return extract((TestActorRef<T>) ref);
	}

	/**
	 * Extract {@link Actor} instance from {@link TestActorRef}.
	 *
	 * @param ref {@link TestActorRef} to extract {@link Actor} from
	 * @return The {@link Actor} instance
	 */
	public <T extends Actor> T extract(final TestActorRef<T> ref) {
		return ref.underlyingActor();
	}

	/**
	 * Extract {@link Actor} instance from {@link ActorRef}. Obtained actor instance is ready to use
	 * (is was already pre-started).
	 *
	 * @param ref {@link ActorRef} to extract {@link Actor} from
	 * @return The {@link Actor} instance
	 */
	public <T extends Actor> T extractReady(final ActorRef ref) {
		awaitForActor(ref);
		return extract(identify(ref, 10, TimeUnit.SECONDS).getActorRef().get());
	}

	public <T extends Actor> T extractReady(final ActorSelection selection) {
		awaitForActor(selection);
		return extract(identify(selection, 10, TimeUnit.SECONDS).getActorRef().get());
	}

	/**
	 * Await 10 seconds for actor to be ready (actor is ready when it is started).
	 *
	 * @param ref actor reference
	 */
	public void awaitForActor(final ActorRef ref) {
		awaitForActor(ref, Duration.ofSeconds(10));
	}

	/**
	 * Await 10 seconds for actor to be ready (actor is ready when it is started).
	 *
	 * @param selection
	 */
	public void awaitForActor(final ActorSelection selection) {
		awaitForActor(selection, Duration.ofSeconds(10));
	}

	/**
	 * Await given amount of time for actor to be ready (actor is ready when it is started).
	 *
	 * @param ref actor reference
	 * @param duration the time duration to wait for actor
	 */
	public void awaitForActor(final ActorRef ref, final Duration duration) {
		Awaitility
			.await("Await until actor identity is ready for " + ref)
			.atMost(duration.getSeconds(), TimeUnit.SECONDS)
			.until(actorIdentityIsPresent(ref));
	}

	/**
	 * Await given amount of time for actor to be ready (actor is ready when it is started).
	 *
	 * @param selection the actor selection
	 * @param duration the time duration to wait for actor
	 */
	public void awaitForActor(final ActorSelection selection, final Duration duration) {
		Awaitility
			.await("Await until actor identity is ready for " + selection)
			.atMost(duration.getSeconds(), TimeUnit.SECONDS)
			.until(actorIdentityIsPresent(selection));
	}

	private Callable<Boolean> actorIdentityIsPresent(final ActorRef ref) {
		return () -> Option
			.ofOptional(identify(ref, 1, TimeUnit.SECONDS).getActorRef())
			.isDefined();
	}

	private Callable<Boolean> actorIdentityIsPresent(final ActorSelection selection) {
		return () -> Option
			.ofOptional(identify(selection, 1, TimeUnit.SECONDS).getActorRef())
			.isDefined();
	}

	private ActorIdentity identify(final ActorRef ref, long await, TimeUnit unit) {

		final AskableActorRef askable = new AskableActorRef(ref);
		final FiniteDuration duration = FiniteDuration.create(await, unit);
		final Timeout timeout = Timeout.durationToTimeout(duration);

		try {
			return (ActorIdentity) Await.result(askable.ask(new Identify(1), timeout), duration);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private ActorIdentity identify(final ActorSelection selection, long await, TimeUnit unit) {

		final AskableActorSelection askable = new AskableActorSelection(selection);
		final FiniteDuration duration = FiniteDuration.create(await, unit);
		final Timeout timeout = Timeout.durationToTimeout(duration);

		try {
			return (ActorIdentity) Await.result(askable.ask(new Identify(1), timeout), duration);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create new actor builder and return it.
	 *
	 * @return New actor builder
	 */
	public ActorBuilder<?> actor() {
		return proxy
			.actor()
			.withActorRefCreator(CREATOR);
	}

	/**
	 * @return The {@link TestKitProbe} to probe messages
	 */
	public TestKitProbe probe() {
		return new TestKitProbe(proxy.system());
	}

	public void kill(final ActorRef ref) {
		ref.tell(PoisonPill.getInstance(), ActorRef.noSender());
	}
}
