package com.example;

public class FruitListMsg {

	private static final FruitListMsg instance = new FruitListMsg();

	private FruitListMsg() {
		// singleton
	}

	public static FruitListMsg getInstance() {
		return instance;
	}
}
