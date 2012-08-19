package com.gl.vn.me.ko.sample.instrumentation.example.intercept;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;
import com.gl.vn.me.ko.sample.instrumentation.example.util.cglib.AbstractSimpleMethodInterceptor;

/**
 * The class provides an ability to create a proxy (enhanced) object of type {@code java.math.BigDecimal} (see {@code getProxy(...)} methods) that can intercept
 * invocation of any non-final method of class {@code BigDecimal} and perform additional actions
 * (see {@link MethodInterceptorExampleB#intercept(Object, Method, Object[], MethodProxy) intercept(Object, Method, Object[], MethodProxy)}).<br/>
 * Note: in general case this class would define a {@code getProxy(...)} method per each {@code BigDecimal(...)} constructor, however {@link com.gl.vn.me.ko.sample.instrumentation.example.ExampleB
 * ExampleB} uses only two constructors of {@code BigDecimal} class, so for simplicity only two corresponding methods are defined ({@link MethodInterceptorExampleB#getProxy(double) getProxy(double)}
 * and {@link MethodInterceptorExampleB#getProxy(String) getProxy(String)}).
 * <p/>
 * Instantiability: explicit instantiation is forbidden. Instances are created implicitly: one instance per any of {@code getProxy(...)} methods invocation.<br/>
 * Mutability: instances of the class are immutable.<br/>
 * Thread safety: the class is thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class MethodInterceptorExampleB extends AbstractSimpleMethodInterceptor<BigDecimal> {
	private final static Logger logger = Logger.getLogger(MethodInterceptorExampleB.class);

	private final static BigDecimal getProxy(final Class<?>[] constructorArgTypes, final Object[] constructorArgs) {
		final MethodInterceptorExampleB interceptor = new MethodInterceptorExampleB();
		final BigDecimal result = interceptor.getProxy(BigDecimal.class, constructorArgTypes, constructorArgs);
		return result;
	}

	/**
	 * Creates a {@code BigDecimal} proxy object from {@code double} argument.
	 * 
	 * @param constructorArg0
	 *            {@code double} value to be converted to {@code BigDecimal}
	 * @return proxy object of type {@code BigDecimal} whose value is {@code constructorArg0}
	 */
	public final static BigDecimal getProxy(final double constructorArg0) {
		final Class<?>[] constructorArgTypes = new Class<?>[] {Double.TYPE};
		final Object[] constructorArgs = new Object[] {constructorArg0};
		final BigDecimal result = getProxy(constructorArgTypes, constructorArgs);
		return result;
	}

	/**
	 * Creates a {@code BigDecimal} proxy object from {@code String} argument.
	 * 
	 * @param constructorArg0
	 *            {@code String} value to be converted to {@code BigDecimal}
	 * @return proxy object of type {@code BigDecimal} whose value is {@code constructorArg0}
	 */
	public final static BigDecimal getProxy(final String constructorArg0) {
		final Class<?>[] constructorArgTypes = new Class<?>[] {String.class};
		final Object[] constructorArgs = new Object[] {constructorArg0};
		final BigDecimal result = getProxy(constructorArgTypes, constructorArgs);
		return result;
	}

	private final static Object invokeInterceptedMethod(final Object enhancedObject, final Method method, final Object[] methodArgs, final MethodProxy methodProxy) throws Throwable {
		final Object result = methodProxy.invokeSuper(enhancedObject, methodArgs);
		return result;
	}

	private MethodInterceptorExampleB() {
	}

	/**
	 * When using proxy object obtained by any of {@code getProxy(...)} methods, this method is invoked instead of invocation of any non-final & non-private method of {@code java.math.BigDecimal}.
	 * The method invokes the original method of {@code BigDecimal} class by itself and prints some information to log before and after the invocation of the original method.
	 */
	@Override
	public final Object intercept(final Object enhancedObject, final Method method, final Object[] methodArgs, final MethodProxy methodProxy) throws Throwable {
		LogHelper.getNumberFormat();
		final String methodName = enhancedObject.getClass().getSuperclass().getCanonicalName() + "." + method.getName();
		logger.info("Before method '" + methodName + "'");
		final Object returnValueFromInterceptedMethod = invokeInterceptedMethod(enhancedObject, method, methodArgs, methodProxy);
		if (returnValueFromInterceptedMethod != enhancedObject) {// can't happen; avoid recursion in case returnValueFromInterceptedMethod is the same proxy object
			logger.info("The return value from method '" + enhancedObject.getClass().getSuperclass().getCanonicalName() + "." + method.getName() + "' is " + returnValueFromInterceptedMethod);
		} else {
			logger.info("Can't display the return value from method '" + enhancedObject.getClass().getSuperclass().getCanonicalName() + "." + method.getName()
					+ "' because this value is the same proxy object");
		}
		logger.info("After method '" + enhancedObject.getClass().getSuperclass().getCanonicalName() + "." + method.getName() + "'");
		return returnValueFromInterceptedMethod;
	}
}