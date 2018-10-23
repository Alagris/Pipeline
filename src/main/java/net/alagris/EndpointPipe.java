package net.alagris;

import java.util.Arrays;

public class EndpointPipe<Cargo> implements Pipe<Cargo> {

	@Config
	String code;

	@Config
	String description;

	@Override
	public void close() throws Exception {
	}

	@Override
	public void onLoad() throws Exception {
	}

	@Override
	public final Output<Cargo> process(Cargo input) throws Exception {

		return Output.none(input, Arrays.asList(new Result<Cargo>(emit(input), description, code)));
	}

	public Cargo emit(Cargo input) throws Exception {
		return input;
	}

}
