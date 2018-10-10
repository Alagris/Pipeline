package net.alagris;

public interface ProcessingCallback<Cargo> {
	Cargo process(Pipework<Cargo> pipework, Cargo input);
}
