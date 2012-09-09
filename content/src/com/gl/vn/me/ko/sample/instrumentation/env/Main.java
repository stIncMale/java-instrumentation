package com.gl.vn.me.ko.sample.instrumentation.env;

import org.apache.log4j.Logger;
import com.beust.jcommander.ParameterException;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;

/**
 * The main class of the application.
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: the class doesn't require thread synchronization because its {@link #main(String[])} method invoked only once per running JVM.
 * 
 * @author Valentin Kovalenko
 */
public final class Main {
	private final static Logger LOGGER;
	private static String exampleName;
	static {
		LOGGER = Logger.getLogger(Main.class);
	}

	private final static void executeExample() {
		final Example example;
		final String exampleClassNamePattern = "com.gl.vn.me.ko.sample.instrumentation.example.Example";
		String exampleClassName = null;
		try {
			exampleClassName = exampleClassNamePattern + exampleName;
			final Class<?> exampleClass = Class.forName(exampleClassName);
			example = (Example)exampleClass.newInstance();
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
	 *            Command line arguments of the application.
	 */
	public final static void main(final String[] args) {
		processArgs(args);
		LOGGER.trace("Invocation");
		executeExample();
		LOGGER.trace("Invocation finished");
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