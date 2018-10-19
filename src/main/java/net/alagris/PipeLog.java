package net.alagris;

public interface PipeLog<Cargo> {
	void log(Pipework<Cargo> pipework, Output<Cargo> out);
}
