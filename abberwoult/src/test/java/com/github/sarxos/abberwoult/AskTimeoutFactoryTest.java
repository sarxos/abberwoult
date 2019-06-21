package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.config.AskTimeout;

import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.duration.FiniteDuration;


@QuarkusTest
public class AskTimeoutFactoryTest {

	@AskTimeout
	Timeout t;

	@AskTimeout
	Duration d;

	@AskTimeout
	FiniteDuration f;

	@Test
	void test_injectTimeout() {
		assertThat(t).isNotNull();
		assertThat(t.duration().toNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(10));
	}

	@Test
	void test_injectDuration() {
		assertThat(d).isNotNull();
		assertThat(d.toNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(10));
	}

	@Test
	void test_injectFiniteDuration() {
		assertThat(f).isNotNull();
		assertThat(f.toNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(10));
	}
}
