package com.github.sarxos.abberwoult.cdi.arc;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;


@Singleton
@QuarkusTest
public class SomeTest {

	@Inject
	@SomeQualifier("foo")
	SomeRef foo;

	// @Inject
	// @SomeQualifier("bar")
	// SomeRef bar;

	@Test
	void test_injectActorRefByClass() {
		assertThat(foo).isNotNull();
		// assertThat(bar).isNotNull();
		// assertThat(foo).isNotSameAs(bar);
	}
}
