package com.github.sarxos.abberwoult;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.annotation.NamedActor;
import com.github.sarxos.abberwoult.testkit.TestKit;


public class ShardTest {

	@NamedActor("karrambashard")
	Sharding sharding;

	@Inject
	ActorUniverse universe;

	@Inject
	TestKit testkit;

}
