package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.DotNames.ABSTRACT_ACTOR_CLASS;
import static com.github.sarxos.abberwoult.DotNames.ACTOR_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.APPLICATION_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.AUTOSTART_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.INJECT_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.SHARD_ENTITY_ID_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.SHARD_ID_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.SHARD_ROUTABLE_MESSAGE_INTERFACE;
import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.cdi.BeanLocator;
import com.github.sarxos.abberwoult.deployment.ActorAutostarter;
import com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry;
import com.github.sarxos.abberwoult.deployment.error.AutostartableLabelAlreadyUsedException;
import com.github.sarxos.abberwoult.deployment.error.AutostartableNameMissingException;
import com.github.sarxos.abberwoult.deployment.error.AutostartableNoArgConstrutorMissingException;
import com.github.sarxos.abberwoult.deployment.error.ImplementationMissingException;
import com.github.sarxos.abberwoult.deployment.item.ActorBuildItem;
import com.github.sarxos.abberwoult.deployment.item.InstrumentedActorBuildItem;
import com.github.sarxos.abberwoult.deployment.item.ShardMessageBuildItem;
import com.github.sarxos.abberwoult.deployment.item.SyntheticFieldReaderBuildItem;
import com.github.sarxos.abberwoult.deployment.util.DeploymentUtils;
import com.github.sarxos.abberwoult.jandex.Reflector;
import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CapabilityBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;


public class AbberwoultProcessor {

	private static final Logger LOG = Logger.getLogger("abberwoult");

	private static final String[] CORE_BEAN_CLASSES = {
		EventsBypass.class.getName(),
		BeanLocator.class.getName(),
		ActorRefFactory.class.getName(),
		ActorSelectionFactory.class.getName(),
		ActorSystemFactory.class.getName(),
		ActorUniverse.class.getName(),
		ActorSystemFactory.class.getName(),
		AskableActorRefFactory.class.getName(),
		AskableActorSelectionFactory.class.getName(),
		ClusterFactory.class.getName(),
		ClusterShardingFactory.class.getName(),
		EventsBypass.class.getName(),
		EventStreamFactory.class.getName(),
		Propser.class.getName(),
		TopicFactory.class.getName(),
		ActorInterceptorRegistry.class.getName(),
	};

	/**
	 * Register extension capability.
	 *
	 * @return Abberwoult capability build item
	 */
	@BuildStep
	CapabilityBuildItem doCreateCapabilitiesItem() {
		return new CapabilityBuildItem("com.github.abberwoult");
	}

	/**
	 * Register extension feature info.
	 *
	 * @return Abberwoult feature build item
	 */
	@BuildStep
	FeatureBuildItem doProvideFeatureName() {
		return new FeatureBuildItem("abberwoult");
	}

	@BuildStep
	Reflector doCreateReflector(final CombinedIndexBuildItem index) {
		return new Reflector(index);
	}

	/**
	 * Register a custom bean defining annotation
	 *
	 * @return Bean defining annotation build item
	 */
	@BuildStep
	BeanDefiningAnnotationBuildItem doRegisterBeanDefinningAnnotations() {
		return new BeanDefiningAnnotationBuildItem(ACTOR_SCOPED_ANNOTATION, APPLICATION_SCOPED_ANNOTATION);
	}

	@BuildStep
	UnremovableBeanBuildItem doCoreBeansUnremovable() {
		return new UnremovableBeanBuildItem(this::isCoreBean);
	}

	private boolean isCoreBean(final BeanInfo bi) {
		for (final String clazz : CORE_BEAN_CLASSES) {
			if (StringUtils.equals(clazz, getBeanClassName(bi))) {
				return true;
			}
		}
		return false;
	}

	private String getBeanClassName(final BeanInfo bi) {
		return bi.getBeanClass().toString();
	}

	/**
	 * Register the CDI beans that are needed by the test extension
	 *
	 * @param additionalBeans - producer for additional bean items
	 */
	@BuildStep
	AdditionalBeanBuildItem doRegisterAdditionalBeans() {
		return new AdditionalBeanBuildItem(CORE_BEAN_CLASSES);
	}

	/**
	 * Scan {@link Index} to get all descendants of {@link SimpleActor} class.
	 *
	 * @param combined the {@link CombinedIndexBuildItem}
	 * @return The {@link List} of {@link ActorBuildItem}
	 */
	@BuildStep
	List<ActorBuildItem> doFindActorClasses(final Reflector reflector) {
		return reflector
			.findSubclassesOf(ABSTRACT_ACTOR_CLASS)
			.map(ActorBuildItem::new)
			.collect(toList());
	}

	@BuildStep(loadsApplicationClasses = true)
	List<InstrumentedActorBuildItem> doInstrumentActorClasses(
		final List<ActorBuildItem> actors,
		final BuildProducer<GeneratedClassBuildItem> generated) {

		return actors.stream()
			.map(ActorBuildItem::getActorClass)
			.distinct()
			.peek(clazz -> LOG.infof("Instrumenting actor class %s", clazz.getName()))
			.map(clazz -> new InstrumentedActorBuildItem(clazz))
			.peek(actor -> generated.produce(new GeneratedClassBuildItem(true, actor.getActorClassName(), actor.getBytecode())))
			.collect(toList());
	}

	@BuildStep
	@Record(STATIC_INIT)
	void doRegisterActors(final List<InstrumentedActorBuildItem> actors, final ActorInterceptorRegistry interceptors) {
		actors.stream()
			.map(InstrumentedActorBuildItem::getActorClassName)
			.peek(clazz -> interceptors.register(clazz))
			.forEach(clazz -> LOG.tracef("Actor %s is registered", clazz));
	}

	/**
	 * Take all {@link ActorBuildItem} and register wrapped actor class as a reflective class.
	 *
	 * @param actors the {@link List} of {@link ActorBuildItem}
	 * @return The {@link List} of {@link ReflectiveClassBuildItem}
	 */
	@BuildStep
	List<ReflectiveClassBuildItem> doRegisterReflectiveClasses(final List<InstrumentedActorBuildItem> actors) {
		return actors.stream()
			.map(InstrumentedActorBuildItem::toReflectiveClassBuildItem)
			.collect(toList());
	}

	private boolean isAutostartPresent(final InstrumentedActorBuildItem actor) {
		return actor.hasAnnotation(AUTOSTART_ANNOTATION);
	}

	private void assertNoArgConstructorIsPresent(final InstrumentedActorBuildItem actor) {
		if (!actor.hasNoArgsConstructor()) {
			throw new AutostartableNoArgConstrutorMissingException(actor);
		}
	}

	private void assertIsActorNamed(final InstrumentedActorBuildItem actor) {
		if (!actor.isNamed()) {
			throw new AutostartableNameMissingException(actor);
		}
	}

	@BuildStep
	@Record(RUNTIME_INIT)
	void doRegisterAutostartableActors(final List<InstrumentedActorBuildItem> actors, final ActorAutostarter autostarter) {

		final Map<String, InstrumentedActorBuildItem> labels = new HashMap<>();
		final Consumer<InstrumentedActorBuildItem> assertNotDuplicated = actor -> {

			final String name = actor.getActorName().get();
			final InstrumentedActorBuildItem other = labels.put(name, actor);

			if (other != null) {

				final String otherClass = other.getActorClassName();
				final String actorClass = actor.getActorClassName();

				if (ObjectUtils.notEqual(otherClass, actorClass)) {
					throw new AutostartableLabelAlreadyUsedException(actor, other, name);
				}
			}
		};

		actors.stream()
			.filter(this::isAutostartPresent)
			.peek(this::assertNoArgConstructorIsPresent)
			.peek(this::assertIsActorNamed)
			.peek(assertNotDuplicated)
			.map(InstrumentedActorBuildItem::getActorClassName)
			.distinct()
			.peek(clazz -> LOG.infof("Registering autostartable actor class %s", clazz))
			.forEach(clazz -> autostarter.register(clazz));
	}

	private void assertShardRoutableMessageIsImplemented(final ClassRef clazz) {
		if (!clazz.hasInterface(SHARD_ROUTABLE_MESSAGE_INTERFACE)) {
			throw new ImplementationMissingException(clazz, SHARD_ROUTABLE_MESSAGE_INTERFACE);
		}
	}

	@BuildStep
	List<ShardMessageBuildItem> doFindShardMessages(final Reflector reflector) {
		return reflector
			.findClassesWithAnnotationInScope(SHARD_ID_ANNOTATION, SHARD_ENTITY_ID_ANNOTATION)
			.peek(this::assertShardRoutableMessageIsImplemented)
			.distinct()
			.map(ShardMessageBuildItem::new)
			.collect(toList());
	}

	@BuildStep(loadsApplicationClasses = true)
	List<SyntheticFieldReaderBuildItem> doCreateSyntheticMessageExtractors(final List<ShardMessageBuildItem> classes) {
		return classes.stream()
			.map(ShardMessageBuildItem::getMessageClass)
			.peek(clazz -> LOG.infof("Synthetizing field reader for class %s", clazz))
			.map(SyntheticFieldReaderBuildItem::new)
			.collect(toList());
	}

	@BuildStep
	@Record(STATIC_INIT)
	List<GeneratedClassBuildItem> doRecordSyntheticFieldReaders(final List<SyntheticFieldReaderBuildItem> readers, final ShardMessageExtractor sre) {
		return readers.stream()
			.peek(r -> sre.register(r.getMessageClassName(), r.getSyntheticFieldReaderInstance()))
			.map(reader -> new GeneratedClassBuildItem(true, reader.getSyntheticClassName(), reader.getBytecode()))
			.collect(toList());
	}

	@BuildStep
	UnremovableBeanBuildItem doActorInjecteesUnremovable(final Reflector reflector, List<BeanDefiningAnnotationBuildItem> beanDefiners) {

		final Set<DotName> beanAnnotations = new HashSet<>();
		beanAnnotations.add(DotNames.SINGLETON_ANNOTATION);
		beanAnnotations.add(DotNames.APPLICATION_SCOPED_ANNOTATION);
		beanAnnotations.add(DotNames.DEPENDENT_ANNOTATION);

		beanDefiners.stream()
			.map(BeanDefiningAnnotationBuildItem::getName)
			.collect(toCollection(() -> beanAnnotations));

		final Set<String> unremovables = reflector
			.findClassesWithAnnotationInScope(INJECT_ANNOTATION)
			.distinct()
			.flatMap(clazz -> DeploymentUtils.getInjecteesTypesWithAnnotations(clazz, beanAnnotations))
			.peek(type -> LOG.debugf("Registering %s as unremovable bean", type))
			.collect(toSet());

		return new UnremovableBeanBuildItem(new UnremovableBeanBuildItem.BeanClassNamesExclusion(unremovables));
	}
}
