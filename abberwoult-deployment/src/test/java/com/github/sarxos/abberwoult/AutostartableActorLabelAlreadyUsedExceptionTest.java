package com.github.sarxos.abberwoult;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.deployment.error.AutostartableActorLabelAlreadyUsedException;

import io.quarkus.test.QuarkusUnitTest;


public class AutostartableActorLabelAlreadyUsedExceptionTest {

	@Autostart
	@Labeled("aaaa")
	public static class LabeledActorOne extends SimpleActor {
	}

	@Autostart
	@Labeled("aaaa")
	public static class LabeledActorTwo extends SimpleActor {
	}

	@RegisterExtension
	static final QuarkusUnitTest config = new QuarkusUnitTest()
		.setExpectedException(AutostartableActorLabelAlreadyUsedException.class)
		.setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

	@Test
	public void test() {
		// do nothing, required to run test
	}
}
