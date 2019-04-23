package com.github.sarxos.abberwoult.deployment;

import org.jboss.jandex.DotName;

import com.github.sarxos.abberwoult.AbberwoultLifecycleListener;
import com.github.sarxos.abberwoult.ActorEngine;
import com.github.sarxos.abberwoult.ActorRefFactory;
import com.github.sarxos.abberwoult.ActorSelectionFactory;
import com.github.sarxos.abberwoult.ActorSystemFactory;
import com.github.sarxos.abberwoult.Propser;
import com.github.sarxos.abberwoult.annotation.ActorScoped;
import com.github.sarxos.abberwoult.cdi.BeanLocator;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;


public class AbberwoultProcessor {

	private static final DotName ACTOR_SCOPED_ANNOTATION = DotName.createSimple(ActorScoped.class.getName());

	/**
	 * Register a extension capability and feature.
	 *
	 * @return abberwoult feature build item
	 */
	@BuildStep(providesCapabilities = "com.github.abberwoult")
	FeatureBuildItem featureBuildItem() {
		return new FeatureBuildItem("abberwoult");
	}

	/**
	 * Register a custom bean defining annotation
	 *
	 * @return Bean defining annotation build item
	 */
	@BuildStep
	BeanDefiningAnnotationBuildItem registerBeanDefinningAnnotations() {
		return new BeanDefiningAnnotationBuildItem(ACTOR_SCOPED_ANNOTATION);
	}

	/**
	 * Register the CDI beans that are needed by the test extension
	 *
	 * @param additionalBeans - producer for additional bean items
	 */
	@BuildStep
	AdditionalBeanBuildItem registerAdditionalBeans() {
		return new AdditionalBeanBuildItem(
			AbberwoultLifecycleListener.class,
			BeanLocator.class,
			ActorEngine.class,
			ActorRefFactory.class,
			ActorSelectionFactory.class,
			ActorSystemFactory.class,
			Propser.class);
	}
}
