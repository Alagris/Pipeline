package net.alagris;

public class Branching implements Pipe<String> {

	@Config
	String left;
	
	@Config
	String right;
	
	@Override
	public Output<String> process(String input) {
		return Output.right(input);
	}

	@Override
	public void onLoad() {

	}

	@Override
	public void close() throws Exception {
		
	}

}
