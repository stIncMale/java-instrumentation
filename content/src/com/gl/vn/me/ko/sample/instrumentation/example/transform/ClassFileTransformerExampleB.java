package com.gl.vn.me.ko.sample.instrumentation.example.transform;

import javassist.CodeConverter;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.util.AbstractClassFileTransformer;
import com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment;

/**
 * Transforms {@link com.gl.vn.me.ko.sample.instrumentation.example.ExampleB} class by modifying all its declared non-native methods according to the following algorithm:<br/>
 * <ul>
 * <li>for every declared non-native method in {@link com.gl.vn.me.ko.sample.instrumentation.example.ExampleB} search for {@link java.math.BigDecimal} constructor calls</li>
 * <li>substitute any found call of {@link java.math.BigDecimal} constructor with call of {@link com.gl.vn.me.ko.sample.instrumentation.example.proxy.BigDecimalProxyFactoryExampleB}{@code .create(...)}
 * method with the same arguments</li>
 * </ul>
 * According to the described above algorithm, the original method body<br/>
 * <blockquote>
 * 
 * <pre>
 * {
 * 	...
 * 	BigDecimal piFromDouble = new BigDecimal(doublePi);
 * 	...
 * 	BigDecimal piFromString = new BigDecimal(stringPi);
 * 	...
 * }
 * </pre>
 * 
 * </blockquote>
 * is transformed to something like<br/>
 * <blockquote>
 * 
 * <pre>
 * {
 * 	...
 * 	BigDecimal piFromDouble = BigDecimalProxyFactoryExampleB.create(doublePi);
 * 	...
 * 	BigDecimal piFromString = BigDecimalProxyFactoryExampleB.create(stringPi);
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
public class ClassFileTransformerExampleB extends AbstractClassFileTransformer {
	private final static Logger LOGGER;
	/**
	 * The only instance of the class.
	 */
	public final static ClassFileTransformerExampleB INSTANCE;
	private final static String CLASS_NAME_TO_TRANSFORM;// internal name of the class to transform
	private final static String CLASS_NAME_ORIGINAL;// fully qualified name of the class that should be substituted
	private final static String CLASS_NAME_TO_SUBSTITUTE_FOR;// fully qualified name of the class that should be used instead the original one
	private final static String METHOD_NAME_TO_SUBSTITUTE_FOR;// name of the method that should be used instead of the invocation of constructor
	static {
		LOGGER = Logger.getLogger(ClassFileTransformerExampleB.class);
		INSTANCE = new ClassFileTransformerExampleB();
		CLASS_NAME_TO_TRANSFORM = "com/gl/vn/me/ko/sample/instrumentation/example/ExampleB";
		CLASS_NAME_ORIGINAL = "java.math.BigDecimal";
		CLASS_NAME_TO_SUBSTITUTE_FOR = "com.gl.vn.me.ko.sample.instrumentation.example.proxy.BigDecimalProxyFactoryExampleB";
		METHOD_NAME_TO_SUBSTITUTE_FOR = "create";
	}

	private ClassFileTransformerExampleB() {
	}

	/**
	 * Returns {@code true} only if {@code className} is equal to {@code "com/gl/vn/me/ko/sample/instrumentation/example/ExampleB"}. The first argument {@code classLoader} is not used.
	 */
	@Override
	protected final boolean acceptClassForTransformation(@Nullable final ClassLoader classLoader, final String className) {
		return CLASS_NAME_TO_TRANSFORM.equals(className);
	}

	/**
	 * Transformation is described in the description of {@link ClassFileTransformerExampleB} class.
	 */
	@Nullable
	@Override
	protected byte[] doTransform(final CtClass ctClass) throws Exception {
		final byte[] result;
		synchronized (ctClass) {
			if (ctClass.isFrozen()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Class '" + ctClass.getName() + "' wasn't transformed because it's frozen");
				}
				result = null;
			} else {
				final CtClass originalClass = JavassistEnvironment.getCtClass(CLASS_NAME_ORIGINAL);
				final CtClass substitutionalClass = JavassistEnvironment.getCtClass(CLASS_NAME_TO_SUBSTITUTE_FOR);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Substituting constructor calls 'new " + originalClass.getName() + "(...)' with the method '" + substitutionalClass.getName() + "." + METHOD_NAME_TO_SUBSTITUTE_FOR
							+ "(...)' invocations");
				}
				final CodeConverter codeConvertor = new CodeConverter();
				codeConvertor.replaceNew(originalClass, substitutionalClass, METHOD_NAME_TO_SUBSTITUTE_FOR);
				for (final CtMethod ctMethod : ctClass.getDeclaredMethods()) {
					if (!Modifier.isNative(ctMethod.getModifiers())) {// check that the method is not native
						ctMethod.instrument(codeConvertor);
					}
				}
				result = JavassistEnvironment.getCtBytes(ctClass);
			}
		}
		return result;
	}
}