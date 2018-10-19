package net.alagris;

public class DummyBranching<Cargo> extends OptionalPipe<Cargo> {

	@Config
	public String alternativeToGo;

	@Override
	public void close() throws Exception {

	}

	@Override
	public void onLoadOptional() throws Exception {

	}

	@Override
	protected Output<Cargo> processOptional(Cargo input) {
		return new Output<Cargo>(input, alternativeToGo);
	}

}
