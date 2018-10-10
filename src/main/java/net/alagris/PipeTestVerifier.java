package net.alagris;

/**
 * PipeTestVerifier is meant to verify is given input/output (pipeline cargo) is
 * acceptable by provided test unit.
 */
public interface PipeTestVerifier<Cargo, TestUnit> {

	TestResult verifyInput(Cargo input, TestUnit testUnit);

	TestResult verifyOutput(Cargo output, TestUnit testUnit);

}
