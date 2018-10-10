package net.alagris;

public class Trim implements Pipe<String> {

	@Override
	public Output<String> process(String input) {
		return new Output<String>(input.trim());
	}

	@Override
	public void onLoad() {

	}

	@Override
	public void close() throws Exception {

	}

}
