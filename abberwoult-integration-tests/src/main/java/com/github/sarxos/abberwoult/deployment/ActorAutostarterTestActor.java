package com.github.sarxos.abberwoult.deployment;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.annotation.PreStart;


@Autostart
@Named("autostartedactor")
public class ActorAutostarterTestActor extends SimpleActor {

	static final AtomicInteger started = new AtomicInteger(0);

	@PreStart
	public void setup() {
		started.incrementAndGet();
	}
}
