package com.github.sarxos.abberwoult.exception;

import java.lang.invoke.MethodHandle;


/**
 * @author Bartosz Firyn (sarxos)
 */
@SuppressWarnings("serial")
public class PreStartInvocationException extends RuntimeException {

	public PreStartInvocationException(final Object thiz, final MethodHandle handle, final Throwable cause) {
		super("Pre start handle " + handle + " invocation failed in " + thiz.getClass(), cause);
	}
}
