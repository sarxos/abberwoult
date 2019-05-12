package com.github.sarxos.abberwoult;

import java.util.concurrent.CompletionStage;


public interface Askable {

	<T> CompletionStage<T> ask(final Object message);
}
