package com.gl.vn.me.ko.sample.instrumentation.util;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment;

/**
 * Provides API that allows to obtain an instance of {@link java.lang.instrument.Instrumentation} from the application {@code main(String[])} method
 * for example.
 * <p/>
 * Example of initialization from Java-agent:<br/>
 * <blockquote>
 * 
 * <pre>
 * class Agent {
 * 	public static void premain(String agentArgs, Instrumentation inst) {
 * 		InstrumentationEnvironment.setInstrumentation(inst);
 * 	}
 * }
 * </pre>
 * 
 * </blockquote> The class also provides a {@link #retransformClasses(Class[])} method similar to {@link java.lang.instrument.Instrumentation}{@code .retransformClasses(Class[])} method that was added
 * in Java SE 6.
 * <p/>
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class InstrumentationEnvironment {
	private static volatile Instrumentation instrumentation;
	private final static Object LOCK;
	static {
		instrumentation = null;
		LOCK = new Object();
	}

	/**
	 * Allows to obtain an instance of {@link java.lang.instrument.Instrumentation}.
	 * Once initialized via {@link #setInstrumentation(Instrumentation)} method, always returns the same object.
	 * 
	 * @return
	 *         An instance of {@link java.lang.instrument.Instrumentation} or {@code null} if instrumentation environment was not initialized.
	 * @see #setInstrumentation(Instrumentation)
	 */
	public final static Instrumentation getInstrumentation() {
		return instrumentation;
	}

	/**
	 * Checks if instrumentation environment was initialized.
	 * 
	 * @return
	 *         <ul>
	 *         <li>{@code true} if instrumentation environment was initialized.</li>
	 *         <li>{@code false} if instrumentation environment wasn't initialized.</li>
	 *         </ul>
	 */
	public final static boolean isInitialized() {
		return getInstrumentation() != null;
	}

	/**
	 * Retransforms the supplied set of classes.
	 * This method was made similar to {@code java.lang.instrument.Instrumentation.retransformClasses(Class[])} that was added in Java SE 6, but this method can be compiled and run on Java SE 5.
	 * <p/>
	 * The method just reads original bytes of classes and calls {@link java.lang.instrument.Instrumentation#redefineClasses(ClassDefinition[])}, so modifications can be made by class file
	 * transformers.
	 * 
	 * @param classes
	 *            Array of classes to retransform. Must be not {@code null}. A zero-length array is allowed, in this case, this method does nothing.
	 * @throws UnmodifiableClassException
	 *             If a redefined class can't be modified. Primitive classes (for example, {@link java.lang.Integer#TYPE}) and array classes are never modifiable. In Java SE 6 there is a method
	 *             {@link java.lang.instrument.Instrumentation}{@code .isModifiableClass(Class)} that allows to check if the class is modifiable.
	 * @see java.lang.instrument.Instrumentation#redefineClasses(ClassDefinition[])
	 * @see com.gl.vn.me.ko.sample.instrumentation.util.AbstractClassFileTransformer#transform(ClassLoader, String, Class, java.security.ProtectionDomain, byte[])
	 */
	public final static void retransformClasses(final Class<?>[] classes) throws UnmodifiableClassException {
		if (classes == null) {
			throw new NullPointerException("The argument 'classes' is null");
		} else if (!isInitialized()) {
			throw new IllegalStateException("Instrumentation environment wasn't initialized");
		} else if (!instrumentation.isRedefineClassesSupported()) {
			throw new NullPointerException("Redefinition is not supported");
		}
		final ClassDefinition[] classDefinitions;
		JavassistEnvironment.lock();
		try {
			JavassistEnvironment.renew(true);// renew environment to have a guarantee that obtained instances of CtClass are unmodified
												// JavassistEnvironment.lock() is required to be sure that all classes are obtained from the new environment (javassist.ClassPool)
			classDefinitions = new ClassDefinition[classes.length];
			for (int i = 0; i < classes.length; i++) {
				final Class<?> classToRetransform = classes[i];
				final CtClass ctClassToRetransform;
				try {
					ctClassToRetransform = JavassistEnvironment.getCtClass(classToRetransform);
				} catch (final NotFoundException e) {
					throw new RuntimeException(e);
				}
				final byte[] bytes;
				try {
					bytes = JavassistEnvironment.getCtBytes(ctClassToRetransform);
				} catch (final CannotCompileException e) {
					throw new RuntimeException(e);
				}
				ctClassToRetransform.defrost();// no changes were made in the class before obtaining its bytes, so defrost is absolutely safe and just restores original class state
				classDefinitions[i] = new ClassDefinition(classToRetransform, bytes);
			}
		} finally {
			JavassistEnvironment.unlock();
		}
		try {
			instrumentation.redefineClasses(classDefinitions);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (final UnmodifiableClassException e) {
			throw e;
		}
	}

	/**
	 * Initializes instrumentation environment.
	 * Being invoked with not {@code null} parameter, does nothing in subsequent invocations.
	 * 
	 * @param instrumentation
	 *            An instance of {@link java.lang.instrument.Instrumentation}. Must be not {@code null}.
	 * @see #getInstrumentation()
	 */
	public final static void setInstrumentation(final Instrumentation instrumentation) {
		if (instrumentation == null) {
			throw new NullPointerException("The argument 'instrumentation' is null");
		}
		synchronized (LOCK) {
			if (InstrumentationEnvironment.instrumentation == null) {
				InstrumentationEnvironment.instrumentation = instrumentation;
			}
		}
	}

	private InstrumentationEnvironment() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}