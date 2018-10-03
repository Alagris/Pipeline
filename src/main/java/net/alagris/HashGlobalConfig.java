package net.alagris;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class HashGlobalConfig implements GlobalConfig {

	@Override
	public void onLoad() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String name, Class<T> type) {
		return (T) Classes.parseString(type, opts.get(name));
	}

	@JsonAnyGetter
	public HashMap<String, String> getOpts() {
		return opts;
	}

	@JsonAnySetter
	public void setOpts(String k, String v) {
		this.opts.put(k, v);
	}

	private HashMap<String, String> opts = new HashMap<>();

}
