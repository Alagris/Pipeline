package net.alagris;

public class InstaFail implements Pipe<String> {

	@Override
	public void close() throws Exception {

	}

	@Override
	public void onLoad() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Output<String> process(String input) throws Exception {
		throw new UnsupportedOperationException();
	}

}
