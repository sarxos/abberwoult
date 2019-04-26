package com.example;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.MessageHandler;


public class EchoActor extends SimpleActor {

	public static class EchoMsg {

	}

	@MessageHandler
	void handleEcho(EchoMsg msg) {
		System.out.println("echo");
	}
}
