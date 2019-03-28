package com.github.sarxos.abberwoult.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.assertj.core.api.Assertions;
import org.assertj.core.internal.bytebuddy.implementation.bind.annotation.IgnoreForBinding;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorCreator;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Assisted;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class InjectorTest {

	@Singleton
	public static class DummyService {
	}
	
	final static class TestBeanNoArg {
	}

	final static class TestBeanInject {

		final DummyService ds;

		@Inject
		TestBeanInject(DummyService ds) {
			this.ds = ds;
		}
	}

	final static class TestBeanInjectWithAssistedArgs {

		final DummyService ds;
		final int x;
		final int y;

		@Inject
		TestBeanInjectWithAssistedArgs(DummyService ds, @Assisted int x, @Assisted int y) {
			this.ds = ds;
			this.x = x;
			this.y = y;
		}
	}

	private class SimpleInjector<T> extends Injector<T> {
		public SimpleInjector(BeanManager bm, Class<T> clazz, Object... args) {
			super(bm, clazz, Object.class, args);
		}
	}

	final BeanManager bm = CDI.current().getBeanManager();

	@Test
	void test_createNoArg() {

		final SimpleInjector<TestBeanNoArg> ac = new SimpleInjector<>(bm, TestBeanNoArg.class);
		final TestBeanNoArg instance = ac.create();

		Assertions
			.assertThat(instance)
			.isNotNull();
	}

	@Test
	void test_createInject() {

		final SimpleInjector<TestBeanInject> ac = new SimpleInjector<>(bm, TestBeanInject.class);
		final TestBeanInject instance = ac.create();

		Assertions
			.assertThat(instance)
			.isNotNull();
		Assertions
			.assertThat(instance.ds)
			.isNotNull();
	}

	@Test
	void test_createInjectWithAssistedArgs() {

		final SimpleInjector<TestBeanInjectWithAssistedArgs> ac = new SimpleInjector<>(bm, TestBeanInjectWithAssistedArgs.class, 5, 6);
		final TestBeanInjectWithAssistedArgs instance = ac.create();

		Assertions
			.assertThat(instance)
			.isNotNull();
		Assertions
			.assertThat(instance.ds)
			.isNotNull();
		Assertions
			.assertThat(instance.x)
			.isEqualTo(5);
		Assertions
			.assertThat(instance.y)
			.isEqualTo(6);
	}


//	@Test
//	void test_constructorNoArgs() {
//
//		final class TestActor extends SimpleActor { }
//
//		final ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
//		final ActorCreator creator = new ActorCreator(locator, TestActor.class);
//
//		Assertions
//			.assertThat(creator.getClazz())
//			.isSameAs(TestActor.class);
//		Assertions
//			.assertThat(creator.getBm())
//			.isSameAs(locator);
//		Assertions
//			.assertThat(creator.getArgs())
//			.isEmpty();
//	}
//
//	@Test
//	void test_constructorWithArgs() {
//
//		final class TestActor extends SimpleActor { }
//
//		final ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
//		final ActorCreator creator = new ActorCreator(locator, TestActor.class, "a", "b", "c");
//
//		Assertions
//			.assertThat(creator.getClazz())
//			.isSameAs(TestActor.class);
//		Assertions
//			.assertThat(creator.getBm())
//			.isSameAs(locator);
//		Assertions
//			.assertThat(creator.getArgs())
//			.containsExactly("a", "b", "c");
//	}
}
