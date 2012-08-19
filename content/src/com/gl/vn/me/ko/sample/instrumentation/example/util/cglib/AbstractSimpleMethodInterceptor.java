package com.gl.vn.me.ko.sample.instrumentation.example.util.cglib;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * The class provides an ability to create a proxy (enhanced) object of type {@code T} that can intercept
 * invocation of any non-final & non-private method of class {@code T} and perform additional actions
 * (see {@link AbstractSimpleMethodInterceptor#intercept(Object, Method, Object[], MethodProxy) intercept(Object, Method, Object[], MethodProxy)}).
 * <p/>
 * Instantiability: instantiation is not forbidden. Depends on the implementation of derived class.<br/>
 * Mutability: this class itself has nothing that can cause mutability. Depends on the implementation of derived class.<br/>
 * Thread safety: this class itself has nothing that can cause thread unsafety. Depends on the implementation of derived class.<br/>
 * Cloneability: cloning is not forbidden. Depends on the implementation of derived class.<br/>
 * Serializability: serialization is not forbidden. Depends on the implementation of derived class.
 * 
 * @author Valentin Kovalenko
 */
public abstract class AbstractSimpleMethodInterceptor<T> implements MethodInterceptor {
	protected AbstractSimpleMethodInterceptor() {
	}

	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("The class is not designed for cloning");
	}

	protected T getProxy(final Class<T> proxifiedObjClass, final Class<?>[] constructorArgTypes, final Object[] constructorArgs) {
		final Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(proxifiedObjClass);
		enhancer.setCallback(this);
		@SuppressWarnings("unchecked")
		final T result = (T) enhancer.create(constructorArgTypes, constructorArgs);
		return result;
	}

	/**
	 * All generated proxied methods call this method instead of the original one.
	 * The original method may either be invoked by normal reflection using the {@code java.lang.reflect.Method} object,
	 * or by using the {@code net.sf.cglib.proxy.MethodProxy.invokeSuper(java.lang.Object, java.lang.Object[])}.<br/>
	 * Example:
	 * <blockquote>
	 * 
	 * <pre>
	 * Object invokeInterceptedMethod(Object enhancedObject, Method method, Object[] methodArgs, MethodProxy methodProxy) throws Throwable {
	 * 	return methodProxy.invokeSuper(enhancedObject, methodArgs);// invocation of the original method, the invocation will not be intercepted
	 * }
	 * </pre>
	 * 
	 * <pre>
	 * Object invokeEnhancedMethod(Object enhancedObject, Method method, Object[] methodArgs, MethodProxy methodProxy) throws Throwable {
	 * 	return method.invoke(enhancedObject, methodArgs);// invocation of the enhanced method, the invocation will be intercepted
	 * 	// if one want to invoke the original method not via {@code MethodProxy} object but via {@code Method} object,
	 * 	// one shouldn't use {@code enhancedObject} but different not enhanced object.
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param enhancedObject
	 *            Enhanced object
	 * @param method
	 *            Intercepted method
	 * @param methodArgs
	 *            Array of arguments (primitive types are wrapped)
	 * @param methodProxy
	 *            Used to invoke original method (not enhanced method, that will not be intercepted). May be called as many times as needed
	 */
	public abstract Object intercept(final Object enhancedObject, final Method method, final Object[] methodArgs, final MethodProxy methodProxy) throws Throwable;
}