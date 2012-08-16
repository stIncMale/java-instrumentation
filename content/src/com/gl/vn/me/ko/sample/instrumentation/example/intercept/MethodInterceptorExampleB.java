package com.gl.vn.me.ko.sample.instrumentation.example.intercept;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;

/**
 * @author Valentin Kovalenko
 */
public class MethodInterceptorExampleB implements MethodInterceptor {
	private final static Logger logger = Logger.getLogger(MethodInterceptorExampleB.class);

	/**
	 * Works only for non final proxifiedObjClass
	 */
	private final static <T> T getProxy(final Class<T> proxifiedObjClass, final Class<?>[] constructorArgTypes, final Object[] constructorArgs) {
		final Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(proxifiedObjClass);
		final Callback callback = new MethodInterceptorExampleB();
		enhancer.setCallback(callback);
		@SuppressWarnings("unchecked")
		final T result = (T) enhancer.create(constructorArgTypes, constructorArgs);
		return result;
	}

	public final static BigDecimal getProxy(final double constructorArg0) {
		final Class<?>[] constructorArgTypes = new Class<?>[] {Double.TYPE};
		final Object[] constructorArgs = new Object[] {constructorArg0};
		final BigDecimal result = getProxy(BigDecimal.class, constructorArgTypes, constructorArgs);
		return result;
	}

	public final static BigDecimal getProxy(final String constructorArg0) {
		final Class<?>[] constructorArgTypes = new Class<?>[] {String.class};
		final Object[] constructorArgs = new Object[] {constructorArg0};
		final BigDecimal result = getProxy(BigDecimal.class, constructorArgTypes, constructorArgs);
		return result;
	}

	private final static Object invokeInterceptedMethod(final Object object, final Method method, final Object[] methodArgs,
			final MethodProxy methodProxy) throws Throwable {
		final Object result = methodProxy.invokeSuper(object, methodArgs);
		return result;
	}

	private MethodInterceptorExampleB() {
	}

	/**
	 * Works only for non final and non private methods
	 */
	public final Object intercept(final Object object, final Method method, final Object[] methodArgs, final MethodProxy methodProxy)
			throws Throwable {
		logger.info("Before method '" + object.getClass().getSuperclass().getCanonicalName() + "." + method.getName() + "'");
		final Object returnValueFromInterceptedMethod = invokeInterceptedMethod(object, method, methodArgs, methodProxy);
		if (returnValueFromInterceptedMethod != object) { // can't happen; avoid recursion in case
															// returnValueFromInterceptedMethod is
															// the same proxy object
			logger.info("The return value from method '" + object.getClass().getSuperclass().getCanonicalName() + "." + method.getName() + "' is "
					+ returnValueFromInterceptedMethod);
		} else {
			logger.info("Can't display the return value from method '" + object.getClass().getSuperclass().getCanonicalName() + "."
					+ method.getName() + "' because this value is the same proxy object");
		}
		logger.info("After method '" + object.getClass().getSuperclass().getCanonicalName() + "." + method.getName() + "'");
		return returnValueFromInterceptedMethod;
	}
}