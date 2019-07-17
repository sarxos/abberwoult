package com.github.sarxos.abberwoult;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.deployment.error.AutostartableLabelValueMissingException;

import io.quarkus.test.QuarkusUnitTest;


public class AutostartableActorLabelValueMissingExceptionTest {

	@Labeled
	@Autostart
	public static class LabelValueMissingActor extends SimpleActor {
	}

	@RegisterExtension
	static final QuarkusUnitTest test = new QuarkusUnitTest()
		.setExpectedException(AutostartableLabelValueMissingException.class)
		.setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

	@Test
	public void test() {
		// do nothing, required to run test
	}
}
