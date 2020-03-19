package com.github.sarxos.abberwoult.deployment.util;

import java.io.InputStream;
import java.net.URL;

import io.quarkus.deployment.util.IoUtil;
import javassist.ClassPath;
import javassist.NotFoundException;


public class CurrentThreadContextClassPath implements ClassPath {

	@Override
	public InputStream openClassfile(final String classname) throws NotFoundException {
		return IoUtil.readClass(classloader(), classname);
	}

	@Override
	public URL find(final String classname) {
		final String classfile = classname.replace('.', '/') + ".class";
		return classloader().getResource(classfile);
	}

	@Override
	public void close() {
		// hey buddy, nothing needs to be closed here
	}

	private ClassLoader classloader() {
		return Thread.currentThread().getContextClassLoader();
	}
}
