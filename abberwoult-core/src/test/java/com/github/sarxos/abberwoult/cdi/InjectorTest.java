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

	final static class TestBeanInjectCtorAndFieldWithAssistedArgs {

		@Inject
		DummyService ds;
		
		final int x;
		final int y;

		@Inject
		TestBeanInjectCtorAndFieldWithAssistedArgs(@Assisted int x, @Assisted int y) {
			this.x = x;
			this.y = y;
		}
	}

	final static class TestBeanArgs {

		@Inject
		DummyService ds;
		
		final int x;
		final int y;

		TestBeanArgs(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private class SimpleInjector<T> extends Injector<T> {
		public SimpleInjector(BeanLocator bm, Class<T> clazz, Object... args) {
			super(bm, clazz, Object.class, args);
		}
	}

	@Inject
	BeanLocator locator;

	@Test
	void test_createNoArg() {

		final SimpleInjector<TestBeanNoArg> ac = new SimpleInjector<>(locator, TestBeanNoArg.class);
		final TestBeanNoArg instance = ac.create();

		Assertions
			.assertThat(instance)
			.isNotNull();
	}

	@Test
	void test_createInject() {

		final SimpleInjector<TestBeanInject> ac = new SimpleInjector<>(locator, TestBeanInject.class);
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

		final SimpleInjector<TestBeanInjectWithAssistedArgs> ac = new SimpleInjector<>(locator, TestBeanInjectWithAssistedArgs.class, 5, 6);
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

	@Test
	void test_createTestBeanInjectCtorAndFieldWithAssistedArgs() {

		final SimpleInjector<TestBeanInjectCtorAndFieldWithAssistedArgs> ac = new SimpleInjector<>(locator, TestBeanInjectCtorAndFieldWithAssistedArgs.class, 5, 6);
		final TestBeanInjectCtorAndFieldWithAssistedArgs instance = ac.create();

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

	@Test
	void test_createTestBeanArgs() {

		final SimpleInjector<TestBeanArgs> ac = new SimpleInjector<>(locator, TestBeanArgs.class, 7, 8);
		final TestBeanArgs instance = ac.create();

		Assertions
			.assertThat(instance)
			.isNotNull();
		Assertions
			.assertThat(instance.ds)
			.isNotNull();
		Assertions
			.assertThat(instance.x)
			.isEqualTo(7);
		Assertions
			.assertThat(instance.y)
			.isEqualTo(8);
	}
}
