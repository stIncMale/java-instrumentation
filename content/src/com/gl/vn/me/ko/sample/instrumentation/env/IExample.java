package com.gl.vn.me.ko.sample.instrumentation.env;

/**
 * Examples must implement this interface in order to {@link com.gl.vn.me.ko.sample.instrumentation.env.Main Main} class
 * be able to run them.
 * 
 * @author Valentin Kovalenko
 */
public interface IExample {
	/**
	 * This method will be invoked by {@link com.gl.vn.me.ko.sample.instrumentation.env.Main Main} class in order to run an example.
	 */
	void run();
}