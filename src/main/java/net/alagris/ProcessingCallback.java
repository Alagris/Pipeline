package net.alagris;

public interface ProcessingCallback<Cargo> {
	/** Called on every {@link Pipework} in {@link Group} */
	Cargo process(Pipework<Cargo> pipework, Cargo input, ResultReceiver<Cargo> resultReceiver);
}
