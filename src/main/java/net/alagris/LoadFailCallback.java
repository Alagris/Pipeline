package net.alagris;

public interface LoadFailCallback {
	<Cargo> void fail(Pipe<Cargo> pipe, Class<Pipe<Cargo>> pipeClass, Exception e);
}