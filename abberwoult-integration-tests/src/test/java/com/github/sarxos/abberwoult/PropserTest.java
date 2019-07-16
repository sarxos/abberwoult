package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.Dispatcher;
import com.github.sarxos.abberwoult.annotation.Mailbox;
import com.github.sarxos.abberwoult.cdi.BeanLocator;

import akka.actor.Props;
import akka.dispatch.Dispatchers;
import akka.dispatch.Mailboxes;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class PropserTest {

	@Inject
	Propser propser;

	@Inject
	BeanLocator locator;

	@Test
	void test_injectPropserIntoTest() {
		assertThat(propser).isNotNull();
	}

	@Test
	void test_propsAnnotatedActor() {

		@Mailbox("foo")
		@Dispatcher("bar")
		class TestActor extends SimpleActor {
		}

		final Props props = propser.props(TestActor.class);

		assertThat(props).isNotNull();
		assertThat(props.mailbox()).isEqualTo("foo");
		assertThat(props.dispatcher()).isEqualTo("bar");
	}

	@Test
	void test_propsNoAnnotations() {

		class TestActor extends SimpleActor {
		}

		final Props props = propser.props(TestActor.class);

		assertThat(props).isNotNull();
		assertThat(props.mailbox()).isEqualTo(Mailboxes.DefaultMailboxId());
		assertThat(props.dispatcher()).isEqualTo(Dispatchers.DefaultDispatcherId());
	}
}
