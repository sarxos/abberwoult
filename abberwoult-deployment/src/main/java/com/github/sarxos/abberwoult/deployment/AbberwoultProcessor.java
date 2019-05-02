package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.ACTOR_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.APPLICATION_SCOPED_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.util.DeploymentUtils.isMessageHandler;
import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.AbberwoultLifecycleListener;
import com.github.sarxos.abberwoult.ActorEngine;
import com.github.sarxos.abberwoult.ActorRefFactory;
import com.github.sarxos.abberwoult.ActorSelectionFactory;
import com.github.sarxos.abberwoult.ActorSystemFactory;
import com.github.sarxos.abberwoult.MessageHandlerRegistry;
import com.github.sarxos.abberwoult.MessageHandlerRegistryTemplate;
import com.github.sarxos.abberwoult.Propser;
import com.github.sarxos.abberwoult.cdi.BeanLocator;
import com.github.sarxos.abberwoult.deployment.error.NoArgMessageHandlerException;
import com.github.sarxos.abberwoult.deployment.error.PrivateMessageHandlerException;
import com.github.sarxos.abberwoult.deployment.error.WrongAssistedArgumentsCountException;
import com.github.sarxos.abberwoult.deployment.util.DeploymentUtils;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;


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
		MessageHandlerRegistry.class.getName(),
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
	@Record(STATIC_INIT)
	AnnotationsTransformerBuildItem doRegisterMessageHandlers(
		final MessageHandlerRegistryTemplate template,
		final CombinedIndexBuildItem combinedIndex) {

		final IndexView index = combinedIndex.getIndex();
		final AnnotationsTransformer transformer = findAndIndexMessageHandlers(template, index);

		return new AnnotationsTransformerBuildItem(transformer);
	}

	private AnnotationsTransformer findAndIndexMessageHandlers(final MessageHandlerRegistryTemplate template, final IndexView index) {
		return tc -> {

			if (!isMessageHandler(tc)) {
				return;
			}

			final MethodInfo handler = tc.getTarget().asMethod();
			final ClassInfo recipientClass = handler.declaringClass();
			final short flags = handler.flags();

			if (!Modifier.isPublic(flags)) {
				throw new PrivateMessageHandlerException(handler, recipientClass);
			}

			final String clazzName = recipientClass.name().toString();
			final String handlerName = handler.name();
			final String handlerType = handler.returnType().name().toString();
			final List<String> parameters = getParameterTypeNames(handler);
			final Set<Short> validables = getValidablePositions(handler);
			final Set<Short> assisted = getAssistedPositions(handler);
			final boolean injectPresent = isInjectPresent(handler);

			if (parameters.isEmpty()) {
				throw new NoArgMessageHandlerException(handler, recipientClass);
			}
			if (injectPresent && assisted.size() != 1) {
				throw new WrongAssistedArgumentsCountException(handler, recipientClass, assisted.size());
			}

			LOG.debug("Register message handler {} {} {} from class {}", handlerType, handlerName, parameters, clazzName);

			template.register(clazzName, handlerName, handlerType, parameters, validables, assisted);
		};
	}

	private static boolean isInjectPresent(final MethodInfo method) {
		return method.annotations().stream()
			.filter(DeploymentUtils::isMethodAnnotation)
			.filter(DeploymentUtils::isInjectAnnotation)
			.findAny()
			.isPresent();
	}

	private static List<String> getParameterTypeNames(final MethodInfo method) {
		final List<Type> parameters = method.parameters();
		return parameters.stream()
			.map(Type::name)
			.map(DotName::toString)
			.collect(toListWithSameSizeAs(parameters));
	}

	private static Set<Short> getValidablePositions(final MethodInfo method) {
		return method.annotations().stream()
			.filter(DeploymentUtils::isMethodParameterAnnotation)
			.filter(DeploymentUtils::isValidAnnotation)
			.map(DeploymentUtils::toMethodParameterPosition)
			.collect(toSet());
	}

	private static Set<Short> getAssistedPositions(final MethodInfo method) {
		return method.annotations().stream()
			.filter(DeploymentUtils::isMethodParameterAnnotation)
			.filter(DeploymentUtils::isAssistedAnnotation)
			.map(DeploymentUtils::toMethodParameterPosition)
			.collect(toSet());
	}
}
