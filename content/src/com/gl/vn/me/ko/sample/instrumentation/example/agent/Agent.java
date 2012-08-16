package com.gl.vn.me.ko.sample.instrumentation.example.agent;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.log4j.Logger;
import com.beust.jcommander.ParameterException;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper.CommandLineParams;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;
import com.gl.vn.me.ko.sample.instrumentation.example.transform.ClassTransformerExampleA;
import com.gl.vn.me.ko.sample.instrumentation.example.transform.ClassTransformerExampleB;
import com.gl.vn.me.ko.sample.instrumentation.example.util.InstrumentationEnvironment;
import com.gl.vn.me.ko.sample.instrumentation.example.util.javassist.JavassistEnvironment;

/**
 * @author Valentin Kovalenko
 */
public final class Agent {
	private final static Logger logger = Logger.getLogger(Agent.class);

	public final static void premain(final String agentArgs, final Instrumentation instrumentation) {
		processArgs(agentArgs);
		logger.trace("Invocation");
		InstrumentationEnvironment.setInstrumentation(instrumentation);
		registerClassTransformers(instrumentation);
		redefineClass(instrumentation);
		"a".concat("b");
		logger.trace("Invocation finished");
	}

	private final static void processArgs(final String args) {
		try {
			final String logLvl = (args != null && args.length() > 0) ? args : CommandLineParams.agentDefaultLoggingLevel;
			LogHelper.configure(logLvl, true);
		} catch (final ParameterException e) {
			CommandLineHelper.printAgentUsageAndAbort(e);
		}
	}

	private final static void redefineClass(final Instrumentation instrumentation) {
		try {
			safeRedefineClass(instrumentation);
		} catch (final Throwable e) {
			final String msg = "VAKO problems with redefine";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
	}

	private final static void registerClassTransformers(final Instrumentation instrumentation) {
		final ClassFileTransformer[] transformers = {new ClassTransformerExampleA(), new ClassTransformerExampleB()};
		for (final ClassFileTransformer transformer : transformers) {
			instrumentation.addTransformer(transformer);
			if (logger.isDebugEnabled()) {
				logger.debug("Class transformer '" + transformer.getClass().getSimpleName() + "' was successfully added to instrumentation");
			}
		}
	}

	private final static void safeRedefineClass(final Instrumentation instrumentation) {
		final CtClass ctClass = JavassistEnvironment.getCtClass("java.lang.String");
		final CtMethod ctMethod = JavassistEnvironment.getCtMethod(ctClass, "concat", "(Ljava/lang/String;)Ljava/lang/String;");
		try {
			ctMethod.insertAfter("{/*System.exit(0)*/;}");
		} catch (final CannotCompileException e) {
			final String msg = "VAKO Can't transform class";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		} catch (final Throwable t) {
			final String msg = "VAKO CRAP";
			final RuntimeException wrapperException = new RuntimeException(msg, t);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
		final byte[] bytes = JavassistEnvironment.getCtBytes(ctClass);
		final ClassDefinition[] classDef = {new ClassDefinition(String.class, bytes)};
		try {
			instrumentation.redefineClasses(classDef);
		} catch (final Exception e) {
			final String msg = "VAKO Can't redefine class";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
	}

	private Agent() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}