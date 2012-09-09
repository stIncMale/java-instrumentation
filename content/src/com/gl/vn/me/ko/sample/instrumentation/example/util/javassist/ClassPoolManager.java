package com.gl.vn.me.ko.sample.instrumentation.example.util.javassist;

import java.util.LinkedHashSet;
import java.util.Set;
import javassist.ClassPath;
import javassist.ClassPool;

/**
 * Provides a convenient way of creating and reusing an instance of {@code javassist.ClassPool}.
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
final class ClassPoolManager {
	private final static Object LOCK;// modifications of 'classPool', 'preserveClassPath', 'prependedClassPathElements', 'appendedClassPathElements' must be synchronized via this object
	private final static Set<ClassPath> prependedClassPathElements;// an order of elements is important
	private final static Set<ClassPath> appendedClassPathElements;// an order of elements is important
	private static boolean preserveClassPath;// all reads and writes are inside synchronized blocks, so volatile modifier isn't required
	private static volatile ClassPool classPool;// must be volatile because there are reads of the field outside synchronized blocks (see getClassPool() method)
	static {
		LOCK = new Object();
		prependedClassPathElements = new LinkedHashSet<ClassPath>();// an order of elements is important
		appendedClassPathElements = new LinkedHashSet<ClassPath>();// an order of elements is important
		preserveClassPath = false;
		classPool = null;
		ClassPool.doPruning = false;
		ClassPool.releaseUnmodifiedClassFile = true;
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
	final static boolean appendClassPath(final ClassPath classPath) {
		final ClassPool classPool = ClassPoolManager.getClassPool();
		final boolean result = modifyClassPath(classPool, classPath, true);
		return result;
	}

	/**
	 * Returns an instance of {@code javassist.ClassPool}. Always returns the same object until {@link ClassPoolManager#recreateClassPool(boolean) recreateClassPool(boolean)} method is called.
	 * 
	 * @return
	 *         An instance of {@code javassist.ClassPool}
	 * @see ClassPoolManager#recreateClassPool()
	 */
	final static ClassPool getClassPool() {
		ClassPool result = classPool;
		if (result == null) {
			synchronized (LOCK) {
				result = classPool;
				if (result == null) {
					initClassPool();
					result = classPool;
				}
			}
		}
		return result;
	}

	/*
	 * Must be only invoked inside 'synchronized (LOCK)' block
	 */
	private final static void initClassPool() {
		final ClassPool classPool = new ClassPool();
		classPool.appendSystemPath();
		if (preserveClassPath) {
			final Set<ClassPath> prependedClassPathElementsCopy = new LinkedHashSet<ClassPath>(prependedClassPathElements);
			prependedClassPathElements.clear();
			for (final ClassPath classPath : prependedClassPathElementsCopy) {
				modifyClassPath(classPool, classPath, false);
			}
			final Set<ClassPath> appendedClassPathElementsCopy = new LinkedHashSet<ClassPath>(appendedClassPathElements);
			appendedClassPathElements.clear();
			for (final ClassPath classPath : appendedClassPathElementsCopy) {
				modifyClassPath(classPool, classPath, true);
			}
		}
		ClassPoolManager.classPool = classPool;
	}

	private final static boolean modifyClassPath(final ClassPool classPool, final ClassPath classPath, final boolean append) {
		final boolean result;
		final Set<ClassPath> classPathElementsToModify = append ? appendedClassPathElements : prependedClassPathElements;
		synchronized (LOCK) {
			if (!classPathElementsToModify.contains(classPath)) {
				if (append) {
					classPool.appendClassPath(classPath);
				} else {
					classPool.insertClassPath(classPath);
				}
				classPathElementsToModify.add(classPath);
				result = true;
			} else {
				result = false;
			}
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
	final static boolean prependClassPath(final ClassPath classPath) {
		final ClassPool classPool = getClassPool();
		final boolean result = modifyClassPath(classPool, classPath, false);
		return result;
	}

	/**
	 * Invocation of the method guarantees creation of a new instance of {@code javassist.ClassPool} the next time method {@link ClassPoolManager#getClassPool() getClassPool()} will be called.
	 * 
	 * @param preserveClassPath
	 *            Specifies if prepended/appended class path elements should be presented in a new instance of {@code javassist.ClassPool}
	 * @see #getClassPool()
	 */
	final static void recreateClassPool(final boolean preserveClassPath) {
		synchronized (LOCK) {
			classPool = null;
			ClassPoolManager.preserveClassPath = preserveClassPath;
		}
	}

	private ClassPoolManager() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}