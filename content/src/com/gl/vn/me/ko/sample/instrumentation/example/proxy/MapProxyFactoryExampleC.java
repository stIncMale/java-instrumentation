package com.gl.vn.me.ko.sample.instrumentation.example.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * The class provides an ability to create a Java-proxy object of type {@link java.util.Map} that can intercept invocation of any method of interface {@link java.util.Map} and perform additional
 * actions (see {@link com.gl.vn.me.ko.sample.instrumentation.example.proxy.InvocationHandlerExampleC#invoke(Object, Method, Object[])}).<br/>
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @see java.lang.reflect.Proxy
 * @author Valentin Kovalenko
 */
public final class MapProxyFactoryExampleC {
	private final static Logger LOGGER;
	private final static Class<?>[] PROXY_INTERFACES;
	static {
		LOGGER = Logger.getLogger(MapProxyFactoryExampleC.class);
		PROXY_INTERFACES = new Class<?>[] {Map.class};
	}

	/**
	 * Creates a proxy object for the provided object.
	 * 
	 * @param object
	 *            The original object that will be hided behind the proxy. Must be not {@code null}.
	 * @return
	 *         Proxy backed by the original object.
	 */
	public final static <K, V> Map<K, V> create(final Map<K, V> object) {
		if (object == null) {
			throw new NullPointerException("The argument 'object' is null");
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Creating a Map proxy object for object '" + object.toString() + "'");
		}
		@SuppressWarnings("unchecked")
		final Map<K, V> result = (Map<K, V>)Proxy.newProxyInstance(object.getClass().getClassLoader(), PROXY_INTERFACES, new InvocationHandlerExampleC(object));
		return result;
	}

	private MapProxyFactoryExampleC() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}
