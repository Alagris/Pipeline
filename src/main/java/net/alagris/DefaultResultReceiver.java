package net.alagris;

public class DefaultResultReceiver<Cargo> implements ResultReceiver<Cargo> {
	@Override
	public void receive(Result<Cargo> result) {
		System.err.println("Result emitted! " + result.toString());
	}
}
