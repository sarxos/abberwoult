package com.github.sarxos.abberwoult.config;

import org.assertj.core.api.Assertions;


//@QuarkusTest
public class HoconTest {

	// @Inject
	Hocon hocon;

	// @Test
	public void test_inject() {
		Assertions.assertThat(hocon).isNotNull();
	}
}
