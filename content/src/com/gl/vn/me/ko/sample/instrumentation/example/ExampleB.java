package com.gl.vn.me.ko.sample.instrumentation.example;

import java.math.BigDecimal;
import java.text.NumberFormat;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.Example;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;

/**
 * Instantiability: allowed.<br/>
 * Mutability: immutable.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class ExampleB implements Example {
	private final static Logger LOGGER;
	static {
		LOGGER = Logger.getLogger(ExampleB.class);
	}

	/**
	 * Constructs an instance of {@link ExampleB}.
	 */
	public ExampleB() {
	}

	public final void run() {
		final NumberFormat numberFormat = LogHelper.getNumberFormat();
		final double doublePi = Math.PI;
		final BigDecimal piFromDouble = new BigDecimal(doublePi);// prefer constructor BigDecimal(String) or method valueOf(double) in the production code; see Java SE API Specification for details
		final BigDecimal squarePi = piFromDouble.pow(2);
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("pi.pow(2) = " + numberFormat.format(squarePi));
		}
		final String stringPi = String.valueOf(Math.PI);
		final BigDecimal piFromString = new BigDecimal(stringPi);
		final BigDecimal minusPi = piFromString.negate();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("pi.negate() = " + numberFormat.format(minusPi));
		}
	}
}