package net.alagris;

import java.util.NoSuchElementException;

public interface GlobalConfig {

	void onLoad();

	void applyCover(GlobalConfig other);

	<T> T get(String name, Class<T> type) throws NoSuchElementException;

	void put(String variable, Object value);
}
