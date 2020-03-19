package com.github.sarxos.abberwoult.cdi.internals;

public class CdiTestConstructorWithArgs {

	private final int x;
	private final int y;

	public CdiTestConstructorWithArgs(int x, int y) {
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
