package net.alagris;

import java.util.HashMap;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DoubleHashGlobalConfig extends HashGlobalConfig {

	@JsonIgnore
	private final HashMap<String, Object> programmaticOpts = new HashMap<>();

	public HashMap<String, Object> getProgrammaticOpts() {
		return programmaticOpts;
	}

	public void setProgrammaticOpt(String key, Object value) {
		this.programmaticOpts.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String name, Class<T> type) {
		try {
			return super.get(name, type);
		} catch (NoSuchElementException e) {
			if (getProgrammaticOpts().containsKey(name)) {
				return (T) getProgrammaticOpts().get(name);
			}
			throw e;
		}
	}

}
