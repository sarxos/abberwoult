package com.example;

import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.trait.Utils;

import lombok.Data;
import lombok.Getter;


@Autostart
@Labeled("fruits-repo")
public class FruitRepositoryActor extends SimpleActor implements Utils {

	private static final Logger LOG = Logger.getLogger(FruitRepositoryActor.class);

	public static final @Data class FruitAddMsg {
		private final @Valid @NotNull Fruit fruit;
		public static @Data class Result {
			private final boolean updated;
			private final int size;
		}
	}

	public static final @Data class FruitListMsg {
		private static final @Getter FruitListMsg instance = new FruitListMsg();
		private FruitListMsg() {
		}
	}

	/**
	 * A {@link Set} which holds all our fruits. It does not have to be synchronized or concurrent
	 * because actors are inherently concurrent themselves.
	 */
	private final Set<Fruit> fruits = new LinkedHashSet<>();

	@PreStart
	public void setup() {
		LOG.infof("Setting up actor %s", getSelf().path());
	}

	@PostStop
	public void teardown() {
		LOG.infof("Tearing down actor %s", getSelf().path());
	}

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
