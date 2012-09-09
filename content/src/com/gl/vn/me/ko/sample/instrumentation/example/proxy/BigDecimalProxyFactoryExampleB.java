package com.gl.vn.me.ko.sample.instrumentation.example.proxy;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.example.util.cglib.Proxy;

/**
 * The class provides an ability to create a CGLib proxy (enhanced) object of type {@code java.math.BigDecimal} that can intercept invocation of any non-final & non-private method of class
 * {@code java.math.BigDecimal} and perform additional actions (see {@link MethodInterceptorExampleB#intercept(Object, Method, Object[], MethodProxy)}).<br/>
 * Note that in general case this class would define a {@code create(...)} method per each {@code java.math.BigDecimal(...)} constructor, however
 * {@link com.gl.vn.me.ko.sample.instrumentation.example.ExampleB ExampleB} uses only two constructors of {@code java.math.BigDecimal} class, so for simplicity only two corresponding methods are
 * defined ({@link #create(double)} and {@link #create(String)}).
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

	private final static BigDecimal create(final Class<?>[] constructorArgTypes, final Object[] constructorArgs) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Creating a BigDecimal proxy object using constructor arg types " + Arrays.toString(constructorArgTypes) + " and args " + Arrays.toString(constructorArgs));
		}
		final BigDecimal result = Proxy.newProxyInstance(BigDecimal.class, null, MethodInterceptorExampleB.INSTANCE, constructorArgTypes, constructorArgs);
		return result;
	}

	/**
	 * Creates a {@code BigDecimal} proxy object from {@code double} argument.
	 * 
	 * @param constructorArg0
	 *            {@code double} value to be converted to {@code BigDecimal}
	 * @return proxy object of type {@code BigDecimal} whose value is {@code constructorArg0}
	 */
	public final static BigDecimal create(final double constructorArg0) {
		final Class<?>[] constructorArgTypes = new Class<?>[] {Double.TYPE};
		final Object[] constructorArgs = new Object[] {constructorArg0};
		final BigDecimal result = create(constructorArgTypes, constructorArgs);
		return result;
	}

	/**
	 * Creates a {@code BigDecimal} proxy object from {@code String} argument.
	 * 
	 * @param constructorArg0
	 *            {@code String} value to be converted to {@code BigDecimal}
	 * @return proxy object of type {@code BigDecimal} whose value is {@code constructorArg0}
	 */
	public final static BigDecimal create(final String constructorArg0) {
		final Class<?>[] constructorArgTypes = new Class<?>[] {String.class};
		final Object[] constructorArgs = new Object[] {constructorArg0};
		final BigDecimal result = create(constructorArgTypes, constructorArgs);
		return result;
	}

	private BigDecimalProxyFactoryExampleB() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}
