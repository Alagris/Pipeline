package net.alagris;

public class UppercaseFirst implements Pipe<String> {


	@Override
	public Output<String> process(String input) {
		input = Character.toUpperCase(input.charAt(0)) + input.substring(1);
		return new Output<String>(input);
	}

	@Override
	public void onLoad() {
		
	}

	@Override
	public void close() throws Exception {
		
	}

}
