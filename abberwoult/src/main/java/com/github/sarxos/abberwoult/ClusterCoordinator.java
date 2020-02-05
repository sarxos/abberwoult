package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;
import static akka.actor.Props.create;
import static com.github.sarxos.abberwoult.util.CollectorUtils.toLinkedSet;

import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.ClusterCoordinator.Boot;
import com.github.sarxos.abberwoult.ClusterCoordinator.ClusterJoined;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.Member;
import akka.event.EventStream;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;


@Singleton
public class ClusterCoordinator {

	private final Cluster cluster;
	private final ActorRef ref;

	@Inject
	public ClusterCoordinator(ActorSystem system, Cluster cluster) {
		this.cluster = cluster;
		this.ref = system.actorOf(create(ClusterCoordinatorActor.class, this));
	}

	public void bootstrap() {
		ref.tell(Boot.INSTANCE, noSender());
	}

	public State getState() {
		return new State(cluster.state());
	}

	public int getMembersCount() {
		return getState().getMembers().size();
	}

	public Cluster getCluster() {
		return cluster;
	}

	static final class Boot {
		private static final Boot INSTANCE = new Boot();
	}

	public static final class ClusterJoined {
		// nothing
	}

	public static final class ClusterLeft {
		// nothing
	}

	public static final class State {

		private final Set<Member> members;
		private final Set<Member> unreachable;
		private final Set<String> unreachableDataCenters;
		private final Set<String> allDataCenters;
		private final Set<String> allRoles;
		private final Set<Address> seenBy;
		private final Address leader;

		protected State(CurrentClusterState state) {
			this.members = toLinkedSet(state.getMembers());
			this.unreachable = state.getUnreachable();
			this.unreachableDataCenters = state.getUnreachableDataCenters();
			this.allDataCenters = state.getAllDataCenters();
			this.allRoles = state.getAllRoles();
			this.seenBy = state.getSeenBy();
			this.leader = state.getLeader();
		}

		public Set<Member> getMembers() {
			return members;
		}

		public Set<Member> getUnreachable() {
			return unreachable;
		}

		public Set<String> getUnreachableDataCenters() {
			return unreachableDataCenters;
		}

		public Set<String> getAllDataCenters() {
			return allDataCenters;
		}

		public Set<String> getAllRoles() {
			return allRoles;
		}

		public Set<Address> getSeenBy() {
			return seenBy;
		}

		public Address getLeader() {
			return leader;
		}
	}
}

final class ClusterCoordinatorActor extends AbstractActor {

	private static final Logger LOG = Logger.getLogger(ClusterCoordinatorActor.class);

	private final AkkaManagement management;
	private final ClusterBootstrap bootstrap;
	private final ClusterCoordinator coordinator;
	private final EventStream events;

	private boolean starting = false;
	private boolean joined = false;

	public ClusterCoordinatorActor(final ClusterCoordinator coordinator) {
		this.management = AkkaManagement.get(getContext().getSystem());
		this.bootstrap = ClusterBootstrap.get(getContext().getSystem());
		this.coordinator = coordinator;
		this.events = getContext()
			.getSystem()
			.getEventStream();
	}

	@Override
	public void preStart() throws Exception {
		subscribe(ClusterEvent.MemberUp.class);
		subscribe(ClusterEvent.MemberLeft.class);
	}

	private void subscribe(final Class<?> clazz) {
		events.subscribe(self(), clazz);
	}

	private void publish(final Object event) {
		events.publish(event);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(ClusterEvent.MemberUp.class, this::onMemberUp)
			.match(ClusterEvent.MemberLeft.class, this::onMemberLeft)
			.match(Boot.class, this::doBoot)
			.build();
	}

	private Cluster getCluster() {
		return coordinator.getCluster();
	}

	private Address getSelfAddress() {
		return getCluster().selfAddress();
	}

	private void onMemberUp(final ClusterEvent.MemberUp up) {

		final Address remote = up.member().address();
		final Address local = getSelfAddress();
		final String which = getAddressForLog(remote, local);

		LOG.debugf("Node [%s] joined [%s]", remote, which);

		if (joined) {
			return;
		}

		publish(new ClusterJoined());

		joined = true;
	}

	private void onMemberLeft(final ClusterEvent.MemberLeft left) {

		final Address remote = left.member().address();
		final Address local = getSelfAddress();
		final String which = getAddressForLog(remote, local);

		LOG.debugf("Node [%s] left [%s]", remote, which);
	}

	private String getAddressForLog(final Address remote, final Address local) {
		if (Objects.equals(remote, local)) {
			return "itself";
		} else {
			return local.toString();
		}
	}

	private void doBoot(final Boot boot) {

		if (starting) {
			return;
		}

		management
			.start()
			.thenRun(bootstrap::start);

		starting = true;
	}
}
