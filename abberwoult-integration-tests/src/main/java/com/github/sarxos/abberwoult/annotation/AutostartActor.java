package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.dsl.Utils;


@Autostart
@Named("testactorstart")
public class AutostartActor extends SimpleActor implements Utils {

	public void onInteger(@Receives Integer i) {
		reply(i);
	}
}
