package net.alagris;

//Not part of public interface
class TestProcessingCallback<Cargo, TestUnit, Verifier extends PipeTestVerifier<Cargo, TestUnit>>
		implements ProcessingCallback<Cargo> {

	private final Verifier verifier;
	private BlueprintTest<Cargo, ? extends CargoBuilder<Cargo>,TestUnit> tests;
	private final ProcessingExceptionCallback<Cargo> processingExceptionCallback;

	public TestProcessingCallback(Verifier verifier, ProcessingExceptionCallback<Cargo> processingExceptionCallback) {
		if (verifier == null)
			throw new NullPointerException("verifier parameter is null");
		this.processingExceptionCallback = processingExceptionCallback;
		this.verifier = verifier;
	}

	@Override
	public Cargo process(Pipework<Cargo> pipework, Cargo input, ResultReceiver<Cargo> resultReceiver) {
		NodeTest<TestUnit> testUnit = getTests().testForId(pipework.getId());
		if (testUnit != null) {
		    try {
    			TestResult inResult = verifier.verifyInput(input, testUnit.getInput());
    			if (!inResult.isPassed()) {
    				throw new TestFailException(true, pipework.getId(), inResult);
    			}
		    }catch (TestAssertionException e) {
		        throw new TestFailException(true, pipework.getId(), e.getResult());
            }
		}
		try {
			input = pipework.process(input,resultReceiver);
		} catch (Exception e) {
			if (processingExceptionCallback != null) {
				processingExceptionCallback.fail(e, input, pipework);
			}
		}
		if (testUnit != null) {
		    try {
    			TestResult outResult = verifier.verifyOutput(input, testUnit.getOutput());
    			if (!outResult.isPassed()) {
    				throw new TestFailException(false, pipework.getId(), outResult);
    			}
		    }catch (TestAssertionException e) {
                throw new TestFailException(true, pipework.getId(), e.getResult());
            }
		}
		return input;
	}

	public BlueprintTest<Cargo,? extends  CargoBuilder<Cargo>, TestUnit> getTests() {
		return tests;
	}

	public void setTests(BlueprintTest<Cargo, ? extends CargoBuilder<Cargo>, TestUnit> tests) {
		this.tests = tests;
	}

}