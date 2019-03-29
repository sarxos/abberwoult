package com.github.sarxos.abberwoult.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.cdi.internals.TestConstructorWithArgs;
import com.github.sarxos.abberwoult.cdi.internals.TestDefaultConstructor;
import com.github.sarxos.abberwoult.cdi.internals.TestInjectServiceByConstructor;
import com.github.sarxos.abberwoult.cdi.internals.TestInjectServiceByConstructorWithAssistedArgs;
import com.github.sarxos.abberwoult.cdi.internals.TestInjectServiceByFieldWithArgs;
import com.github.sarxos.abberwoult.cdi.internals.TestInjectServiceByFieldWithAssistedArgs;
import com.github.sarxos.abberwoult.cdi.internals.TestPostConstruct;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class UnmanagedBeanFactoryTest {

	private class SimpleInjector<T> extends UnmanagedBeanFactory<T> {
		public SimpleInjector(BeanLocator bm, Class<T> clazz, Object... args) {
			super(bm, clazz, Object.class, args);
		}
	}

	@Inject
	BeanLocator locator;

	@Test
	void test_createTestDefaultConstructor() {

		final SimpleInjector<TestDefaultConstructor> ac = new SimpleInjector<>(locator, TestDefaultConstructor.class);
		final TestDefaultConstructor instance = ac.create();

		assertThat(instance).isNotNull();
	}

	@Test
	void test_createTestConstructorWithArgs() {

		final SimpleInjector<TestConstructorWithArgs> ac = new SimpleInjector<>(locator, TestConstructorWithArgs.class, 1, 2);
		final TestConstructorWithArgs instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getX()).isEqualTo(1);
		assertThat(instance.getY()).isEqualTo(2);
	}

	@Test
	void test_createTestInjectServiceByConstructor() {

		final SimpleInjector<TestInjectServiceByConstructor> ac = new SimpleInjector<>(locator, TestInjectServiceByConstructor.class);
		final TestInjectServiceByConstructor instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getService()).isNotNull();
	}

	@Test
	void test_createTestInjectServiceByConstructorWithAssistedArgs() {

		final SimpleInjector<TestInjectServiceByConstructorWithAssistedArgs> ac = new SimpleInjector<>(locator, TestInjectServiceByConstructorWithAssistedArgs.class, 5, 6);
		final TestInjectServiceByConstructorWithAssistedArgs instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getService()).isNotNull();
		assertThat(instance.getX()).isEqualTo(5);
		assertThat(instance.getY()).isEqualTo(6);
	}

	@Test
	void test_createTestBeanInjectCtorAndFieldWithAssistedArgs() {

		final SimpleInjector<TestInjectServiceByFieldWithAssistedArgs> ac = new SimpleInjector<>(locator, TestInjectServiceByFieldWithAssistedArgs.class, 5, 6);
		final TestInjectServiceByFieldWithAssistedArgs instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getService()).isNotNull();
		assertThat(instance.getX()).isEqualTo(5);
		assertThat(instance.getY()).isEqualTo(6);
	}

	@Test
	void test_createTestInjectServiceByFieldWithArgs() {

		final SimpleInjector<TestInjectServiceByFieldWithArgs> ac = new SimpleInjector<>(locator, TestInjectServiceByFieldWithArgs.class, 7, 8);
		final TestInjectServiceByFieldWithArgs instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getService()).isNotNull();
		assertThat(instance.getX()).isEqualTo(7);
		assertThat(instance.getY()).isEqualTo(8);
	}

	@Test
	void test_createTestPostConstruct() {

		final AtomicBoolean invoked = new AtomicBoolean(false);
		final SimpleInjector<TestPostConstruct> ac = new SimpleInjector<>(locator, TestPostConstruct.class, invoked);
		final TestPostConstruct instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.wasInvoked()).isTrue();
	}

}
