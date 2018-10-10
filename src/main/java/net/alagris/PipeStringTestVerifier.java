package net.alagris;

/**
 * This is the most basic pre-made implementation of PipeTestVerifier. It only
 * compares strings.
 */
public class PipeStringTestVerifier implements PipeTestVerifier<String, String> {

	@Override
	public TestResult verifyInput(String input, String testUnit) {
		if (testUnit == null)
			return TestResult.pass();
		return new TestResult(input.equals(testUnit), testUnit, input);
	}

	@Override
	public TestResult verifyOutput(String output, String testUnit) {
		if (testUnit == null)
			return TestResult.pass();
		return new TestResult(output.equals(testUnit), testUnit, output);
	}

}
