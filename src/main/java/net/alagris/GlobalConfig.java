package net.alagris;

import java.util.NoSuchElementException;

public interface GlobalConfig {
	/**
	 * Called when Blueprint finished parsing from file
	 */
	void onLoad();

	/**
	 * Called when Blueprint is converted into executable pipeline ( {@link Group})
	 */
	void onMake();

	void applyCover(GlobalConfig other);

	<T> T get(String name, Class<T> type) throws NoSuchElementException;

	void put(String variable, Object value);
}
