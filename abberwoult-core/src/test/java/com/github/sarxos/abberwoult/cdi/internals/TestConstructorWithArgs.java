package com.github.sarxos.abberwoult.cdi.internals;

public class TestConstructorWithArgs {

	private final int x;
	private final int y;

	public TestConstructorWithArgs(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
