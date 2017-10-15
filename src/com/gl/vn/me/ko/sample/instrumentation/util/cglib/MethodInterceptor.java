package com.gl.vn.me.ko.sample.instrumentation.util.cglib;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This interface just extends {@link net.sf.cglib.proxy.MethodInterceptor} and doesn't declare any additional methods.
 * It was introduces to provide slightly more documentation than the original
 * {@link net.sf.cglib.proxy.MethodInterceptor} interface does.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NM_SAME_SIMPLE_NAME_AS_INTERFACE",
    justification = "Name is made intentionally the same as the name of implemented interface " +
        "because this derived interface provides only additional documentation and no any new functionality")
public interface MethodInterceptor extends net.sf.cglib.proxy.MethodInterceptor {
  /**
   * All proxy methods of proxy object generated with method interceptor invoke this method instead of the original one.
   * Note that CGLib can generate proxy methods only for non-final and
   * non-private methods of the original class. The original methods may either be invoked by normal reflection using the
   * {@link java.lang.reflect.Method} object, or by using the {@link net.sf.cglib.proxy.MethodProxy} object, for example:<br>
   * <blockquote>
   *
   * <pre>
   * Object invokeInterceptedMethod(Object enhancedObject, Method method, Object[] methodArgs, MethodProxy methodProxy) throws Throwable {
   * 	// invocation of the original method, the invocation will not be intercepted
   * 	return methodProxy.invokeSuper(enhancedObject, methodArgs);
   * }
   *
   * Object invokeEnhancedMethod(Object enhancedObject, Method method, Object[] methodArgs, MethodProxy methodProxy) throws Throwable {
   * 	// invocation of the enhanced method, the invocation will be intercepted.
   * 	// If one want to invoke the original method not via the MethodProxy object but via the Method
   * 	// object, one should use not 'enhancedObject' but different not enhanced object
   * 	return method.invoke(enhancedObject, methodArgs);
   * }
   * </pre>
   *
   * </blockquote>
   *
   * @param proxyObject Enhanced (proxy) object.
   * @param method Intercepted method.
   * @param methodArgs Array of arguments (primitive types are wrapped).
   * @param proxyMethod Used to invoke original method (not enhanced method, that will not be intercepted). May be called as many times as needed.
   *
   * @return Any value compatible with the signature of the proxied method (can be {@code null}). Method returning void will ignore this value.
   *
   * @throws java.lang.Throwable If something goes wrong.
   */
  @Nullable
  Object intercept(
      @SuppressWarnings("null") final Object proxyObject,
      @SuppressWarnings("null") final Method method,
      @SuppressWarnings("null") final Object[] methodArgs,
      @SuppressWarnings("null") final MethodProxy proxyMethod) throws Throwable;
}