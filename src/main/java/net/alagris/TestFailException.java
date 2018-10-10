package net.alagris;

public class TestFailException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final TestResult inputResult;
	private final boolean wasInput;

	public TestFailException(boolean wasInput, String pipeID, TestResult inputResult) {
		super(pipeID + " (" + (wasInput ? "input" : "output") + ") Expected: " + inputResult.getExpected() + "|Found: "
				+ inputResult.getFound());
		this.wasInput = wasInput;
		this.inputResult = inputResult;
	}

	public TestResult getInputResult() {
		return inputResult;
	}

	public boolean wasInput() {
		return wasInput;
	}

}
