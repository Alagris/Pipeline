package net.alagris;

import java.util.ArrayList;

public class Preprocessor extends OptionalPipe<String> {

	@Config
	String suffix;

	@Config
	String[] paths;

	@Config
	int[] ints;

	@Config
	String country;

	@Config("strings")
	ArrayList<String> dynPaths;

	@Override
	public Output<String> proc(String input) {
		return new Output<String>(input + suffix);
	}

	@Override
	public void onLoad() {

	}

	@Override
	public void close() throws Exception {

	}

}
