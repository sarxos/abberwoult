package com.github.sarxos.abberwoult;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.deployment.error.AutostartableActorNotLabeledException;

import io.quarkus.test.QuarkusUnitTest;


public class AutostartableActorNotLabeledExceptionTest {

	@Autostart
	public static class NotLabeledActor extends SimpleActor {
	}

	@RegisterExtension
	static final QuarkusUnitTest config = new QuarkusUnitTest()
		.setExpectedException(AutostartableActorNotLabeledException.class)
		.setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

	@Test
	public void test() {
		// do nothing, required to run test
	}
}
