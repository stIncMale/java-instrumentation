package com.gl.vn.me.ko.sample.instrumentation.example.util.javassist;

import java.io.IOException;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Provides convenient API to work with Javassist framework.
 * It also facilitates the creation/recreation of an underlying {@code javassist.ClassPool} object.
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 */
/*
 * This class uses javassist.ClassPool. Although it's not specified in the API specification,
 * according to javassist.ClassPool source code, it seems like the class is thread-safe.
 * So JavassistEnvironment is written with the assumption that javassist.ClassPool is thread-safe.
 */
public final class JavassistEnvironment {
	public final static char PACKAGE_SEPARATOR_CHAR;// example: java.lang.Class
	public final static char INTERNAL_PACKAGE_SEPARATOR_CHAR;// example: java/lang/Class
	static {
		PACKAGE_SEPARATOR_CHAR = '.';
		INTERNAL_PACKAGE_SEPARATOR_CHAR = '/';
	}

	/**
	 * Appends a {@code javassist.ClassPath} object to the end of the search path of the {@code javassist.ClassPool} object.
	 * 
	 * @param classPath
	 *            The class path to append
	 * @return
	 *         <ul>
	 *         <li>true if specified class path was appended</li>
	 *         <li>false if specified class path wasn't appended because it was appended previously</li>
	 *         </ul>
	 */
	public final static boolean appendClassPath(final ClassPath classPath) {
		if (classPath == null) {
			throw new NullPointerException("The argument 'classPath' is null");
		}
		final boolean result = ClassPoolManager.appendClassPath(classPath);
		return result;
	}

	/**
	 * Converts provided class object to a class file.
	 * Once this method is called, the class object becomes frozen
	 * and further modifications are not possible till the defrost procedure.
	 * 
	 * @param ctClass
	 *            Class object to convert
	 * @return
	 *         The contents of the class file
	 * @throws CannotCompileException
	 * @see {@code javassist.CtClass.isFrozen()}
	 * @see {@code javassist.CtClass.defrost()}
	 */
	public final static byte[] getCtBytes(final CtClass ctClass) throws CannotCompileException {
		if (ctClass == null) {
			throw new NullPointerException("The argument 'ctClass' is null");
		}
		final byte[] bytes;
		ctClass.rebuildClassFile();
		try {
			bytes = ctClass.toBytecode();
		} catch (final CannotCompileException cce) {
			throw new CannotCompileException("Can't compile class '" + ctClass.getName() + "'", cce);
		} catch (final IOException ioe) {
			throw new RuntimeException("Exception occured while trying to get bytecode from class '" + ctClass.getName() + "'", ioe);
		}
		return bytes;
	}

	/**
	 * Reads a class file from the source and returns a reference to the {@code javassist.CtClass} object representing that class file.
	 * If that class file has been already read from the same {@code javassist.ClassPool},
	 * this method returns a reference to the {@code javassist.CtClass} created when that class file was read at the first time.
	 * <p/>
	 * If class name ends with "[]", then this method returns a {@code javassist.CtClass} object for that array type. To obtain an nested class, use "$" instead of "." for separating the enclosing
	 * class name and the inner class name.
	 * 
	 * @param className
	 *            A fully-qualified class name
	 * @return
	 *         A {@code javassist.CtClass} object representing specified class
	 * @throws NotFoundException
	 */
	public final static CtClass getCtClass(final String className) throws NotFoundException {
		if (className == null) {
			throw new NullPointerException("The argument 'className' is null");
		}
		final ClassPool classPool = ClassPoolManager.getClassPool();
		final String classNameToSearchInClassPool = className.replace(INTERNAL_PACKAGE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
		final CtClass result;
		try {
			result = classPool.get(classNameToSearchInClassPool);
		} catch (final NotFoundException e) {
			throw new NotFoundException("Can't find class '" + className + "' in the class pool '" + classPool + "'", e);
		}
		return result;
	}

	/**
	 * Reads a class file from the source and returns a reference to the CtClass object representing that class file.
	 * This method is equivalent to {@link JavassistEnvironment#getCtClass(String) getCtClass(String)} except that it returns null when a class file is not found and it never throws an exception.
	 * 
	 * @param className
	 *            A fully-qualified class name
	 * @return
	 *         A {@code javassist.CtClass} object representing specified class or null
	 */
	public final static CtClass getCtClassOrNull(final String className) {
		if (className == null) {
			throw new NullPointerException("The argument 'className' is null");
		}
		final ClassPool classPool = ClassPoolManager.getClassPool();
		final String classNameToSearchInClassPool = className.replace(INTERNAL_PACKAGE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
		final CtClass result = classPool.getOrNull(classNameToSearchInClassPool);
		if (result != null) {
			result.stopPruning(true);
		}
		return result;
	}

	/**
	 * Prepends a {@code javassist.ClassPath} object to the head of the search path of the {@code javassist.ClassPool} object.
	 * 
	 * @param classPath
	 *            The class path to append
	 * @return
	 *         <ul>
	 *         <li>true if specified class path was prepended</li>
	 *         <li>false if specified class path wasn't prepended because it was prepended previously</li>
	 *         </ul>
	 */
	public final static boolean prependClassPath(final ClassPath classPath) {
		if (classPath == null) {
			throw new NullPointerException("The argument 'classPath' is null");
		}
		final boolean result = ClassPoolManager.prependClassPath(classPath);
		return result;
	}

	/**
	 * Recreates an instance of {@code javassist.ClassPool} used by {@code JavassistEnvironment}.
	 * 
	 * @param preserveClassPath
	 *            Specifies if class path elements prepended/appended via methods {@link JavassistEnvironment#prependClassPath(ClassPath) prependClassPath(ClassPath)},
	 *            {@link JavassistEnvironment#appendClassPath(ClassPath) appendClassPath(ClassPath)} must be presented in a new instance of {@code javassist.ClassPool}
	 */
	public final static void renew(final boolean preserveClassPath) {
		ClassPoolManager.recreateClassPool(preserveClassPath);
	}

	private JavassistEnvironment() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}