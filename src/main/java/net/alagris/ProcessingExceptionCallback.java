package net.alagris;

public interface ProcessingExceptionCallback<Cargo> {
	void fail(Exception e, Cargo input, Pipework<Cargo> pipework);
}