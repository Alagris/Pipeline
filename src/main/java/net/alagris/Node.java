package net.alagris;

import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contains configurations loaded from JSON. Every Node is mutable but can later
 * on be converted into {@link Pipework} which is no longer mutable. Nodes make
 * up {@link Blueprint}.
 */
@JsonIgnoreProperties({ "parent", "myIndexInParent" })
public class Node implements Identifiable {
	private String name;
	private String id;
	private HashMap<String, Object> config = new HashMap<>();
	private HashMap<String, Pipeline> alternatives = new HashMap<>();
	private String[] aliases;
	private Pipeline parent;

	/**
	 * If true, then all alternatives will be executed one by one, all feed the same
	 * input and the output of each alternative is discarded. This allows for
	 * branching pipeline in a way that produces multiple end points. While
	 * alternative outputs are discarded, you can still emit results.
	 */
	private boolean runAllAlternatives = false;

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
	 * This method is here only for Jackson. Normally ID would be final. Don't use
	 * this setter!
	 */
	@Deprecated
	void setId(String id) {
		this.id = id;
	}

	public HashMap<String, Object> getConfig() {
		return config;
	}

	void setConfig(HashMap<String, Object> config) {
		this.config = config;
	}

	public HashMap<String, Pipeline> getAlternatives() {
		return alternatives;
	}

	void setAlternatives(HashMap<String, Pipeline> alternatives) {
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

	public boolean isRunAllAlternatives() {
		return runAllAlternatives;
	}

	public void setRunAllAlternatives(boolean runAllAlternatives) {
		this.runAllAlternatives = runAllAlternatives;
	}

	void connectNodesWithParents(Pipeline parent) {
		this.parent = parent;
		for (Pipeline alt : alternatives.values()) {
			alt.connectNodesWithParents(this);
		}
	}

	/**
	 * @param index - if you use 0 you will get this exact same node. If you use 1,
	 *              you will get right neighbor. If you use -1 you will get left
	 *              neighbor. And so on. If you exceed limit of neighbors, you will
	 *              get null!
	 * @return null if this node doesn't belong to any pipeline (highly unlikely
	 *         unless you decided to perform some manual manipulation of blueprint).
	 */
	public Node selectNeighbour(int index) {

		final int myIndex = getIndex();
		if (myIndex == -1)
			return null;
		final int neighbourIndex = myIndex + index;
		if (neighbourIndex < 0 || neighbourIndex >= parent.size())
			return null;
		return parent.get(neighbourIndex);
	}

	/**
	 * For each (not necessarily direct) child in every alternative that comes from
	 * this node execute callback. The order in which alternative branches are
	 * visited is not strictly determined!
	 */
	public <SearchResult> SearchResult forEachChild(NodeSearchCallback<SearchResult> callback) {
		for (Pipeline alt : alternatives.values()) {
			SearchResult out = alt.deepForEach(callback);
			if (out != null)
				return out;
		}
		return null;
	}

	/**
	 * For each (not necessarily direct) child in every alternative that comes from
	 * this node execute callback but the order is reverse (from right to left). The
	 * order in which alternative branches are visited is not strictly determined!
	 */
	public <SearchResult> SearchResult forEachChildBackwards(NodeSearchCallback<SearchResult> callback) {
		for (Pipeline alt : alternatives.values()) {
			SearchResult out = alt.deepForEachBackwards(callback);
			if (out != null)
				return out;
		}
		return null;
	}

	/**
	 * Iterates all parent nodes starting from direct parent and going towards root
	 * of pipeline. This node is not taken into account itself.
	 */
	public <SearchResult> SearchResult forEachParent(NodeSearchCallback<SearchResult> callback) {
		Pipeline parentPipeline = parent;
		while (parentPipeline != null && parentPipeline.getParent() != null) {
			SearchResult out = callback.doFor(parentPipeline.getParent());
			if (out != null)
				return out;
			parentPipeline = parentPipeline.getParent().parent;
		}
		return null;
	}

	int getIndex() {
		if (parent == null)
			return -1;
		return parent.indexOf(this);
	}

	/**
	 * All nodes that are executed before this one are considered "to the left".
	 * Children of this node are not taken into account (as they are always executed
	 * after their parent). Nodes on alternative branches that don't lead to this
	 * one are omitted. This node is not taken into account itself. Parent nodes of
	 * this node are considered to be to the left (parents are executed before their
	 * children).The order in which alternative branches are visited is not strictly
	 * determined!
	 */
	public <SearchResult> SearchResult forEachToTheLeft(NodeSearchCallback<SearchResult> callback) {
		int i = getIndex();
		if (i == -1)
			return null;

		// First, all sibling nodes on the left were executed
		while (--i >= 0) {
			Node n = parent.get(i);
			SearchResult out = callback.doFor(n);
			if (out != null)
				return out;
			out = n.forEachChildBackwards(callback);
			if (out != null)
				return out;
		}
		Node parentNode = parent.getParent();
		if (parentNode == null)
			return null;
		// Before them, the parent itself was executed
		SearchResult out = callback.doFor(parentNode);
		if (out != null)
			return out;
		// Even earlier, siblings of parent were executed.
		return parentNode.forEachToTheLeft(callback);
	}

	/**
	 * All nodes that are executed after this one (and in all alternative branches
	 * that could possibly be executed ) are considered "to the right". This node is
	 * not taken into account itself. Parent nodes of this node are not taken into
	 * account neither. The order in which alternative branches are visited is not
	 * strictly determined!
	 */
	public <SearchResult> SearchResult forEachToTheRight(NodeSearchCallback<SearchResult> callback) {
		{
			// First, all children are executed after this node
			SearchResult out = forEachChild(callback);
			if (out != null)
				return out;
		}
		return forEachToTheRightExcludeChildren(callback);
	}

	private <SearchResult> SearchResult forEachToTheRightExcludeChildren(NodeSearchCallback<SearchResult> callback) {
		int i = getIndex();
		if (i == -1)
			return null;

		// Second, consecutive nodes in the same branch are executed after this node
		while (++i < parent.size()) {
			Node n = parent.get(i);
			SearchResult out = callback.doFor(n);
			if (out != null)
				return out;
			out = n.forEachChild(callback);
			if (out != null)
				return out;
		}
		Node parentNode = parent.getParent();
		if (parentNode == null)
			return null;
		// Then, all nodes after parent are executed
		return parentNode.forEachToTheRightExcludeChildren(callback);
	}

	/** True if there is no node that would be a parent of this one. */
	public boolean isInRoot() {
		return parent == null || parent.getParent() == null;
	}

	public boolean hasAlias(String identifier) {
		if (aliases == null)
			return false;
		for (String alias : aliases)
			if (alias.equals(identifier))
				return true;
		return false;
	}

	@Override
	public String toString() {
		return getName() + (getId() == null ? "" : ":" + getId());
	}
}