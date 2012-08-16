package com.gl.vn.me.ko.sample.instrumentation.example;

import java.math.BigDecimal;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.IExample;

/**
 * @author Valentin Kovalenko
 */
public class ExampleB implements IExample {
	private final static Logger logger = Logger.getLogger(ExampleB.class);

	public final void run() {
		final double doublePi = Math.PI;
		final String stringPi = String.valueOf(Math.PI);
		final BigDecimal piFromDouble = new BigDecimal(doublePi);
		final BigDecimal piFromString = new BigDecimal(stringPi);
		final BigDecimal squarePi = piFromDouble.pow(2);
		logger.info("pi.pow(2) = " + squarePi);
		final BigDecimal minusPi = piFromString.negate();
		logger.info("pi.negate() = " + minusPi);
	}
}