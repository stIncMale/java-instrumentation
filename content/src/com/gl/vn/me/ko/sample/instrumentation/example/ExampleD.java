package com.gl.vn.me.ko.sample.instrumentation.example;

import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.Example;

/**
 * Instantiability: allowed.<br/>
 * Mutability: immutable.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class ExampleD implements Example {
	private final static Logger LOGGER;
	static {
		LOGGER = Logger.getLogger(ExampleD.class);
	}

	/**
	 * Constructs an instance of {@link ExampleD}.
	 */
	public ExampleD() {
	}

	public final void run() {
		final boolean comparisonResult = ("test" == "test".toString());
		LOGGER.info("(\"test\" == \"test\".toString()) is " + comparisonResult);
	}
}