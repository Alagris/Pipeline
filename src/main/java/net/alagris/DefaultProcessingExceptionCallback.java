package net.alagris;

public class DefaultProcessingExceptionCallback implements ProcessingExceptionCallback{

	@Override
	public <Cargo> void fail(Exception e, Cargo input, Pipework<Cargo> pipework) {
		System.err.println("Processing fail! Pipe ID=" + pipework.getId());
		e.printStackTrace();
	}
}