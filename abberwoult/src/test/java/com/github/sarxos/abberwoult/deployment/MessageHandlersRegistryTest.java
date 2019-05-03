package com.github.sarxos.abberwoult.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.deployment.MessageHandlerRegistry.MessageHandlerMethod;
import com.github.sarxos.abberwoult.util.ReflectionUtils;


public class MessageHandlersRegistryTest {

	public static class TestDeclaratorA extends SimpleActor {

		@MessageHandler
		public void handleInteger(final Integer i) {
			// nothing
		}
	}

	public static class TestDeclaratorB extends TestDeclaratorA {

		@MessageHandler
		public void handleInteger2(final Integer i) {
			// nothing
		}
	}

	@BeforeAll
	static void setup() {
		MessageHandlerRegistry.register(TestDeclaratorA.class);
		MessageHandlerRegistry.register(TestDeclaratorB.class);
	}

	@Test
	void test_store() {

		final MessageHandlerRegistry registry = new MessageHandlerRegistry();
		final Map<String, MessageHandlerMethod> handlers = registry.getMessageHandlersFor(TestDeclaratorA.class).get();

		assertThat(handlers)
			.isNotNull()
			.isNotEmpty();
	}

	@Test
	void test_methodFromActualClassShouldBeUsed() {

		final MessageHandlerRegistry registry = new MessageHandlerRegistry();
		final MessageHandlerMethod entry = registry.getHandlerFor(TestDeclaratorA.class, Integer.class).get();

		assertThat(entry).isNotNull();
		assertThat(entry.getDeclaringClass()).isSameAs(TestDeclaratorA.class);
		assertThat(entry.getMessageClass()).isSameAs(Integer.class);
		assertThat(entry.getName()).isEqualTo("handleInteger");
		assertThat(entry.getReturnedType()).isEqualTo(ReflectionUtils.getClazz("void"));
	}

	@Test
	void test_methodFromSubclassShouldBeUsed() {

		final MessageHandlerRegistry registry = new MessageHandlerRegistry();
		final MessageHandlerMethod entry = registry.getHandlerFor(TestDeclaratorB.class, Integer.class).get();

		assertThat(entry).isNotNull();
		assertThat(entry.getDeclaringClass()).isSameAs(TestDeclaratorB.class);
		assertThat(entry.getMessageClass()).isSameAs(Integer.class);
		assertThat(entry.getName()).isEqualTo("handleInteger2");
		assertThat(entry.getReturnedType()).isEqualTo(ReflectionUtils.getClazz("void"));
	}
}
