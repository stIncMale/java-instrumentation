package com.gl.vn.me.ko.sample.instrumentation.example.util.cglib;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

/**
 * Provides static methods for creating dynamic proxy instances by using CGLib framework.
 * The class intended to look and feel similar to {@code java.lang.reflect.Proxy}, but this is only a shallow similarity.
 * Unlike {@code java.lang.reflect.Proxy} this class is not a superclass of all dynamic proxy classes created by CGLib.
 * </p>
 * Simple example:
 * <blockquote>
 * 
 * <pre>
 * interface MyInterface {
 * 	Integer getMaxInteger();
 * }
 * 
 * class MyMethodInterceptor implements MethodInterceptor {
 * 	public Object intercept(Object enhancedObject, Method method, Object[] methodArgs, MethodProxy methodProxy) throws Throwable {
 * 		if (method.getName().equals(&quot;getMaxInteger&quot;)) {
 * 			return Integer.MAX_VALUE;
 * 		} else if (method.getName().equals(&quot;toString&quot;)) {
 * 			return &quot;some CGLib proxy instance&quot;;
 * 		} else {
 * 			return methodProxy.invokeSuper(enhancedObject, methodArgs);
 * 		}
 * 	}
 * }
 * 
 * ...
 * final MyInterface proxy = Proxy.newProxyInstance(Object.class, new Class&lt;?&gt;[] {MyInterface.class}, new MyMethodInterceptor(), null, null);
 * proxy.toString();// return &quot;some CGLib proxy instance&quot;
 * proxy.getMaxInteger();// return Integer.MAX_VALUE
 * </pre>
 * 
 * </blockquote>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
/*
 * For simplicity thread-safety is implemented in far not the best way, so if one intend to use this class in performance-critical multithreading environment,
 * one should consider to use more that one instance of {@code net.sf.cglib.proxy.Enhancer} and refactor the synchronization.
 */
public final class Proxy {
	private final static Enhancer ENHANCER;
	private final static Object LOCK;
	static {
		ENHANCER = new Enhancer();
		ENHANCER.setUseCache(true);
		LOCK = new Object();
	}

	/**
	 * Generates a proxy class that extends provided {@code superclass} and implements {@code interfaces} and creates an instance of generated class using using provided constructor arguments.
	 * Provided {@code callback} will be used for every invocation of any non-final & non-private method inherited from {@code superclass} and {@code interfaces}.
	 * </p>
	 * Method parameter {@code T} can be a type/supertype of {@code superclass} or type/supertype of any interface in {@code interfaces}. One can also explicitly cast returned object to any type that
	 * is allowed to be used as {@code T}.
	 * </p>
	 * Note that if required proxy class was already generated, it will be reused (see {@code net.sf.cglib.proxy.Enhancer.setUseCache(boolean)} method).
	 * 
	 * @param superclass
	 *            The class which the generated class will extend. Must be not {@code null} and must have accessible constructor suitable for provided {@code constructorArgTypes} and
	 *            {@code constructorArgs}.
	 * @param interfaces
	 *            The interfaces to implement. Can be {@code null} or empty. {@code net.sf.cglib.proxy.Factory} interface will always be implemented regardless of what is specified here
	 * @param callback
	 *            The callback to use in generated proxy class. Must be not null
	 * @param constructorArgTypes
	 *            Types of constructor arguments of {@code superclass}. Must be {@code null} or empty for the no-arguments constructor, must be the same length as 'constructorArgs'
	 * @param constructorArgs
	 *            Constructor arguments of {@code superclass}. Must be {@code null} or empty for the no-arguments constructor, must be the same length as 'constructorArgTypes'
	 * @return
	 *         Proxy (enhanced) object. Note that CGLib proxies are fundamentally different from {@code java.lang.reflect.Proxy} proxies. While Java proxies are really wrappers that can delegate calls
	 *         to the original object, CGLib proxies are self-sufficient objects that can have some additional logic besides the original one. This is why CGLib proxies sometimes called enhanced
	 *         objects.
	 */
	@SuppressWarnings("unchecked")
	public final static <T> T newProxyInstance(final Class<?> superclass, Class<?>[] interfaces, final Callback callback, Class<?>[] constructorArgTypes, Object[] constructorArgs) {
		if (superclass == null) {
			throw new NullPointerException("The first argument 'superclass' is null");
		} else if (callback == null) {
			throw new NullPointerException("The second argument 'methodInterceptor' is null");
		}
		if (constructorArgTypes != null) {
			constructorArgTypes = constructorArgTypes.clone();// should be changed to Arrays.copyOf(...) for JDK 6 and later
		}
		if (constructorArgs != null) {
			constructorArgs = constructorArgs.clone();// should be changed to Arrays.copyOf(...) for JDK 6 and later
		}
		final boolean useNoArgConstructor = ((constructorArgTypes == null) && (constructorArgs == null)) || ((constructorArgTypes.length == 0) && (constructorArgs.length == 0));
		final T result;
		synchronized (LOCK) {
			ENHANCER.setCallback(callback);
			ENHANCER.setSuperclass(superclass);
			if (interfaces != null) {
				interfaces = interfaces.clone();// should be changed to Arrays.copyOf(...) for JDK 6 and later
				if (interfaces.length > 0) {
					ENHANCER.setInterfaces(interfaces);
				}
			}
			result = useNoArgConstructor ? (T)ENHANCER.create() : (T)ENHANCER.create(constructorArgTypes, constructorArgs);
		}
		return result;
	}

	private Proxy() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}