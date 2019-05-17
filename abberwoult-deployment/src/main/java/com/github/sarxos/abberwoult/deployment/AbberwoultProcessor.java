package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.deployment.DotNames.ACTOR_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.DotNames.APPLICATION_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.DotNames.SIMPLE_ACTOR_CLASS;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.EventsBypass;
import com.github.sarxos.abberwoult.Propser;
import com.github.sarxos.abberwoult.cdi.ActorRefFactory;
import com.github.sarxos.abberwoult.cdi.ActorSelectionFactory;
import com.github.sarxos.abberwoult.cdi.ActorSystemFactory;
import com.github.sarxos.abberwoult.cdi.BeanLocator;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
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
		Propser.class.getName(),
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

	@BuildStep
	void doRegisterReflectiveClasses(
		final CombinedIndexBuildItem combinedIndex,
		final BuildProducer<ReflectiveClassBuildItem> reflectives) {

		final IndexView index = combinedIndex.getIndex();

		index
			.getAllKnownSubclasses(SIMPLE_ACTOR_CLASS)
			.stream()
			.map(clazz -> new ReflectiveClassBuildItem(true, false, clazz.name().toString()))
			.forEach(item -> reflectives.produce(item));
	}

	@BuildStep
	@Record(STATIC_INIT)
	void doRegisterActors(final ActorInterceptorRegistryTemplate template, final CombinedIndexBuildItem combinedIndex) {
		combinedIndex.getIndex()
			.getAllKnownSubclasses(SIMPLE_ACTOR_CLASS)
			.stream()
			.peek(clazz -> LOG.debug("Record actor class {} in registry", clazz))
			.forEach(clazz -> template.register(clazz.name().toString()));
	}
}
