package net.alagris;

public interface ConfigCallback<Cargo> {
	void doFor(Pipework<Cargo> pipe, String field, Object value, Class<?> fieldType);
}
