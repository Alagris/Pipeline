package net.alagris;

public interface GlobalConfig {

	void onLoad();
	<T> T get(String name, Class<T> type);
}
