package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.MessageHandler;


public class BuuActor extends SimpleActor implements WithKam {

	public static class BuuMsg {

	}

	@MessageHandler
	void handleEcho(BuuMsg msg) {
		System.out.println("echo");
	}

	@Override
	@MessageHandler
	public void doSomethinhg(Object o) {
		WithKam.super.doSomethinhg(o);
	}
}
