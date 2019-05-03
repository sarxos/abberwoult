package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.ACTOR_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.APPLICATION_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.SIMPLE_ACTOR_CLASS;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.AbberwoultLifecycleListener;
import com.github.sarxos.abberwoult.ActorEngine;
import com.github.sarxos.abberwoult.Propser;
import com.github.sarxos.abberwoult.cdi.ActorRefFactory;
import com.github.sarxos.abberwoult.cdi.ActorSelectionFactory;
import com.github.sarxos.abberwoult.cdi.ActorSystemFactory;
import com.github.sarxos.abberwoult.cdi.BeanLocator;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.substrate.ReflectiveClassBuildItem;


public class AbberwoultProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(AbberwoultProcessor.class);

	private static final String[] CORE_BEAN_CLASSES = {
		AbberwoultLifecycleListener.class.getName(),
		BeanLocator.class.getName(),
		ActorEngine.class.getName(),
		ActorRefFactory.class.getName(),
		ActorSelectionFactory.class.getName(),
		ActorSystemFactory.class.getName(),
		Propser.class.getName(),
		MessageHandlersRegistry.class.getName(),
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
	List<ReflectiveClassBuildItem> doRegisterReflectiveClasses(final CombinedIndexBuildItem combinedIndex) {
		return combinedIndex
			.getIndex()
			.getAllKnownSubclasses(SIMPLE_ACTOR_CLASS)
			.stream()
			.map(clazz -> new ReflectiveClassBuildItem(true, false, clazz.name().toString()))
			.collect(toList());

	}

	@BuildStep
	@Record(STATIC_INIT)
	void doRegisterMessageHandlers(final MessageHandlersRegistryTemplate template, final CombinedIndexBuildItem combinedIndex) {
		combinedIndex.getIndex()
			.getAllKnownSubclasses(SIMPLE_ACTOR_CLASS)
			.stream()
			.peek(clazz -> LOG.debug("Record actor class {} registration in registry", clazz))
			.forEach(clazz -> template.register(clazz.name().toString()));
	}
}
