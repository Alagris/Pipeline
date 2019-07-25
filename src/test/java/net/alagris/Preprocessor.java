package net.alagris;

import java.util.ArrayList;
import java.util.Map;

public class Preprocessor extends OptionalPipe<String> {

    @PipeID
    String name;
    
    @PipeID
    String nameDuplicate;
    
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
	
	@Config
	Map<String,Object> dictionary;

	@Override
	public Output<String> processOptional(String input) {
		return new Output<String>(input + suffix);
	}

	@Override
	public void onLoadOptional() {

	}

	@Override
	public void close() throws Exception {

	}

}
