package net.alagris;

import java.util.HashMap;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class HashGlobalConfig implements GlobalConfig {

	@Override
	public void onLoad() {

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
