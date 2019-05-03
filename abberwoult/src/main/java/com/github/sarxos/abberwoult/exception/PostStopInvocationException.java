package com.github.sarxos.abberwoult.exception;

import java.lang.invoke.MethodHandle;


/**
 * @author Bartosz Firyn (sarxos)
 */
@SuppressWarnings("serial")
public class PostStopInvocationException extends RuntimeException {

	public PostStopInvocationException(final Object thiz, final MethodHandle handle, final Throwable cause) {
		super("Post stop handle " + handle + " invocation failed in " + thiz.getClass(), cause);
	}
}
