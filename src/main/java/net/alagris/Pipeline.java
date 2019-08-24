package net.alagris;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "parent" })
public class Pipeline extends ArrayList<Node> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Node parent;

	void connectNodesWithParents(Node parent) {
		this.parent = parent;
		for (Node child : this) {
			child.connectNodesWithParents(this);
		}
	}

	Node getParent() {
		return parent;
	}

	public <SearchResult> SearchResult deepForEach(NodeSearchCallback<SearchResult> callback) {
		for (Node child : this) {
			SearchResult out = callback.doFor(child);
			if (out != null)
				return out;
			out = child.forEachChild(callback);
			if (out != null)
				return out;
		}
		return null;
	}

	public <SearchResult> SearchResult deepForEachBackwards(NodeSearchCallback<SearchResult> callback) {
		for (int i = size() - 1; i >= 0; i--) {
			Node child = get(i);
			SearchResult out = callback.doFor(child);
			if (out != null)
				return out;
			out = child.forEachChildBackwards(callback);
			if (out != null)
				return out;
		}
		return null;
	}

	public <SearchResult> SearchResult deepForEachToTheRight(int indexInclusive,
			NodeSearchCallback<SearchResult> callback) {
		while (indexInclusive < size()) {
			Node child = get(indexInclusive);
			callback.doFor(child);
			SearchResult out = child.forEachChild(callback);
			if (out != null)
				return out;
			indexInclusive++;
		}
		return null;
	}

	public <SearchResult> SearchResult deepForEachToTheLeft(int indexExclusive,
			NodeSearchCallback<SearchResult> callback) {
		while (--indexExclusive >= 0) {
			Node child = get(indexExclusive);
			callback.doFor(child);
			SearchResult out = child.forEachChildBackwards(callback);
			if (out != null)
				return out;
		}
		return null;
	}

}
