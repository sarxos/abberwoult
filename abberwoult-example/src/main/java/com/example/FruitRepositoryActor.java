package com.example;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.MessageHandler;


public class FruitRepositoryActor extends SimpleActor {

	@MessageHandler
	void handleFruitListGet(final FruitListGetMsg msg) {
		System.out.println("RECEIVED LIST");
	}

	@MessageHandler
	void handleFruitAddMsg(final FruitAddMsg msg) {
		System.out.println("RECEIVED ADD");
	}
}
