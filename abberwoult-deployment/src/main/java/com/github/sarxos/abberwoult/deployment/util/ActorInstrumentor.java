package com.github.sarxos.abberwoult.deployment.util;

import static java.util.stream.Collectors.toList;
import static javassist.Modifier.isAbstract;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.annotation.Instrumented;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;

import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;


/**
 * This class performs actor classes instrumentation. It does few interesting things:
 * <ul>
 * <li>Generate <code>preStart()</code> method in actors with {@link PreStart} bindings.</li>
 * <li>Generate <code>postStop()</code> method in actors with {@link PostStop} bindings.</li>
 * </ul>
 * <br>
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ActorInstrumentor {

	private static final Logger LOG = Logger.getLogger(ActorInstrumentor.class);

	private final ClassPool pool = createClassPool();

	private final ClassPool createClassPool() {
		final ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new CurrentThreadContextClassPath());
		return pool;
	}

	public byte[] instrument(final ClassRef clazz) {
		try {
			return instrument0(clazz).toBytecode();
		} catch (Throwable e) {
			LOG.errorf("Cannot instrument actor class %s", clazz, e);
			throw new IllegalStateException(e);
		}
	}

	private synchronized CtClass instrument0(final ClassRef clazz) throws Throwable {

		final String name = clazz.getName();
		final CtClass cc = pool.get(name);

		return Option.of(cc)
			.filter(this::isInstrumentationEligible)
			.peek($ -> LOG.tracef("Instrument actor class %s", name))
			.map(instrumentObservers(clazz))
			.map(this::generatePreStart)
			.map(this::generatePostStop)
			.map(this::addInstrumentedAnnotation)
			.map(this::debugWriteClass)
			.onEmpty(() -> LOG.tracef("No instrumentation required for actor class %s", name))
			.getOrElse(cc);
	}

	private boolean isAbstractClass(final CtClass cc) {
		return isAbstract(cc.getModifiers());
	}

	private UnaryOperator<CtClass> instrumentObservers(final ClassRef clazz) {
		return cc -> {

			cc.getMethods();

			return cc;
		};
	}

	private CtClass generatePreStart(final CtClass cc) {

		if (isAbstractClass(cc)) {
			return cc;
		}

		final List<String> invocations = Arrays
			.stream(cc.getMethods())
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
	}

	private CtClass generatePostStop(final CtClass cc) {

		if (isAbstractClass(cc)) {
			return cc;
		}

		final List<String> invocations = Arrays
			.stream(cc.getMethods())
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
	}

	private boolean isPreStartDeclaredInClass(final CtClass cc) {
		return isNonFinalMethodDeclaredInClass(cc, "preStart", "()V");
	}

	private boolean isPostStopDeclaredInClass(final CtClass cc) {
		return isNonFinalMethodDeclaredInClass(cc, "postStop", "()V");
	}

	private boolean isNonFinalMethodDeclaredInClass(final CtClass cc, String name, String signature) {
		return Try
			.of(() -> cc.getMethod(name, signature))
			.peek(this::assertNotFinal)
			.filter(method -> method.getDeclaringClass().equals(cc))
			.isSuccess();
	}

	private void assertNotFinal(final CtMethod method) {
		if (Modifier.isFinal(method.getModifiers())) {
			throw new IllegalStateException("Method " + method.getLongName() + " must not be final");
		}
	}

	private void aroundPreStart(final CtClass cc, final List<String> invocations) throws CannotCompileException {

		final CtMethod prestart = Try
			.of(() -> cc.getMethod("preStart", "()V"))
			.get();

		final String code = ""
			+ "{\n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		prestart.insertAfter(code);
	}

	private void aroundPostStop(final CtClass cc, final List<String> invocations) throws CannotCompileException {

		final CtMethod poststop = Try
			.of(() -> cc.getMethod("postStop", "()V"))
			.get();

		final String code = ""
			+ "{\n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		poststop.insertAfter(code);
	}

	private void overridePreStart(final CtClass cc, final List<String> invocations) throws CannotCompileException {

		final String code = ""
			+ "public void preStart() throws Exception {\n"
			+ "  super.preStart(); \n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		cc.addMethod(CtNewMethod.make(code, cc));
	}

	private void overridePostStop(final CtClass cc, final List<String> invocations) throws CannotCompileException {

		final String code = ""
			+ "public void postStop() throws Exception {\n"
			+ "  super.postStop(); \n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		cc.addMethod(CtNewMethod.make(code, cc));
	}

	private boolean isPreStart(final CtMethod cm) {
		return cm.hasAnnotation(PreStart.class);
	}

	private boolean isPostStop(final CtMethod cm) {
		return cm.hasAnnotation(PostStop.class);
	}

	private String toInvocationLine(final CtMethod method) {

		final CtClass clazz = method.getDeclaringClass();
		final String clazzName = clazz.getName();
		final String methodName = method.getName();

		if (clazz.isInterface()) {
			return clazzName + ".super." + methodName + "();\n";
		} else {
			return methodName + "();\n";
		}
	}

	private CtClass addInstrumentedAnnotation(final CtClass cc) {

		final String aname = Instrumented.class.getName();
		final String cname = cc.getName();
		final ClassFile classfile = cc.getClassFile();
		final ConstPool constpool = classfile.getConstPool();
		final Annotation annotation = new Annotation(aname, constpool);

		final String runtimeAnnotations = AnnotationsAttribute.visibleTag;
		final AnnotationsAttribute attribute = Option
			.of(classfile.getAttribute(runtimeAnnotations))
			.map(AnnotationsAttribute.class::cast)
			.getOrElse(() -> new AnnotationsAttribute(constpool, runtimeAnnotations));

		LOG.tracef("Annotating actor class %s with %s", cname, aname);

		attribute.addAnnotation(annotation);
		classfile.addAttribute(attribute);

		return cc;
	}

	private CtClass debugWriteClass(final CtClass cc) {

		final String name = cc.getName();
		final String path = "target/abberwoult/generated-classes";

		LOG.tracef("Debug writing class %s in %s ", name, path);

		cc.debugWriteFile(path);

		return cc;
	}

	private boolean isInstrumentationEligible(final CtClass cc) {
		if (cc.isFrozen()) {
			return false;
		}
		return !cc.hasAnnotation(Instrumented.class);
	}
}
