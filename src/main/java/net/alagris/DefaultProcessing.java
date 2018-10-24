package net.alagris;

class DefaultProcessing<Cargo> implements ProcessingCallback<Cargo> {

	private final ProcessingExceptionCallback processingExceptionCallback;

	public DefaultProcessing() {
		this(null);
	}

	public DefaultProcessing(ProcessingExceptionCallback processingExceptionCallback) {
		this.processingExceptionCallback = processingExceptionCallback;
	}

	@Override
	public Cargo process(Pipework<Cargo> pipework, Cargo input, ResultReceiver<Cargo> resultReceiver) {
		try {
			return pipework.process(input, resultReceiver);
		} catch (Exception e) {
			if (processingExceptionCallback != null) {
				processingExceptionCallback.fail(e, input, pipework);
			}
			return input;
		}

	}

}