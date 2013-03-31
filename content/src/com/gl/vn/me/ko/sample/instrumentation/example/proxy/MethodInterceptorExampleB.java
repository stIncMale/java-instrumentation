package com.gl.vn.me.ko.sample.instrumentation.example.proxy;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import javax.annotation.Nullable;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;
import com.gl.vn.me.ko.sample.instrumentation.util.cglib.MethodInterceptor;

/**
 * This class is a method interceptor that is used by {@link com.gl.vn.me.ko.sample.instrumentation.example.proxy.BigDecimalProxyFactoryExampleB} to create CGLib-proxy objects.
 * <p/>
 * Instantiability: explicit instantiation is forbidden; singleton (see {@link #INSTANCE} field).<br/>
 * Mutability: immutable.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
final class MethodInterceptorExampleB implements MethodInterceptor {
	private final static Logger LOGGER;
	/**
	 * The only instance of the class.
	 */
	public final static MethodInterceptorExampleB INSTANCE;
	static {
		LOGGER = Logger.getLogger(MethodInterceptorExampleB.class);
		INSTANCE = new MethodInterceptorExampleB();
	}

	@Nullable
	private final static Object invokeOriginalMethod(final Object proxyObject, final Method method, final Object[] methodArgs, final MethodProxy methodProxy) throws Throwable {
		final Object result = methodProxy.invokeSuper(proxyObject, methodArgs);
		return result;
	}

	private MethodInterceptorExampleB() {
	}

	/**
	 * Invokes the original method of proxied class. Negates the value returned from the original method if this value is of type {@link java.math.BigDecimal}.
	 * 
	 * @return
	 *         Result of the original method invocation (can be {@code null}) or negated value of the original result if it's of type {@link java.math.BigDecimal}.
	 * @throws java.lang.Throwable
	 *             If something goes wrong.
	 */
	@Nullable
	public final Object intercept(final Object proxyObject, final Method method, final Object[] methodArgs, final MethodProxy proxyMethod) throws Throwable {
		final NumberFormat numberFormat = LogHelper.getNumberFormat();
		final Object returnValueFromOriginalMethod = invokeOriginalMethod(proxyObject, method, methodArgs, proxyMethod);
		final Object result;
		if (returnValueFromOriginalMethod instanceof BigDecimal) {
			final String stringRepresentationOfReturnValue = numberFormat.format(returnValueFromOriginalMethod);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The return value of the method '" + method.toString() + "' is '" + stringRepresentationOfReturnValue + "'" + " and it's a BigDecimal. Return it with the opposite sign");
			}
			result = ((BigDecimal)returnValueFromOriginalMethod).negate();
		} else {
			result = returnValueFromOriginalMethod;
		}
		return result;
	}
}