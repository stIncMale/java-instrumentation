package com.gl.vn.me.ko.sample.instrumentation.util.javassist;

import java.util.LinkedHashSet;
import java.util.Set;
import javassist.ClassPath;
import javassist.ClassPool;

/**
 * Provides a convenient API of creating and reusing an instance of {@link javassist.ClassPool}.
 * <p/>
 * Note that it doesn't support hierarchy of class loaders. It means that the class is not applicable for cases when classes with the same name are loaded with different class loaders.
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
final class ClassPoolManager {
	private final static Object LOCK;// modifications of 'classPool', 'preserveClassPath', 'prependedClassPathElements', 'appendedClassPathElements' must be synchronized via this object
	private final static Set<ClassPath> PREPENDED_CLASSPATH_ELEMENTS;
	private final static Set<ClassPath> APPENDED_CLASSPATH_ELEMENTS;
	private static boolean preserveClassPath;// all reads and writes are inside synchronized blocks, so volatile modifier isn't required
	private static volatile ClassPool classPool;// must be volatile because there are reads of the field outside synchronized blocks (see getClassPool() method)
	static {
		LOCK = new Object();
		PREPENDED_CLASSPATH_ELEMENTS = new LinkedHashSet<ClassPath>();// an order of elements is important
		APPENDED_CLASSPATH_ELEMENTS = new LinkedHashSet<ClassPath>();// an order of elements is important
		preserveClassPath = false;
		classPool = null;
		ClassPool.doPruning = false;
		ClassPool.releaseUnmodifiedClassFile = true;
	}

	/**
	 * Appends {@code classPath} to the end of the search path of the {@link javassist.ClassPool} object.
	 * 
	 * @param classPath
	 *            The class path to append. Must be not {@code null}.
	 * @return
	 *         <ul>
	 *         <li>{@code true} if specified class path was appended.</li>
	 *         <li>{@code false} if specified class path wasn't appended because it was appended previously.</li>
	 *         </ul>
	 */
	final static boolean appendClassPath(final ClassPath classPath) {
		final ClassPool classPool = ClassPoolManager.getClassPool();
		final boolean result = modifyClassPath(classPool, classPath, true);
		return result;
	}

	/**
	 * Returns an instance of {@link javassist.ClassPool}. Always returns the same object until {@link #recreateClassPool(boolean)} method is called.
	 * 
	 * @return
	 *         An instance of {@link javassist.ClassPool}.
	 * @see #recreateClassPool(boolean)
	 */
	final static ClassPool getClassPool() {
		ClassPool result = classPool;
		if (result == null) {
			synchronized (LOCK) {
				result = classPool;
				if (result == null) {
					initClassPoolUnsync();
					result = classPool;
				}
			}
		}
		return result;
	}

	/**
	 * Must be only invoked inside {@code synchronized (LOCK)} block.
	 */
	private final static void initClassPoolUnsync() {
		final ClassPool classPool = new ClassPool(true);
		if (preserveClassPath) {
			final Set<ClassPath> prependedClassPathElementsCopy = new LinkedHashSet<ClassPath>(PREPENDED_CLASSPATH_ELEMENTS);
			PREPENDED_CLASSPATH_ELEMENTS.clear();
			for (final ClassPath classPath : prependedClassPathElementsCopy) {
				modifyClassPath(classPool, classPath, false);
			}
			final Set<ClassPath> appendedClassPathElementsCopy = new LinkedHashSet<ClassPath>(APPENDED_CLASSPATH_ELEMENTS);
			APPENDED_CLASSPATH_ELEMENTS.clear();
			for (final ClassPath classPath : appendedClassPathElementsCopy) {
				modifyClassPath(classPool, classPath, true);
			}
		}
		ClassPoolManager.classPool = classPool;
	}

	private final static boolean modifyClassPath(final ClassPool classPool, final ClassPath classPath, final boolean append) {
		final boolean result;
		final Set<ClassPath> classPathElementsToModify = append ? APPENDED_CLASSPATH_ELEMENTS : PREPENDED_CLASSPATH_ELEMENTS;
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
	 * Prepends {@code classPath} to the head of the search path of the {@link javassist.ClassPool} object.
	 * 
	 * @param classPath
	 *            The class path to prepend. Must be not {@code null}.
	 * @return
	 *         <ul>
	 *         <li>{@code true} if specified class path was prepended.</li>
	 *         <li>{@code false} if specified class path wasn't prepended because it was prepended previously.</li>
	 *         </ul>
	 */
	final static boolean prependClassPath(final ClassPath classPath) {
		final ClassPool classPool = getClassPool();
		final boolean result = modifyClassPath(classPool, classPath, false);
		return result;
	}

	/**
	 * Invocation of the method guarantees creation of a new instance of {@link javassist.ClassPool} the next time method {@link #getClassPool()} will be called.
	 * 
	 * @param preserveClassPath
	 *            Specifies if prepended/appended class path elements should be presented in a new instance of {@link javassist.ClassPool}.
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