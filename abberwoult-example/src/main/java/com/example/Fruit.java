package com.example;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.NotEmpty;

import lombok.Data;


public @Data class Fruit {

	@NotEmpty
	@JsonbProperty("name")
	private String name;

	@NotEmpty
	@JsonbProperty("desc")
	private String description;
}
