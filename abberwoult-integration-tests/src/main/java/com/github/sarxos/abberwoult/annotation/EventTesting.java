package com.github.sarxos.abberwoult.annotation;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.dsl.Utils;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;


public class EventTesting {

	public static class EventActor extends SimpleActor implements Utils {

		public static interface Some {
		}

		public static class SomeImpl implements Some {
		}

		public static class Book {

			@Size(min = 5)
			private final String title;

			public Book(final String title) {
				this.title = title;
			}

			public String getTitle() {
				return title;
			}
		}

		private final ActorRef ref;

		public EventActor(final TestKitProbe probe) {
			this.ref = probe.getRef();
		}

		public void onInteger(@Event Integer i) {
			forward(ref, i);
		}

		public void onLong(@Receives @Event Long l) {
			forward(ref, l);
		}

		public void onSomething(@Receives @Event Some s) {
			forward(ref, s);
		}

		public void onBook(@Receives @Valid @Event Book b) {
			forward(ref, b);
		}
	}
}
