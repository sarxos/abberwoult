package com.github.sarxos.abberwoult.deployment;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.deployment.MessageHandlersRegistry;
import com.github.sarxos.abberwoult.deployment.MessageHandlersRegistry.MessageHandlerMethod;
import com.github.sarxos.abberwoult.deployment.MessageHandlersRegistry.ParameterList;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class MessageHandlersRegistryTest {

	public static class TestDeclarator extends SimpleActor {

		Integer received;

		// @MessageHandler skip purposefully
		public void handleInteger(final Integer i) {
			sender().tell(i, self());
		}

		@Override
		public void unhandled(Object message) {
			sender().tell(999, self());
		}
	}

	@BeforeAll
	static void setup() {

		final Class<?> declaringClass = TestDeclarator.class;
		final String handlerName = "handleInteger";
		final Class<?> handlerType = ReflectionUtils.getClazz("void");
		final ParameterList parameters = ParameterList.of(Arrays.asList(Integer.class), emptySet(), emptySet());

		MessageHandlersRegistry.store(declaringClass, handlerName, handlerType, parameters);
	}

	@Inject
	MessageHandlersRegistry registry;

	@Inject
	@ActorOf(TestDeclarator.class)
	ActorRef ref;

	@Test
	void test_store() {

		final Map<Class<?>, MessageHandlerMethod> handlers = registry.getHandlersFor(TestDeclarator.class).get();

		assertThat(handlers)
			.isNotNull()
			.isNotEmpty();
	}

	@Test
	void test_store2() {

		final MessageHandlerMethod entry = registry.getHandlerFor(TestDeclarator.class, Integer.class).get();

		assertThat(entry).isNotNull();
		assertThat(entry.getDeclaringClass()).isSameAs(TestDeclarator.class);
		assertThat(entry.getMessageClass()).isSameAs(Integer.class);
		assertThat(entry.getName()).isEqualTo("handleInteger");
		assertThat(entry.getReturnedType()).isEqualTo(ReflectionUtils.getClazz("void"));
	}

	private Object askResult(final Object message) throws Exception {
		final Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		final Future<Object> future = Patterns.ask(ref, message, timeout);
		return Await.result(future, timeout.duration());
	}

	@Test
	void test_newReceive() throws Exception {
		assertThat(askResult(111)).isEqualTo(111);
		assertThat(askResult(222)).isEqualTo(222);
		assertThat(askResult("a")).isEqualTo(999);
	}
}
