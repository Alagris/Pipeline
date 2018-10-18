package net.alagris;

public class DummyPipe<T> implements Pipe<T> {

	@Override
	public void close() throws Exception {
	}

	@Override
	public void onLoad() throws Exception {
	}

	@Override
	public Output<T> process(T input) throws Exception {
		return Output.none(input);
	}

}
