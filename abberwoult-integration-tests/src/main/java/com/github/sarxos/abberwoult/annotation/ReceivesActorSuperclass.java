package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.dsl.Utils;


public class ReceivesActorSuperclass extends SimpleActor implements Utils {

	public void onIntegerXXX(@Receives Integer i) {
		reply(22);
	}
}
