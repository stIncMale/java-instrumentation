package com.gl.vn.me.ko.sample.instrumentation.example.util;

import java.lang.instrument.Instrumentation;
import org.apache.log4j.Logger;

/**
 * Intended to provide a static API that allows to obtain an instance of {@code java.lang.instrument.Instrumentation} from the application {@code main(String[])} method
 * for example. InstrumentationEnvironment should be initialized from Java-agent.
 * Example:
 * <blockquote>
 * 
 * <pre>
 * class Agent {
 * 	public static void premain(String agentArgs, Instrumentation instrumentation) {
 * 		InstrumentationEnvironment.setInstrumentation(instrumentation);
 * 	}
 * }
 * </pre>
 * 
 * </blockquote>
 * <p/>
 * The class doesn't require thread synchronization because Java-agents can only run coherently one by one.
 * 
 * @author Valentin Kovalenko
 */
public final class InstrumentationEnvironment {
	private final static Logger logger = Logger.getLogger(InstrumentationEnvironment.class);
	private static Instrumentation instrumentation = null;

	/**
	 * Allows to obtain an instance of Instrumentation interface from the application {@code main(String[])} method for example.
	 * Once initialized from an Java-agent, always return the same object.
	 * 
	 * @return
	 *         An instance of {@code java.lang.instrument.Instrumentation} interface
	 * @see InstrumentationEnvironment#setInstrumentation(Instrumentation)
	 */
	public final static Instrumentation getInstrumentation() {
		return InstrumentationEnvironment.instrumentation;
	}

	/**
	 * Initializes reference to instance of {@code java.lang.instrument.Instrumentation} interface,
	 * so it can be obtained later from the application {@code main(String[])} method for example.
	 * Being invoked with not null parameter, does nothing in subsequent invocations.
	 * This method should only be called from Java-agent.
	 * 
	 * @param instrumentation
	 *            An instance of Instrumentation interface
	 * @see InstrumentationEnvironment#getInstrumentation()
	 */
	/*
	 * No synchronization is required between setInstrumentation(Instrumentation) and
	 * getInstrumentation() methods,
	 * because setInstrumentation(Instrumentation) can only be called from Java-agents, that always
	 * run coherently one by one
	 */
	public final static void setInstrumentation(final Instrumentation instrumentation) {
		if (instrumentation == null) {
			throw new NullPointerException("The argument 'instrumentation' is null");
		}
		if (InstrumentationEnvironment.instrumentation == null) {
			InstrumentationEnvironment.instrumentation = instrumentation;
			logger.debug("Instrumentation environment was initialized");
		}
	}

	private InstrumentationEnvironment() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}