package net.alagris;

public class UppercaseFirst implements Pipe<String> {

	@Override
	public Output<String> process(String input) {
		if (input.length() == 0)
			return Output.none(input);
		char capital = Character.toUpperCase(input.charAt(0));
		StringBuilder builder = new StringBuilder(input);
		builder.setCharAt(0, capital);
		return Output.none(builder.toString());
	}

	@Override
	public void onLoad() {

	}

	@Override
	public void close() throws Exception {

	}

}
