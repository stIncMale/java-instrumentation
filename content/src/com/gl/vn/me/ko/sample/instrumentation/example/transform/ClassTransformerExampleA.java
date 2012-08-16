package com.gl.vn.me.ko.sample.instrumentation.example.transform;

import java.lang.instrument.IllegalClassFormatException;
import java.util.Collections;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;
import com.gl.vn.me.ko.sample.instrumentation.example.util.AbstractClassTransformer;
import com.gl.vn.me.ko.sample.instrumentation.example.util.javassist.JavassistEnvironment;

/**
 * @author Valentin Kovalenko
 */
public final class ClassTransformerExampleA extends AbstractClassTransformer {
	private final static Logger logger = Logger.getLogger(ClassTransformerExampleA.class);

	public ClassTransformerExampleA() {
		super(Collections.singleton("com.gl.vn.me.ko.sample.instrumentation.example.ExampleA"), false);
		logger.trace("Invocation finished");
	}

	@Override
	protected final byte[] doTransform(final CtClass ctClass) throws IllegalClassFormatException {
		final String methodName = "incrementCounter";
		final String jvmMethodDescriptor = "()V";// method descriptor as specified in
													// "The Java Virtual Machine Specification"
		if (logger.isDebugEnabled()) {
			logger.debug("Transforming method '" + methodName + " " + jvmMethodDescriptor + "'");
		}
		final CtMethod ctMethod = JavassistEnvironment.getCtMethod(ctClass, methodName, jvmMethodDescriptor);
		try {
			ctMethod.instrument(new ExprEditor() {
				@Override
				public final void edit(final FieldAccess fieldAccess) throws CannotCompileException {
					if (fieldAccess.isWriter()) {
						fieldAccess.replace("{" + fieldAccess.getFieldName() + " += 2; }");// increment
																							// field
																							// by 2
					}
				}
			});
		} catch (final CannotCompileException e) {
			final String msg = "Can't transform method '" + methodName + "'";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
		final byte[] result = JavassistEnvironment.getCtBytes(ctClass);
		return result;
	}
}