package com.gl.vn.me.ko.sample.instrumentation.env.misc;

import java.util.Locale;

/**
 * Provides methods and constants that must be used in order the application to demonstrate the same behavior
 * regardless of the system locale.
 * Instantiability: forbidden.<br/>
 * Thread safety: thread-safe.
 * 
 * @author Valentin Kovalenko
 */
public final class Internationalization {
	/**
	 * Instance of {@code java.util.Locale} that should be used throughout the application code
	 */
	public final static Locale LOCALE;
	static {
		LOCALE = Locale.ENGLISH;
	}

	private Internationalization() {
		throw new UnsupportedOperationException("The class is not designed to be instantiated");
	}
}