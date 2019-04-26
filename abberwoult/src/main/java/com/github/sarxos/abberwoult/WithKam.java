package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.MessageHandler;

public interface WithKam {

	@MessageHandler
	default void doSomethinhg(Object o) {

	}

}
