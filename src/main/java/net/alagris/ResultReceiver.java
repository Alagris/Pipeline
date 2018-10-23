package net.alagris;

public interface ResultReceiver<Cargo> {
	void receive(Result<Cargo> result);
}
