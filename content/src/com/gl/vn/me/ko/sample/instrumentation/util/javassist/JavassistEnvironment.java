package com.gl.vn.me.ko.sample.instrumentation.util.javassist;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * Provides API to work with Javassist framework.
 * It also facilitates the creation/recreation of an underlying {@link javassist.ClassPool} object.
 * <p/>
 * Note that it doesn't support hierarchy of class loaders. It means that the class is not applicable for cases when classes with the same name are loaded with different class loaders (don't be
 * disoriented by the method {@link #getCtClass(ClassLoader, String)}).
 * <p/>
 * The class provides an ability to obtain an exclusive access to its methods, see {@link #lock()} and {@link #unlock()} methods.
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
	private final static char PACKAGE_SEPARATOR_CHAR;// example: java.lang.Class
	private final static char INTERNAL_PACKAGE_SEPARATOR_CHAR;// example: java/lang/Class
	private final static Lock sharedLock;
	private final static Lock exclusiveLock;
	static {
		PACKAGE_SEPARATOR_CHAR = '.';
		INTERNAL_PACKAGE_SEPARATOR_CHAR = '/';
		final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);// there is no preferences between read and write locks in this case, therefore fair ordering policy should be used
		sharedLock = rwLock.readLock();
		exclusiveLock = rwLock.writeLock();
	}

	/**
	 * Appends a {@link javassist.ClassPath} object to the end of the search path of the underlying {@link javassist.ClassPool} object.
	 * 
	 * @param classPath
	 *            The class path to append. Must be not {@code null}.
	 * @return
	 *         <ul>
	 *         <li>{@code true} if specified class path was appended.</li>
	 *         <li>{@code false} if specified class path wasn't appended because it was appended previously.</li>
	 *         </ul>
	 */
	public final static boolean appendClassPath(final ClassPath classPath) {
		if (classPath == null) {
			throw new NullPointerException("The argument 'classPath' is null");
		}
		final boolean result;
		sharedLock.lock();
		try {
			result = ClassPoolManager.appendClassPath(classPath);
		} finally {
			sharedLock.unlock();
		}
		return result;
	}

	/**
	 * Converts provided class object to a class file.
	 * Once this method is called, the class object becomes frozen and further modifications are not possible till the defrost procedure.
	 * 
	 * @param ctClass
	 *            Class object to convert. Must be not {@code null}.
	 * @return
	 *         The contents of the class file.
	 * @throws javassist.CannotCompileException
	 *             When bytecode transformation has failed.
	 * @see javassist.CtClass#isFrozen()
	 * @see javassist.CtClass#defrost()
	 */
	public final static byte[] getCtBytes(final CtClass ctClass) throws CannotCompileException {
		if (ctClass == null) {
			throw new NullPointerException("The argument 'ctClass' is null");
		}
		final byte[] bytes;
		sharedLock.lock();
		try {
			ctClass.rebuildClassFile();
			try {
				bytes = ctClass.toBytecode();
			} catch (final CannotCompileException e) {
				throw new CannotCompileException("Can't compile class '" + ctClass.getName() + "'", e);
			} catch (final IOException e) {
				throw new RuntimeException("Exception occured while trying to get bytecode from class '" + ctClass.getName() + "'", e);
			}
		} finally {
			sharedLock.unlock();
		}
		return bytes;
	}

	/**
	 * Acts just like {@link #getCtClass(ClassLoader, String)} considering that supplied instance of {@link java.lang.Class} have information about defining class loader and class name.
	 * 
	 * @param clazz
	 *            Class to search. Must be not {@code null}.
	 * @return
	 *         A {@link javassist.CtClass} object representing specified class.
	 * @throws javassist.NotFoundException
	 *             When the requested class can't be find in the underlying {@link javassist.ClassPool} object.
	 * @see #getCtClass(ClassLoader, String)
	 */
	public final static CtClass getCtClass(final Class<?> clazz) throws NotFoundException {
		if (clazz == null) {
			throw new NullPointerException("The argument 'clazz' is null");
		}
		final String className = clazz.getName();
		final String classNameToSearchInClassPool = className.replace(INTERNAL_PACKAGE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
		final ClassLoader classLoader = clazz.getClassLoader();
		final CtClass result;
		sharedLock.lock();
		try {
			result = getCtClassUnsync(classLoader, classNameToSearchInClassPool);
			processCtClassBeforeReturnUnsync(result);
		} finally {
			sharedLock.unlock();
		}
		return result;
	}

	/**
	 * Reads a class file from the source and returns a reference to the {@link javassist.CtClass} object representing that class file.
	 * This method is equivalent to {@link #getCtClass(String)} except that it tries to get {@link javassist.CtClass} object using known class path (see {@link #getCtClassOrNull(String)}) and in case
	 * of failure it appends class path obtained from the provided class loader and tries to get the class once again (see {@link #getCtClass(String)}). In other words the method does something like<br/>
	 * <blockquote>
	 * 
	 * <pre>
	 * {
	 * 	CtClass result;
	 * 	if (classLoader == null) {// bootstrap class loader
	 * 		result = JavassistEnvironment.getCtClass(className);
	 * 	} else {
	 * 		// try to get CtClass using known class path
	 * 		result = JavassistEnvironment.getCtClassOrNull(className);
	 * 		if (result == null) {// class wasn't found in the known class path
	 * 			JavassistEnvironment.appendClassPath(new LoaderClassPath(classLoader));
	 * 			result = JavassistEnvironment.getCtClassUnsync(className);
	 * 		}
	 * 	}
	 * 	return result;
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param classLoader
	 *            The defining loader of the class (should be {@code null} if the bootstrap loader).
	 * @param className
	 *            A fully-qualified class name. Must be not {@code null}.
	 * @return
	 *         A {@link javassist.CtClass} object representing specified class.
	 * @throws javassist.NotFoundException
	 *             When the requested class can't be find in the underlying {@link javassist.ClassPool} object.
	 * @see #getCtClass(String)
	 */
	public final static CtClass getCtClass(final ClassLoader classLoader, final String className) throws NotFoundException {
		if (className == null) {
			throw new NullPointerException("The second argument 'className' is null");
		}
		final String classNameToSearchInClassPool = className.replace(INTERNAL_PACKAGE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
		final CtClass result;
		sharedLock.lock();
		try {
			result = getCtClassUnsync(classLoader, classNameToSearchInClassPool);
			processCtClassBeforeReturnUnsync(result);
		} finally {
			sharedLock.unlock();
		}
		return result;
	}

	/**
	 * Reads a class file from the source and returns a reference to the {@link javassist.CtClass} object representing that class file.
	 * If that class file has been already read from the same {@link javassist.ClassPool} object,
	 * this method returns a reference to the {@link javassist.CtClass} created when that class file was read at the first time.
	 * <p/>
	 * If class name ends with {@code "[]"}, then this method returns a {@link javassist.CtClass} object for that array type. To obtain an nested class, use {@code "$"} instead of {@code "."} for
	 * separating the enclosing class name and the nested class name.
	 * 
	 * @param className
	 *            A fully qualified class name (for example {@code "java.lang.Object"} or {@code "java/lang/Object"}). Must be not {@code null}.
	 * @return
	 *         A {@link javassist.CtClass} object representing specified class.
	 * @throws javassist.NotFoundException
	 *             When the requested class can't be find in the underlying {@link javassist.ClassPool} object.
	 */
	public final static CtClass getCtClass(final String className) throws NotFoundException {
		if (className == null) {
			throw new NullPointerException("The argument 'className' is null");
		}
		final String classNameToSearchInClassPool = className.replace(INTERNAL_PACKAGE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
		final CtClass result;
		sharedLock.lock();
		try {
			result = getCtClassUnsync(classNameToSearchInClassPool);
			processCtClassBeforeReturnUnsync(result);
		} finally {
			sharedLock.unlock();
		}
		return result;
	}

	/**
	 * Reads a class file from the source and returns a reference to the {@link javassist.CtClass} object representing that class file.
	 * This method is equivalent to {@link #getCtClass(String)} except that it returns {@code null} when a class file is not found and it never throws an exception.
	 * 
	 * @param className
	 *            A fully-qualified class name. Must be not {@code null}.
	 * @return
	 *         A {@link javassist.CtClass} object representing specified class or {@code null}.
	 * @see #getCtClass(String)
	 */
	public final static CtClass getCtClassOrNull(final String className) {
		if (className == null) {
			throw new NullPointerException("The argument 'className' is null");
		}
		final String classNameToSearchInClassPool = className.replace(INTERNAL_PACKAGE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
		final CtClass result;
		sharedLock.lock();
		try {
			result = getCtClassOrNullUnsync(classNameToSearchInClassPool);
			processCtClassBeforeReturnUnsync(result);
		} finally {
			sharedLock.unlock();
		}
		return result;
	}

	/**
	 * Invocations must be synchronized using {@link #sharedLock} object.
	 */
	private final static CtClass getCtClassOrNullUnsync(final String className) {
		final ClassPool classPool = ClassPoolManager.getClassPool();
		final CtClass result = classPool.getOrNull(className);
		return result;
	}

	/**
	 * Invocations must be synchronized using {@link #sharedLock} object.
	 */
	private final static CtClass getCtClassUnsync(final ClassLoader classLoader, final String className) throws NotFoundException {
		CtClass result;
		if (classLoader == null) {// bootstrap class loader
			result = getCtClassUnsync(className);
		} else {
			result = getCtClassOrNullUnsync(className);// try to get CtClass using known class path
			if (result == null) {// class wasn't found in the known class path
				final ClassPath classPath = new LoaderClassPath(classLoader);
				appendClassPath(classPath);
				result = getCtClassUnsync(className);
			}
		}
		return result;
	}

	/**
	 * Invocations must be synchronized using {@link #sharedLock} object.
	 */
	private final static CtClass getCtClassUnsync(final String className) throws NotFoundException {
		final ClassPool classPool = ClassPoolManager.getClassPool();
		final CtClass result;
		try {
			result = classPool.get(className);
		} catch (final NotFoundException e) {
			throw new NotFoundException("Can't find class '" + className + "' in the class pool '" + classPool + "'", e);
		}
		return result;
	}

	/**
	 * Provides an ability to control access to {@link JavassistEnvironment} class methods.
	 * Acquires the lock if the lock aren't held by another thread and returns immediately. The lock is reentrant so if the current thread already holds the lock then the hold count is incremented by
	 * one and the method returns immediately.
	 * <p/>
	 * If one need to obtain exclusive access to {@link JavassistEnvironment}, one can lock the environment, do required actions and unlock the environment. The following idiom should be used
	 * <blockquote>
	 * 
	 * <pre>
	 * JavassistEnvironment.lock();
	 * try {
	 * 	// exclusive work with JavassistEnvironment
	 * } finally {
	 * 	JavassistEnvironment.unlock();
	 * }
	 * </pre>
	 * 
	 * </blockquote> If a thread tries to invoke a method (except {@link #unlock()}) of {@link JavassistEnvironment} class (even without trying to acquire the lock) and the lock is held by another
	 * thread, then current thread becomes disabled for thread scheduling purposes and lies dormant until lock has been released.
	 * 
	 * @see #unlock()
	 */
	public final static void lock() {
		exclusiveLock.lock();
	}

	/**
	 * Prepends a {@link javassist.ClassPath} object to the head of the search path of the underlying {@link javassist.ClassPool} object.
	 * 
	 * @param classPath
	 *            The class path to append. Must be not {@code null}.
	 * @return
	 *         <ul>
	 *         <li>{@code true} if specified class path was prepended.</li>
	 *         <li>{@code false} if specified class path wasn't prepended because it was prepended previously.</li>
	 *         </ul>
	 */
	public final static boolean prependClassPath(final ClassPath classPath) {
		if (classPath == null) {
			throw new NullPointerException("The argument 'classPath' is null");
		}
		final boolean result;
		sharedLock.lock();
		try {
			result = ClassPoolManager.prependClassPath(classPath);
		} finally {
			sharedLock.unlock();
		}
		return result;
	}

	/**
	 * Invocations must be synchronized using {@link #sharedLock} object.
	 */
	private final static void processCtClassBeforeReturnUnsync(final CtClass ctClass) {
		if (ctClass != null) {
			ctClass.stopPruning(true);
		}
	}

	/**
	 * Recreates the underlying instance of {@link javassist.ClassPool} that is used by {@link JavassistEnvironment}.
	 * 
	 * @param preserveClassPath
	 *            Specifies if class path elements prepended/appended via methods {@link #prependClassPath(ClassPath)}, {@link #appendClassPath(ClassPath)} must be presented
	 *            in a new instance of {@link javassist.ClassPool}.
	 */
	public final static void renew(final boolean preserveClassPath) {
		sharedLock.lock();
		try {
			ClassPoolManager.recreateClassPool(preserveClassPath);
		} finally {
			sharedLock.unlock();
		}
	}

	/**
	 * Provides an ability to control access to {@link JavassistEnvironment} class methods.
	 * Attempts to release the lock. If the current thread is the holder of the lock then the hold count is decremented. If the hold count is now zero then the lock is released.
	 * If the current thread is not the holder of this lock then {@link java.lang.IllegalMonitorStateException} is thrown.
	 * 
	 * @see #lock()
	 */
	public final static void unlock() {
		exclusiveLock.unlock();
	}

	private JavassistEnvironment() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}