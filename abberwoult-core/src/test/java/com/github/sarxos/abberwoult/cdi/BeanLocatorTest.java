package com.github.sarxos.abberwoult.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.cdi.internals.DummyService;
import com.github.sarxos.abberwoult.cdi.internals.SomeService;
import com.github.sarxos.abberwoult.cdi.internals.SomeServiceBarImpl;
import com.github.sarxos.abberwoult.cdi.internals.SomeServiceFooImpl;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class BeanLocatorTest {

	@Inject
	BeanLocator locator;

	@Inject
	@Named("foo")
	SomeService foo;

	@Inject
	@Named("bar")
	SomeService bar;

	static class TestClass {

		DummyService ds;

		public TestClass(
			DummyService ds,
			@Named("foo") SomeService fss,
			@Named("bar") SomeService bss) {
		}
	}

	@Test
	void test_injectBeanLocatorInTest() {
		assertThat(locator).isNotNull();
	}

	@Test
	void test_findBeanForField() throws NoSuchFieldException, SecurityException {

		final Field field = TestClass.class.getDeclaredField("ds");

		assertThat(locator.findBeanFor(field))
			.isInstanceOf(DummyService.class)
			.isNotNull();
	}

	@Test
	void test_findBeanForFieldParameter0() throws NoSuchFieldException, SecurityException {

		final Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		final Parameter parameter = constructor.getParameters()[0];

		assertThat(locator.findBeanFor(constructor, parameter, 0))
			.isInstanceOf(DummyService.class)
			.isNotNull();
	}

	@Test
	void test_findBeanForFieldParameter1() throws NoSuchFieldException, SecurityException {

		final Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		final Parameter parameter = constructor.getParameters()[1];

		assertThat(locator.findBeanFor(constructor, parameter, 1))
			.isInstanceOf(SomeService.class)
			.isSameAs(foo)
			.isNotNull();
	}

	@Test
	void test_findBeanForFieldParameter2() throws NoSuchFieldException, SecurityException {

		final Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		final Parameter parameter = constructor.getParameters()[2];

		assertThat(locator.findBeanFor(constructor, parameter, 2))
			.isInstanceOf(SomeService.class)
			.isSameAs(bar)
			.isNotNull();
	}

	@Test
	void test_findBean() {
		assertThat(locator.findBean(DummyService.class))
			.isInstanceOf(DummyService.class)
			.isNotNull();
	}

	@Test
	void test_findSomeServiceNamedFoo() {
		assertThat(foo)
			.isInstanceOf(SomeServiceFooImpl.class)
			.isNotNull();
	}

	@Test
	void test_findSomeServiceNamedBar() {
		assertThat(bar)
			.isInstanceOf(SomeServiceBarImpl.class)
			.isNotNull();
	}
}
