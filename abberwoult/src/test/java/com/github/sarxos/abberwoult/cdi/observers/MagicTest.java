package com.github.sarxos.abberwoult.cdi.observers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class MagicTest {

	@Test
	void test_isTrueReallyTrue() {
		assertThat(true).isTrue();
	}
}
