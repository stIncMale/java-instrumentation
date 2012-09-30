package com.gl.vn.me.ko.sample.instrumentation.example.transform;

import javassist.CtClass;
import javassist.CtMethod;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.example.ExampleC;
import com.gl.vn.me.ko.sample.instrumentation.example.proxy.MapProxyFactoryExampleC;
import com.gl.vn.me.ko.sample.instrumentation.util.AbstractClassFileTransformer;
import com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment;

/**
 * Transforms {@link ExampleC} class by modifying the return value of its {@code private} method {@code createFaucetColorCodeMap()}. The method returns instance of {@link java.util.HashMap},
 * transformation substitutes the return object with Java-proxy created by using {@link MapProxyFactoryExampleC#create(java.util.Map)} method, so the original method body<br/>
 * <blockquote>
 * 
 * <pre>
 * {
 * 	Map<Integer, String> result = new HashMap<Integer, String>(2);
 * 	...
 * 	return result;
 * }
 * </pre>
 * 
 * </blockquote>
 * is transformed to something like<br/>
 * <blockquote>
 * 
 * <pre>
 * {
 * 	Map<Integer, String> result = new HashMap<Integer, String>(2);
 * 	...
 * 	return MapProxyFactoryExampleC.create(result);
 * }
 * </pre>
 * 
 * </blockquote>
 * Instantiability: explicit instantiation is forbidden; singleton (see {@link #INSTANCE} field).<br/>
 * Mutability: immutable.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class ClassFileTransformerExampleC extends AbstractClassFileTransformer {
	private final static Logger LOGGER;
	/**
	 * The only instance of the class.
	 */
	public final static ClassFileTransformerExampleC INSTANCE;
	private final static String CLASS_NAME_TO_TRANSFORM;// internal name of the class to transform
	private final static String METHOD_NAME_TO_TRANSFORM;// name of the method that should be transformed
	static {
		LOGGER = Logger.getLogger(ClassFileTransformerExampleC.class);
		INSTANCE = new ClassFileTransformerExampleC();
		CLASS_NAME_TO_TRANSFORM = "com/gl/vn/me/ko/sample/instrumentation/example/ExampleC";
		METHOD_NAME_TO_TRANSFORM = "createFaucetColorCodeMap";
	}

	private ClassFileTransformerExampleC() {
	}

	/**
	 * Returns {@code true} only if {@code className} is equal to {@code "com/gl/vn/me/ko/sample/instrumentation/example/ExampleC"}. The first argument {@code classLoader} is not used.
	 */
	@Override
	protected final boolean acceptClassForTransformation(final ClassLoader classLoader, final String className) {
		return CLASS_NAME_TO_TRANSFORM.equals(className);
	}

	/**
	 * Transformation is described in the description of {@link ClassFileTransformerExampleC} class.
	 */
	@Override
	protected final byte[] doTransform(final CtClass ctClass) throws Exception {
		final byte[] result;
		synchronized (ctClass) {
			if (ctClass.isFrozen()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Class '" + ctClass.getName() + "' wasn't transformed because it's frozen");
				}
				result = null;
			} else {
				final CtMethod ctMethod = ctClass.getDeclaredMethod(METHOD_NAME_TO_TRANSFORM, null);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Modifying of return value of the method '" + ctMethod.getLongName() + "'");
				}
				final String code = "{$_ = com.gl.vn.me.ko.sample.instrumentation.example.proxy.MapProxyFactoryExampleC.create($_);}";// $_ - the resulting value of the method
				ctMethod.insertAfter(code);
				result = JavassistEnvironment.getCtBytes(ctClass);
			}
		}
		return result;
	}
}