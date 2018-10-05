package net.alagris;

import java.util.HashMap;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link DoubleHashGlobalConfig} extends {@link HashGlobalConfig} with
 * additional {@link HashMap} dedicated to programmatically generated
 * configurations. Use <br>
 * <code> void onLoad(){<br>
 * 	setProgrammaticOpt("key", value);<br>
 * } </code><br>
 * to add your own values. <br>
 * <b>Important note!</b><br>
 * Programmatic options are not influenced by {@link BlueprintCover}. If you
 * wish to update them accordingly, then you should override
 * <code>void applyCover(GlobalConfig other)</code>
 */
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
