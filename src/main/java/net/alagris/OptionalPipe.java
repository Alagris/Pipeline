package net.alagris;

public abstract class OptionalPipe<T> implements Pipe<T> {

	@Config
	boolean enabled;

	@Override
	public final Output<T> process(T input) {
		return isEnabled() ? proc(input) : Output.none(input);
	}

	/** This method is called if and only if this {@link OptionalPipe} is enabled */
	protected abstract Output<T> proc(T input);

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
