package com.gl.vn.me.ko.sample.instrumentation.example.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import com.gl.vn.me.ko.sample.instrumentation.example.transform.ClassFileTransformerExampleA;
import com.gl.vn.me.ko.sample.instrumentation.example.transform.ClassFileTransformerExampleB;
import com.gl.vn.me.ko.sample.instrumentation.example.transform.ClassFileTransformerExampleC;

/**
 * Java-agent class intended to access an instance of {@link java.lang.instrument.Instrumentation} and register class file transformers
 * for examples A, B and C in it.
 * <p>
 * Instantiability: forbidden.<br>
 * Thread safety: thread-safe.
 */
public final class AgentExampleAbc extends Agent {
  /**
   * Java-agent entry point.
   *
   * @param agentArgs Command-line arguments for Java-agent.
   * @param inst Instance of {@link java.lang.instrument.Instrumentation} provided by JVM.
   */
  public final static void premain(final String agentArgs, final Instrumentation inst) {
    processArgs(agentArgs);
    LOGGER.trace("Invocation");
    try {
      initInstrumentationEnvironment(inst);
      registerClassFileTransformers(new ClassFileTransformer[] {ClassFileTransformerExampleA.INSTANCE, ClassFileTransformerExampleB.INSTANCE,
          ClassFileTransformerExampleC.INSTANCE});
    } finally {
      LOGGER.trace("Invocation finished");
    }
  }

  private AgentExampleAbc() {
    throw new UnsupportedOperationException("The class is not designed to be instantiated");
  }
}