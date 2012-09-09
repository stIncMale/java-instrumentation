package com.gl.vn.me.ko.sample.instrumentation.example;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.gl.vn.me.ko.sample.instrumentation.env.Example;

/**
 * Instantiability: allowed.<br/>
 * Mutability: immutable.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class ExampleC implements Example {
	private final static Logger LOGGER;
	private final static Integer COLD;
	private final static Integer HOT;
	static {
		LOGGER = Logger.getLogger(ExampleC.class);
		COLD = Integer.valueOf(-1);
		HOT = Integer.valueOf(1);
	}

	private final static Map<Integer, String> createFaucetColorCodeMap() {
		final Map<Integer, String> result = new HashMap<Integer, String>(2);
		result.put(COLD, "blue");
		result.put(HOT, "red");
		return result;
	}

	/**
	 * Constructs an instance of {@link ExampleC}.
	 */
	public ExampleC() {
	}

	public final void run() {
		final Map<Integer, String> faucetColorCode = createFaucetColorCodeMap();
		LOGGER.info("The 'cold' color code is '" + faucetColorCode.get(COLD) + "'");
		LOGGER.info("The 'hot' color code is '" + faucetColorCode.get(HOT) + "'");
	}
}
