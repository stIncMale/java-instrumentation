package com.gl.vn.me.ko.sample.instrumentation.example.util.javassist;

import java.util.concurrent.locks.ReentrantLock;
import javassist.ClassPool;

/**
 * Intended to provide a convenient way of creating, reusing and recreating an instance of {@code javassist.ClassPool}.
 * Uses lazy initialization, so an instance of {@code javassist.ClassPool} will not be created till the moment it's needed.
 * <p/>
 * The class is thread safe.
 * 
 * @author Valentin Kovalenko
 */
final class ClassPoolManager {
	private final static ReentrantLock lock = new ReentrantLock();
	private static volatile ClassPool classPool = null;

	private ClassPoolManager() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}

	/**
	 * Returns an instance of {@code javassist.ClassPool}. Always return the same object until {@link ClassPoolManager#recreateClassPool() recreateClassPool()} method is called.
	 * 
	 * @return
	 *         An instance of {@code javassist.ClassPool}
	 * @see ClassPoolManager#recreateClassPool()
	 */
	/*
	 * Uses double-check idiom
	 */
	final static ClassPool getClassPool() {
		ClassPool result = classPool;
		if (result == null) {// first check
			lock.lock();
			try {
				result = classPool;
				if (result == null) {// second check
					result = new ClassPool();
					result.appendSystemPath();
					ClassPool.doPruning = false;
					ClassPool.releaseUnmodifiedClassFile = true;
					classPool = result;
				}
			}
			finally {
				lock.unlock();
			}
		}
		return result;
	}

	/**
	 * Invocation of the method guarantees creation of a new instance of {@code javassist.ClassPool} the next time method {@link ClassPoolManager#getClassPool() getClassPool()} will be called.
	 * 
	 * @see ClassPoolManager#getClassPool()
	 */
	final static void recreateClassPool() {
		lock.lock();
		try {
			classPool = null;
		}
		finally {
			lock.unlock();
		}
	}
}