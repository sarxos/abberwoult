package com.github.sarxos.abberwoult;

import io.quarkus.runtime.annotations.Template;


@Template
public class TestClassTemplate {

	public void test(String clazz) {
		try {
			Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
