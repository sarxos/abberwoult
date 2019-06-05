package com.example;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Received;


public class FruitRepositoryActor extends SimpleActor {

	public void handleFruitListGet(@Received final FruitListGetMsg msg) {
		System.out.println("RECEIVED LIST");
	}

	public void handleFruitAddMsg(@Received final FruitAddMsg msg) {
		System.out.println("RECEIVED ADD");
	}
}
