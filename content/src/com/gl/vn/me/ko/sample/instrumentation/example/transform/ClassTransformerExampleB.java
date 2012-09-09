package com.gl.vn.me.ko.sample.instrumentation.example.transform;

import javassist.CodeConverter;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.example.ExampleB;
import com.gl.vn.me.ko.sample.instrumentation.example.util.AbstractClassTransformer;
import com.gl.vn.me.ko.sample.instrumentation.example.util.javassist.JavassistEnvironment;

/**
 * Transforms {@link ExampleB} class by modifying all its methods according to the following algorithm:<br/>
 * <ul>
 * <li>for every method in {@link ExampleB} search for {@code java.math.BigDecimal} constructor calls</li>
 * <li>substitute any found call of {@code java.math.BigDecimal} constructor with call of {@code com.gl.vn.me.ko.sample.instrumentation.example.proxy.BigDecimalProxyFactoryExampleB.create(...)} method
 * with the same arguments</li>
 * </ul>
 * According to the described above algorithm, the original method body
 * <blockquote>
 * 
 * <pre>
 * {
 * 	...
 * 	final double doublePi = Math.PI;
 * 	final BigDecimal piFromDouble = new BigDecimal(doublePi);
 * 	...
 * }
 * </pre>
 * 
 * </blockquote>
 * is transformed to
 * <blockquote>
 * 
 * <pre>
 * {
 * 	...
 * 	final double doublePi = Math.PI;
 * 	final BigDecimal piFromDouble = BigDecimalProxyFactoryExampleB.create(doublePi);
 * 	...
 * }
 * </pre>
 * 
 * </blockquote>
 * Instantiability: explicit instantiation is forbidden; singleton.<br/>
 * Mutability: immutable.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public class ClassTransformerExampleB extends AbstractClassTransformer {
	private final static Logger LOGGER;
	/**
	 * The only possible instance of the class per its class loader.
	 */
	public final static ClassTransformerExampleB INSTANCE;
	private final static String CLASS_NAME_TO_TRANSFORM;// internal name of the class to transform
	private final static String CLASS_NAME_ORIGINAL;// fully qualified name of the class that should be substituted
	private final static String CLASS_NAME_TO_SUBSTITUTE_FOR;// fully qualified name of the class that should be used instead the original one
	private final static String METHOD_NAME_TO_SUBSTITUTE_FOR;// name of the method that should be used instead of the invocation of constructor
	static {
		LOGGER = Logger.getLogger(ClassTransformerExampleB.class);
		INSTANCE = new ClassTransformerExampleB();
		CLASS_NAME_TO_TRANSFORM = "com/gl/vn/me/ko/sample/instrumentation/example/ExampleB";
		CLASS_NAME_ORIGINAL = "java.math.BigDecimal";
		CLASS_NAME_TO_SUBSTITUTE_FOR = "com.gl.vn.me.ko.sample.instrumentation.example.proxy.BigDecimalProxyFactoryExampleB";
		METHOD_NAME_TO_SUBSTITUTE_FOR = "create";
	}

	/**
	 * Constructs an instance of {@link ClassTransformerExampleB}.
	 */
	private ClassTransformerExampleB() {
	}

	@Override
	protected final boolean acceptClassForTransformation(final ClassLoader classLoader, final String className) {
		return CLASS_NAME_TO_TRANSFORM.equals(className);
	}

	@Override
	protected byte[] doTransform(final CtClass ctClass) throws Exception {
		final byte[] result;
		if (ctClass.isFrozen()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Class '" + ctClass.getName() + "' wasn't transformed because it's frozen");
			}
			result = null;
		} else {
			synchronized (ctClass) {
				LOGGER.debug("Substituting constructor calls 'new " + CLASS_NAME_ORIGINAL + "(...)' with the method '" + CLASS_NAME_TO_SUBSTITUTE_FOR + "." + METHOD_NAME_TO_SUBSTITUTE_FOR
						+ "(...)' invocations");
				final CtMethod[] methods = ctClass.getMethods();
				final CtClass originalClass = JavassistEnvironment.getCtClass(CLASS_NAME_ORIGINAL);
				final CtClass substitutionalClass = JavassistEnvironment.getCtClass(CLASS_NAME_TO_SUBSTITUTE_FOR);
				final CodeConverter codeConvertor = new CodeConverter();
				codeConvertor.replaceNew(originalClass, substitutionalClass, METHOD_NAME_TO_SUBSTITUTE_FOR);
				for (final CtMethod method : methods) {
					method.instrument(codeConvertor);
				}
				result = JavassistEnvironment.getCtBytes(ctClass);
			}
		}
		return result;
	}
}