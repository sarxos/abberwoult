package com.github.sarxos.abberwoult.deployment.util;

import org.jboss.jandex.AnnotationTarget.Kind;

import com.github.sarxos.abberwoult.jandex.Reflector;

import io.quarkus.arc.processor.AnnotationsTransformer;


public class MissingParameterAnnotationsTransformer implements AnnotationsTransformer {

	private final Reflector reflector;

	public MissingParameterAnnotationsTransformer(Reflector reflector) {
		this.reflector = reflector;
	}

	@Override
	public boolean appliesTo(Kind kind) {
		return true;
	}

	@Override
	public void transform(final TransformationContext tc) {

		// final AnnotationTarget target = tc.getTarget();
		//
		// if (target.kind() == AnnotationTarget.Kind.FIELD) {
		// System.out.println(target.asField().name() + " f= " + target.asField().annotations());
		// }
		// if (target.kind() == AnnotationTarget.Kind.METHOD) {
		// System.out.println(target.asMethod().name() + " m= " + target.asMethod().annotations());
		// }
		// if (target.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
		// System.out.println(target.asMethodParameter().name() + " p= " +
		// target.asMethodParameter());
		// }
		//
		// // System.out.println(target + " " + target.kind());
		//
		// if (target.kind() != AnnotationTarget.Kind.METHOD_PARAMETER) {
		// return;
		// }
		//
		// final MethodParameterInfo mpi = tc
		// .getTarget()
		// .asMethodParameter();
		//
		// final ParameterRef parameter = reflector.from(mpi);
		// final MethodRef method = parameter.getMethod();
		//
		// // ignore static methods
		//
		// if (method.isStatic()) {
		// return;
		// }
		//
		// System.out.println(parameter.getMethodName() + " " + parameter.getName());
	}

}
