package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.DotNames.ACTOR_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.APPLICATION_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.AUTOSTART_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.SIMPLE_ACTOR_CLASS;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.cdi.BeanLocator;
import com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry;
import com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistryTemplate;
import com.github.sarxos.abberwoult.deployment.ActorStarterTemplate;
import com.github.sarxos.abberwoult.deployment.error.AutostartableActorNoArgConstrutorMissingException;
import com.github.sarxos.abberwoult.deployment.error.AutostartableActorNotLabeledException;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.substrate.ReflectiveClassBuildItem;


public class AbberwoultProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(AbberwoultProcessor.class);

	private static final String[] CORE_BEAN_CLASSES = {
		EventsBypass.class.getName(),
		BeanLocator.class.getName(),
		ActorRefFactory.class.getName(),
		ActorSelectionFactory.class.getName(),
		ActorSystemFactory.class.getName(),
		ActorSystemUniverse.class.getName(),
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
	 * Register a extension capability and feature.
	 *
	 * @return Abberwoult feature build item
	 */
	@BuildStep(providesCapabilities = "com.github.abberwoult")
	FeatureBuildItem doProvideFeatureName() {
		return new FeatureBuildItem("abberwoult");
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
	List<ActorBuildItem> doFindActorClasses(final CombinedIndexBuildItem combined) {
		return combined.getIndex()
			.getAllKnownSubclasses(SIMPLE_ACTOR_CLASS)
			.stream()
			.map(ActorBuildItem::new)
			.collect(toList());
	}

	/**
	 * Take all {@link ActorBuildItem} and register wrapped actor class as a reflective class.
	 *
	 * @param actors the {@link List} of {@link ActorBuildItem}
	 * @return The {@link List} of {@link ReflectiveClassBuildItem}
	 */
	@BuildStep
	List<ReflectiveClassBuildItem> doRegisterReflectiveClasses(final List<ActorBuildItem> actors) {
		return actors.stream()
			.map(ActorBuildItem::getActorClassName)
			.map(clazz -> new ReflectiveClassBuildItem(true, false, clazz))
			.collect(toList());
	}

	@BuildStep
	@Record(STATIC_INIT)
	void doRegisterActors(final List<ActorBuildItem> actors, final ActorInterceptorRegistryTemplate registry) {
		actors.stream()
			.map(ActorBuildItem::getActorClassName)
			.peek(clazz -> LOG.debug("Record actor {} in registry", clazz))
			.forEach(clazz -> registry.register(clazz));
	}

	private boolean isAutostartPresent(final ActorBuildItem item) {
		return item.getActorClass().asClass().classAnnotation(AUTOSTART_ANNOTATION) != null;
	}

	private void assertNoArgConstructorIsPresent(final ActorBuildItem item) {
		if (!item.getActorClass().hasNoArgsConstructor()) {
			throw new AutostartableActorNoArgConstrutorMissingException(item);
		}
	}

	private void assertIsActorLabeled(final ActorBuildItem item) {
		if (item.getActorClass().classAnnotation(DotNames.LABELED_ANNOTATION) == null) {
			throw new AutostartableActorNotLabeledException(item);
		}
	}

	@BuildStep
	@Record(value = ExecutionTime.RUNTIME_INIT)
	void doAutostartActors(final List<ActorBuildItem> actors, final ActorStarterTemplate autostarter) {
		actors.stream()
			.filter(this::isAutostartPresent)
			.peek(this::assertNoArgConstructorIsPresent)
			.peek(this::assertIsActorLabeled)
			.map(ActorBuildItem::getActorClassName)
			.peek(clazz -> LOG.debug("Autostarting actor {}", clazz))
			.forEach(clazz -> autostarter.register(clazz));
	}
}
