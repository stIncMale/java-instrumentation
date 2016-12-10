package com.gl.vn.me.ko.sample.instrumentation.env.misc;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import com.beust.jcommander.ParameterException;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams.LogLevelConverter;

/**
 * This class helps to configure and use Apache Log4j framework. It also provides some utility methods that can be useful for logging.
 * <p>
 * Instantiability: forbidden.<br>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class LogHelper {
	private final static Properties PROPERTIES;
	private final static Object LOCK;
	/**
	 * Default logging level for Java-agents.
	 * Value of this constant is {@value} .
	 */
	public final static String AGENT_DEFAULT_LOGGING_LEVEL = "INFO";
	/**
	 * Default logging level for application.
	 * Value of this constant is {@value} .
	 */
	public final static String APPLICATION_DEFAULT_LOGGING_LEVEL = "INFO";
	static {
		PROPERTIES = new Properties();
		PROPERTIES.setProperty("log4j.rootLogger", "INFO, ConsoleAppender");
		PROPERTIES.setProperty("log4j.appender.ConsoleAppender", "org.apache.log4j.ConsoleAppender");
		PROPERTIES.setProperty("log4j.appender.ConsoleAppender.layout", "org.apache.log4j.PatternLayout");
		PROPERTIES.setProperty("log4j.appender.ConsoleAppender.layout.ConversionPattern", ">%-30C{1} %M - %m%n");
		LOCK = new Object();
	}

	/**
	 * Performs configuration of logger with the supplied logging level.
	 * 
	 * @param lvl
	 *            Logging level to be applied. Must be not {@code null}.
	 */
	public final static void configure(final Level lvl) {
		if (lvl == null) {
			throw new NullPointerException("The argument 'lvl' is null");
		}
		synchronized (LOCK) {
			PROPERTIES.setProperty("log4j.rootLogger", lvl + ", ConsoleAppender");
			PropertyConfigurator.configure(PROPERTIES);
		}
	}

	/**
	 * Performs configuration of logger with the supplied logging level.
	 * 
	 * @param stringLvl
	 *            String representation of logging level as specified in {@link com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams.LogLevelConverter#convert(String)}
	 *            method. Must be not {@code null}.
	 * @param calledFromAgent
	 *            Specifies if the method was called from Java-agent class or from main class of the application. This argument only affects an exception message in case the first argument
	 *            {@code stringLvl} has incorrect format.
	 * @throws com.beust.jcommander.ParameterException
	 *             If the first argument {@code stringLvl} has incorrect format.
	 */
	public final static void configure(final String stringLvl, final boolean calledFromAgent) throws ParameterException {
		if (stringLvl == null) {
			throw new NullPointerException("The argument 'stringLvl' is null");
		}
		final LogLevelConverter converter = new CommandLineParams.LogLevelConverter(calledFromAgent);
		final Level lvl = converter.convert(stringLvl);
		configure(lvl);
	}

	/**
	 * Returns instance of {@link java.text.NumberFormat} that should be used to represent numbers in log entries.
	 * 
	 * @return
	 *         Number format that allows not more that two fraction digits.
	 */
	public final static NumberFormat getNumberFormat() {
		final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
		numberFormat.setMaximumFractionDigits(2);
		return numberFormat;
	}

	private LogHelper() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}