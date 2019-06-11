package com.example;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


public class FruitAddMsg {

	@Valid
	@NotNull
	private final Fruit fruit;

	public FruitAddMsg(final Fruit fruit) {
		this.fruit = fruit;
	}

	public Fruit getFruit() {
		return fruit;
	}

	public static class Result {

		private final boolean updated;
		private final int size;

		public Result(final boolean updated, int size) {
			this.updated = updated;
			this.size = size;
		}

		public boolean isUpdated() {
			return updated;
		}

		public int getSize() {
			return size;
		}
	}
}
