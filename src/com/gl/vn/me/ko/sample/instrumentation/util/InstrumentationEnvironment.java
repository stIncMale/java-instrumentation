package com.gl.vn.me.ko.sample.instrumentation.util;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import javassist.CtClass;
import javax.annotation.Nullable;
import com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment;

/**
 * Provides API that allows to obtain an instance of {@link java.lang.instrument.Instrumentation} from the application {@code main(String[])} method
 * for example.
 * <p>
 * Example of initialization from Java-agent:<br>
 * <blockquote>
 *
 * <pre>
 * class Agent {
 * 	public static void premain(String agentArgs, Instrumentation inst) {
 * 		InstrumentationEnvironment.setInstrumentation(inst);
 *  }
 * }
 * </pre>
 *
 * </blockquote> The class also provides a {@link #retransformClasses(Class[])} method similar to
 * {@link java.lang.instrument.Instrumentation}{@code .retransformClasses(Class[])} method that was added in Java SE 6.
 * <p>
 * Instantiability: forbidden.<br>
 * Thread safety: thread-safe.
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
   * @return An instance of {@link java.lang.instrument.Instrumentation} or {@code null} if instrumentation environment was not initialized.
   *
   * @see #setInstrumentation(Instrumentation)
   * @see #isInitialized()
   */
  @Nullable
  public final static Instrumentation getInstrumentation() {
    return instrumentation;
  }

  /**
   * Checks if instrumentation environment was initialized.
   *
   * @return <ul>
   * <li>{@code true} if instrumentation environment was initialized.</li>
   * <li>{@code false} if instrumentation environment wasn't initialized.</li>
   * </ul>
   */
  public final static boolean isInitialized() {
    return instrumentation != null;
  }

  /**
   * Retransforms the supplied set of classes.
   * This method was made similar to method {@code java.lang.instrument.Instrumentation.retransformClasses(Class<?>...)} that was added in Java SE 6,
   * but this method can be compiled and run on Java SE 5.
   * <p>
   * The method just reads original bytes of classes and calls {@link java.lang.instrument.Instrumentation#redefineClasses(ClassDefinition[])},
   * so modifications can be made by class file transformers. A separate private instance of {@link javassist.ClassPool} is used to read bytecode.
   * <p>
   * Note that it doesn't support hierarchy of class loaders.
   * It means that the method is not applicable for cases when classes with the same name are loaded with different class loaders.
   *
   * @param classes Array of classes to retransform. Must be not {@code null}. A zero-length array is allowed, in this case, this method does nothing.
   *
   * @throws UnmodifiableClassException If a redefined class can't be modified.
   * Primitive classes (for example, {@link java.lang.Integer#TYPE}) and array classes are never modifiable. In Java SE 6 there is a method
   * {@link java.lang.instrument.Instrumentation}{@code .isModifiableClass(Class)} that allows to check if the class is modifiable.
   * @see java.lang.instrument.Instrumentation#redefineClasses(ClassDefinition[])
   * @see com.gl.vn.me.ko.sample.instrumentation.util.AbstractClassFileTransformer#transform(ClassLoader, String, Class, ProtectionDomain, byte[])
   */
  public final static void retransformClasses(final Class<?>[] classes) throws UnmodifiableClassException {
    if (classes == null) {
      throw new NullPointerException("The argument 'classes' is null");
    } else if (!isInitialized()) {
      throw new IllegalStateException("Instrumentation environment wasn't initialized");
    } else if (!instrumentation.isRedefineClassesSupported()) {
      throw new RuntimeException("Redefinition is not supported by the current JVM configuration");
    }
    JavassistEnvironment.lock();
    try {
      final ClassDefinition[] classDefinitions = new ClassDefinition[classes.length];
      final byte[][] classFileBytes = getOriginalBytes(classes);
      for (int i = 0; i < classes.length; i++) {
        classDefinitions[i] = new ClassDefinition(classes[i], classFileBytes[i]);
      }
      try {
        instrumentation.redefineClasses(classDefinitions);
      } catch (final ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } finally {
      JavassistEnvironment.unlock();
    }
  }

  /**
   * Initializes instrumentation environment.
   * Being invoked with not {@code null} parameter, does nothing in subsequent invocations.
   *
   * @param instrumentation An instance of {@link java.lang.instrument.Instrumentation}. Must be not {@code null}.
   *
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

  /**
   * Invocations must be synchronized using {@link com.gl.vn.me.ko.sample.instrumentation.util.javassist.JavassistEnvironment#lock()} method.
   */
  private final static byte[][] getOriginalBytes(final Class<?>[] classes) {
    final byte[][] result = new byte[classes.length][];
    try {
      for (int i = 0; i < classes.length; i++) {
        final Class<?> clazz = classes[i];
        final CtClass possiblyModifiedCtClass = JavassistEnvironment.getCtClass(clazz);
        synchronized (possiblyModifiedCtClass) {
          possiblyModifiedCtClass.detach();// remove this object from its ClassPool
        }
        final CtClass originalCtClass = JavassistEnvironment.getCtClass(clazz);// this time it will be unmodified class
        synchronized (originalCtClass) {
          result[i] = originalCtClass.toBytecode();
          // no modifications were made with the class, so one can safely defrost it in order to allow further transformations
          originalCtClass.defrost();
        }
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private InstrumentationEnvironment() {
    throw new UnsupportedOperationException("The class is not designed to be instantiated");
  }
}