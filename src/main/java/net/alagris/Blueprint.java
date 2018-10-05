package net.alagris;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Blueprint stores information loaded from JSON. You can still modify them
 * later on, either by doing so manually or applying BlueprintCover(s). In order
 * to use Blueprint you should convert it to (immutable) Group. BlueprintLoader
 * allows for such operations.
 */
public class Blueprint<T extends GlobalConfig> {
	private T global;

	private ArrayList<Node> pipeline = new ArrayList<>();

	public ArrayList<Node> getPipeline() {
		return pipeline;
	}

	public void setPipeline(ArrayList<Node> pipeline) {
		this.pipeline = pipeline;
	}

	private static <T extends GlobalConfig> Blueprint<T> afterParsing(Blueprint<T> blueprint)
			throws DuplicateIdException {
		blueprint.getGlobal().onLoad();
		final ArrayList<String> ids = blueprint.collectIds();
		Collections.sort(ids);
		for (int i = 1; i < ids.size(); i++) {
			if (ids.get(i).equals(ids.get(i - 1))) {
				throw new DuplicateIdException("ID \"" + ids.get(i) + "\" is duplicated!");
			}
		}
		return blueprint;
	}

	public static <T extends GlobalConfig> Blueprint<T> load(File f, Class<T> config)
			throws JsonProcessingException, IOException, DuplicateIdException {
		Blueprint<T> blueprint = makeReader(config).readValue(f);
		return afterParsing(blueprint);
	}

	public static <T extends GlobalConfig> Blueprint<T> load(String s, Class<T> config)
			throws JsonProcessingException, IOException, DuplicateIdException {
		Blueprint<T> blueprint = makeReader(config).readValue(s);
		return afterParsing(blueprint);
	}

	private static <T extends GlobalConfig> ObjectReader makeReader(Class<T> config) {
		ObjectMapper mapper = new ObjectMapper();
		JavaType type = mapper.getTypeFactory().constructParametricType(Blueprint.class, config);
		return mapper.reader(type);
	}

	@Override
	public String toString() {
		try {
			return JacksonUtils.prettyString(this);
		} catch (IOException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

	public T getGlobal() {
		return global;
	}

	public void setGlobal(T global) {
		this.global = global;
	}

	/**
	 * Collects all {@link Node}s that have an assigned ID and puts them in one
	 * {@link HashMap}.
	 */
	public HashMap<String, Node> collectById() {
		final HashMap<String, Node> out = new HashMap<>();
		forEachNode(new NodeCallback() {
			@Override
			public void doFor(Node node) {
				if (node.getId() != null) {
					out.put(node.getId(), node);
				}
			}
		});
		return out;
	}

	private ArrayList<String> collectIds() {
		final ArrayList<String> out = new ArrayList<>();
		forEachNode(new NodeCallback() {
			@Override
			public void doFor(Node node) {
				if (node.getId() != null) {
					out.add(node.getId());
				}
			}
		});
		return out;
	}

	public void forEachNode(NodeCallback callback) {
		forEachNode(pipeline, callback);
	}

	private void forEachNode(ArrayList<Node> pipeline, NodeCallback callback) {
		for (Node node : pipeline) {
			callback.doFor(node);
			for (ArrayList<Node> alt : node.getAlternatives().values()) {
				forEachNode(alt, callback);
			}
		}
	}

	public void applyCover(String cover, Class<T> config) throws IOException {
		applyCover(BlueprintCover.load(cover, config));
	}

	public void applyCover(File cover, Class<T> config) throws IOException {
		applyCover(BlueprintCover.load(cover, config));
	}

	public void applyCover(final BlueprintCover<T> cover) {
		global.applyCover(cover.getGlobal());
		forEachNode(new NodeCallback() {
			@Override
			public void doFor(Node node) {
				NodeCover nodeCover = cover.getCover().get(node.getId());
				if (nodeCover != null) {
					node.applyCover(nodeCover);
				}
			}
		});
	}

}
