package com.gl.vn.me.ko.sample.instrumentation.example.util.cglib;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This interface just extends {@code net.sf.cglib.proxy.MethodInterceptor} and doesn't declare any additional methods. It was introduces to provide slightyl more documentation than the original
 * {@code net.sf.cglib.proxy.MethodInterceptor} interface does.
 * 
 * @author Valentin Kovalenko
 */
public interface MethodInterceptor extends net.sf.cglib.proxy.MethodInterceptor {
	/**
	 * All proxy methods of proxy object generated with method interceptor invoke this method instead of the original one. Note that CGLib can generate proxy methods only for non-final &
	 * non-private methods of the original class. The original methods may either be invoked by normal reflection using the {@code java.lang.reflect.Method} object,
	 * or by using the {@code net.sf.cglib.proxy.MethodProxy} object.<br/>
	 * Example:
	 * <blockquote>
	 * 
	 * <pre>
	 * Object invokeInterceptedMethod(Object enhancedObject, Method method, Object[] methodArgs, MethodProxy methodProxy) throws Throwable {
	 * 	return methodProxy.invokeSuper(enhancedObject, methodArgs);// invocation of the original method, the invocation will not be intercepted
	 * }
	 * 
	 * Object invokeEnhancedMethod(Object enhancedObject, Method method, Object[] methodArgs, MethodProxy methodProxy) throws Throwable {
	 * 	return method.invoke(enhancedObject, methodArgs);// invocation of the enhanced method, the invocation will be intercepted
	 * 	// If one want to invoke the original method not via {@code MethodProxy} object but via {@code Method} object,
	 * 	// one shouldn't use 'enhancedObject' but different not enhanced object.
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param enhancedObject
	 *            Enhanced (proxy) object.
	 * @param method
	 *            Intercepted method.
	 * @param methodArgs
	 *            Array of arguments (primitive types are wrapped).
	 * @param methodProxy
	 *            Used to invoke original method (not enhanced method, that will not be intercepted). May be called as many times as needed.
	 * @return
	 *         Any value compatible with the signature of the proxied method. Method returning void will ignore this value.
	 * @see net.sf.cglib.proxy.MethodInterceptor.intercept(Object, Method, Object[], MethodProxy)
	 */
	public Object intercept(final Object enhancedObject, final Method method, final Object[] methodArgs, final MethodProxy methodProxy) throws Throwable;
}