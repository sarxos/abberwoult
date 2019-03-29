package com.github.sarxos.abberwoult.cdi;

import static org.assertj.core.api.Assertions.assertThat;

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

	@Test
	void test_injectBeanLocatorInTest() {
		assertThat(locator).isNotNull();
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
