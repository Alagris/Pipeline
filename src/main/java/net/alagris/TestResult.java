package net.alagris;

public class TestResult {

	private boolean passed;

	private String expected;

	private String found;

	public TestResult(boolean passed, String expected, String found) {
		this.passed = passed;
		this.expected = expected;
		this.found = found;
	}

	public static TestResult pass() {
		return new TestResult(true, null, null);
	}

	public static TestResult fail(String expected, String found) {
		return new TestResult(false, expected, found);
	}

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public String getExpected() {
		return expected;
	}

	public void setExpected(String expected) {
		this.expected = expected;
	}

	public String getFound() {
		return found;
	}

	public void setFound(String found) {
		this.found = found;
	}
}
