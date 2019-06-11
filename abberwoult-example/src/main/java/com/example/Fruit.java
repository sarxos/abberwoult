package com.example;

import javax.validation.constraints.NotEmpty;


public class Fruit {

	@NotEmpty
	private String name;

	@NotEmpty
	private String description;

	public Fruit() {
	}

	public Fruit(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
