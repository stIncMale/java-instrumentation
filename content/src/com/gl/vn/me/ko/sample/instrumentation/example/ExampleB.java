package com.gl.vn.me.ko.sample.instrumentation.example;

import java.math.BigDecimal;
import java.text.NumberFormat;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.IExample;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;

/**
 * Instantiability and mutability: instances of the class are immutable.<br/>
 * Thread safety: the class is thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class ExampleB implements IExample {
	private final static Logger logger = Logger.getLogger(ExampleB.class);

	public final void run() {
		final NumberFormat numberFormat = LogHelper.getNumberFormat();
		final double doublePi = Math.PI;
		final BigDecimal piFromDouble = new BigDecimal(doublePi);
		final BigDecimal squarePi = piFromDouble.pow(2);
		logger.info("pi.pow(2) = " + numberFormat.format(squarePi));
		final String stringPi = String.valueOf(Math.PI);
		final BigDecimal piFromString = new BigDecimal(stringPi);
		final BigDecimal minusPi = piFromString.negate();
		logger.info("pi.negate() = " + numberFormat.format(minusPi));
	}
}