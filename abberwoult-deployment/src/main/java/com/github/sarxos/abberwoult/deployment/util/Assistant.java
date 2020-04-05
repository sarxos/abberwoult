package com.github.sarxos.abberwoult.deployment.util;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.vavr.control.Option;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;


public class Assistant {

	private static final Logger LOG = Logger.getLogger(Assistant.class);

	private static final String RUNTIME_ANNOTATIONS = AnnotationsAttribute.visibleTag;

	private final ClassPool cp;

	public Assistant(final ClassPool cp) {
		this.cp = cp;
	}

	public boolean exists(final DotName name) {
		return exists(name.toString());
	}

	public boolean exists(final String name) {
		return cp.getOrNull(name) != null;
	}

	public AssistedClass findClass(final DotName dn) {
		return findClass(dn.toString());
	}

	public AssistedClass findClass(final String name) {
		return new AssistedClass(getClassFromPool(name));
	}

	public AssistedClass findClass(final Class<?> clazz) {
		return new AssistedClass(getClassFromPool(clazz.getName()));
	}

	public AssistedClass newClass(final String name, final DotName superclass) {
		final AssistedClass ac = findClass(superclass);
		final CtClass supClass = ac.getCtClass();
		final CtClass newClass = cp.makeClass(name, supClass);
		return new AssistedClass(newClass);
	}

	public AssistedClass newClass(final DotName dn) {
		return newClass(dn.toString());
	}

	public AssistedClass newClass(final String name) {
		return new AssistedClass(cp.makeClass(name));
	}

	private CtClass getClassFromPool(final String name) {
		try {
			return cp.get(name);
		} catch (NotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private CtClass[] typesToCtClassArray(final String... types) {
		return Arrays
			.stream(types)
			.map(type -> getClassFromPool(type))
			.toArray(CtClass[]::new);
	}

	public String signature(final AssistedClass returnClass, final AssistedClass... paramClasses) {

		final CtClass[] paramTypes = Arrays
			.stream(paramClasses)
			.map(c -> c.cc)
			.toArray(n -> new CtClass[n]);

		return Descriptor.ofMethod(returnClass.cc, paramTypes);
	}

	public class AssistedClass {

		private final CtClass cc;

		public AssistedClass(CtClass cc) {
			this.cc = cc;
		}

		public AssistedClass addAnnotation(final Class<? extends java.lang.annotation.Annotation> clazz) {

			final String aname = clazz.getName();
			final String cname = cc.getName();
			final ClassFile classfile = cc.getClassFile();
			final ConstPool constpool = classfile.getConstPool();
			final Annotation annotation = new Annotation(aname, constpool);

			final AnnotationsAttribute attribute = Option
				.of(classfile.getAttribute(RUNTIME_ANNOTATIONS))
				.map(AnnotationsAttribute.class::cast)
				.getOrElse(() -> new AnnotationsAttribute(constpool, RUNTIME_ANNOTATIONS));

			LOG.tracef("Annotating class %s with %s", cname, aname);

			attribute.addAnnotation(annotation);
			classfile.addAttribute(attribute);

			return this;
		}

		public CtClass getCtClass() {
			return cc;
		}

		public String makeUniqueName(final String prefix) {
			return cc.makeUniqueName(prefix);
		}

		public int distance() {
			int d = 0;
			AssistedClass tmp = this;
			while ((tmp = tmp.getSuperclass()) != null) {
				d++;
			}
			return d;
		}

		public String getName() {
			return cc.getName();
		}

		public AssistedClass getSuperclass() {
			final CtClass superclass;
			try {
				superclass = cc.getSuperclass();
			} catch (NotFoundException e) {
				throw new IllegalStateException(e);
			}
			if (superclass == null) {
				return null;
			}
			return new AssistedClass(superclass);
		}

		public boolean isFrozen() {
			return cc.isFrozen();
		}

		public boolean isAbstract() {
			return Modifier.isAbstract(cc.getModifiers());
		}

		public boolean isFinal() {
			return Modifier.isFinal(cc.getModifiers());
		}

		public boolean isInterface() {
			return Modifier.isInterface(cc.getModifiers());
		}

		public boolean hasAnnotation(final Class<? extends java.lang.annotation.Annotation> clazz) {
			return cc.hasAnnotation(clazz);
		}

		@Override
		public String toString() {
			return cc.getName();
		}

		public byte[] toBytecode() {
			try {
				return cc.toBytecode();
			} catch (IOException | CannotCompileException e) {
				throw new IllegalStateException(e);
			}
		}

		public AssistedClass implement(final DotName dn) {

			final AssistedClass interf = findClass(dn);
			final CtClass intefClass = interf.getCtClass();

			cc.addInterface(intefClass);

			return this;
		}

		public AssistedField newField(final CharSequence code) {

			final CtField field;
			try {
				field = CtField.make(code.toString(), cc);
				cc.addField(field);
			} catch (CannotCompileException e) {
				throw new IllegalStateException(e);
			}

			return new AssistedField(this, field);
		}

		public AssistedMethod newMethod(final CharSequence code) {

			final CtMethod method;
			try {
				method = CtNewMethod.make(code.toString(), cc);
				cc.addMethod(method);
			} catch (CannotCompileException e) {
				throw new IllegalStateException(e);
			}

			return new AssistedMethod(this, method);
		}

		public AssistedConstructor newConstructor(final CharSequence code) {

			final CtConstructor constructor;
			try {
				constructor = CtNewConstructor.make(code.toString(), cc);
				cc.addConstructor(constructor);
			} catch (CannotCompileException e) {
				throw new IllegalStateException(e);
			}

			return new AssistedConstructor(this, constructor);
		}

		public AssistedClass newInnerClass(final DotName name) {
			return newInnerClass(name.toString());
		}

		public AssistedClass newInnerClass(final String name) {
			return new AssistedClass(cc.makeNestedClass(name, true));
		}

		public Stream<AssistedMethod> methods() {
			return Arrays
				.stream(cc.getMethods())
				.map(method -> new AssistedMethod(this, method));
		}

		public List<AssistedMethod> getMethods() {
			return methods().collect(toList());
		}

		public AssistedMethod getMethod(final String name, final String signature) {
			try {
				return new AssistedMethod(this, cc.getMethod(name, signature));
			} catch (NotFoundException e) {
				throw new IllegalStateException(e);
			}
		}

		public boolean hasMethod(final String name, final String signature) {
			try {
				return cc.getMethod(name, signature) != null;
			} catch (NotFoundException e) {
				return false;
			}
		}

		/**
		 * Is instance of this class assignable to the class in parameter.
		 *
		 * @param dn
		 * @return
		 */
		public boolean isAssignableTo(final DotName dn) {
			return isAssignableTo(dn.toString());
		}

		public boolean isAssignableTo(final String dn) {
			try {
				return isAssignable(cc, dn);
			} catch (NotFoundException e) {
				throw new IllegalStateException(e);
			}
		}

		private boolean isAssignable(final CtClass clazz, final String dn) throws NotFoundException {

			if (clazz == null) {
				return false;
			}
			if (Objects.equals(clazz.getName(), dn)) {
				return true;
			}

			for (CtClass interf : clazz.getInterfaces()) {
				if (isAssignable(interf, dn)) {
					return true;
				}
			}

			return isAssignable(clazz.getSuperclass(), dn);
		}

		public Stream<AssistedMethod> declaredMethods(final String name) {
			return Arrays
				.stream(cc.getDeclaredMethods())
				.map(method -> new AssistedMethod(this, method));
		}

		public List<AssistedMethod> getDeclaredMethods(final String name) {
			return declaredMethods(name).collect(toList());
		}

		public AssistedMethod getDeclaredMethod(final String name, final String... parameters) {
			final CtClass[] params = typesToCtClassArray(parameters);
			try {
				return new AssistedMethod(this, cc.getDeclaredMethod(name, params));
			} catch (NotFoundException e) {
				throw new IllegalStateException(e);
			}
		}

		public boolean hasDeclaredMethod(final String name, final String... parameters) {
			final CtClass[] params = typesToCtClassArray(parameters);
			try {
				return cc.getDeclaredMethod(name, params) != null;
			} catch (NotFoundException e) {
				return false;
			}
		}

		public AssistedClass debugWriteClass(final String path) {
			LOG.tracef("Debug writing class %s in %s ", getName(), path);
			cc.debugWriteFile(path);
			return this;
		}
	}

	public class AssistedField {

		private final AssistedClass clazz;
		private final CtField field;

		public AssistedField(AssistedClass clazz, CtField field) {
			this.clazz = clazz;
			this.field = field;
		}

	}

	public class AssistedConstructor {

		private final AssistedClass clazz;
		private final CtConstructor constructor;

		public AssistedConstructor(AssistedClass clazz, CtConstructor constructor) {
			this.clazz = clazz;
			this.constructor = constructor;
		}

	}

	public class AssistedMethod {

		private final AssistedClass clazz;
		private final CtMethod method;

		public AssistedMethod(final AssistedClass clazz, final CtMethod method) {
			this.clazz = clazz;
			this.method = method;
		}

		public AssistedMethod addAnnotation(final Class<? extends java.lang.annotation.Annotation> clazz) {

			final String aname = clazz.getName();
			final String mname = method.getLongName();
			final MethodInfo info = method.getMethodInfo();
			final ConstPool constpool = info.getConstPool();

			final AnnotationsAttribute attribute = Option
				.of(info.getAttribute(RUNTIME_ANNOTATIONS))
				.map(AnnotationsAttribute.class::cast)
				.getOrElse(() -> new AnnotationsAttribute(constpool, RUNTIME_ANNOTATIONS));

			final Annotation annotation = new Annotation(aname, constpool);

			LOG.tracef("Annotating method %s with %s", mname, aname);

			attribute.addAnnotation(annotation);
			info.addAttribute(attribute);

			return this;
		}

		public boolean hasAnnotation(final Class<? extends java.lang.annotation.Annotation> clazz) {
			return method.hasAnnotation(clazz);
		}

		public AssistedClass getDeclaringClass() {
			return clazz;
		}

		public String getName() {
			return method.getName();
		}

		public String getLongName() {
			return method.getLongName();
		}

		public boolean isFinal() {
			return Modifier.isFinal(method.getModifiers());
		}

		public boolean isAbstract() {
			return Modifier.isAbstract(method.getModifiers());
		}

		public boolean isStatic() {
			return Modifier.isStatic(method.getModifiers());
		}

		public boolean isPrivate() {
			return Modifier.isPrivate(method.getModifiers());
		}

		public boolean isProtected() {
			return Modifier.isProtected(method.getModifiers());
		}

		public boolean isPublic() {
			return Modifier.isPublic(method.getModifiers());
		}

		public boolean isSynchronized() {
			return Modifier.isSynchronized(method.getModifiers());
		}

		public AssistedMethod insertAfter(final String code) {
			try {
				method.insertAfter(code);
			} catch (CannotCompileException e) {
				throw new IllegalStateException(e);
			}
			return this;
		}

		public AssistedMethod insertBefore(final String code) {
			try {
				method.insertBefore(code);
			} catch (CannotCompileException e) {
				throw new IllegalStateException(e);
			}
			return this;
		}

		public int getParametersCount() {
			return Descriptor.numOfParameters(method.getSignature());
		}

		public AssistedClass getParameterType(int position) {
			try {
				return new AssistedClass(method.getParameterTypes()[position]);
			} catch (NotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public class AssistedMethodParameter {

	}

}
