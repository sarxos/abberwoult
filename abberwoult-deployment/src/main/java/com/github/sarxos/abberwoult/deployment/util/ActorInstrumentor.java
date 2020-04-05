package com.github.sarxos.abberwoult.deployment.util;

import static com.github.sarxos.abberwoult.DotNames.EVENT_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.RECEIVERS;
import static com.github.sarxos.abberwoult.DotNames.SIMPLE_ACTOR_CLASS;
import static io.vavr.Predicates.not;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.annotation.Generated;
import com.github.sarxos.abberwoult.annotation.Instrumented;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.deployment.util.Assistant.AssistedClass;
import com.github.sarxos.abberwoult.deployment.util.Assistant.AssistedMethod;
import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;
import com.github.sarxos.abberwoult.jandex.Reflector.MethodRef;
import com.github.sarxos.abberwoult.jandex.Reflector.ParameterRef;

import io.vavr.control.Option;
import javassist.CannotCompileException;


/**
 * This class performs actor classes instrumentation.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ActorInstrumentor {

	private static final Logger LOG = Logger.getLogger(ActorInstrumentor.class);

	public byte[] instrument(final ClassRef clazz) {
		final AssistedClass cc = clazz.assisted();
		return Option.of(cc)
			.filter(this::isInstrumentationEligible)
			.peek($ -> LOG.tracef("Instrument actor class %s", cc))
			.map(generateEventSubscribtionInvoker(clazz))
			.map(generatePreStart())
			.map(generatePostStop())
			.map(generateReceive(clazz))
			.map(addInstrumentedAnnotation())
			.map(debugWriteClass())
			.onEmpty(() -> LOG.tracef("No instrumentation required for actor class %s", cc.getName()))
			.getOrElse(cc)
			.toBytecode();
	}

	private UnaryOperator<AssistedClass> generateEventSubscribtionInvoker(final ClassRef clazz) {
		return cc -> {

			final List<String> invocations = clazz.getMethods()
				.stream()
				.flatMap(method -> method.getParameters().stream())
				.filter(parameter -> parameter.hasAnnotation(EVENT_ANNOTATION))
				.map(parameter -> parameter.getTypeName())
				.map(type -> "events.subscribe(self, " + type + ".class);")
				.collect(toList());

			if (invocations.isEmpty()) {
				return cc;
			}

			final String methodName = cc.makeUniqueName("synthSubscribeEventsFromEventStream_" + currentTimeMillis());

			final String code = ""
				+ "void " + methodName + "() {"
				+ "  final akka.actor.ActorRef self = getSelf();"
				+ "  final akka.event.EventStream events = getContext().getSystem().getEventStream();"
				+ "  " + StringUtils.join(invocations, "\n") + "\n"
				+ "}";

			final AssistedMethod invoker = cc.newMethod(code);
			invoker.addAnnotation(PreStart.class);
			invoker.addAnnotation(Generated.class);

			return cc;
		};
	}

	private void assertExactlyOneReceivedParameterPresent(MethodRef m) {
		final List<ParameterRef> received = m.getParametersAnnotatedBy(RECEIVERS);
		if (received.size() < 1) {
			throw new IllegalStateException(""
				+ "Method " + m.getLongName() + " does not accept any parameters annotated "
				+ "with " + Arrays.asList(RECEIVERS) + " where exactly one such parameter "
				+ "must be present.");
		}
		if (received.size() > 1) {
			throw new IllegalStateException(""
				+ "Method " + m.getLongName() + " accepts multiple parameters annotated "
				+ "with " + Arrays.asList(RECEIVERS) + " where only one such parameter "
				+ "is allowed.");
		}
	}

	private UnaryOperator<AssistedClass> generateReceive(final ClassRef clazz) {
		return cc -> {

			if (cc.isAbstract()) {
				return cc;
			}

			if (cc.hasDeclaredMethod("createReceive")) {
				LOG.warnf(""
					+ "Cannot create receive automation for class %s because "
					+ "createReceive() method is already declared", cc.getName());
				return cc;
			}

			if (!cc.isAssignableTo(SIMPLE_ACTOR_CLASS)) {
				LOG.infof("Skipping receive automation for classs %s", cc.getName());
				return cc;
			}

			final Predicate<MethodRef> isReceiver = method -> method.hasParameterAnnotatedBy(RECEIVERS);

			final List<MethodRef> receivers = clazz
				.methods()
				.filter(not(MethodRef::isStatic))
				.filter(isReceiver)
				.peek(this::assertExactlyOneReceivedParameterPresent)
				.collect(toList());

			if (receivers.isEmpty()) {
				return cc;
			}

			receivers.sort((m1, m2) -> {
				final ParameterRef pt1 = m1.getParameter(0);
				final ParameterRef pt2 = m2.getParameter(0);
				return pt2.getTypeDistance() - pt1.getTypeDistance();
			});

			final StringBuilder code = new StringBuilder(""
				+ "public akka.actor.AbstractActor.Receive createReceive() {\n"
			// + " akka.actor.AbstractActor.Receive fallback = super.createReceive();\n"
				+ "  return akka.japi.pf.ReceiveBuilder\n"
				+ "    .create()\n");

			for (MethodRef method : receivers) {

				final String type = method
					.getParameter(0)
					.getTypeName()
					.replaceAll("\\$", ".");

				final DotName invoker = ReceiveInvokerGenerator.createReceiveInvokerName(clazz, method);

				code.append("    .match(" + type + ".class, new " + invoker + "(this))\n");
			}

			code.append(""
				+ "    .matchAny(new com.github.sarxos.abberwoult.ReceiveInvoker.UnhandledReceiveInvoker(this))\n"
				+ "    .build();\n"
				+ "}");

			cc
				.newMethod(code)
				.addAnnotation(Generated.class);

			return cc;
		};
	}

	private UnaryOperator<AssistedClass> generatePreStart() {
		return cc -> {

			if (cc.isAbstract()) {
				return cc;
			}

			final List<String> invocations = cc
				.methods()
				.filter(this::isPreStart)
				.map(this::toInvocationLine)
				.collect(toList());

			if (invocations.isEmpty()) {
				return cc;
			}

			try {
				if (isPreStartDeclaredInClass(cc)) {
					aroundPreStart(cc, invocations);
				} else {
					overridePreStart(cc, invocations);
				}
			} catch (CannotCompileException e) {
				throw new IllegalStateException(e);
			}

			return cc;
		};
	}

	private UnaryOperator<AssistedClass> generatePostStop() {
		return cc -> {

			if (cc.isAbstract()) {
				return cc;
			}

			final List<String> invocations = cc
				.methods()
				.filter(this::isPostStop)
				.map(this::toInvocationLine)
				.collect(toList());

			if (invocations.isEmpty()) {
				return cc;
			}

			try {
				if (isPostStopDeclaredInClass(cc)) {
					aroundPostStop(cc, invocations);
				} else {
					overridePostStop(cc, invocations);
				}
			} catch (CannotCompileException e) {
				throw new IllegalStateException(e);
			}

			return cc;
		};
	}

	private boolean isPreStartDeclaredInClass(final AssistedClass cc) {
		return isNonFinalMethodDeclaredInClass(cc, "preStart");
	}

	private boolean isPostStopDeclaredInClass(final AssistedClass cc) {
		return isNonFinalMethodDeclaredInClass(cc, "postStop");
	}

	private boolean isNonFinalMethodDeclaredInClass(final AssistedClass cc, String name) {
		if (cc.hasDeclaredMethod(name)) {
			return assertNotFinal(cc.getDeclaredMethod(name));
		}
		return false;
	}

	private boolean assertNotFinal(final AssistedMethod method) {
		if (method.isFinal()) {
			throw new IllegalStateException("Method " + method.getLongName() + " must not be final");
		}
		return true;
	}

	private void aroundPreStart(final AssistedClass clazz, final List<String> invocations) throws CannotCompileException {

		final String code = ""
			+ "{\n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		clazz
			.getMethod("preStart", "()V")
			.insertAfter(code);
	}

	private void aroundPostStop(final AssistedClass clazz, final List<String> invocations) throws CannotCompileException {

		final String code = ""
			+ "{\n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		clazz
			.getMethod("postStop", "()V")
			.insertAfter(code);
	}

	private void overridePreStart(final AssistedClass clazz, final List<String> invocations) throws CannotCompileException {

		final String code = ""
			+ "public void preStart() throws Exception {\n"
			+ "  super.preStart(); \n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		clazz
			.newMethod(code)
			.addAnnotation(Generated.class);
	}

	private void overridePostStop(final AssistedClass clazz, final List<String> invocations) throws CannotCompileException {

		final String code = ""
			+ "public void postStop() throws Exception {\n"
			+ "  super.postStop(); \n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		clazz
			.newMethod(code)
			.addAnnotation(Generated.class);
	}

	private boolean isPreStart(final AssistedMethod method) {
		return method.hasAnnotation(PreStart.class);
	}

	private boolean isPostStop(final AssistedMethod method) {
		return method.hasAnnotation(PostStop.class);
	}

	private String toInvocationLine(final AssistedMethod method) {

		final AssistedClass clazz = method.getDeclaringClass();
		final String clazzName = clazz.getName();
		final String methodName = method.getName();

		if (clazz.isInterface()) {
			return clazzName + ".super." + methodName + "();\n";
		} else {
			return methodName + "();\n";
		}
	}

	private UnaryOperator<AssistedClass> addInstrumentedAnnotation() {
		return cc -> cc.addAnnotation(Instrumented.class);
	}

	private UnaryOperator<AssistedClass> debugWriteClass() {
		return cc -> cc.debugWriteClass("target/abberwoult/generated-classes");
	}

	private boolean isInstrumentationEligible(final AssistedClass clazz) {
		if (clazz.isFrozen()) {
			return false;
		}
		return !clazz.hasAnnotation(Instrumented.class);
	}
}
