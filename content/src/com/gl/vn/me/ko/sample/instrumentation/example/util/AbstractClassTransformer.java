package com.gl.vn.me.ko.sample.instrumentation.example.util;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import javassist.CtClass;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;
import com.gl.vn.me.ko.sample.instrumentation.example.util.javassist.JavassistEnvironment;

/**
 * This class is not synchronized.
 */
public abstract class AbstractClassTransformer implements ClassFileTransformer {
	private final static Logger logger = Logger.getLogger(AbstractClassTransformer.class);
	private final Set<String> classesToTransform;
	private final boolean defrostAllowed;

	public AbstractClassTransformer(final Set<String> classesToTransform, final boolean defrostAllowed) {
		logger.trace("Invocation");
		this.classesToTransform = new HashSet<String>(classesToTransform.size());
		for (final String classToTransform : classesToTransform) {
			final String internalRepresentationClassToTransform = classToTransform.replace(JavassistEnvironment.PACKAGE_SEPARATOR, JavassistEnvironment.INTERNAL_PACKAGE_SEPARATOR);
			this.classesToTransform.add(internalRepresentationClassToTransform);
		}
		this.defrostAllowed = defrostAllowed;
		logger.trace("Invocation finished");
	}

	private final boolean acceptClassForTransformation(final String className) {
		final boolean result = classesToTransform.contains(className) ? true : false;
		return result;
	}

	protected abstract byte[] doTransform(final CtClass ctClass) throws IllegalClassFormatException;

	public final byte[] transform(final ClassLoader classLoader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
			throws IllegalClassFormatException {
		final byte[] transformedBytes;
		try {
			transformedBytes = unsafeTransform(classLoader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		} catch (final Throwable e) {
			final String msg = "Problem was encountered during transformation of the class '" + className + "'";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
		return transformedBytes;
	}

	private final byte[] unsafeTransform(final ClassLoader classLoader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
			final byte[] classfileBuffer) throws IllegalClassFormatException {
		final byte[] transformedBytes;
		if (acceptClassForTransformation(className)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Transforming class '" + className + "'");
			}
			CtClass ctClass;
			if (classLoader == null) {// bootstrap class loader
				ctClass = JavassistEnvironment.getCtClass(className);
			} else {
				ctClass = JavassistEnvironment.getCtClassOrNull(className);// try faster way of getting CtClass, without class path calculation
				if (ctClass == null) {
					final String classPath = JavassistEnvironment.getClassPath(className, classLoader);
					ctClass = JavassistEnvironment.getCtClass(className, classPath);
				}
			}
			if (ctClass.isFrozen()) {
				if (defrostAllowed) {
					ctClass.defrost();
					transformedBytes = doTransform(ctClass);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Class '" + className + "' can't be transformed, because the corresponding CtClass is frozen and defrost is not allowed");
					}
					transformedBytes = null;
				}
			} else {
				transformedBytes = doTransform(ctClass);
			}
			if (transformedBytes != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Class '" + className + "' was successfully transformed");
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("No transformation was performed for class '" + className + "'");
				}
			}
		} else {
			transformedBytes = null;
		}
		return transformedBytes;
	}
}