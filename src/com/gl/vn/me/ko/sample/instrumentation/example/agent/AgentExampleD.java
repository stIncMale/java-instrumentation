package com.gl.vn.me.ko.sample.instrumentation.example.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import com.gl.vn.me.ko.sample.instrumentation.example.transform.ClassFileTransformerExampleD;
import com.gl.vn.me.ko.sample.instrumentation.util.InstrumentationEnvironment;

/**
 * Java-agent class intended to access an instance of {@link java.lang.instrument.Instrumentation} and redefine Java SE class {@link java.lang.String}.
 * <p>
 * Instantiability: forbidden.<br>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class AgentExampleD extends Agent {
	/**
	 * Java-agent entry point.
	 * 
	 * @param agentArgs
	 *            Command-line arguments for Java-agent.
	 * @param inst
	 *            Instance of {@link java.lang.instrument.Instrumentation} provided by JVM.
	 */
	public final static void premain(final String agentArgs, final Instrumentation inst) {
		processArgs(agentArgs);
		LOGGER.trace("Invocation");
		try {
			initInstrumentationEnvironment(inst);
			registerClassFileTransformers(new ClassFileTransformer[] {ClassFileTransformerExampleD.INSTANCE});
			retransformClass(inst, String.class);
		} finally {
			LOGGER.trace("Invocation finished");
		}
	}

	private final static void retransformClass(final Instrumentation inst, final Class<?> classToRetransform) {
		if (LOGGER.isDebugEnabled()) {
			final ClassLoader classLoader = classToRetransform.getClassLoader();
			final String classLoaderStringRepresentation = classLoader == null ? "bootstrap" : classLoader.toString();
			LOGGER.debug("Class '" + classToRetransform.getName() + "' was loaded via '" + classLoaderStringRepresentation + "' class loader. The class will be retransformed");
		}
		try {
			InstrumentationEnvironment.retransformClasses(new Class<?>[] {classToRetransform});
		} catch (final UnmodifiableClassException e) {
			throw new RuntimeException("Class '" + classToRetransform.getName() + "' can't be modified", e);
		}
	}

	private AgentExampleD() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}