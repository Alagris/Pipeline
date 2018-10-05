package net.alagris;

public abstract class OptionalPipe<T> implements Pipe<T> {

	@Config
	boolean enabled;

	@Override
	public final Output<T> process(T input) {
		return enabled ? proc(input) : Output.none(input);
	}

	/** This method is called if and only if this {@link OptionalPipe} is enabled */
	protected abstract Output<T> proc(T input);

}
