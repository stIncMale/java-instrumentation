package com.gl.vn.me.ko.sample.instrumentation.example.util.javassist;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;

/**
 * This class is not synchronized.
 */
public final class JavassistEnvironment {
	private final static Logger logger = Logger.getLogger(JavassistEnvironment.class);
	public final static String PACKAGE_SEPARATOR = ".";// example: java.lang.Class - binary name as specified in "The Java Language Specification"
	public final static String INTERNAL_PACKAGE_SEPARATOR = "/";// example: java/lang/Class - binary name as specified in "The Java Virtual Machine Specification"
	private final static String INTERNAL_ARCHIVE_PATH_SEPARATOR = "!";// example: rt.jar!/java/lang/Class.class
	private final static String CLASS_EXTENSION_WITH_SEPARATOR = ".class";
	private final static String URL_ENCODING = "UTF-8";
	private final static Pattern URL_PROTOCOL_PATTERN = Pattern.compile(".*?:/+?");// .*? and /+? are reluctant quantifiers, the most appropriate in this particular use case
	private final static Set<String> appendedClassPathElements = new LinkedHashSet<String>();// an order can be important

	private final static synchronized void appendClassPathToClassPool(final String classPath) {
		if (!appendedClassPathElements.contains(classPath)) {
			final ClassPool classPool = ClassPoolManager.getClassPool();
			try {
				classPool.appendClassPath(classPath);
				appendedClassPathElements.add(classPath);
			} catch (final NotFoundException e) {
				final String msg = "Can't append classpath '" + classPath + "' to class pool '" + classPool + "'";
				final RuntimeException wrapperException = new RuntimeException(msg, e);
				final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
				logger.error(msgWithStackTrace);
				throw wrapperException;
			}
		}
	}

	public final static String getClassPath(final String className, final ClassLoader classLoader) {
		final String classFileName = className + CLASS_EXTENSION_WITH_SEPARATOR;
		final URL classResource = classLoader.getResource(classFileName);
		if (classResource == null) {
			final String msg = "Can't find resource with name '" + classFileName + "' via class loader '" + classLoader + "'";
			final RuntimeException e = new RuntimeException(msg);
			final String msgWithStackTrace = LogHelper.throwableToString(e);
			logger.error(msgWithStackTrace);
			throw e;
		}
		final String encodedClassLocation = classResource.getPath();
		final String classLocation;
		try {
			classLocation = URLDecoder.decode(encodedClassLocation, URL_ENCODING);
		} catch (final UnsupportedEncodingException e) {
			final String msg = "Can't decode string '" + encodedClassLocation + "' with the '" + URL_ENCODING + "' encoding";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
		final String[] classLocationParts = URL_PROTOCOL_PATTERN.split(classLocation, 2);
		final String classPathWithPossibleArchivePath = classLocationParts.length > 1 ? classLocationParts[1] : classLocation;
		final String classPath = classPathWithPossibleArchivePath.substring(0, classPathWithPossibleArchivePath.indexOf(INTERNAL_ARCHIVE_PATH_SEPARATOR));
		return classPath;
	}

	public final static byte[] getCtBytes(final CtClass ctClass) {
		final byte[] bytes;
		ctClass.rebuildClassFile();
		try {
			bytes = ctClass.toBytecode();
		} catch (final CannotCompileException e) {
			final String msg = "Can't compile class '" + ctClass.getName() + "'";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		} catch (final IOException e) {
			final String msg = "Exception occured while trying to get bytecode from class '" + ctClass.getName() + "'";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
		return bytes;
	}

	public final static CtClass getCtClass(final String className) {
		if (className == null) {
			final String msg = "The argument 'className' is null";
			final RuntimeException e = new NullPointerException(msg);
			final String msgWithStackTrace = LogHelper.throwableToString(e);
			logger.error(msgWithStackTrace);
			throw e;
		}
		final CtClass result = getCtClassFromClassPool(className, null);
		return result;
	}

	public final static CtClass getCtClass(final String className, final String classPath) {
		if (className == null) {
			final String msg = "The first argument 'className' is null";
			final RuntimeException e = new NullPointerException(msg);
			final String msgWithStackTrace = LogHelper.throwableToString(e);
			logger.error(msgWithStackTrace);
			throw e;
		}
		if (classPath == null) {
			final String msg = "The second argument 'classPath' is null";
			final RuntimeException e = new NullPointerException(msg);
			final String msgWithStackTrace = LogHelper.throwableToString(e);
			logger.error(msgWithStackTrace);
			throw e;
		}
		final CtClass result = getCtClassFromClassPool(className, classPath);
		return result;
	}

	private final static CtClass getCtClassFromClassPool(final String className, final String classPath) {
		final ClassPool classPool = ClassPoolManager.getClassPool();
		final String classNameToSearchInClassPool = className.replace(INTERNAL_PACKAGE_SEPARATOR, PACKAGE_SEPARATOR);
		if (classPath != null) {
			appendClassPathToClassPool(classPath);
		}
		final CtClass result;
		try {
			result = classPool.get(classNameToSearchInClassPool);
		} catch (final NotFoundException e) {
			final String msg = "Can't get class '" + className + "' with specified class path '" + classPath + "' from class pool";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
		result.stopPruning(true);
		return result;
	}

	public final static CtClass getCtClassOrNull(final String className) {
		if (className == null) {
			final String msg = "The first argument 'className' is null";
			final RuntimeException e = new NullPointerException(msg);
			final String msgWithStackTrace = LogHelper.throwableToString(e);
			logger.error(msgWithStackTrace);
			throw e;
		}
		final ClassPool classPool = ClassPoolManager.getClassPool();
		final String classNameToSearchInClassPool = className.replace(INTERNAL_PACKAGE_SEPARATOR, PACKAGE_SEPARATOR);
		final CtClass result = classPool.getOrNull(classNameToSearchInClassPool);
		if (result != null) {
			result.stopPruning(true);
		}
		return result;
	}

	public final static CtMethod getCtMethod(final CtClass ctClass, final String methodName, final String methodDescriptor) {
		final CtMethod ctMethod;
		try {
			ctMethod = ctClass.getMethod(methodName, methodDescriptor);
		} catch (final NotFoundException e) {
			final String msg = "Can't find method with name '" + methodName + "' and descriptor '" + methodDescriptor + "' in the class '" + ctClass.getName() + "'";
			final RuntimeException wrapperException = new RuntimeException(msg, e);
			final String msgWithStackTrace = LogHelper.throwableToString(wrapperException);
			logger.error(msgWithStackTrace);
			throw wrapperException;
		}
		return ctMethod;
	}

	public final static synchronized void renew(final boolean preserveClassPath) {
		ClassPoolManager.recreateClassPool();
		if (preserveClassPath) {
			final Set<String> classPathElements = new LinkedHashSet<String>(appendedClassPathElements);
			appendedClassPathElements.clear();
			for (final String classPathElement : classPathElements) {
				appendClassPathToClassPool(classPathElement);
			}
		} else {
			appendedClassPathElements.clear();
		}
	}

	private JavassistEnvironment() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}