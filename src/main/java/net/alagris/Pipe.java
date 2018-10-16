package net.alagris;

/**
 * This is the most fundamental processing unit. Every {@link Node} will
 * eventually be used to instantiate and inject a Pipe.
 */
public interface Pipe<Cargo> extends AutoCloseable {

	void onLoad() throws Exception;

	Output<Cargo> process(Cargo input) throws Exception;

}
