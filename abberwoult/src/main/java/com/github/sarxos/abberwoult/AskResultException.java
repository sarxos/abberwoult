package com.github.sarxos.abberwoult;

/**
 * The exception which is being thrown when ask failed.
 *
 * @author Bartosz Firyn (sarxos)
 */
@SuppressWarnings("serial")
public class AskResultException extends RuntimeException {

	public AskResultException(Throwable cause) {
		super(cause);
	}
}
