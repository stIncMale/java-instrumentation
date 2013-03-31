package com.gl.vn.me.ko.sample.instrumentation.util;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import javassist.CtClass;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment;

/**
 * Provides an ability to transform class files. See methods {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[])} and {@link #doTransform(CtClass)} for details.
 * <p/>
 * Derived classes must be thread save.
 * <p/>
 * Instantiability: allowed from inside the derived class.<br/>
 * Mutability: this class itself has nothing that can cause mutability; depends on the implementation of derived class.<br/>
 * Thread safety: this class itself has nothing that can cause thread unsafety. Implementations of derived classes must be thread-safe.<br/>
 * Cloneability: cloning is not forbidden; depends on the implementation of derived class.<br/>
 * Serializability: serialization is not forbidden; depends on the implementation of derived class.
 * 
 * @author Valentin Kovalenko
 */
public abstract class AbstractClassFileTransformer implements ClassFileTransformer {
	private final static Logger LOGGER;
	static {
		LOGGER = Logger.getLogger(AbstractClassFileTransformer.class);
	}

	/**
	 * Performs a part of instantiation process that belongs to {@link AbstractClassFileTransformer} class.
	 */
	protected AbstractClassFileTransformer() {
	}

	/**
	 * Transforms the supplied class file and returns a new replacement class file. This method invokes method {@link #doTransform(CtClass)}.
	 * <p/>
	 * Once a transformer has been registered with {@link java.lang.instrument.Instrumentation#addTransformer(ClassFileTransformer)}, the method will be called for every new class definition and every
	 * class redefinition. The request for a new class definition is made with one of {@link java.lang.ClassLoader}{@code .defineClass(...)} methods. The request for a class redefinition is made with
	 * {@link java.lang.instrument.Instrumentation#redefineClasses(ClassDefinition[])} or its native equivalents. The transformer is called during the processing of the request, before the class file
	 * bytes have been verified or applied.
	 * <p/>
	 * While it's possible to create situations where method will be called concurrently for the same class, such situations must be avoided because they can lead to unpredictable results. Concurrent
	 * invocations for different classes are allowed and correct.
	 * <p/>
	 * The method returns {@code null} if no transformation is needed, otherwise it returns an array of bytes in class file format as specified in "The Java Virtual Machine Specification".
	 * 
	 * @param classLoader
	 *            The defining loader of the class to be transformed, may be {@code null} if the bootstrap loader.
	 * @param className
	 *            The name of the class in the internal form of fully qualified class and interface names as defined in "The Java Virtual Machine Specification". For example:
	 *            {@code "java/lang/Object"}.
	 * @param classBeingRedefined
	 *            If this is a redefine, the class being redefined, otherwise {@code null}.
	 * @param protectionDomain
	 *            The protection domain of the class being defined or redefined. Actually the parameter is not used in this implementation.
	 * @param classfileBuffer
	 *            The input byte buffer in class file format (stays unmodified). Actually the parameter is not used in this implementation, because class file is obtained via Javassist framework from
	 *            {@link javassist.ClassPool}. So if one want to chain transformations of the same class, one should use the same instance of {@link javassist.CtClass} obtained from the same
	 *            {@link javassist.ClassPool} instance.
	 * @return
	 *         A well-formed class file buffer (the result of the transform), or {@code null} if no transform is performed.
	 * @throws java.lang.instrument.IllegalClassFormatException
	 *             If the input does not represent a well-formed class file. Actually {@code classfileBuffer} argument is not used by this implementation so the method will newer throw an
	 *             {@link java.lang.instrument.IllegalClassFormatException}.
	 * @see java.lang.instrument.ClassFileTransformer#transform(ClassLoader, String, Class, ProtectionDomain, byte[])
	 * @see #doTransform(CtClass)
	 */
	@Nullable
	public final byte[] transform(@Nullable final ClassLoader classLoader, @SuppressWarnings("null") final String className, @Nullable final Class<?> classBeingRedefined,
			@SuppressWarnings("null") final ProtectionDomain protectionDomain, @SuppressWarnings("null") final byte[] classfileBuffer) throws IllegalClassFormatException {
		final byte[] transformedBytes;
		try {
			transformedBytes = unsafeTransform(classLoader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		} catch (final Error e) {
			throw e;
		} catch (final RuntimeException e) {
			LOGGER.error("RuntimeException occured during transformation of the class '" + className + "'", e);
			throw e;
		} catch (final IllegalClassFormatException e) {
			LOGGER.error("IllegalClassFormatException occured during transformation of the class '" + className + "'", e);
			throw e;
		} catch (final Exception e) {
			final String msg = "Exception occured during transformation of the class '" + className + "'";
			LOGGER.error(msg, e);
			throw new RuntimeException(msg, e);
		} catch (final Throwable e) {
			final String msg = "Problem was encountered during transformation of the class '" + className + "'";
			LOGGER.error(msg, e);
			throw new RuntimeException(msg, e);
		}
		return transformedBytes;
	}

	/**
	 * An implementation of the method must decide whether the class can be transformed or not. If the method return {@code true}, transformer will try to obtain corresponded {@link javassist.CtClass}
	 * object from the {@link javassist.ClassPool} and if succeed the method {@link #doTransform(CtClass)} will be called.
	 * 
	 * @param classLoader
	 *            The defining loader of the class to be transformed. Can be {@code null} if the bootstrap loader.
	 * @param className
	 *            The name of the class in the internal form of fully qualified class and interface names as defined in "The Java Virtual Machine Specification". For example:
	 *            {@code "java/lang/Object"}.
	 * @return
	 *         <ul>
	 *         <li>{@code true} if the class should be transformed.</li>
	 *         <li>{@code false} if the class must be skipped without any transformations applied.</li>
	 *         </ul>
	 * @see #doTransform(CtClass)
	 */
	protected abstract boolean acceptClassForTransformation(@Nullable ClassLoader classLoader, String className);

	/**
	 * An implementation of this method may transform the supplied class and return an array of transformed bytes in class file format as specified in "The Java Virtual Machine Specification".
	 * This method is called from method {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[])} for any invocation of the method except when class can't pass the filter (see
	 * {@link #acceptClassForTransformation(ClassLoader, String)}) or {link javassist.CtClass} object can't be
	 * obtained from the {@link javassist.ClassPool}.
	 * <p/>
	 * Remember that once bytes were obtained from {@link javassist.CtClass} object (see {@link com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment#getCtBytes(CtClass)} method),
	 * the object becomes frozen and the next time one want to modify it, one should defrost the object (see {@link javassist.CtClass#isFrozen()} and {@link javassist.CtClass#defrost()} methods).
	 * <p/>
	 * Implementation of this method must synchronize access to the supplied instance of {@link javassist.CtClass}, because {@link javassist.CtClass} is not thread-safe and the method
	 * {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[])} can be called concurrently.
	 * 
	 * @param ctClass
	 *            Object that represents class file of a class to be transformed.
	 * @return
	 *         An array of bytes in class file format (the result of the transform), or {@code null} if no transform was performed.
	 * @throws java.lang.Exception
	 *             If something goes wrong.
	 * @see #acceptClassForTransformation(ClassLoader, String)
	 * @see #transform(ClassLoader, String, Class, ProtectionDomain, byte[])
	 */
	@Nullable
	protected abstract byte[] doTransform(final CtClass ctClass) throws Exception;

	@Nullable
	private final byte[] unsafeTransform(@Nullable final ClassLoader classLoader, final String className, @Nullable final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
			final byte[] classfileBuffer) throws Exception {
		final byte[] transformedBytes;
		if (acceptClassForTransformation(classLoader, className)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Transforming class '" + className + "'");
			}
			final CtClass ctClass = JavassistEnvironment.getCtClass(classLoader, className);
			transformedBytes = doTransform(ctClass); // it's generally a bad idea to invoke external (unknown at compile-time) methods inside synchronized blocks,
														// therefore documentation of the method doTransform(CtClass) specifies that the access to an instance of CtClass must be synchronized
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