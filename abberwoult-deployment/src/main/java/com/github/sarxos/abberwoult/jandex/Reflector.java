package com.github.sarxos.abberwoult.jandex;

import static com.github.sarxos.abberwoult.util.CollectorUtils.map;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.AnnotationUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

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

	public Stream<ClassRef> findClassesWithAnnotationInScope(final DotName... annotations) {
		return Arrays
			.stream(annotations)
			.flatMap(dn -> index.getAnnotations(dn).stream())
			.map(AnnotationInstance::target)
			.map(this::getClassInfo)
			.map(ClassRef::new);
	}

	public Stream<AnnotationRef> findAnnotatedBy(DotName... annotations) {
		return Arrays
			.stream(annotations)
			.flatMap(dn -> index.getAnnotations(dn).stream())
			.map(AnnotationRef::new)
			.distinct();
	}

	public Stream<ParameterRef> findAnnotatedParametersBy(DotName... annotations) {
		return findAnnotatedBy(annotations)
			.filter(AnnotationRef::isOnParameter)
			.map(AnnotationRef::getAnnotationTarget)
			.map(AnnotationTarget::asMethodParameter)
			.map(p -> new ParameterRef(p.method(), p.position()));
	}

	public ClassRef from(final ClassInfo ci) {
		return new ClassRef(ci);
	}

	public ParameterRef from(final MethodParameterInfo mpi) {
		return new ParameterRef(mpi.method(), mpi.position());
	}

	private ClassInfo getClassInfo(final AnnotationTarget target) {

		switch (target.kind()) {
			case METHOD:
				return target
					.asMethod()
					.declaringClass();
			case METHOD_PARAMETER:
				return target
					.asMethodParameter()
					.method()
					.declaringClass();
			case FIELD:
				return target
					.asField()
					.declaringClass();
			case CLASS:
				return target
					.asClass();
			case TYPE:
				throw new NotImplementedException("TYPE kind is not supported yet");
		}

		throw new IllegalStateException("Not supported " + target.kind());
	}

	public abstract class Reflection {

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	public class ClassRef extends Reflection {

		final ClassInfo ci;

		ClassRef(final ClassInfo ci) {
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

		public MethodRef getMethodByName(final String name) {
			return new MethodRef(ci.method(name));
		}

		public boolean hasAnnotation(DotName dn) {
			return ci.classAnnotation(dn) != null;
		}

		public boolean hasInterface(DotName dn) {
			return ci.interfaceNames().contains(dn);
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
		 * @return Fields declared on this class and all superclasses
		 */
		public List<FieldRef> getFields() {
			return getFieldsRecursive(ci)
				.stream()
				.map(fi -> new FieldRef(fi, this))
				.collect(toList());
		}

		public List<FieldRef> getFieldsAnnotatedWith(final DotName... dns) {
			return getFields()
				.stream()
				.filter(field -> field.hasAnnotation(dns))
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

		/**
		 * @return Methods from this class only, not from its superclasses
		 */
		public List<MethodRef> getDeclaredMethods() {
			return ci
				.methods()
				.stream()
				.filter(this::notInit)
				.filter(this::notClassInit)
				.map(mi -> new MethodRef(mi))
				.collect(toList());
		}

		/**
		 * @return Methods from this class and all superclasses
		 */
		public List<MethodRef> getMethods() {
			return getMethodsRecursive(ci)
				.stream()
				.filter(this::notInit)
				.filter(this::notClassInit)
				.map(mi -> new MethodRef(mi))
				.collect(toList());
		}

		private boolean notInit(MethodInfo mi) {
			return !isInit(mi);
		}

		private boolean isInit(MethodInfo mi) {
			return "<init>".equals(mi.name());
		}

		private boolean notClassInit(MethodInfo mi) {
			return !isClassInit(mi);
		}

		private boolean isClassInit(MethodInfo mi) {
			return "<clinit>".equals(mi.name());
		}

		public List<MethodRef> getMethodsAnnotatedWith(final DotName... dns) {
			return getMethods()
				.stream()
				.filter(m -> m.hasAnnotation(dns))
				.collect(toList());
		}

		public List<ConstructorRef> getConstructors() {
			return ci
				.methods()
				.stream()
				.filter(this::isInit)
				.map(mi -> new ConstructorRef(mi))
				.collect(toList());
		}

		public List<ConstructorRef> getConstructorsAnnotatedWith(final DotName... dns) {
			return getConstructors()
				.stream()
				.filter(c -> c.hasAnnotation(dns))
				.collect(toList());
		}

		public List<AnnotationRef> getAnnotations() {
			return ci.classAnnotations()
				.stream()
				.map(AnnotationRef::new)
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

	public class InvocationRef extends Reflection {

		final MethodInfo mi;

		InvocationRef(final MethodInfo mi) {
			this.mi = mi;
		}

		public boolean hasAnnotation(final DotName... dns) {

			final Predicate<AnnotationInstance> isOneOfAnnotatyionsPresent = ai -> {
				final DotName name = ai.name();
				for (final DotName dn : dns) {
					if (name.equals(dn)) {
						return true;
					}
				}
				return false;
			};

			return mi
				.annotations()
				.stream()
				.filter(isOneOfAnnotatyionsPresent)
				.filter(ai -> ai.target().kind() == Kind.METHOD)
				.filter(ai -> ai.target().equals(mi))
				.findAny()
				.isPresent();
		}

		public boolean hasAnnotationInMethodScope(final DotName... dns) {
			for (DotName dn : dns) {
				if (mi.hasAnnotation(dn)) {
					return true;
				}
			}
			return false;
		}

		public ClassRef getDeclaringClass() {
			return new ClassRef(mi.declaringClass());
		}

		public String getDeclaringClassName() {
			return getDeclaringClass().getName();
		}

		public int getParametersCount() {
			return mi.parameters().size();
		}

		public List<ParameterRef> getParameters() {
			final AtomicInteger index = new AtomicInteger();
			return mi.parameters()
				.stream()
				.map(t -> new ParameterRef(mi, index.getAndIncrement()))
				.collect(toList());
		}
	}

	public class ConstructorRef extends InvocationRef {

		ConstructorRef(final MethodInfo mi) {
			super(mi);
		}
	}

	public class MethodRef extends InvocationRef {

		MethodRef(final MethodInfo mi) {
			super(mi);
		}

		public String getName() {
			return mi.name();
		}

		public String getSignature() {
			return mi.toString();
		}

		public String getReturnTypeName() {
			return mi.returnType().name().toString();
		}

		@Override
		public List<ParameterRef> getParameters() {
			return IntStream
				.range(0, mi.parameters().size())
				.mapToObj(position -> new ParameterRef(mi, position))
				.collect(toList());
		}

		public boolean isStatic() {
			return Modifier.isStatic(getFlags());
		}

		public boolean isAbstract() {
			return Modifier.isAbstract(getFlags());
		}

		public boolean isFinal() {
			return Modifier.isFinal(getFlags());
		}

		public boolean isPrivate() {
			return Modifier.isPrivate(getFlags());
		}

		public boolean isProtected() {
			return Modifier.isProtected(getFlags());
		}

		public boolean isPublic() {
			return Modifier.isPublic(getFlags());
		}

		public int getFlags() {
			return mi.flags();
		}
	}

	public class ParameterRef extends Reflection {

		private final MethodInfo mi;
		private final int position;
		private final int hash;

		ParameterRef(MethodInfo mi, int position) {
			this.mi = mi;
			this.position = position;
			this.hash = new HashCodeBuilder()
				.append(mi.returnType().name().toString())
				.append(mi.declaringClass().name().toString())
				.append(mi.name())
				.append(position)
				.build();
		}

		public Type getType() {
			return mi.parameters().get(position);
		}

		public String getTypeName() {
			return getType().name().toString();
		}

		public Option<ClassRef> getTypeClass() {
			return findClass(getType().name());
		}

		public MethodRef getMethod() {
			return new MethodRef(mi);
		}

		public String getMethodName() {
			return mi.name();
		}

		public int getPosition() {
			return position;
		}

		public String getName() {
			return mi.parameterName(position);
		}

		public Set<AnnotationRef> getAnnotations() {
			return mi
				.annotations()
				.stream()
				.filter(ai -> ai.target().kind() == AnnotationTarget.Kind.METHOD_PARAMETER)
				.filter(ai -> ai.target().asMethodParameter().position() == position)
				.map(AnnotationRef::new)
				.collect(toSet());
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ParameterRef) {
				final ParameterRef pi = (ParameterRef) obj;
				return new EqualsBuilder()
					.append(mi.declaringClass(), pi.mi.declaringClass())
					.append(mi.name(), pi.mi.name())
					.append(position, pi.position)
					.build();
			}
			return false;
		}
	}

	public class FieldRef extends Reflection {

		final FieldInfo fi;
		final ClassRef cr;

		FieldRef(final FieldInfo fi, final ClassRef cr) {
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

		public boolean hasAnnotation(DotName... dns) {
			for (final DotName dn : dns) {
				if (fi.hasAnnotation(dn)) {
					return true;
				}
			}
			return false;
		}

		public String getName() {
			return fi.name();
		}

		public DotName getType() {
			return fi.type().name();
		}

		public String getParameterTypeName(final int index) {
			return fi.type()
				.asParameterizedType()
				.arguments()
				.get(index)
				.name()
				.toString();
		}

		public boolean isTypeOf(DotName dn) {
			return getType().equals(dn);
		}

		public String getTypeName() {
			return getType().toString();
		}

		public boolean isParametrized() {
			return fi.type().kind() == Type.Kind.PARAMETERIZED_TYPE;
		}
	}

	public class AnnotationRef extends Reflection {

		final AnnotationInstance ai;

		AnnotationRef(final AnnotationInstance ai) {
			this.ai = ai;
		}

		public Option<ClassRef> getAnnotationClass() {
			return findClass(ai.name());
		}

		public String getAnnotationClassName() {
			return ai.name().toString();
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

		public AnnotationTarget getAnnotationTarget() {
			return ai.target();
		}

		public AnnotationTarget.Kind getAnnotationTargetKind() {
			return getAnnotationTarget().kind();
		}

		public boolean isOnClass() {
			return getAnnotationTargetKind() == AnnotationTarget.Kind.CLASS;
		}

		public boolean isOnField() {
			return getAnnotationTargetKind() == AnnotationTarget.Kind.FIELD;
		}

		public boolean isOnMethod() {
			return getAnnotationTargetKind() == AnnotationTarget.Kind.METHOD;
		}

		public boolean isOnParameter() {
			return getAnnotationTargetKind() == AnnotationTarget.Kind.METHOD_PARAMETER;
		}

		/**
		 * Is annotation class annotated with a given annotation.
		 *
		 * @param dn the annotated annotation name
		 * @return True if class is annotated with given annotation or false otherwise
		 */
		public boolean hasAnnotation(DotName dn) {
			return getAnnotationClass()
				.filter(c -> c.hasAnnotation(dn))
				.isDefined();
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

		@Override
		public String toString() {
			return "@" + ai.name().toString();
		}
	}

}
