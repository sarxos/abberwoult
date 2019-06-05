package com.example;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Received;


public class EchoActor extends SimpleActor {

	public static class EchoMsg {

	}

	public void handleEcho(@Received EchoMsg msg) {
		System.out.println("echo");
	}
}
