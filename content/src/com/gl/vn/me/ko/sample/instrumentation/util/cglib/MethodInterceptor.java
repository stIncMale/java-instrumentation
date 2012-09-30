package com.gl.vn.me.ko.sample.instrumentation.util.cglib;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This interface just extends {@link net.sf.cglib.proxy.MethodInterceptor} and doesn't declare any additional methods. It was introduces to provide slightly more documentation than the original
 * {@link net.sf.cglib.proxy.MethodInterceptor} interface does.
 * 
 * @author Valentin Kovalenko
 */
public interface MethodInterceptor extends net.sf.cglib.proxy.MethodInterceptor {
	/**
	 * All proxy methods of proxy object generated with method interceptor invoke this method instead of the original one. Note that CGLib can generate proxy methods only for non-final &
	 * non-private methods of the original class. The original methods may either be invoked by normal reflection using the {@link java.lang.reflect.Method} object,
	 * or by using the {@link net.sf.cglib.proxy.MethodProxy} object, for example:<br/>
	 * <blockquote>
	 * 
	 * <pre>
	 * Object invokeInterceptedMethod(Object enhancedObject, Method method, Object[] methodArgs,
	 * 		MethodProxy methodProxy) throws Throwable {
	 * 	// invocation of the original method, the invocation will not be intercepted
	 * 	return methodProxy.invokeSuper(enhancedObject, methodArgs);
	 * }
	 * 
	 * Object invokeEnhancedMethod(Object enhancedObject, Method method, Object[] methodArgs,
	 * 		MethodProxy methodProxy) throws Throwable {
	 * 	// invocation of the enhanced method, the invocation will be intercepted.
	 * 	// If one want to invoke the original method not via the MethodProxy object but via the Method
	 * 	// object, one should use not 'enhancedObject' but different not enhanced object
	 * 	return method.invoke(enhancedObject, methodArgs);
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param proxyObject
	 *            Enhanced (proxy) object.
	 * @param method
	 *            Intercepted method.
	 * @param methodArgs
	 *            Array of arguments (primitive types are wrapped).
	 * @param proxyMethod
	 *            Used to invoke original method (not enhanced method, that will not be intercepted). May be called as many times as needed.
	 * @return
	 *         Any value compatible with the signature of the proxied method. Method returning void will ignore this value.
	 * @throws java.lang.Throwable
	 *             If something goes wrong.
	 */
	public Object intercept(final Object proxyObject, final Method method, final Object[] methodArgs, final MethodProxy proxyMethod) throws Throwable;
}