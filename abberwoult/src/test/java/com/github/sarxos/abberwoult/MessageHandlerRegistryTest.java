package com.github.sarxos.abberwoult;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.util.ReflectionUtils;

import akka.actor.AbstractActor.Receive;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class MessageHandlerRegistryTest {

	public static class TestDeclarator {

		Integer received;

		// @MessageHandler
		public void handleInteger(final Integer i) {
			received = i;
		}
	}

	@BeforeAll
	static void setup() {

		final Class<?> declaringClass = TestDeclarator.class;
		final String handlerName = "handleInteger";
		final Class<?> handlerType = ReflectionUtils.getClazz("void");
		final ParameterList parameters = ParameterList.of(Arrays.asList(Integer.class), emptySet(), emptySet());

		MessageHandlerRegistry.store(declaringClass, handlerName, handlerType, parameters);
	}

	@Inject
	MessageHandlerRegistry registry;

	@Test
	void test_store() {

		final Map<Class<?>, MessageHandlerMethod> handlers = registry.getHandlersFor(TestDeclarator.class);

		assertThat(handlers)
			.isNotNull()
			.isNotEmpty();
	}

	@Test
	void test_store2() {

		final MessageHandlerMethod entry = registry.getHandlerFor(TestDeclarator.class, Integer.class);

		assertThat(entry).isNotNull();
		assertThat(entry.getDeclaringClass()).isSameAs(TestDeclarator.class);
		assertThat(entry.getMessageClass()).isSameAs(Integer.class);
		assertThat(entry.getName()).isEqualTo("handleInteger");
		assertThat(entry.getReturnedType()).isEqualTo(ReflectionUtils.getClazz("void"));
	}

	@Test
	void test_newReceive() {

		final AtomicBoolean wasUnhandled = new AtomicBoolean();
		final TestDeclarator thiz = new TestDeclarator();
		final Lookup caller = MethodHandles.lookup();
		final Consumer<Object> unhandled = msg -> wasUnhandled.set(true);

		final Object message1 = Integer.valueOf(3);
		final Object message2 = new String("aaa");
		final Receive receive = registry.newReceive(thiz, caller.in(thiz.getClass()), unhandled);

		receive.onMessage().apply(message1);

		assertThat(wasUnhandled).isFalse();
		assertThat(thiz.received).isSameAs(message1);

		receive.onMessage().apply(message2);

		assertThat(wasUnhandled).isTrue();
		assertThat(thiz.received).isSameAs(message1);
	}

}
