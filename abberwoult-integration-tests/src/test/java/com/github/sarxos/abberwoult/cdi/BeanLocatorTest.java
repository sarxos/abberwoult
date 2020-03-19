package com.github.sarxos.abberwoult.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.cdi.internals.CdiDummyService;
import com.github.sarxos.abberwoult.cdi.internals.CdiSomeService;
import com.github.sarxos.abberwoult.cdi.internals.CdiSomeServiceBarImpl;
import com.github.sarxos.abberwoult.cdi.internals.CdiSomeServiceFooImpl;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class BeanLocatorTest {

	@Inject
	BeanLocator locator;

	@Inject
	@Named("foo")
	CdiSomeService foo;

	@Inject
	@Named("bar")
	CdiSomeService bar;

	static class TestClass {

		CdiDummyService ds;

		public TestClass(
			CdiDummyService ds,
			@Named("foo") CdiSomeService fss,
			@Named("bar") CdiSomeService bss) {
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
			.isInstanceOf(CdiDummyService.class)
			.isNotNull();
	}

	@Test
	void test_findBeanForFieldParameter0() throws NoSuchFieldException, SecurityException {

		final Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		final Parameter parameter = constructor.getParameters()[0];

		assertThat(locator.findBeanFor(constructor, parameter, 0))
			.isInstanceOf(CdiDummyService.class)
			.isNotNull();
	}

	@Test
	void test_findBeanForFieldParameter1() throws NoSuchFieldException, SecurityException {

		final Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		final Parameter parameter = constructor.getParameters()[1];

		assertThat(locator.findBeanFor(constructor, parameter, 1))
			.isInstanceOf(CdiSomeService.class)
			.isSameAs(foo)
			.isNotNull();
	}

	@Test
	void test_findBeanForFieldParameter2() throws NoSuchFieldException, SecurityException {

		final Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		final Parameter parameter = constructor.getParameters()[2];

		assertThat(locator.findBeanFor(constructor, parameter, 2))
			.isInstanceOf(CdiSomeService.class)
			.isSameAs(bar)
			.isNotNull();
	}

	@Test
	void test_findBean() {
		assertThat(locator.findBean(CdiDummyService.class))
			.isInstanceOf(CdiDummyService.class)
			.isNotNull();
	}

	@Test
	void test_findSomeServiceNamedFoo() {
		assertThat(foo)
			.isInstanceOf(CdiSomeServiceFooImpl.class)
			.isNotNull();
	}

	@Test
	void test_findSomeServiceNamedBar() {
		assertThat(bar)
			.isInstanceOf(CdiSomeServiceBarImpl.class)
			.isNotNull();
	}
}
