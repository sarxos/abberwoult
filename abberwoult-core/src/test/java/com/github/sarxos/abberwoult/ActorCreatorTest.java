package com.github.sarxos.abberwoult;

import org.assertj.core.api.Assertions;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.jupiter.api.Test;

public class ActorCreatorTest {

	@Test
	void test_constructorNoArgs() {

		final class TestActor extends SimpleActor { }

		final ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
		final ActorCreator creator = new ActorCreator(locator, TestActor.class);
		
		Assertions
			.assertThat(creator.getClazz())
			.isSameAs(TestActor.class);
		Assertions
			.assertThat(creator.getLocator())
			.isSameAs(locator);
		Assertions
			.assertThat(creator.getArgs())
			.isEmpty();
	}

	@Test
	void test_constructorWithArgs() {

		final class TestActor extends SimpleActor { }

		final ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
		final ActorCreator creator = new ActorCreator(locator, TestActor.class, "a", "b", "c");
		
		Assertions
			.assertThat(creator.getClazz())
			.isSameAs(TestActor.class);
		Assertions
			.assertThat(creator.getLocator())
			.isSameAs(locator);
		Assertions
			.assertThat(creator.getArgs())
			.containsExactly("a", "b", "c");
	}
}
