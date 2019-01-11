package net.alagris;

public class TestAssertionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final TestResult result;

	public TestAssertionException( TestResult result) {
		this.result = result;
	}

	public TestResult getResult() {
		return result;
	}

}
