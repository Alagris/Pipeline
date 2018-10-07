package net.alagris;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * HashGlobalConfig provides the most basic and generic implementation of
 * {@link GlobalConfig}. It only caches key-value pairs in a {@link HashMap}.
 */
public class HashGlobalConfig implements GlobalConfig {

	@Override
	public void onLoad() {

	}

	@Override
	public void applyCover(GlobalConfig other) {
		for (Entry<String, Object> entry : opts.entrySet()) {
			try {
				entry.setValue(other.get(entry.getKey(), Object.class));
			} catch (NoSuchElementException e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String name, Class<T> type) throws NoSuchElementException {
		if (opts.containsKey(name)) {
			return (T) Classes.parseObject(type, opts.get(name));
		} else {
			throw new NoSuchElementException("No config named: " + name);
		}
	}

	@Override
	public void put(String variable, Object value) {
		setOpts(variable, value);
	}

	@JsonAnyGetter
	public HashMap<String, Object> getOpts() {
		return opts;
	}

	@JsonAnySetter
	public void setOpts(String k, Object v) {
		this.opts.put(k, v);
	}

	private HashMap<String, Object> opts = new HashMap<>();

}
