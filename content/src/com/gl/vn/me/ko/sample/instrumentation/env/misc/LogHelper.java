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
 * This class helps to configure and use Apache Log4j logging framework
 * as well as provides some utility methods that can be useful for logging.
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class LogHelper {
	private final static Properties props;
	static {
		props = new Properties();
		props.setProperty("log4j.rootLogger", "INFO, ConsoleAppender");
		props.setProperty("log4j.appender.ConsoleAppender", "org.apache.log4j.ConsoleAppender");
		props.setProperty("log4j.appender.ConsoleAppender.layout", "org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.ConsoleAppender.layout.ConversionPattern", ">%-30C{1} %M - %m%n");
	}

	/**
	 * Perform configuration of Apache Log4j framework with provided logging level.
	 * 
	 * @param lvl
	 *            Logging level to be applied.
	 */
	public final static synchronized void configure(final Level lvl) {
		props.setProperty("log4j.rootLogger", lvl + ", ConsoleAppender");
		PropertyConfigurator.configure(props);
	}

	/**
	 * Perform configuration of Apache Log4j framework with provided logging level.
	 * 
	 * @param stringLvl
	 *            String representation of logging level as specified in {@link com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams.LogLevelConverter#convert(String)
	 *            LogLevelConverter.convert(String)} method.
	 * @param calledFromAgent
	 *            Specifies if the method was called from Java-agent or from main class of the application. This argument only affects an exception message in case the first argument {@code stringLvl}
	 *            has incorrect format.
	 * @throws ParameterException
	 *             If the first argument {@code stringLvl} has incorrect format.
	 */
	public final static synchronized void configure(final String stringLvl, final boolean calledFromAgent) throws ParameterException {
		final LogLevelConverter converter = new CommandLineParams.LogLevelConverter(calledFromAgent);
		final Level lvl = converter.convert(stringLvl);
		configure(lvl);
	}

	/**
	 * Returns the number format that can be used to represent fractional numbers in log entries.
	 * 
	 * @return
	 *         Number format that allows not more that two fraction digits
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