package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.sarxos.abberwoult.annotation.ActorOf;

import akka.actor.ActorRef;


@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

	@Inject
	@ActorOf(FruitRepositoryActor.class)
	ActorRef repository;

	@GET
	public List<Fruit> fruitList() {
		return new ArrayList<>(Arrays.asList(
			new Fruit("apple", "round and red"),
			new Fruit("banana", "curvy and yellow")));
	}
}
