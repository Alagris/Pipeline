package net.alagris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Contains configurations loaded from JSON. Every Node is mutable but can later
 * on be converted into {@link Pipework} which is no longer mutable. Nodes make
 * up {@link Blueprint}.
 */
public class Node implements Identifiable {
	private String name;
	private String id;
	private HashMap<String, Object> config = new HashMap<>();
	private HashMap<String, ArrayList<Node>> alternatives = new HashMap<>();
	private String[] aliases;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * This method is here only for Jenkins. Normally ID would be final. Don't use
	 * this setter!
	 */
	@Deprecated
	void setId(String id) {
		this.id = id;
	}

	public HashMap<String, Object> getConfig() {
		return config;
	}

	public void setConfig(HashMap<String, Object> config) {
		this.config = config;
	}

	public HashMap<String, ArrayList<Node>> getAlternatives() {
		return alternatives;
	}

	public void setAlternatives(HashMap<String, ArrayList<Node>> alternatives) {
		this.alternatives = alternatives;
	}

	void applyCover(NodeCover cover) {
		config.putAll(cover.getConfig());
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

	public void applyAlias(NodeCover nodeCover, Alias alias) {
		for (Entry<String, Object> entry : nodeCover.getConfig().entrySet()) {
			String field = entry.getKey();
			if (alias.allowsField(field)) {
				config.put(field, entry.getValue());
			} else {
				Logger.jsonWarnings.log("Tried to set field (" + field + ") not allowed by alias! Pipe: " + getName());
			}
		}
	}

}