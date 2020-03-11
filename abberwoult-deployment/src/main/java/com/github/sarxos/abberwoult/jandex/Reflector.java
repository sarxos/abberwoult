package com.github.sarxos.abberwoult.jandex;

import static com.github.sarxos.abberwoult.util.CollectorUtils.map;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.AnnotationUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.vavr.control.Option;


public final class Reflector extends SimpleBuildItem {

	private final IndexView index;

	public Reflector(CombinedIndexBuildItem combined) {
		this(combined.getIndex());
	}

	public Reflector(IndexView index) {
		this.index = index;
	}

	public IndexView getIndex() {
		return index;
	}

	public Option<ClassRef> findClass(DotName dn) {
		return Option
			.of(index.getClassByName(dn))
			.map(ClassRef::new);
	}

	public Stream<ClassRef> findSubclassesOf(DotName dn) {
		return index
			.getAllKnownSubclasses(dn)
			.stream()
			.map(ClassRef::new);
	}

	public abstract class Reflection {

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	public class ClassRef extends Reflection {

		final ClassInfo ci;

		public ClassRef(final ClassInfo ci) {
			this.ci = requireNonNull(ci, "class info must not be null");
		}

		public String getName() {
			return ci.name().toString();
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		public boolean equals(Object obj) {
			return Objects.equals(getName(), Option.of(obj)
				.filter(ClassRef.class::isInstance)
				.map(ClassRef.class::cast)
				.map(ClassRef::getName)
				.getOrNull());
		}

		@Override
		public int hashCode() {
			return getName().hashCode();
		}

		public String getPath() {
			return ci.name().toString('/');
		}

		public ClassRef getSuperclass() {
			return findClass(ci.superName()).getOrNull();
		}

		public boolean hasAnnotation(DotName dn) {
			return ci.classAnnotation(dn) != null;
		}

		public boolean hasAnnotationInClassScope(DotName... dns) {
			for (final DotName dn : dns) {
				if (ci.annotations().containsKey(dn)) {
					return true;
				}
			}
			return false;
		}

		public boolean hasNoArgsConstructor() {
			return ci.hasNoArgsConstructor();
		}

		/**
		 * @return Fields declared on a given class only
		 */
		public List<FieldInfo> getDeclaredFields() {
			return ci.fields();
		}

		public Option<AnnotationRef> getAnnotation(final DotName dn) {
			return Option
				.of(ci.classAnnotation(dn))
				.map(AnnotationRef::new);
		}

		public <A extends Annotation> Option<A> getAnnotation(final Class<A> clazz) {
			final String cn = clazz.getName();
			final DotName dn = DotName.createSimple(cn);
			return getAnnotation(dn).map(a -> a.toInstance(clazz));
		}

		/**
		 * @return Fields declared on this class and its superclasses
		 */
		public List<FieldRef> getFields() {
			return getFieldsRecursive(ci)
				.stream()
				.map(fi -> new FieldRef(fi, this))
				.collect(toList());
		}

		private List<FieldInfo> getFieldsRecursive(ClassInfo ci) {

			if (ci == null) {
				return Collections.emptyList();
			}

			final List<FieldInfo> fields = new ArrayList<FieldInfo>();
			fields.addAll(ci.fields());
			fields.addAll(getFieldsRecursive(index.getClassByName(ci.superName())));

			return fields;
		}

		public List<MethodRef> getDeclaredMethods() {
			return ci
				.methods()
				.stream()
				.map(mi -> new MethodRef(mi, this))
				.collect(toList());
		}

		public List<MethodRef> getMethods() {
			return getMethodsRecursive(ci)
				.stream()
				.filter(this::notInit)
				.filter(this::notClassInit)
				.map(mi -> new MethodRef(mi, this))
				.collect(toList());
		}

		private boolean notInit(MethodInfo mi) {
			return !"<init>".equals(mi.name());
		}

		private boolean notClassInit(MethodInfo mi) {
			return !"<clinit>".equals(mi.name());
		}

		public List<MethodRef> getMethodsAnnotatedWith(DotName dn) {
			return getMethods()
				.stream()
				.filter(m -> m.hasAnnotation(dn))
				.collect(toList());
		}

		private List<MethodInfo> getMethodsRecursive(ClassInfo ci) {

			if (ci == null) {
				return Collections.emptyList();
			}

			final List<MethodInfo> methods = new ArrayList<MethodInfo>();

			methods.addAll(ci.methods());
			methods.addAll(getMethodsRecursive(index.getClassByName(ci.superName())));
			methods.addAll(getInterfaceMethodsRecursive(ci.interfaceNames()));

			final Map<String, MethodInfo> reduced = new LinkedHashMap<String, MethodInfo>();

			methods.forEach(m -> reduced.computeIfAbsent(m.toString(), signature -> m));

			return Collections.unmodifiableList(new ArrayList<>(reduced.values()));
		}

		private List<MethodInfo> getInterfaceMethodsRecursive(List<DotName> interfaces) {
			final List<MethodInfo> methods = new ArrayList<MethodInfo>();
			interfaces.forEach(idn -> methods.addAll(getMethodsRecursive(index.getClassByName(idn))));
			return methods;
		}
	}

	public class MethodRef extends Reflection {

		final MethodInfo mi;
		final ClassRef cr;

		public MethodRef(final MethodInfo mi, ClassRef cr) {
			this.mi = mi;
			this.cr = cr;
		}

		public boolean hasAnnotation(DotName dn) {
			return mi
				.annotations()
				.stream()
				.filter(ai -> ai.name().equals(dn))
				.filter(ai -> ai.target().kind() == Kind.METHOD)
				.filter(ai -> ai.target().equals(mi))
				.findAny()
				.isPresent();
		}

		public String getName() {
			return mi.name();
		}

		public String getSignature() {
			return mi.toString();
		}

		public ClassRef getDeclaringClass() {
			return cr;
		}

		public String getDeclaringClassName() {
			return getDeclaringClass().getName();
		}
	}

	public class FieldRef extends Reflection {

		final FieldInfo fi;
		final ClassRef cr;

		public FieldRef(final FieldInfo fi, final ClassRef cr) {
			this.fi = fi;
			this.cr = cr;
		}

		public List<AnnotationRef> getAnnotations() {
			return fi
				.annotations()
				.stream()
				.map(AnnotationRef::new)
				.collect(toList());
		}

		public boolean hasAnnotation(DotName dn) {
			return fi.hasAnnotation(dn);
		}
	}

	public class AnnotationRef extends Reflection {

		final AnnotationInstance ai;

		public AnnotationRef(final AnnotationInstance ai) {
			this.ai = ai;
		}

		public ClassRef getAnnotationClass() {
			return findClass(ai.name()).get();
		}

		public List<AnnotationRef> getAnnotations() {

			final ClassInfo ci = index.getClassByName(ai.name());
			if (ci == null) {
				return Collections.emptyList();
			}

			return ci
				.classAnnotations()
				.stream()
				.map(AnnotationRef::new)
				.collect(toList());
		}

		/**
		 * Is annotation class annotated with a given annotation.
		 *
		 * @param dn the annotated annotation name
		 * @return True if class is annotated with given annotation or false otherwise
		 */
		public boolean hasAnnotation(DotName dn) {
			return getAnnotationClass().hasAnnotation(dn);
		}

		public <A extends Annotation> A toInstance(final Class<A> clazz) {
			return proxy(clazz, map(ai.values(), AnnotationValue::name, AnnotationValue::value));
		}

		@SuppressWarnings("unchecked")
		private <A extends Annotation> A proxy(Class<A> clazz, Map<String, Object> properties) {

			final ClassLoader cl = clazz.getClassLoader();
			final Class<?>[] interfaces = new Class<?>[] { clazz };

			return (A) Proxy.newProxyInstance(cl, interfaces, (proxy, method, args) -> {

				final Annotation annotation = (Annotation) proxy;
				final String methodName = method.getName();

				switch (methodName) {
					case "toString":
						return AnnotationUtils.toString(annotation);
					case "hashCode":
						return AnnotationUtils.hashCode(annotation);
					case "equals":
						return AnnotationUtils.equals(annotation, (Annotation) args[0]);
					case "annotationType":
						return clazz;
					default:
						if (!properties.containsKey(methodName)) {
							throw new NoSuchMethodException(String.format("Missing value for mocked annotation property '%s'. Pass the correct value in the 'properties' parameter", methodName));
						}
						return properties.get(methodName);
				}
			});
		}
	}

}
