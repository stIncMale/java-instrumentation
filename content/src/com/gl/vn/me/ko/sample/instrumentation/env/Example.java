package com.gl.vn.me.ko.sample.instrumentation.env;

/**
 * Examples must implement this interface in order to {@link com.gl.vn.me.ko.sample.instrumentation.env.Main Main} class
 * be able to run them.
 * 
 * @author Valentin Kovalenko
 */
public interface Example extends Runnable {
	/**
	 * This method should be invoked in order to run the example.
	 */
	void run();
}