package net.alagris;

public abstract class OptionalPipe<T> implements Pipe<T> {

	@Config
	boolean enabled;

	@Override
	public final void onLoad() throws Exception {
		try {
			onLoadOptional();
		} catch (Exception e) {
			enabled = false;
			throw e;
		}
	}

	/**
	 * If this methods throws any exception, this pipe will automatically be set to
	 * disabled
	 */
	public abstract void onLoadOptional() throws Exception;

	@Override
	public final Output<T> process(T input) {
		return isEnabled() ? processOptional(input) : Output.none(input);
	}

	/** This method is called if and only if this {@link OptionalPipe} is enabled */
	protected abstract Output<T> processOptional(T input);

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
