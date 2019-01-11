package net.alagris;

public class DefaultProcessingExceptionCallback<Cargo> implements ProcessingExceptionCallback<Cargo>{

	@Override
	public void fail(Exception e, Cargo input, Pipework<Cargo> pipework) {
		System.err.println("Processing fail! Pipe ID=" + pipework.getId());
		e.printStackTrace();
	}
}