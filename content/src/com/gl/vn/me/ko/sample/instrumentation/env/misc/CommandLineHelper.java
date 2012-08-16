package com.gl.vn.me.ko.sample.instrumentation.env.misc;

import org.apache.log4j.Level;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * @author Valentin Kovalenko
 */
public final class CommandLineHelper {
	public final static class CommandLineParams {
		public final static class ExampleNameConverter implements IStringConverter<String> {
			public final String convert(final String paramValue) {
				final String value = paramValue.toUpperCase();
				return value;
			}
		}

		public final static class LogLevelConverter implements IStringConverter<Level> {
			private final boolean calledFromAgent;

			public LogLevelConverter() {
				calledFromAgent = false;
			}

			public LogLevelConverter(final boolean calledFromAgent) {
				this.calledFromAgent = calledFromAgent;
			}

			public final Level convert(final String paramValue) throws ParameterException {
				final Level value;
				if ("info".equalsIgnoreCase(paramValue)) {
					value = Level.INFO;
				} else if ("debug".equalsIgnoreCase(paramValue)) {
					value = Level.DEBUG;
				} else if ("trace".equalsIgnoreCase(paramValue)) {
					value = Level.TRACE;
				} else {
					final String msg = calledFromAgent ? "The value '" + paramValue + "' is incorrect for level of logging of the agent"
							: "The value '" + paramValue + "' is incorrect for the parameter -logLevel";
					throw new ParameterException(msg);
				}
				return value;
			}
		}

		public final static String agentDefaultLoggingLevel = "INFO";
		@Parameter(names = {"-logLevel", "-logLvl"}, description = "Level of logging (possible values: INFO, DEBUG, TRACE)", converter = LogLevelConverter.class)
		public Level logLevel = Level.INFO;
		@Parameter(names = {"-example", "-ex"}, description = "Name of the example to launch (possible values: A, B, C, ...)", required = true, converter = ExampleNameConverter.class)
		public String exampleName = null;
	}

	private final static void abort() {
		final int errorExitCode = 1;
		System.exit(errorExitCode);
	}

	public final static CommandLineParams getCommandLineParams(final String[] args) {
		final CommandLineParams clParams = new CommandLineParams();
		new JCommander(clParams, args);
		return clParams;
	}

	public final static void printAgentUsageAndAbort(final Throwable cause) {
		final String lineSeparator = System.getProperty("line.separator");
		final String usage = "Usage: -javaagent:agent.jar[=options]" + lineSeparator + "  One can optionally specify a level of logging of the agent"
				+ lineSeparator + "  Possible values: INFO, DEBUG, TRACE" + lineSeparator + "  Default: "
				+ CommandLineParams.agentDefaultLoggingLevel;
		printUsageAndAbort(usage, cause);
	}

	public final static void printAppUsageAndAbort(final Throwable cause) {
		final CommandLineParams clParams = new CommandLineParams();
		final JCommander jCommander = new JCommander(clParams);
		jCommander.setProgramName("-jar app.jar");
		final StringBuilder sb = new StringBuilder();
		jCommander.usage(sb);
		final String usage = sb.toString();
		printUsageAndAbort(usage, cause);
	}

	private final static void printUsageAndAbort(final String usage, final Throwable cause) {
		System.out.println(cause.getMessage());
		System.out.print(usage);
		abort();
	}

	private CommandLineHelper() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}