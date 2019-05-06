package com.github.sarxos.abberwoult.exception;

@SuppressWarnings("serial")
public abstract class BeanInjectionException extends IllegalStateException {

	public BeanInjectionException(final String message, final Throwable t) {
		super(message, t);
	}

	public BeanInjectionException(final String message) {
		super(message);
	}

	public BeanInjectionException() {
		super();
	}
}
