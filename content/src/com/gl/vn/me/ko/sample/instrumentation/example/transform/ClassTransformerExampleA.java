package com.gl.vn.me.ko.sample.instrumentation.example.transform;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.example.ExampleA;
import com.gl.vn.me.ko.sample.instrumentation.example.util.AbstractClassTransformer;
import com.gl.vn.me.ko.sample.instrumentation.example.util.javassist.JavassistEnvironment;

/**
 * Transforms {@link ExampleA} class by modifying its {@code private} method {@code incrementCounter()} according to the following algorithm:<br/>
 * <ul>
 * <li>search for any field access inside the method {@code incrementCounter()}</li>
 * <li>if it's a write access and the JVM field signature is "I", replace the access by increment by 2</li>
 * </ul>
 * According to the described above algorithm, the original method body
 * <blockquote>
 * 
 * <pre>
 * {
 * 	counter++;
 * 	logger.info(&quot;The counter was incremented&quot;);
 * }
 * </pre>
 * 
 * </blockquote>
 * is transformed to
 * <blockquote>
 * 
 * <pre>
 * {
 * 	counter += 2;
 * 	logger.info(&quot;The counter was incremented&quot;);
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
public final class ClassTransformerExampleA extends AbstractClassTransformer {
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
		 * Replaces a write-access expression of any {@code int} field {@code x} as following:
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
	 * The only possible instance of the class per its class loader.
	 */
	public final static ClassTransformerExampleA INSTANCE;
	private final static String CLASS_NAME_TO_TRANSFORM;// internal name of the class that should be transformed
	private final static String METHOD_NAME_TO_TRANSFORM;// name of the method that should be transformed
	private final static String JVM_METHOD_DESCRIPTOR;// method descriptor (specified in "The Java Virtual Machine Specification") of the method that should be transformed
	static {
		LOGGER = Logger.getLogger(ClassTransformerExampleA.class);
		INSTANCE = new ClassTransformerExampleA();
		CLASS_NAME_TO_TRANSFORM = "com/gl/vn/me/ko/sample/instrumentation/example/ExampleA";
		METHOD_NAME_TO_TRANSFORM = "incrementCounter";
		JVM_METHOD_DESCRIPTOR = "()V";
	}

	/**
	 * Constructs an instance of {@link ClassTransformerExampleA}.
	 */
	private ClassTransformerExampleA() {
	}

	@Override
	protected final boolean acceptClassForTransformation(final ClassLoader classLoader, final String className) {
		return CLASS_NAME_TO_TRANSFORM.equals(className);
	}

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
					LOGGER.debug("Transforming method '" + METHOD_NAME_TO_TRANSFORM + " " + JVM_METHOD_DESCRIPTOR + "'");
				}
				final CtMethod ctMethod = ctClass.getMethod(METHOD_NAME_TO_TRANSFORM, JVM_METHOD_DESCRIPTOR);
				ctMethod.instrument(WriteAccessIntExprEditor.INSTANCE);
				result = JavassistEnvironment.getCtBytes(ctClass);
			}
		}
		return result;
	}
}