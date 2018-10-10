package net.alagris;

//Not part of public interface
class TestProcessingCallback<Cargo, TestUnit, Verifier extends PipeTestVerifier<Cargo, TestUnit>>
		implements ProcessingCallback<Cargo> {

	private final Verifier verifier;
	private BlueprintTest<Cargo, TestUnit> tests;

	public TestProcessingCallback(Verifier verifier) {
		if (verifier == null)
			throw new NullPointerException("verifier parameter is null");
		this.verifier = verifier;
	}

	@Override
	public Cargo process(Pipework<Cargo> pipework, Cargo input) {
		NodeTest<TestUnit> testUnit = getTests().testForId(pipework.getId());
		if (testUnit != null) {
			TestResult inResult = verifier.verifyInput(input, testUnit.getInput());
			if (!inResult.isPassed()) {
				throw new TestFailException(true, pipework.getId(), inResult);
			}
		}
		input = pipework.process(input);
		if (testUnit != null) {
			TestResult outResult = verifier.verifyOutput(input, testUnit.getOutput());
			if (!outResult.isPassed()) {
				throw new TestFailException(false, pipework.getId(), outResult);
			}
		}
		return input;
	}

	public BlueprintTest<Cargo, TestUnit> getTests() {
		return tests;
	}

	public void setTests(BlueprintTest<Cargo, TestUnit> tests) {
		this.tests = tests;
	}

}