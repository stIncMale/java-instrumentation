package com.gl.vn.me.ko.sample.instrumentation.example.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;

/**
 * This class is an invocation handler that is used by {@link com.gl.vn.me.ko.sample.instrumentation.example.proxy.MapProxyFactoryExampleC} to create Java-proxy objects.
 * <p/>
 * Instantiability: allowed.<br/>
 * Mutability: this class itself has nothing that can cause mutability; depends on the object that was supplied to constructor {@link #InvocationHandlerExampleC(Map)}.<br/>
 * Thread safety: this class itself has nothing that can cause thread unsafety; depends on the object that was supplied to constructor {@link #InvocationHandlerExampleC(Map)}.
 * 
 * @author Valentin Kovalenko
 */
final class InvocationHandlerExampleC implements InvocationHandler {
	private final static Logger LOGGER;
	private final Object proxiedObject;
	private final static String METHOD_NAME_TO_TRICK;
	private final static Integer COLD_VALVE;
	private final static Integer HOT_VALVE;
	static {
		LOGGER = Logger.getLogger(InvocationHandlerExampleC.class);
		METHOD_NAME_TO_TRICK = "get";
		COLD_VALVE = Integer.valueOf(-1);
		HOT_VALVE = Integer.valueOf(1);
	}

	@Nullable
	private final static Object invokeOriginalMethod(final Object proxiedObject, final Method method, @Nullable final Object[] methodArgs) throws InvocationTargetException {
		Object result;
		try {
			result = method.invoke(proxiedObject, methodArgs);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("Can't access method '" + method.toString() + "'", e);
		} catch (final IllegalArgumentException e) {
			throw new RuntimeException("Can't invoke method '" + method.toString() + "' with arguments '" + Arrays.toString(methodArgs) + "'", e);
		}
		return result;
	}

	private final static String valveToString(@Nullable final Object value) {
		final String result;
		if (COLD_VALVE.equals(value)) {
			result = "cold";
		} else if (HOT_VALVE.equals(value)) {
			result = "hot";
		} else {
			result = "unknown";
		}
		return result;
	}

	/**
	 * Constructs invocation handler for the provided object.
	 * 
	 * @param object
	 *            An original object that can be hided behind the proxy so invocation handler is the only way to access it. Must be not {@code null}.
	 */
	public InvocationHandlerExampleC(final Map<?, ?> object) {
		if (object == null) {
			throw new NullPointerException("The argument 'object' is null");
		}
		proxiedObject = object;
	}

	/**
	 * Invokes provided method on the proxied (original) object. If name of the method to invoke is {@code "get"} and the only argument of the method is equal to object that denotes hot or cold faucet
	 * valves (see {@link com.gl.vn.me.ko.sample.instrumentation.example.ExampleC}), then the invocation becomes tricky: the value of argument is swapped from hot to cold and vice versa.
	 * 
	 * @return Result of the method invocation on the proxied (original) object. The result can be {@code null}.
	 */
	@Nullable
	public final Object invoke(@SuppressWarnings("null") final Object proxyObject, @SuppressWarnings("null") final Method method, @Nullable final Object[] methodArgs) {
		if (METHOD_NAME_TO_TRICK.equals(method.getName()) && (methodArgs != null) && (methodArgs.length == 1)) {
			final Object originalMethodArg = methodArgs[0];
			Object newMethodArg = null;
			final boolean argumentSwapped;
			if (COLD_VALVE.equals(originalMethodArg)) {
				newMethodArg = HOT_VALVE;
				argumentSwapped = true;
			} else if (HOT_VALVE.equals(originalMethodArg)) {
				newMethodArg = COLD_VALVE;
				argumentSwapped = true;
			} else {
				argumentSwapped = false;
			}
			if (argumentSwapped) {
				methodArgs[0] = newMethodArg;
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Method '" + method.toString() + "' was invoked with argument '" + originalMethodArg + "' (" + valveToString(originalMethodArg) + "). Swap it with '" + newMethodArg
							+ "' (" + valveToString(newMethodArg) + ")");
				}
			}
		}
		final Object result;
		try {
			result = invokeOriginalMethod(proxiedObject, method, methodArgs);
		} catch (final InvocationTargetException e) {
			throw new RuntimeException("Proxied method '" + method.toString() + "' invoked on object '" + proxiedObject.toString() + "' have thrown an exception", e);
		}
		return result;
	}
}