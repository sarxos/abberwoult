package com.example;

import java.util.Collection;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.sarxos.abberwoult.AskableActorRef;
import com.github.sarxos.abberwoult.annotation.ActorOf;


@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

	@Inject
	@ActorOf(FruitRepositoryActor.class)
	AskableActorRef repository;

	@GET
	public CompletionStage<Collection<Fruit>> fruitList() {
		return repository.ask(FruitListMsg.getInstance());
	}

	@PUT
	public CompletionStage<Collection<Fruit>> fruitAdd(@Valid Fruit fruit) {
		return repository.ask(new FruitAddMsg(fruit));
	}
}
