package com.github.sarxos.abberwoult;

import org.assertj.core.api.Assertions;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.Dispatcher;
import com.github.sarxos.abberwoult.annotation.Mailbox;

import akka.actor.CreatorConsumer;
import akka.actor.Props;
import akka.dispatch.Dispatchers;
import akka.dispatch.Mailboxes;
import scala.collection.JavaConversions;

public class PropserTest {

	@Test
	void test_service() {
		Assertions
			.assertThat(ServiceLocatorUtilities
				.createAndPopulateServiceLocator()
				.getService(Propser.class))
			.isNotNull();
	}

	@Test
	void test_propsAnnotatedActor() {

		@Mailbox("foo")
		@Dispatcher("bar")
		class TestActor extends SimpleActor { }

		final Props props = ServiceLocatorUtilities
			.createAndPopulateServiceLocator()
			.getService(Propser.class)
			.props(TestActor.class);

		Assertions
			.assertThat(props)
			.isNotNull();
		Assertions
			.assertThat(props.mailbox())
			.isEqualTo("foo");
		Assertions
			.assertThat(props.dispatcher())
			.isEqualTo("bar");
	}

	@Test
	void test_propsNoAnnotations() {

		class TestActor extends SimpleActor { }

		final Props props = ServiceLocatorUtilities
			.createAndPopulateServiceLocator()
			.getService(Propser.class)
			.props(TestActor.class);

		Assertions
			.assertThat(props)
			.isNotNull();
		Assertions
			.assertThat(props.mailbox())
			.isEqualTo(Mailboxes.DefaultMailboxId());
		Assertions
			.assertThat(props.dispatcher())
			.isEqualTo(Dispatchers.DefaultDispatcherId());
	}
}
