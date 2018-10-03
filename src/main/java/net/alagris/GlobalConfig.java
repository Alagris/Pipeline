package net.alagris;

import java.util.NoSuchElementException;

public interface GlobalConfig {

	void onLoad();
	<T> T get(String name, Class<T> type) throws NoSuchElementException;
}
