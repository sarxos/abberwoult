package com.github.sarxos.abberwoult;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.util.Timeout;


public interface Askable extends Tellable {

	<T> CompletionStage<T> ask(final Object message);

	<T> CompletionStage<T> ask(final Object message, final Timeout timeout);

	<T> CompletionStage<T> ask(final Object message, final Duration timeout);
}
