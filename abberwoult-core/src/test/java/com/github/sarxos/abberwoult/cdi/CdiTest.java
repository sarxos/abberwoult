package com.github.sarxos.abberwoult.cdi;

import io.quarkus.test.junit.QuarkusTest;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Singleton
@QuarkusTest
public class CdiTest {
	
	@Singleton
	public static class TestService {
	}

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
	void test_lookupService() {
		Assertions
			.assertThat(CDI.current().select(TestService.class).get())
			.isNotNull();
	}
}
