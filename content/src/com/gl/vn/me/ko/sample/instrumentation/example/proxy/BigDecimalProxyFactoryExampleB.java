package com.gl.vn.me.ko.sample.instrumentation.example.proxy;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.util.cglib.Proxy;

/**
 * The class provides an ability to create a CGLib-proxy (enhanced object) of type {@link java.math.BigDecimal} that can intercept invocation of any non-final & non-private method of class
 * {@link java.math.BigDecimal} and perform additional actions (see
 * {@link com.gl.vn.me.ko.sample.instrumentation.example.proxy.MethodInterceptorExampleB#intercept(Object, Method, Object[], MethodProxy)}).
 * <p/>
 * Note that in general case this class would define a {@code create(...)} method for each {@link java.math.BigDecimal}{@code (...)} constructor, however
 * {@link com.gl.vn.me.ko.sample.instrumentation.example.ExampleB} only uses two constructors of {@link java.math.BigDecimal} class, so for simplicity only two corresponding methods are defined (
 * {@link #create(double)} and {@link #create(String)}).
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class BigDecimalProxyFactoryExampleB {
	private final static Logger LOGGER;
	static {
		LOGGER = Logger.getLogger(BigDecimalProxyFactoryExampleB.class);
	}

	/**
	 * Creates a {@link java.math.BigDecimal} proxy object from {@code double} argument.
	 * 
	 * @param value
	 *            {@code double} value to be converted to {@link java.math.BigDecimal}.
	 * @return Proxy object whose value is {@code value}.
	 * @see java.math.BigDecimal#BigDecimal(double)
	 */
	public final static BigDecimal create(final double value) {
		final Class<?>[] constructorArgTypes = new Class<?>[] {Double.TYPE};
		final Object[] constructorArgs = new Object[] {Double.valueOf(value)};
		final BigDecimal result = create(constructorArgTypes, constructorArgs);
		return result;
	}

	/**
	 * Creates a {@link java.math.BigDecimal} proxy object from {@link java.lang.String} argument.
	 * 
	 * @param value
	 *            {@link java.lang.String} value to be converted to {@link java.math.BigDecimal}. Must be not {@code null}.
	 * @return Proxy object whose value is {@code value}.
	 * @see java.math.BigDecimal#BigDecimal(String)
	 */
	public final static BigDecimal create(final String value) {
		if (value == null) {
			throw new NullPointerException("The argument 'value' is null");
		}
		final Class<?>[] constructorArgTypes = new Class<?>[] {String.class};
		final Object[] constructorArgs = new Object[] {value};
		final BigDecimal result = create(constructorArgTypes, constructorArgs);
		return result;
	}

	private final static BigDecimal create(final Class<?>[] constructorArgTypes, final Object[] constructorArgs) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Creating a BigDecimal proxy object using constructor arg types " + Arrays.toString(constructorArgTypes) + " and args " + Arrays.toString(constructorArgs));
		}
		final BigDecimal result = Proxy.newProxyInstance(BigDecimal.class, null, MethodInterceptorExampleB.INSTANCE, constructorArgTypes, constructorArgs);
		return result;
	}

	private BigDecimalProxyFactoryExampleB() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}