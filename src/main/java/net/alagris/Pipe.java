package net.alagris;

public interface Pipe<T> {

	void onLoad();
	Output<T> process(T input);

}
