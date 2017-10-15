package com.gl.vn.me.ko.sample.instrumentation.env;

/**
 * Examples must implement this interface in order to {@link com.gl.vn.me.ko.sample.instrumentation.env.Main} class
 * be able to run them. Note that examples is executed via Java reflection mechanism.
 */
public interface Example {
  /**
   * This method should be invoked in order to run the example.
   */
  void run();
}