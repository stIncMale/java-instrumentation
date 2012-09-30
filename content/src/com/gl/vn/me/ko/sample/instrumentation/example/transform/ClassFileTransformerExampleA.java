package com.gl.vn.me.ko.sample.instrumentation.example.transform;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.example.ExampleA;
import com.gl.vn.me.ko.sample.instrumentation.util.AbstractClassFileTransformer;
import com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment;

/**
 * Transforms {@link ExampleA} class by modifying its {@code private} method {@code incrementCounter()} according to the following algorithm:<br/>
 * <ul>
 * <li>search for any field access inside the method {@code incrementCounter()}</li>
 * <li>if it's a write access and the JVM field signature is {@code "I"}, replace the access by increment by 2</li>
 * </ul>
 * According to the algorithm described above, the original method body<br/>
 * <blockquote>
 * 
 * <pre>
 * {
 * 	counter += incrementValue;
 * }
 * </pre>
 * 
 * </blockquote>
 * is transformed to something like<br/>
 * <blockquote>
 * 
 * <pre>
 * {
 * 	counter += 2;
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
public final class ClassFileTransformerExampleA extends AbstractClassFileTransformer {
	private final static class WriteAccessIntExprEditor extends ExprEditor {
		private final static WriteAccessIntExprEditor INSTANCE;
		private final static String JVM_INT_FIELD_SIGNATURE;// signature for int field as specified in "The Java Virtual Machine Specification"
		static {
			INSTANCE = new WriteAccessIntExprEditor();
			JVM_INT_FIELD_SIGNATURE = "I";
		}

		private WriteAccessIntExprEditor() {
		}

		/**
		 * Replaces a write-access expression of any {@code int} field {@code x} as following:<br/>
		 * <blockquote>
		 * 
		 * <pre>
		 * // before
		 * x = ...;
		 * </pre>
		 * 
		 * <pre>
		 * // after
		 * {
		 * 	x += 2;
		 * }
		 * </pre>
		 * 
		 * </blockquote>
		 */
		@Override
		public final void edit(final FieldAccess fieldAccess) throws CannotCompileException {
			if (fieldAccess.isWriter()) {
				final String jvmFieldSignature = fieldAccess.getSignature();// field signature as specified in "The Java Virtual Machine Specification"
				if (JVM_INT_FIELD_SIGNATURE.equals(jvmFieldSignature)) {
					final String fieldName = fieldAccess.getFieldName();
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Modifying access to the field '" + fieldName + " " + jvmFieldSignature + "'");
					}
					fieldAccess.replace("{" + fieldAccess.getFieldName() + " += 2; }");// increment field by 2
				}
			}
		}
	}

	private final static Logger LOGGER;
	/**
	 * The only instance of the class.
	 */
	public final static ClassFileTransformerExampleA INSTANCE;
	private final static String CLASS_NAME_TO_TRANSFORM;// internal name of the class that should be transformed
	private final static String METHOD_NAME_TO_TRANSFORM;// name of the method that should be transformed
	private final static CtClass[] METHOD_ARG_TYPES;// types of arguments of method that should be transformed
	static {
		LOGGER = Logger.getLogger(ClassFileTransformerExampleA.class);
		INSTANCE = new ClassFileTransformerExampleA();
		CLASS_NAME_TO_TRANSFORM = "com/gl/vn/me/ko/sample/instrumentation/example/ExampleA";
		METHOD_NAME_TO_TRANSFORM = "increment";
		try {
			METHOD_ARG_TYPES = new CtClass[] {JavassistEnvironment.getCtClass(Integer.TYPE.getCanonicalName())};
		} catch (final NotFoundException e) {
			throw new RuntimeException("Initialization of static field 'METHOD_ARG_TYPES' has failed", e);
		}
	}

	private ClassFileTransformerExampleA() {
	}

	/**
	 * Returns {@code true} only if {@code className} is equal to {@code "com/gl/vn/me/ko/sample/instrumentation/example/ExampleA"}. The first argument {@code classLoader} is not used.
	 */
	@Override
	protected final boolean acceptClassForTransformation(final ClassLoader classLoader, final String className) {
		return CLASS_NAME_TO_TRANSFORM.equals(className);
	}

	/**
	 * Transformation is described in the description of {@link ClassFileTransformerExampleA} class.
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
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Transforming method '" + METHOD_NAME_TO_TRANSFORM + "(...)'");
				}
				final CtMethod ctMethod = ctClass.getDeclaredMethod(METHOD_NAME_TO_TRANSFORM, METHOD_ARG_TYPES);
				ctMethod.instrument(WriteAccessIntExprEditor.INSTANCE);
				result = JavassistEnvironment.getCtBytes(ctClass);
			}
		}
		return result;
	}
}