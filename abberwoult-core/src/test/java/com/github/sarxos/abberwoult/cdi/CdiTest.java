package com.github.sarxos.abberwoult.cdi;

import javax.enterprise.inject.spi.CDI;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.cdi.internals.DummyService;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class CdiTest {

	@Test
	void test_cdiCurrent() {
		Assertions
			.assertThat(CDI.current())
			.isNotNull();
	}

	@Test
	void test_getBeanManager() {
		Assertions
			.assertThat(CDI.current().getBeanManager())
			.isNotNull();
	}

	@Test
	void test_cdiSelectTestService() {
		Assertions
			.assertThat(CDI.current().select(DummyService.class).get())
			.isNotNull();
	}

	@Test
	void test_singletonServiceCreatedOnlyOnce() {
		Assertions
			.assertThat(CDI.current().select(DummyService.class).get())
			.isNotNull();
	}
}
