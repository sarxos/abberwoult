package com.github.sarxos.abberwoult.cdi.arc;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ArcTest {

	@Inject
	@SomeQualifier("foo")
	SomeRef foo;

	@Inject
	@SomeQualifier("bar")
	SomeRef bar;

	@Test
	void test_injectActorRefByClass() {
		assertThat(foo).isNotNull();
		assertThat(bar).isNotNull();
		assertThat(foo).isNotSameAs(bar);
	}
}
