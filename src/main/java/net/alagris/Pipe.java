package net.alagris;

public interface Pipe<T> {

	Output<T> process(T input);

}
