package net.alagris;

import java.util.ArrayList;
import java.util.HashMap;

public class Node {
	private String name;
	private String id;
	private HashMap<String, String> config = new HashMap<>();
	private HashMap<String, ArrayList<Node>> alternatives = new HashMap<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public HashMap<String, String> getConfig() {
		return config;
	}

	public void setConfig(HashMap<String, String> config) {
		this.config = config;
	}

	public HashMap<String, ArrayList<Node>> getAlternatives() {
		return alternatives;
	}

	public void setAlternatives(HashMap<String, ArrayList<Node>> alternatives) {
		this.alternatives = alternatives;
	}
	
}