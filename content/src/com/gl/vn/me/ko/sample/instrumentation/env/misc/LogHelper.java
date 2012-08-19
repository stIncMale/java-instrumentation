package com.gl.vn.me.ko.sample.instrumentation.env.misc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import com.beust.jcommander.ParameterException;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams.LogLevelConverter;

/**
 * Helper class that helps to configure and use Apache Log4j logging framework.
 * <p/>
 * Instantiability and mutability: the class can't be instantiated.<br/>
 * Thread safety: the class is thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class LogHelper {
	private final static Properties props = new Properties();
	static {
		props.setProperty("log4j.rootLogger", "INFO, ConsoleAppender");
		props.setProperty("log4j.appender.ConsoleAppender", "org.apache.log4j.ConsoleAppender");
		props.setProperty("log4j.appender.ConsoleAppender.layout", "org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.ConsoleAppender.layout.ConversionPattern", ">%-30C{1} %M - %m%n");
	}

	public final static synchronized void configure(final Level lvl) {
		props.setProperty("log4j.rootLogger", lvl + ", ConsoleAppender");
		PropertyConfigurator.configure(props);
	}

	public final static synchronized void configure(final String stringLvl, final boolean calledFromAgent) throws ParameterException {
		final LogLevelConverter converter = new CommandLineParams.LogLevelConverter(calledFromAgent);
		final Level lvl = converter.convert(stringLvl);
		configure(lvl);
	}

	public final static NumberFormat getNumberFormat() {
		final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
		numberFormat.setMaximumFractionDigits(2);
		return numberFormat;
	}

	public final static String throwableToString(final Throwable e) {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		final String stackTrace = stringWriter.toString();
		printWriter.close();
		return stackTrace;
	}

	private LogHelper() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated.");
	}
}