package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.dsl.Utils;


public class ReceivesActor extends SimpleActor implements Utils {

	public void onInteger(@Receives Integer i) {
		reply(11);
	}
}
