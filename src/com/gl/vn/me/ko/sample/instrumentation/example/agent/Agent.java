package com.gl.vn.me.ko.sample.instrumentation.example.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;
import com.beust.jcommander.ParameterException;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.CommandLineHelper;
import com.gl.vn.me.ko.sample.instrumentation.env.misc.LogHelper;
import com.gl.vn.me.ko.sample.instrumentation.util.InstrumentationEnvironment;

/**
 * Provides methods that can be used by Java-agent classes in order to fit an existing execution environment
 * {@code com.gl.vn.me.ko.sample.instrumentation.env*}.
 * <p>
 * Instantiability: forbidden.<br>
 * Thread safety: thread-safe.
 */
public class Agent {
  /**
   * Instance of {@link org.apache.log4j.Logger} that should be used for logging.
   * Logging system must be configured via invocation of {@link Agent#processArgs(String)} method before using this instance.
   *
   * @see #processArgs(String)
   */
  protected final static Logger LOGGER;

  static {
    LOGGER = Logger.getLogger(Agent.class);
  }

  /**
   * Initializes instrumentation environment.
   * Being invoked with not {@code null} parameter, does nothing in subsequent invocations.
   *
   * @param inst Instance of {@link java.lang.instrument.Instrumentation} provided by JVM. Must be not {@code null}.
   */
  protected final static void initInstrumentationEnvironment(final Instrumentation inst) {
    InstrumentationEnvironment.setInstrumentation(inst);
    LOGGER.trace("Instrumentation environment was initialized");
  }

  /**
   * Process command-line arguments and configures logging system.
   *
   * @param args Command-line arguments for Java-agent.
   */
  protected final static void processArgs(final String args) {
    try {
      final String logLvl = ((args != null) && (args.length() > 0)) ? args : LogHelper.AGENT_DEFAULT_LOGGING_LEVEL;
      LogHelper.configure(logLvl, true);
    } catch (final ParameterException e) {
      CommandLineHelper.printAgentUsageAndExit(e);
    }
  }

  /**
   * Registers supplied class file transformers in the order they are presented in the supplied array.
   * Instrumentation environment must be initialized (see {@link #initInstrumentationEnvironment(Instrumentation)} before using this method.
   *
   * @param transformers Array of {@link java.lang.instrument.ClassFileTransformer} instances to register in the
   * {@link java.lang.instrument.Instrumentation}.
   *
   * @see #initInstrumentationEnvironment(Instrumentation)
   */
  protected final static void registerClassFileTransformers(final ClassFileTransformer[] transformers) {
    if (!InstrumentationEnvironment.isInitialized()) {
      throw new IllegalStateException("Instrumentation environment isn't initialized");
    }
    @SuppressWarnings("null")
    @Nonnull
    // can't be null because InstrumentationEnvironment is initialized
    final Instrumentation inst = InstrumentationEnvironment.getInstrumentation();
    for (final ClassFileTransformer transformer : transformers) {
      inst.addTransformer(transformer);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Class transformer '" + transformer.getClass()
            .getSimpleName() + "' was successfully added to instrumentation");
      }
    }
  }

  /**
   * Constructor was made {@code protected} and not {@code private} because the class is supposed to be extended.
   *
   * @throws java.lang.UnsupportedOperationException The class is not designed to be instantiated so the constructor always throws an exception.
   */
  protected Agent() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("The class is not designed to be instantiated");
  }
}