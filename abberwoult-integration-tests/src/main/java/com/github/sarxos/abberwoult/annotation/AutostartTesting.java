package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.dsl.Utils;


public class AutostartTesting {

	@Autostart
	@NamedActor("testactorstart")
	public static class AutostartActor extends SimpleActor implements Utils {

		public void onInteger(@Received Integer i) {
			reply(i);
		}
	}
}
