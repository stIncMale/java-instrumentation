package com.gl.vn.me.ko.sample.instrumentation.example;

import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.Example;

/**
 * Instantiability: allowed.<br>
 * Mutability: mutable.<br>
 * Thread safety: not thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class ExampleA implements Example {
	private final static Logger LOGGER;
	private int counter;
	static {
		LOGGER = Logger.getLogger(ExampleA.class);
	}

	/**
	 * Constructs an instance of {@link ExampleA}.
	 */
	public ExampleA() {
		counter = 0;
	}

	public final void run() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("counter = " + counter + " before incrementation");
		}
		increment(1);
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("counter = " + counter + " after incrementation");
		}
	}

	private final void increment(final int incrementValue) {
		counter += incrementValue;
	}
}