package net.alagris;

import java.util.HashMap;

/**
 * Contains configurations loaded from JSON. Every Node is mutable but can later
 * on be converted into {@link Pipework} which is no longer mutable. Nodes make
 * up {@link Blueprint}.
 */
public class NodeCover{
	private HashMap<String, Object> config = new HashMap<>();

	public HashMap<String, Object> getConfig() {
		return config;
	}

	public void setConfig(HashMap<String, Object> config) {
		this.config = config;
	}

}