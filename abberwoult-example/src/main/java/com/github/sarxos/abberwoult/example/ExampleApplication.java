package com.github.sarxos.abberwoult.example;

import com.github.sarxos.abberwoult.Application;
import com.github.sarxos.abberwoult.Engine;
import javax.inject.Inject;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;

@Service
public class ExampleApplication implements Application {

	private final Engine engine;

	@Inject
	public ExampleApplication(final Engine engine) {
		this.engine = engine;
	}

	@Override
	public void start() {
		System.out.println(engine);
	}

	public static void main(String[] args) {
		ServiceLocatorUtilities
			.createAndPopulateServiceLocator()
			.getAllServices(Application.class).stream()
			.forEach(Application::start);
	}
}
