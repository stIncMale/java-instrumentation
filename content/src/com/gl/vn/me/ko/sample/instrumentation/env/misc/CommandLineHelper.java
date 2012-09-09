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
	 * This class defines possible command line arguments as described in JCommander documentation.
	 * It also contains classes for converting string representation of command line arguments to appropriate Java-objects.
	 * <p/>
	 * Instantiability: allowed only from inside {@link CommandLineHelper} class.<br/>
	 * Mutability: immutable.<br/>
	 * Thread safety: thread-safe.
	 * 
	 * @author Valentin Kovalenko
	 */
	public final static class CommandLineParams {
		/**
		 * This class provides a method to convert command line argument into Java-object with the same type as {@link CommandLineParams#exampleName} field.
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
			 * Converts {@code java.lang.String} into another {@code java.lang.String} that can be used as value of {@link CommandLineParams#exampleName} field.
			 * 
			 * @param paramValue
			 *            {@code java.lang.String} to convert.
			 * @return
			 *         {@code java.lang.String} in correct format.
			 */
			public final String convert(final String paramValue) {
				final String value = paramValue.toUpperCase(Internationalization.LOCALE);
				return value;
			}
		}

		/**
		 * This class provides a method to convert command line argument into Java-object with the same type as {@link CommandLineParams#logLevel} field.
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
			 */
			public LogLevelConverter() {
				calledFromAgent = false;
			}

			/**
			 * Constructs an instance of {@link LogLevelConverter}.
			 * 
			 * @param calledFromAgent
			 *            Specifies if the constructor was called from Java-agent or from main class of the application.
			 *            The argument only affects an exception message in case fail of convertation.
			 */
			public LogLevelConverter(final boolean calledFromAgent) {
				this.calledFromAgent = calledFromAgent;
			}

			/**
			 * Converts {@code java.lang.String} into {@code org.apache.log4j.Level} that can be used as value of {@link CommandLineParams#logLevel} field.
			 * 
			 * @param paramValue
			 *            {@code java.lang.String} to convert
			 * @return
			 *         Corresponding {@code org.apache.log4j.Level}
			 * @throws ParameterException
			 *             If the specified {@code java.lang.String} can't be converted into {@code org.apache.log4j.Level}
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
		 * Default logging level for Java-agents.
		 */
		public final static String agentDefaultLoggingLevel;
		/**
		 * Definition of command line argument that specify level of logging.
		 */
		@Parameter(names = {"-logLevel", "-logLvl"}, description = "Level of logging (possible values: INFO, DEBUG, TRACE)", converter = LogLevelConverter.class)
		public Level logLevel;
		/**
		 * Definition of command line argument that specify name of the example to launch.
		 */
		@Parameter(names = {"-example", "-ex"}, description = "Name of the example to launch (possible values: A, B, C, ...)", required = true, converter = ExampleNameConverter.class)
		public String exampleName;
		static {
			agentDefaultLoggingLevel = "INFO";
		}

		private CommandLineParams() {
			logLevel = Level.INFO;
			exampleName = null;
		}
	}

	private final static void abort() {
		final int errorExitCode = 1;
		System.exit(errorExitCode);
	}

	/**
	 * Processes command line arguments.
	 * 
	 * @param args
	 *            Command line arguments of the application.
	 * @return
	 *         Object that contains values of processed command line arguments.
	 */
	public final static CommandLineParams getCommandLineParams(final String[] args) {
		final CommandLineParams clParams = new CommandLineParams();
		new JCommander(clParams, args);
		return clParams;
	}

	/**
	 * This method should be called if {@code com.beust.jcommander.ParameterException} occurred during processing of Java-agent command line arguments.
	 * Prints usage and terminates application.
	 * 
	 * @param cause
	 *            Exception that caused invocation of the method.
	 */
	public final static void printAgentUsageAndAbort(final ParameterException cause) {
		final String lineSeparator = System.getProperty("line.separator");
		final String usage = "Usage: -javaagent:agent.jar[=options]" + lineSeparator + "  One can optionally specify a level of logging of the agent" + lineSeparator
				+ "  Possible values: INFO, DEBUG, TRACE" + lineSeparator + "  Default: " + CommandLineParams.agentDefaultLoggingLevel;
		printUsageAndAbort(usage, cause);
	}

	/**
	 * This method should be called if {@code com.beust.jcommander.ParameterException} occurred during processing of application command line arguments.
	 * Prints usage and terminates application.
	 * 
	 * @param cause
	 *            Exception that caused invocation of the method.
	 */
	public final static void printAppUsageAndAbort(final ParameterException cause) {
		final CommandLineParams clParams = new CommandLineParams();
		final JCommander jCommander = new JCommander(clParams);
		jCommander.setProgramName("-jar app.jar");
		final StringBuilder sb = new StringBuilder();
		jCommander.usage(sb);
		final String usage = sb.toString();
		printUsageAndAbort(usage, cause);
	}

	private final static void printUsageAndAbort(final String usage, final ParameterException cause) {
		System.out.println(cause.getMessage());
		System.out.print(usage);
		abort();
	}

	private CommandLineHelper() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}