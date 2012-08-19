package com.gl.vn.me.ko.sample.instrumentation.env;

import org.apache.log4j.Logger;
import com.beust.jcommander.ParameterException;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;

/**
 * The main class of the application.
 * <p/>
 * Instantiability and mutability: instantiation is forbidden.<br/>
 * Thread safety: the class doesn't require thread synchronization because its {@link Main#main(String[]) main(String[])} method invoked only once per running JVM.
 * 
 * @author Valentin Kovalenko
 */
public final class Main {
	private final static Logger logger = Logger.getLogger(Main.class);
	private static String exampleName;

	private final static void executeExample() {
		final IExample example;
		final String exampleClassNamePattern = "com.gl.vn.me.ko.sample.instrumentation.example.Example";
		String exampleClassName = null;
		try {
			exampleClassName = exampleClassNamePattern + exampleName;
			final Class<?> exampleClass = Class.forName(exampleClassName);
			example = (IExample) exampleClass.newInstance();
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("Can't load example class '" + exampleClassName + "' for example '" + exampleName + "'", e);
		} catch (final InstantiationException e) {
			throw new RuntimeException("Can't instantiate example class '" + exampleClassName + "' for example '" + exampleName + "'", e);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("Can't access example class '" + exampleClassName + "' for example '" + exampleName + "'", e);
		}
		example.run();
	}

	/**
	 * Application entry point.
	 * 
	 * @param args
	 *            Command-line arguments of the application
	 */
	public final static void main(final String[] args) {
		processArgs(args);
		logger.trace("Invocation");
		executeExample();
		logger.trace("Invocation finished");
	}

	private final static void processArgs(final String[] args) {
		try {
			final CommandLineParams clParams = CommandLineHelper.getCommandLineParams(args);
			LogHelper.configure(clParams.logLevel);
			exampleName = clParams.exampleName;
		} catch (final ParameterException e) {
			CommandLineHelper.printAppUsageAndAbort(e);
		}
	}

	private Main() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}