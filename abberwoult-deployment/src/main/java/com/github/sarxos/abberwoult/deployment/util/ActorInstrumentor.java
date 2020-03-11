package com.github.sarxos.abberwoult.deployment.util;

import static java.util.stream.Collectors.toList;
import static javassist.Modifier.isAbstract;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.annotation.Instrumented;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;

import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;


public class ActorInstrumentor {

	private static final Logger LOG = Logger.getLogger(ActorInstrumentor.class);

	private final ClassPool pool = createClassPool();

	private final ClassPool createClassPool() {
		final ClassLoader cl = getClass().getClassLoader();
		final ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new LoaderClassPath(cl));
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
			.map(this::instrumentPreStart)
			.map(this::addInstrumentedAnnotation)
			.map(this::debugWriteClass)
			.onEmpty(() -> LOG.tracef("No instrumentation required for actor class %s", name))
			.getOrElse(cc);
	}

	private CtClass instrumentPreStart(final CtClass cc) {

		if (isAbstract(cc.getModifiers())) {
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

	private boolean isPreStartDeclaredInClass(final CtClass cc) {
		return Try
			.of(() -> cc.getMethod("preStart", "()V"))
			.peek(this::assertNotFinal)
			.filter(method -> method.getDeclaringClass().equals(cc))
			.isSuccess();
	}

	private void assertNotFinal(final CtMethod method) {
		if (Modifier.isFinal(method.getModifiers())) {
			throw new IllegalStateException(""
				+ "Method void preStart() in "
				+ method.getDeclaringClass().getName() + " "
				+ "must not be final");
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

	private void overridePreStart(final CtClass cc, final List<String> invocations) throws CannotCompileException {

		final String code = ""
			+ "public void preStart() throws Exception {\n"
			+ "  super.preStart(); \n"
			+ "  " + StringUtils.join(invocations, '\n') + "\n"
			+ "}";

		final CtMethod method = CtNewMethod.make(code, cc);

		cc.addMethod(method);

	}

	private boolean isPreStart(final CtMethod cm) {
		return cm.hasAnnotation(PreStart.class);
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
