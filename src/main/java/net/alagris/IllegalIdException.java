package net.alagris;

public class IllegalIdException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalIdException() {
		super();
	}

	public IllegalIdException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalIdException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalIdException(String message) {
		super(message);
	}

	public IllegalIdException(Throwable cause) {
		super(cause);
	}

}
