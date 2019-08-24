package net.alagris;

public class IllegalAliasException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalAliasException() {
		super();
	}

	public IllegalAliasException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalAliasException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalAliasException(String message) {
		super(message);
	}

	public IllegalAliasException(Throwable cause) {
		super(cause);
	}

}
