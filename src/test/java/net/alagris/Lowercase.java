package net.alagris;

import java.util.Locale;

public class Lowercase implements Pipe<String> {

	@Config
	Locale locale = Locale.getDefault();

	@Override
	public Output<String> process(String input) {

		return new Output<String>(input.toLowerCase(locale));
	}

	@Override
	public void onLoad() {
	}

	@Override
	public void close() throws Exception {

	}

}
