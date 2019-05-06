package com.example;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Receives;


public class FruitRepositoryActor extends SimpleActor {

	public void handleFruitListGet(@Receives final FruitListGetMsg msg) {
		System.out.println("RECEIVED LIST");
	}

	public void handleFruitAddMsg(@Receives final FruitAddMsg msg) {
		System.out.println("RECEIVED ADD");
	}
}
