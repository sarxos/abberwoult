package com.github.sarxos.abberwoult;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class AbberwoultLifecycleListener {

	private static final Logger LOG = LoggerFactory.getLogger(AbberwoultLifecycleListener.class);

	void onStart(@Observes StartupEvent ev) {
		LOG.info("The application is starting... {}", ev);
		
		final Iterator<Object> beans = CDI.current().getBeanManager().createInstance().iterator();
		
		while (beans.hasNext()) {
			LOG.info("Found bean {}", beans.next());
		}
		
	}

	void onStop(@Observes ShutdownEvent ev) {
		LOG.info("The application is stopping... {}", ev);
	}
}
