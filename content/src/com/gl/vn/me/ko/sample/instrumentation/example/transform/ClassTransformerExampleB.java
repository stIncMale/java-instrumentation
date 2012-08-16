package com.gl.vn.me.ko.sample.instrumentation.example.transform;

import java.lang.instrument.IllegalClassFormatException;
import java.util.Collections;
import javassist.CannotCompileException;
import javassist.CodeConverter;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;
import com.gl.vn.me.ko.sample.instrumentation.example.util.AbstractClassTransformer;
import com.gl.vn.me.ko.sample.instrumentation.example.util.javassist.JavassistEnvironment;

/**
 * @author Valentin Kovalenko
 */
public class ClassTransformerExampleB extends AbstractClassTransformer {
	private final static Logger logger = Logger.getLogger(ClassTransformerExampleB.class);

	public ClassTransformerExampleB() {
		super(Collections.singleton("com.gl.vn.me.ko.sample.instrumentation.example.ExampleB"), false);
		logger.trace("Invocation finished");
	}

	@Override
	protected byte[] doTransform(final CtClass ctClass) throws IllegalClassFormatException {
		final CtMethod[] methods = ctClass.getMethods();
		final CodeConverter codeConvertor = new CodeConverter();
		final CtClass originalClass = JavassistEnvironment.getCtClass("java.math.BigDecimal");
		final CtClass substitutionalClass = JavassistEnvironment
				.getCtClass("com.gl.vn.me.ko.sample.instrumentation.example.intercept.MethodInterceptorExampleB");
		final String substitutionalMethod = "getProxy";
		codeConvertor.replaceNew(originalClass, substitutionalClass, substitutionalMethod);
		for (final CtMethod method : methods) {
			try {
				method.instrument(codeConvertor);
			} catch (final CannotCompileException e) {
				final String methodName = method.getName();
				final String msg = "Can't transform method '" + methodName + "'";
				final RuntimeException wrapperException = new RuntimeException(msg, e);
				final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
				logger.error(msgWithStackTrace);
				throw wrapperException;
			}
		}
		final byte[] result = JavassistEnvironment.getCtBytes(ctClass);
		return result;
	}
}