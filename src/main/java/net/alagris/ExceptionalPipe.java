package net.alagris;

/**
 * {@link ExceptionalPipe} is very similar to {@link OptionalPipe} with the only
 * difference that it doesn't set <code>enabled</code> to false on fail but
 * rather uses internal flag to block.
 */
public abstract class ExceptionalPipe<Cargo> implements Pipe<Cargo> {

	@Config
	boolean enabled;

	private boolean noException = true;

	@Override
	public final void onLoad() throws Exception {
		try {
			onLoadOptional();
		} catch (Exception e) {
			noException = false;
			throw e;
		}
	}

	/**
	 * If this methods throws any exception, this pipe will automatically be set to
	 * disabled
	 */
	public abstract void onLoadOptional() throws Exception;

	@Override
	public final Output<Cargo> process(Cargo input) {
		return isEnabled() && isNoException() ? processOptional(input) : Output.none(input);
	}

	/**
	 * This method is called if and only if this {@link ExceptionalPipe} is enabled
	 */
	protected abstract Output<Cargo> processOptional(Cargo input);

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isNoException() {
		return noException;
	}

}
