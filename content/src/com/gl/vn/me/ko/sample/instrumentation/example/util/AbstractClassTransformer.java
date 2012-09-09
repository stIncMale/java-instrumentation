package com.gl.vn.me.ko.sample.instrumentation.example.util;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import javassist.ClassPath;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.example.util.javassist.JavassistEnvironment;

/**
 * Provides an ability to transform class files. See methods {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[])} and {@link #doTransform(CtClass)} for details.
 * <p/>
 * Derived classes must be thread save.
 * <p/>
 * Instantiability: allowed from inside the derived class.<br/>
 * Mutability: this class itself has nothing that can cause mutability. Depends on the implementation of derived class.<br/>
 * Thread safety: this class itself has nothing that can cause thread unsafety. Implementations of derived classes must be thread-safe.<br/>
 * Cloneability: cloning is not forbidden. Depends on the implementation of derived class.<br/>
 * Serializability: serialization is not forbidden. Depends on the implementation of derived class.
 * 
 * @author Valentin Kovalenko
 */
public abstract class AbstractClassTransformer implements ClassFileTransformer {
	private final static Logger LOGGER;
	static {
		LOGGER = Logger.getLogger(AbstractClassTransformer.class);
	};

	/**
	 * Performs a part of initialization that belongs to {@link AbstractClassTransformer} class.
	 */
	protected AbstractClassTransformer() {
	}

	/**
	 * At runtime any class loaded in JVM is uniquely identified by class loader that have loaded the class and fully qualified name of the class.
	 * Implementation of the method must decide whether the class can be transformed or not. If the method return {@code true}, transformer will try to obtain corresponded {@code javassist.CtClass}
	 * object from the {@code javassist.ClassPool} and if succeed the method {@link #doTransform(CtClass)} will be called.
	 * 
	 * @param classLoader
	 *            The defining loader of the class to be transformed, may be null if the bootstrap loader.
	 * @param className
	 *            The name of the class in the internal form of fully qualified class and interface names as defined in "The Java Virtual Machine Specification". For example:
	 *            {@code "java/lang/Object"}.
	 * @return
	 *         <ul>
	 *         <li>{@code true} if the class should be transformed.</li>
	 *         <li>{@code false} if the class must be skipped without any transformations.</li>
	 *         </ul>
	 * @see #doTransform(CtClass)
	 */
	protected abstract boolean acceptClassForTransformation(ClassLoader classLoader, String className);

	/**
	 * The implementation may transform the supplied class and return an array of transformed bytes in class file format as specified in "The Java Virtual Machine Specification".
	 * This method is called from method {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[]) transform(ClassLoader, String, Class, ProtectionDomain,
	 * byte[])} for any invocation of the method except when class can't pass the filter (see {@link #acceptClassForTransformation(ClassLoader, String)}) or {@code javassist.CtClass} object can't be
	 * obtained from the {@code javassist.ClassPool}.
	 * <p/>
	 * Remember that once bytes were obtained from {@code javassist.CtClass} object (see {@link JavassistEnvironment#getCtBytes(CtClass) getCtBytes(CtClass)} method), the object becomes frozen and the
	 * next time one want to modify it, one should defrost the object (see {@code javassist.CtClass.isFrozen()} and {@code javassist.CtClass.defrost()} methods).
	 * <p/>
	 * Implementation of this method must synchronize access to the supplied instance of {@code javassist.CtClass}, because {@code javassist.CtClass} is not thread-safe and the method
	 * {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[])} can be called concurrently.
	 * 
	 * @param ctClass
	 *            Object that represents class file of a class to be transformed.
	 * @return
	 *         An array of bytes in class file format (the result of the transform), or null if no transform was performed.
	 * @throws Exception
	 *             If something goes wrong.
	 * @see #acceptClassForTransformation(ClassLoader, String)
	 * @see #transform(ClassLoader, String, Class, ProtectionDomain, byte[])
	 */
	protected abstract byte[] doTransform(final CtClass ctClass) throws Exception;

	/**
	 * Transforms the supplied class file and returns a new replacement class file. This method invokes method {@link #doTransform(CtClass)}
	 * <p/>
	 * Once a transformer has been registered with {@code java.lang.instrument.Instrumentation.addTransformer(java.lang.instrument.ClassFileTransformer)}, the method will be called for every new class
	 * definition and every class redefinition. The request for a new class definition is made with one of {@code java.lang.ClassLoader.defineClass(...)} methods. The request for a class redefinition
	 * is made with {@code java.lang.instrument.Instrumentation.redefineClasses(java.lang.instrument.ClassDefinition[])} or its native equivalents. The transformer is called during the processing of
	 * the request, before the class file bytes have been verified or applied.
	 * <p/>
	 * The method returns null if no transformation is needed, otherwise it returns an array of bytes in class file format.
	 * 
	 * @param classLoader
	 *            The defining loader of the class to be transformed, may be null if the bootstrap loader.
	 * @param className
	 *            The name of the class in the internal form of fully qualified class and interface names as defined in "The Java Virtual Machine Specification". For example: {@code "java/util/List"}.
	 * @param classBeingRedefined
	 *            If this is a redefine, the class being redefined, otherwise null.
	 * @param protectionDomain
	 *            The protection domain of the class being defined or redefined. Actually this parameter is not used in this implementation.
	 * @param classfileBuffer
	 *            The input byte buffer in class file format (stays unmodified). Actually this parameter is not used in this implementation, because class file is obtained via Javassist framework from
	 *            {@code javassist.ClassPool}. So if one want to chain transformations of the same class, one should use the same instance of {@code javassist.CtClass} obtained from the same
	 *            {@code javassist.ClassPool} instance.
	 * @return
	 *         A class file buffer (the result of the transform), or null if no transform is performed.
	 * @throws IllegalClassFormatException
	 *             If the input does not represent a well-formed class file. Usage of {@code javassist.CtClass} eliminates the need to check the correctness of supplied {@code classfileBuffer}, so the
	 *             method actually will newer throw {@code java.lang.instrument.IllegalClassFormatException}.
	 * @see java.lang.instrument.ClassFileTransformer.transform(ClassLoader, String, Class, ProtectionDomain, byte[])
	 * @see #doTransform(CtClass)
	 */
	public final byte[] transform(final ClassLoader classLoader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
			throws IllegalClassFormatException {
		final byte[] transformedBytes;
		try {
			transformedBytes = unsafeTransform(classLoader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		} catch (final Error err) {
			throw err;
		} catch (final RuntimeException rex) {
			LOGGER.error("RuntimeException occured during transformation of the class '" + className + "'", rex);
			throw rex;
		} catch (final IllegalClassFormatException icf) {
			LOGGER.error("IllegalClassFormatException occured during transformation of the class '" + className + "'", icf);
			throw icf;
		} catch (final Exception exc) {
			final String msg = "Exception occured during transformation of the class '" + className + "'";
			LOGGER.error(msg, exc);
			final RuntimeException wre = new RuntimeException(msg, exc);
			throw wre;
		} catch (final Throwable thr) {
			final String msg = "Problem was encountered during transformation of the class '" + className + "'";
			LOGGER.error(msg, thr);
			final RuntimeException wre = new RuntimeException(msg, thr);
			throw wre;
		}
		return transformedBytes;
	}

	private final byte[] unsafeTransform(final ClassLoader classLoader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
			final byte[] classfileBuffer) throws Exception {
		final byte[] transformedBytes;
		if (acceptClassForTransformation(classLoader, className)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Transforming class '" + className + "'");
			}
			CtClass ctClass;
			if (classLoader == null) {// bootstrap class loader
				ctClass = JavassistEnvironment.getCtClass(className);
			} else {
				ctClass = JavassistEnvironment.getCtClassOrNull(className);// try to get CtClass using known class path
				if (ctClass == null) {// class wasn't found in the known class path
					final ClassPath classPath = new LoaderClassPath(classLoader);
					JavassistEnvironment.appendClassPath(classPath);
					ctClass = JavassistEnvironment.getCtClass(className);
				}
			}
			transformedBytes = doTransform(ctClass);
			if (transformedBytes != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Class '" + className + "' was successfully transformed");
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("No transformation was performed for class '" + className + "'");
				}
			}
		} else {
			transformedBytes = null;
		}
		return transformedBytes;
	}
}