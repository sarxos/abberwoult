package com.example;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Receives;


public class EchoActor extends SimpleActor {

	public static class EchoMsg {

	}

	public void handleEcho(@Receives EchoMsg msg) {
		System.out.println("echo");
	}
}
