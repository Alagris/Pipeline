package net.alagris;

public class DuplicateIdException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateIdException() {
		super();
	}

	public DuplicateIdException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DuplicateIdException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateIdException(String message) {
		super(message);
	}

	public DuplicateIdException(Throwable cause) {
		super(cause);
	}

}
