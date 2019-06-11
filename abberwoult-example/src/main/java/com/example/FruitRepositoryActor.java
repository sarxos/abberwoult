package com.example;

import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.Valid;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.trait.Comm;


public class FruitRepositoryActor extends SimpleActor implements Comm {

	/**
	 * A {@link Set} which will hold all our fruits. It does not have to be synchronized or
	 * concurrent because actors are inherently concurrent themselves.
	 */
	private final Set<Fruit> fruits = new LinkedHashSet<>();

	public void onFruitListMsg(@Receives final FruitListMsg msg) {

		// We reply with unmodifiable view of internal fruits set because we do not want actor state
		// to be modified outside the actor. Modifying internal actor state (a fruits set in this
		// case) violates actor model principles and introduces concurrency issues.

		reply(unmodifiableSet(fruits));
	}

	public void onFruitAddMsg(@Receives @Valid final FruitAddMsg msg) {

		final boolean updated = fruits.add(msg.getFruit());
		final int size = fruits.size();

		reply(new FruitAddMsg.Result(updated, size));
	}
}
