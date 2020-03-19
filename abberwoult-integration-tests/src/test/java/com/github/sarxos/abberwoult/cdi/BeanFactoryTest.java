package com.github.sarxos.abberwoult.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.cdi.internals.CdiTestConstructorWithArgs;
import com.github.sarxos.abberwoult.cdi.internals.CdiTestDefaultConstructor;
import com.github.sarxos.abberwoult.cdi.internals.CdiTestInjectServiceByConstructor;
import com.github.sarxos.abberwoult.cdi.internals.CdiTestInjectServiceByConstructorWithAssistedArgs;
import com.github.sarxos.abberwoult.cdi.internals.CdiTestInjectServiceByFieldWithArgs;
import com.github.sarxos.abberwoult.cdi.internals.CdiTestInjectServiceByFieldWithAssistedArgs;
import com.github.sarxos.abberwoult.cdi.internals.CdiTestPostConstruct;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class BeanFactoryTest {

	private class SimpleInjector<T> extends BeanFactory<T> {
		public SimpleInjector(BeanLocator bm, Class<T> clazz, Object... args) {
			super(bm, clazz, Object.class, args);
		}
	}

	@Inject
	BeanLocator locator;

	@Test
	void test_createTestDefaultConstructor() {

		final SimpleInjector<CdiTestDefaultConstructor> ac = new SimpleInjector<>(locator, CdiTestDefaultConstructor.class);
		final CdiTestDefaultConstructor instance = ac.create();

		assertThat(instance).isNotNull();
	}

	@Test
	void test_createTestConstructorWithArgs() {

		final SimpleInjector<CdiTestConstructorWithArgs> ac = new SimpleInjector<>(locator, CdiTestConstructorWithArgs.class, 1, 2);
		final CdiTestConstructorWithArgs instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getX()).isEqualTo(1);
		assertThat(instance.getY()).isEqualTo(2);
	}

	@Test
	void test_createTestInjectServiceByConstructor() {

		final SimpleInjector<CdiTestInjectServiceByConstructor> ac = new SimpleInjector<>(locator, CdiTestInjectServiceByConstructor.class);
		final CdiTestInjectServiceByConstructor instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getService()).isNotNull();
	}

	@Test
	void test_createTestInjectServiceByConstructorWithAssistedArgs() {

		final SimpleInjector<CdiTestInjectServiceByConstructorWithAssistedArgs> ac = new SimpleInjector<>(locator, CdiTestInjectServiceByConstructorWithAssistedArgs.class, 5, 6);
		final CdiTestInjectServiceByConstructorWithAssistedArgs instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getService()).isNotNull();
		assertThat(instance.getX()).isEqualTo(5);
		assertThat(instance.getY()).isEqualTo(6);
	}

	@Test
	void test_createTestBeanInjectCtorAndFieldWithAssistedArgs() {

		final SimpleInjector<CdiTestInjectServiceByFieldWithAssistedArgs> ac = new SimpleInjector<>(locator, CdiTestInjectServiceByFieldWithAssistedArgs.class, 5, 6);
		final CdiTestInjectServiceByFieldWithAssistedArgs instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getService()).isNotNull();
		assertThat(instance.getX()).isEqualTo(5);
		assertThat(instance.getY()).isEqualTo(6);
	}

	@Test
	void test_createTestInjectServiceByFieldWithArgs() {

		final SimpleInjector<CdiTestInjectServiceByFieldWithArgs> ac = new SimpleInjector<>(locator, CdiTestInjectServiceByFieldWithArgs.class, 7, 8);
		final CdiTestInjectServiceByFieldWithArgs instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.getService()).isNotNull();
		assertThat(instance.getX()).isEqualTo(7);
		assertThat(instance.getY()).isEqualTo(8);
	}

	@Test
	void test_createTestPostConstruct() {

		final AtomicBoolean invoked = new AtomicBoolean(false);
		final SimpleInjector<CdiTestPostConstruct> ac = new SimpleInjector<>(locator, CdiTestPostConstruct.class, invoked);
		final CdiTestPostConstruct instance = ac.create();

		assertThat(instance).isNotNull();
		assertThat(instance.wasInvoked()).isTrue();
	}

}
