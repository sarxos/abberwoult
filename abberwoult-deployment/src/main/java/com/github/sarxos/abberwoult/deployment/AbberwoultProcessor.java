package com.github.sarxos.abberwoult.deployment;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import com.github.sarxos.abberwoult.AbberwoultLifecycleListener;
import com.github.sarxos.abberwoult.ActorEngine;
import com.github.sarxos.abberwoult.ActorRefFactory;
import com.github.sarxos.abberwoult.ActorSelectionFactory;
import com.github.sarxos.abberwoult.ActorSystemFactory;
import com.github.sarxos.abberwoult.Propser;
import com.github.sarxos.abberwoult.annotation.ActorScoped;
import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.cdi.BeanLocator;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer.TransformationContext;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;


public class AbberwoultProcessor {

	private static final DotName ACTOR_SCOPED_ANNOTATION = DotName.createSimple(ActorScoped.class.getName());
	private static final DotName APPLICATION_SCOPED_ANNOTATION = DotName.createSimple(ApplicationScoped.class.getName());
	private static final DotName MESSAGE_HANDLER_ANNOTATION = DotName.createSimple(MessageHandler.class.getName());

	private static final String[] CORE_BEAN_CLASSES = {
		AbberwoultLifecycleListener.class.getName(),
		BeanLocator.class.getName(),
		ActorEngine.class.getName(),
		ActorRefFactory.class.getName(),
		ActorSelectionFactory.class.getName(),
		ActorSystemFactory.class.getName(),
		Propser.class.getName()
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
		return new AdditionalBeanBuildItem(
			AbberwoultLifecycleListener.class,
			BeanLocator.class,
			ActorEngine.class,
			ActorRefFactory.class,
			ActorSelectionFactory.class,
			ActorSystemFactory.class,
			Propser.class);
	}

	@BuildStep
	AnnotationsTransformerBuildItem doTransformAnnotations(BuildProducer<AnnotationsTransformerBuildItem> transformers) {
		return new AnnotationsTransformerBuildItem(this::findAndIndexMessageHandlers);
	}

	private boolean isMessageHandler(final TransformationContext tc) {

		// only methods can be message handlers

		if (!tc.isMethod()) {
			return false;
		}

		// check if method is annotated with the proper annotation

		final MethodInfo info = tc.getTarget().asMethod();
		final AnnotationInstance annotation = info.annotation(MESSAGE_HANDLER_ANNOTATION);

		// null annotation means that given annotation was not present on the element

		if (annotation == null) {
			return false;
		}

		// if we found given annotation on the element then we need to double check if
		// this is method annotations and not for example parameter or type annotation,
		// this can be done by checking target kind which needs to indicate a method

		return annotation.target().kind() == Kind.METHOD;
	}

	private void findAndIndexMessageHandlers(final TransformationContext tc) {

		// skip transformation if annotated target is message handler

		if (!isMessageHandler(tc)) {
			return;
		}

		System.out.println(tc.getTarget());
	}
}
