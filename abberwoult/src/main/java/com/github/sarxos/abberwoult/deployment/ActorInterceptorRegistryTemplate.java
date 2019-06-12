package com.github.sarxos.abberwoult.deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.annotation.Receives;

import io.quarkus.runtime.annotations.Template;


/**
 * A {@link Template} used to record bytecode responsible for {@link Receives} registration.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Template
public class ActorInterceptorRegistryTemplate {

	private static final Logger LOG = LoggerFactory.getLogger(ActorInterceptorRegistry.class);

	// public static class CustomClassLoader extends ClassLoader {
	//
	// @Override
	// public Class<?> findClass(String name) throws ClassNotFoundException {
	// final byte[] b = loadClassFromFile(name);
	// return defineClass(name, b, 0, b.length);
	// }
	//
	// private byte[] loadClassFromFile(final String className) throws ClassNotFoundException {
	//
	// LOG.debug("Loading class {}", className);
	//
	// final String resourcePath = className.replace('.', File.separatorChar) + ".class";
	//
	// int value = 0;
	//
	// try (
	// final InputStream is = new FileInputStream(resourcePath);
	// final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	//
	// while ((value = is.read()) != -1) {
	// baos.write(value);
	// }
	//
	// return baos.toByteArray();
	//
	// } catch (FileNotFoundException e) {
	// throw new ClassNotFoundException("Cannot find resource " + resourcePath, e);
	// } catch (IOException e) {
	// throw new IllegalStateException(e);
	// }
	// }
	// }
	//
	// private static final CustomClassLoader ccl = new CustomClassLoader();

	public void register(final String className) {

		final Class<?> clazz;
		try {
			// clazz = ccl.loadClass(className);
			clazz = Class.forName(className); // UNCOMMENT TO OBSERVE ISSUE
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Cannot load " + className, e);
		}

		LOG.debug("Loaded {}", clazz);

		ActorInterceptorRegistry.registerReceiversFrom(clazz);
		ActorInterceptorRegistry.registerPreStartsFrom(clazz);
		ActorInterceptorRegistry.registerPostStopsFrom(clazz);
	}
}
