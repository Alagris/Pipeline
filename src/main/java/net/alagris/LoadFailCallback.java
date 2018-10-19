package net.alagris;

import java.util.Map;

public interface LoadFailCallback {
	<Cargo> void fail(Pipe<Cargo> pipe, Class<Pipe<Cargo>> pipeClass, Map<String, Object> cnfg, String id, Exception e);

}