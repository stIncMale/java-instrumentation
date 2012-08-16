package com.gl.vn.me.ko.sample.instrumentation.example;

import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.IExample;

/**
 * @author Valentin Kovalenko
 */
public final class ExampleA implements IExample {
	private final static Logger logger = Logger.getLogger(ExampleA.class);
	private int counter;

	public ExampleA() {
		counter = 0;
	}

	private final synchronized void incrementCounter() {
		counter++;
		logger.info("The counter was incremented");
	}

	private final synchronized void printCounter() {
		logger.info("counter = " + counter);
	}

	public final void run() {
		printCounter();
		incrementCounter();
		printCounter();
	}
}