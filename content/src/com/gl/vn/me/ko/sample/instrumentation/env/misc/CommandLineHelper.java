package com.gl.vn.me.ko.sample.instrumentation.env.misc;

import org.apache.log4j.Level;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * This class helps to process command-line arguments.
 * It uses JCommander library for processing of command-line arguments.
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
/*
 * Some static nested classes and fields can't be declared non-public,
 * because are designed to be used by JCommander library classes.
 * See JCommander documentation for details.
 */
public final class CommandLineHelper {
	/**
	 * This class defines possible application command-line arguments as described in JCommander documentation.
	 * It also contains classes for converting string representation of command line arguments to appropriate Java-objects.
	 * <p/>
	 * Instantiability: allowed only from inside {@link CommandLineHelper} class.<br/>
	 * Mutability: mutable.<br/>
	 * Thread safety: not thread-safe.
	 * 
	 * @author Valentin Kovalenko
	 */
	public final static class CommandLineParams {
		/**
		 * This class provides a method to convert a value of command-line argument into Java-object with the same type as {@link CommandLineParams#exampleName} field.
		 * <p/>
		 * Instantiability: allowed.<br/>
		 * Mutability: immutable.<br/>
		 * Thread safety: thread-safe.
		 * 
		 * @author Valentin Kovalenko
		 */
		public final static class ExampleNameConverter implements IStringConverter<String> {
			/**
			 * Constructs an instance of {@link ExampleNameConverter}.
			 */
			public ExampleNameConverter() {
			}

			/**
			 * Converts {@link java.lang.String} object into another {@link java.lang.String} object that can be used as value of {@link CommandLineParams#exampleName} field.
			 * 
			 * @param paramValue
			 *            {@link java.lang.String} object to convert.
			 * @return
			 *         Converted {@link java.lang.String} object.
			 */
			public final String convert(final String paramValue) {
				final String value = (paramValue == null ? "" : paramValue.toUpperCase(Internationalization.LOCALE));
				return value;
			}
		}

		/**
		 * This class provides a method to convert a value command line-argument into Java-object with the same type as {@link CommandLineParams#logLevel} field.
		 * <p/>
		 * Instantiability: allowed.<br/>
		 * Mutability: immutable.<br/>
		 * Thread safety: thread-safe.
		 * 
		 * @author Valentin Kovalenko
		 */
		public final static class LogLevelConverter implements IStringConverter<Level> {
			private final boolean calledFromAgent;

			/**
			 * Constructs an instance of {@link LogLevelConverter}.
			 * Is equivalent to {@code LogLevelConverter(false)}.
			 */
			public LogLevelConverter() {
				calledFromAgent = false;
			}

			/**
			 * Constructs an instance of {@link LogLevelConverter}.
			 * 
			 * @param calledFromAgent
			 *            Specifies if the constructor was called from Java-agent class or from main class of the application.
			 *            The argument only affects an exception message in case fail of convertation.
			 */
			public LogLevelConverter(final boolean calledFromAgent) {
				this.calledFromAgent = calledFromAgent;
			}

			/**
			 * Converts {@link java.lang.String} object into {@link org.apache.log4j.Level} object that can be used as value of {@link CommandLineParams#logLevel} field.
			 * 
			 * @param paramValue
			 *            {@link java.lang.String} object to convert.
			 * @return
			 *         Converted {@link org.apache.log4j.Level} object.
			 * @throws com.beust.jcommander.ParameterException
			 *             If the specified {@link java.lang.String} can't be converted into {@link org.apache.log4j.Level}.
			 */
			public final Level convert(final String paramValue) throws ParameterException {
				final Level value;
				if ("info".equalsIgnoreCase(paramValue)) {
					value = Level.INFO;
				} else if ("debug".equalsIgnoreCase(paramValue)) {
					value = Level.DEBUG;
				} else if ("trace".equalsIgnoreCase(paramValue)) {
					value = Level.TRACE;
				} else {
					final String msg = calledFromAgent ? "The value '" + paramValue + "' is incorrect for level of logging of the agent" : "The value '" + paramValue
							+ "' is incorrect for the parameter -logLevel";
					throw new ParameterException(msg);
				}
				return value;
			}
		}

		/**
		 * Definition of command-line argument that specifies level of logging.
		 */
		@Parameter(names = {"-logLevel", "-logLvl"}, description = "Level of logging (possible values: INFO, DEBUG, TRACE)", converter = LogLevelConverter.class)
		public Level logLevel;
		/**
		 * Definition of command-line argument that specifies name of the example to launch.
		 */
		@Parameter(names = {"-example", "-ex"}, description = "Name of the example to launch (possible values: A, B, C, D)", required = true, converter = ExampleNameConverter.class)
		public String exampleName;

		private CommandLineParams() {
			logLevel = (new LogLevelConverter()).convert(LogHelper.APPLICATION_DEFAULT_LOGGING_LEVEL);
			exampleName = null;
		}
	}

	private final static void errorExit() {
		final int errorExitCode = 1;
		System.exit(errorExitCode);
	}

	/**
	 * Processes command-line arguments and returns an object that contains processed values of arguments.
	 * 
	 * @param args
	 *            Command-line arguments of the application. Must be not {@code null}.
	 * @return
	 *         Object that contains values of processed command-line arguments.
	 */
	public final static CommandLineParams getCommandLineParams(final String[] args) {
		if (args == null) {
			throw new NullPointerException("The argument 'args' is null");
		}
		final CommandLineParams clParams = new CommandLineParams();
		new JCommander(clParams, args);
		return clParams;
	}

	/**
	 * Prints usage and terminates application.
	 * This method should be called if {@link com.beust.jcommander.ParameterException} occurred during processing of Java-agent command-line arguments.
	 * 
	 * @param cause
	 *            Exception that caused invocation of the method. Must be not {@code null}.
	 */
	public final static void printAgentUsageAndExit(final ParameterException cause) {
		if (cause == null) {
			throw new NullPointerException("The argument 'cause' is null");
		}
		final String lineSeparator = System.getProperty("line.separator");
		final String usage = "Usage: -javaagent:agent.jar[=options]" + lineSeparator + "  One can optionally specify a level of logging of the agent" + lineSeparator
				+ "  Possible values: INFO, DEBUG, TRACE" + lineSeparator + "  Default: " + LogHelper.AGENT_DEFAULT_LOGGING_LEVEL;
		printUsageAndExit(usage, cause);
	}

	/**
	 * Prints usage and terminates application.
	 * This method should be called if {@link com.beust.jcommander.ParameterException} occurred during processing of application command-line arguments.
	 * 
	 * @param cause
	 *            Exception that caused invocation of the method. Must be not {@code null}.
	 */
	public final static void printAppUsageAndExit(final ParameterException cause) {
		if (cause == null) {
			throw new NullPointerException("The argument 'cause' is null");
		}
		final CommandLineParams clParams = new CommandLineParams();
		final JCommander jCommander = new JCommander(clParams);
		jCommander.setProgramName("-jar app.jar");
		final StringBuilder usage = new StringBuilder();
		jCommander.usage(usage);
		printUsageAndExit(usage.toString(), cause);
	}

	/*
	 * This method can't rely on Log4j framework because loggers can be not configured, therefore the "standard" error output stream is used.
	 */
	private final static void printUsageAndExit(final String usage, final ParameterException cause) {
		System.err.println(cause.getMessage());
		System.err.print(usage);
		errorExit();
	}

	private CommandLineHelper() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}