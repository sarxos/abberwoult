package com.github.sarxos.abberwoult.util;

import java.util.concurrent.CompletionStage;


public class AskableUtils {

	@SuppressWarnings("unchecked")
	public static <T> CompletionStage<T> throwIfThrowable(final CompletionStage<?> stage) {
		return (CompletionStage<T>) stage.thenApply(AskableUtils::throwIfThrowable);
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> Void sneaky(Throwable e) throws E {
		throw (E) e;
	}

	@SuppressWarnings("unchecked")
	private static <T> T throwIfThrowable(T value) {
		if (value instanceof Throwable) {
			return (T) sneaky((Throwable) value);
		}
		return value;
	}
}
