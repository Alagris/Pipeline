package net.alagris;

/**
 * This is the most fundamental processing unit. Every {@link Node} will
 * eventually be used to instantiate and inject a Pipe.
 */
public interface Pipe<T> extends AutoCloseable {

	void onLoad();

	Output<T> process(T input);

}
