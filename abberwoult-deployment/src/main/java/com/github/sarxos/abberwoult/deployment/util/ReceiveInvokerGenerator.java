package com.github.sarxos.abberwoult.deployment.util;

import static com.github.sarxos.abberwoult.DotNames.RECEIVERS;
import static com.github.sarxos.abberwoult.DotNames.RECEIVE_INVOKER_INTERFACE;
import static com.github.sarxos.abberwoult.DotNames.UNIVERSE_INTERFACE;
import static com.github.sarxos.abberwoult.DotNames.VALID_ANNOTATION;
import static io.vavr.Predicates.not;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.shared.utils.StringUtils;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.annotation.Generated;
import com.github.sarxos.abberwoult.deployment.util.Assistant.AssistedClass;
import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;
import com.github.sarxos.abberwoult.jandex.Reflector.MethodRef;
import com.github.sarxos.abberwoult.jandex.Reflector.ParameterRef;


public class ReceiveInvokerGenerator {

	private static final Logger LOG = Logger.getLogger(ReceiveInvokerGenerator.class);

	private static final String SUFIX = "ReceiveInvoker";

	public Map<String, byte[]> generate(final ClassRef clazz) {

		final List<MethodRef> receivers = clazz
			.methods()
			.filter(not(MethodRef::isStatic))
			.filter(method -> method.hasParameterAnnotatedBy(RECEIVERS))
			.peek(this::assertExactlyOneReceivedParameterPresent)
			.collect(toList());

		if (receivers.isEmpty()) {
			return emptyMap();
		}

		final Map<String, byte[]> invokers = new HashMap<>();

		for (final MethodRef receiver : receivers) {

			final AssistedClass invoker = createReceiveInvoker(clazz, receiver);
			final String name = invoker.getName();
			final byte[] bytecode = invoker.toBytecode();

			if (invokers.put(name, bytecode) != null) {
				throw new IllegalStateException("Invoker " + name + " for " + receiver.getLongName() + " in " + clazz + " is already present");
			}
		}

		return invokers;
	}

	public static final DotName createReceiveInvokerName(final ClassRef clazz, final MethodRef receiver) {
		final String actorClass = clazz.getName();
		final String payload = StringUtils.repeat(receiver.getSignature(), 10);
		final String digest = DigestUtils.md5Hex(payload);
		return DotName.createSimple(actorClass + "$" + SUFIX + digest.toUpperCase());
	}

	private AssistedClass createReceiveInvoker(final ClassRef clazz, final MethodRef receiver) {

		final boolean validable = receiver.hasParameterAnnotatedBy(VALID_ANNOTATION);

		if (validable) {
			assertDeclaringClassInUniverse(receiver);
		}

		final Assistant assistant = clazz
			.getReflector()
			.getAssistant();

		final String actorClass = clazz.getName();
		final ParameterRef parameter = receiver.getParametersAnnotatedBy(RECEIVERS).get(0);
		final String typeName = parameter.getTypeName();
		final DotName fullName = createReceiveInvokerName(clazz, receiver);

		if (assistant.exists(fullName)) {
			return assistant.findClass(fullName);
		}

		LOG.debugf("Generating receive invoker %s", fullName);

		final String fieldActor = "private " + actorClass + " actor;";
		final String fieldValid = "private boolean validate;";

		final String constructor = ""
			+ "public " + fullName.withoutPackagePrefix() + "(" + actorClass + " actor) {\n"
			+ "  this.actor = actor;\n"
			+ "  this.validate = " + validable + ";\n"
			+ "}\n";

		final String apply = ""
			+ "public void apply(Object message) throws Exception {\n"
			+ "  if (this.validate) {\n"
			+ "    com.github.sarxos.abberwoult.Validation.validate(\n"
			+ "      (com.github.sarxos.abberwoult.dsl.Universe) this.actor, \n"
			+ "      (Object) message);\n"
			+ "  }\n"
			+ "  this.actor." + receiver.getName() + "((" + typeName + ") message);\n"
			+ "}\n";

		final AssistedClass newClass = assistant.newClass(fullName);
		newClass.newField(fieldActor);
		newClass.newField(fieldValid);
		newClass.newConstructor(constructor);
		newClass.newMethod(apply);
		newClass.implement(RECEIVE_INVOKER_INTERFACE);
		newClass.debugWriteClass("target/abberwoult/generated-classes");
		newClass.addAnnotation(Generated.class);

		return newClass;
	}

	private void assertExactlyOneReceivedParameterPresent(final MethodRef m) {

		final List<ParameterRef> parameters = m.getParametersAnnotatedBy(RECEIVERS);

		if (parameters.size() < 1) {
			throw new IllegalStateException(""
				+ "Method " + m.getLongName() + " does not accept any parameters annotated "
				+ "with " + Arrays.asList(RECEIVERS) + " where exactly one such parameter "
				+ "must be present.");
		}

		if (parameters.size() > 1) {
			throw new IllegalStateException(""
				+ "Method " + m.getLongName() + " accepts multiple parameters annotated "
				+ "with " + Arrays.asList(RECEIVERS) + " where only one such parameter "
				+ "is allowed.");
		}
	}

	private void assertDeclaringClassInUniverse(final MethodRef m) {

		final ClassRef clazz = m.getDeclaringClass();
		final AssistedClass as = clazz.assisted();

		if (!as.isAssignableTo(UNIVERSE_INTERFACE)) {
			throw new IllegalStateException(""
				+ "Classes with methods consuming @" + VALID_ANNOTATION + " "
				+ "parameters need to implement " + UNIVERSE_INTERFACE + " interface "
				+ "but class " + clazz + " does not implement it.");
		}
	}
}
