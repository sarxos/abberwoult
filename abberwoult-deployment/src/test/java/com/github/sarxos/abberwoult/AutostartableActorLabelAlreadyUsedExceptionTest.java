package com.github.sarxos.abberwoult;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.NamedActor;
import com.github.sarxos.abberwoult.deployment.error.AutostartableLabelAlreadyUsedException;

import io.quarkus.test.QuarkusUnitTest;


public class AutostartableActorLabelAlreadyUsedExceptionTest {

	@Autostart
	@NamedActor("aaaa")
	public static class LabeledActorOne extends SimpleActor {
	}

	@Autostart
	@NamedActor("aaaa")
	public static class LabeledActorTwo extends SimpleActor {
	}

	@RegisterExtension
	static final QuarkusUnitTest test = new QuarkusUnitTest()
		.setExpectedException(AutostartableLabelAlreadyUsedException.class)
		.setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

	@Test
	public void test() {
		// do nothing, required to run test
	}
}
