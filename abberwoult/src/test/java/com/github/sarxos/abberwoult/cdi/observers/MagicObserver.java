package com.github.sarxos.abberwoult.cdi.observers;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class MagicObserver {

	private final MagicSystem system;

	@Inject
	public MagicObserver(final MagicSystem system, final BeanManager bm) {
		// ArcContainer container = Arc.container();
		// InjectableContext context = container.getActiveContext(Singleton.class);
		// System.out.println("Container context is " + context);
		//
		// System.out.println(bm.getContext(Singleton.class).isActive());
		//
		this.system = system;
	}

	public void observe(@Observes(during = TransactionPhase.BEFORE_COMPLETION) Object event) {
		System.out.println("observed " + event);
	}
}
