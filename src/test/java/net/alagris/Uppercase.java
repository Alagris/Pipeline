package net.alagris;

import java.util.Locale;

public class Uppercase implements Pipe<String>{


	@Config
	Locale locale;
	
	@Override
	public Output<String> process(String input) {
		return new Output<String>(input.toUpperCase(locale));
	}

	@Override
	public void onLoad() {
		
	}

	@Override
	public void close() throws Exception {
		
	}
	
	
	
}
