package com.github.sarxos.abberwoult.exception;

import java.lang.invoke.MethodHandle;

import com.github.sarxos.abberwoult.annotation.Receives;


/**
 * This exception is thrown when invocation of a method annotated with {@link Receives}
 * failed.
 *
 * @author Bartosz Firyn (sarxos)
 */
@SuppressWarnings("serial")
public class MessageHandlerInvocationException extends RuntimeException {

	public MessageHandlerInvocationException(final Object thiz, final MethodHandle handle, final Throwable cause) {
		super("Message handler " + handle + " invocation failed in " + thiz.getClass(), cause);
	}
}
