package net.alagris;

public class UndefinedAliasException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UndefinedAliasException() {
		super();
	}

	public UndefinedAliasException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UndefinedAliasException(String message, Throwable cause) {
		super(message, cause);
	}

	public UndefinedAliasException(String message) {
		super(message);
	}

	public UndefinedAliasException(Throwable cause) {
		super(cause);
	}

}
