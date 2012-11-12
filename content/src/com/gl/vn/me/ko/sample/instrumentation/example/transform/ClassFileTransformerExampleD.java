package com.gl.vn.me.ko.sample.instrumentation.example.transform;

import javassist.CtClass;
import javassist.CtMethod;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.util.AbstractClassFileTransformer;
import com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment;

/**
 * Transforms {@link java.lang.String} class by modifying the body of its method {@link java.lang.String#toString()}. Transformed method returns a new instance of {@link java.lang.String} so the
 * original method body<br/>
 * <blockquote>
 * 
 * <pre>
 * // this is just an approximation of the original method body
 * {
 * 	return this;
 * }
 * </pre>
 * 
 * </blockquote>
 * is transformed to something like<br/>
 * <blockquote>
 * 
 * <pre>
 * {
 * 	Logger logger = Logger.getLogger(String.class);
 * 	if(logger.isInfoEnabled()) {
 * 		if(Logger.getRootLogger().getAllAppenders().hasMoreElements())// check if Log4j is configured
 * 			logger.info("Invocation of method 'java.lang.String.toString()' for object '" + this + "'");
 * 		}
 * 	}
 * 	return new String(this);
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
public final class ClassFileTransformerExampleD extends AbstractClassFileTransformer {
	private final static Logger LOGGER;
	/**
	 * The only instance of the class.
	 */
	public final static ClassFileTransformerExampleD INSTANCE;
	private final static String CLASS_NAME_TO_TRANSFORM;// internal name of the class to transform
	private final static String METHOD_NAME_TO_TRANSFORM;// name of the method that should be transformed
	static {
		LOGGER = Logger.getLogger(ClassFileTransformerExampleD.class);
		INSTANCE = new ClassFileTransformerExampleD();
		CLASS_NAME_TO_TRANSFORM = "java/lang/String";
		METHOD_NAME_TO_TRANSFORM = "toString";
	}

	private ClassFileTransformerExampleD() {
	}

	/**
	 * Returns {@code true} only if {@code className} is equal to {@code "java/lang/String"}. The first argument {@code classLoader} is not used.
	 */
	@Override
	protected final boolean acceptClassForTransformation(final ClassLoader classLoader, final String className) {
		return CLASS_NAME_TO_TRANSFORM.equals(className);
	}

	/**
	 * Transformation is described in the description of {@link ClassFileTransformerExampleD} class.
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
					LOGGER.debug("Modifying return value of the method '" + ctMethod.getLongName() + "'");
				}
				ctMethod.setBody("{" +
						"	final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(String.class);" +
						"	if(logger.isDebugEnabled()) {" +
						"		if(org.apache.log4j.Logger.getRootLogger().getAllAppenders().hasMoreElements()) {" + // check if Log4j is configured
						"			logger.debug(\"Invocation for object '\" + $0 + \"'\");" +
						"		}" +
						"	}" +
						"	return new String((String)$0);" + // $0 - is equivalent to 'this'
						"}");
				result = JavassistEnvironment.getCtBytes(ctClass);
			}
		}
		return result;
	}
}