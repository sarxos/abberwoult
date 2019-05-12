package com.github.sarxos.abberwoult.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;

import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class TimeoutConverterTest {

	// @ConfigProperty(name = "some.timeout", defaultValue = "999")
	// Provider<Timeout> timeout;

	@Inject
	Config config;

	@Test
	public void test_rawConversion() {
		final Timeout timeout = new TimeoutConverter().convert("777");
		assertThat(timeout.duration().length()).isEqualTo(777);
	}

	@Test
	public void test_getValueFromInjectableConfig() {
		final Timeout timeout = config.getValue("some.timeout", Timeout.class);
		assertThat(timeout.duration().length()).isEqualTo(999);
	}

	// @Test
	// public void test_injectValueIntoFiled() {
	// assertThat(timeout.get().duration().length()).isEqualTo(999);
	// }
}
