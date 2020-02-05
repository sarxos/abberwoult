package com.example;

import static java.nio.channels.Channels.newChannel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.example.FruitRepositoryActor.FruitAddMsg;
import com.example.FruitRepositoryActor.FruitListMsg;
import com.github.sarxos.abberwoult.AskableActorSelection;
import com.github.sarxos.abberwoult.ClusterCoordinator;
import com.github.sarxos.abberwoult.annotation.ActorOf;

import io.quarkus.runtime.StartupEvent;


@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

	private static final String FILE = "./SampleVideo_1280x720_30mb.mp4";

	@Inject
	@ActorOf(FruitRepositoryActor.class)
	AskableActorSelection repository;

	@Inject
	ClusterCoordinator coordinator;

	void onStart(@Observes StartupEvent ev) {
		coordinator.bootstrap();
	}

	@GET
	public CompletionStage<Collection<Fruit>> fruitList() {
		return repository.ask(FruitListMsg.getInstance());
	}

	@PUT
	public CompletionStage<Object> fruitAdd(@Valid Fruit fruit) {
		return repository.ask(new FruitAddMsg(fruit));
	}

	@GET
	@Path("stream")
	@Produces("video/mp4")
	public StreamingOutput videoGet() throws IOException {
		return output -> {
			try (final FileChannel ic = new FileInputStream(FILE).getChannel()) {
				try (final WritableByteChannel oc = newChannel(output)) {
					ic.transferTo(0, ic.size(), oc);
				}
			}
		};
	}
}
