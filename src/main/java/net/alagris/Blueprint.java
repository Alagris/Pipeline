package net.alagris;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

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
public class Blueprint<Cnfg extends GlobalConfig> {
	private Cnfg global;

	private Pipeline pipeline = new Pipeline();
	private HashMap<String, Alias> aliases = new HashMap<>();

	public Pipeline getPipeline() {
		return pipeline;
	}

	void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	private static <T extends GlobalConfig> Blueprint<T> afterParsing(Blueprint<T> blueprint)
			throws DuplicateIdException, UndefinedAliasException, IllegalIdException, IllegalAliasException {
		blueprint.connectNodesWithParents();
		final ArrayList<String> ids = blueprint.collectIds();
		findDuplicateIds(ids);
		findIllegalIds(ids);
		findAliasesConflictingWithIds(blueprint, ids);
		findIllegalAliases(blueprint);
		findUndefinedAliases(blueprint);
		if (blueprint.getGlobal() != null) {
			blueprint.getGlobal().onLoad();
		}
		return blueprint;
	}

	private static <T extends GlobalConfig> void findIllegalAliases(Blueprint<T> blueprint)
			throws IllegalAliasException {
		for (String alias : blueprint.aliases.keySet()) {
			if (!VALID_ID_REGEX.matcher(alias).matches()) {
				throw new IllegalAliasException(alias + " " + VALID_NAMES_HINT_MESSAGE);
			}
		}
	}

	private static final Pattern VALID_ID_REGEX = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
	private static final String VALID_NAMES_HINT_MESSAGE = "Use only a-z, A-Z, 0-9, underscores. First letter must be cannot be a digit!";

	private static void findIllegalIds(ArrayList<String> ids) throws IllegalIdException {
		for (String id : ids) {
			if (!VALID_ID_REGEX.matcher(id).matches()) {
				throw new IllegalIdException(id + " " + VALID_NAMES_HINT_MESSAGE);
			}
		}
	}

	/**
	 * If you ever decide to manually modify a blueprint (insert/remove nodes), run
	 * this method when you are done. It will ensure integrity of blueprint. This
	 * method is part of public API but it is strongly advised against using it!
	 */
	@Deprecated
	public void connectNodesWithParents() {
		pipeline.connectNodesWithParents(null);
	}

	private static void findDuplicateIds(final ArrayList<String> ids) throws DuplicateIdException {
		Collections.sort(ids);
		for (int i = 1; i < ids.size(); i++) {
			if (ids.get(i).equals(ids.get(i - 1))) {
				throw new DuplicateIdException("ID \"" + ids.get(i) + "\" is duplicated!");
			}
		}
	}

	private static <T extends GlobalConfig> void findAliasesConflictingWithIds(Blueprint<T> blueprint,
			final ArrayList<String> ids) throws DuplicateIdException {
		final Set<String> aliases = blueprint.getAliases().keySet();
		for (String id : ids) {
			if (aliases.contains(id)) {
				throw new DuplicateIdException("ID \"" + id + "\" is the same as some existing alias!");
			}
		}
	}

	private static <T extends GlobalConfig> void findUndefinedAliases(Blueprint<T> blueprint)
			throws UndefinedAliasException {
		final Set<String> aliases = blueprint.getAliases().keySet();
		final String undefinedAlias = blueprint.forEachNode(new NodeSearchCallback<String>() {
			@Override
			public String doFor(Node node) {
				if (node.getAliases() != null) {
					for (String alias : node.getAliases()) {
						if (!aliases.contains(alias)) {
							return alias;
						}
					}
				}
				return null;
			}
		});
		if (undefinedAlias != null)
			throw new UndefinedAliasException(undefinedAlias);
	}

	public static <T extends GlobalConfig> Blueprint<T> load(InputStream in, Class<T> config)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException,
			IllegalIdException, IllegalAliasException {
		Blueprint<T> blueprint = makeReader(config).readValue(in);
		return afterParsing(blueprint);
	}

	public static <T extends GlobalConfig> Blueprint<T> load(File f, Class<T> config) throws JsonProcessingException,
			IOException, DuplicateIdException, UndefinedAliasException, IllegalIdException, IllegalAliasException {
		Blueprint<T> blueprint = makeReader(config).readValue(f);
		return afterParsing(blueprint);
	}

	public static <T extends GlobalConfig> Blueprint<T> load(String s, Class<T> config) throws JsonProcessingException,
			IOException, DuplicateIdException, UndefinedAliasException, IllegalIdException, IllegalAliasException {
		Blueprint<T> blueprint = makeReader(config).readValue(s);
		return afterParsing(blueprint);
	}

	private static <T extends GlobalConfig> ObjectReader makeReader(Class<T> config) {
		ObjectMapper mapper = new ObjectMapper();
		JavaType type = mapper.getTypeFactory().constructParametricType(Blueprint.class, config);
		return mapper.readerFor(type);
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

	public Cnfg getGlobal() {
		return global;
	}

	public void setGlobal(Cnfg global) {
		this.global = global;
	}

	/**
	 * Collects all {@link Node}s that have an assigned ID and puts them in one
	 * {@link HashMap} (ID as key, node as value).
	 */
	public HashMap<String, Node> collectById() {
		final HashMap<String, Node> out = new HashMap<>();
		forEachNode(new NodeCallback() {
			@Override
			public Void doFor(Node node) {
				if (node.getId() != null) {
					out.put(node.getId(), node);
				}
				return null;
			}
		});
		return out;
	}

	/**
	 * Collects all {@link Node}s that have an assigned alias (one or more) and puts
	 * them in one {@link HashMap} (alias as key, {@link ArrayList} of nodes as
	 * value).
	 */
	public HashMap<String, ArrayList<Node>> collectByAlias() {
		final HashMap<String, ArrayList<Node>> out = new HashMap<>();
		for (String alias : getAliases().keySet()) {
			out.put(alias, new ArrayList<Node>());
		}
		forEachNode(new NodeCallback() {
			@Override
			public Void doFor(Node node) {
				if (node.getAliases() != null) {
					for (String alias : node.getAliases()) {
						out.get(alias).add(node);
					}
				}
				return null;
			}
		});
		return out;
	}

	private ArrayList<String> collectIds() {
		final ArrayList<String> out = new ArrayList<>();
		forEachNode(new NodeSearchCallback<Void>() {
			@Override
			public Void doFor(Node node) {
				if (node.getId() != null) {
					out.add(node.getId());
				}
				return null;
			}
		});
		return out;
	}

	public <SearchResult> SearchResult forEachSelected(Selector selector, NodeSearchCallback<SearchResult> callback) {
		return pipeline.deepForEach(n -> selector.match(n) ? callback.doFor(n) : null);
	}

	public <SearchResult> SearchResult forEachNode(NodeSearchCallback<SearchResult> callback) {
		return pipeline.deepForEach(callback);
	}

	public void applyCover(String cover, Class<Cnfg> config) throws IOException {
		applyCover(BlueprintCover.load(cover, config));
	}

	public void applyCover(File cover, Class<Cnfg> config) throws IOException {
		applyCover(BlueprintCover.load(cover, config));
	}

	public void applyCover(final BlueprintCover<Cnfg> cover) {
		if (cover.getGlobal() != null && global != null) {
			global.applyCover(cover.getGlobal());
		}
		cover.applyToBlueprint(this);
//		forEachNode(new NodeCallback() {
//			@Override
//			public Void doFor(Node node) {
//				if (node.getAliases() != null) {
//					for (String alias : node.getAliases()) {
//						NodeCover nodeCover = cover.getCover().get(alias);
//						if (nodeCover != null) {
//							node.applyAlias(nodeCover, getAliases().get(alias));
//						}
//					}
//				}
//				NodeCover nodeCover = cover.getCover().get(node.getId());
//				if (nodeCover != null) {
//					node.applyCover(nodeCover);
//				}
//				return null;
//			}
//		});
	}

	public HashMap<String, Alias> getAliases() {
		return aliases;
	}

	void setAliases(HashMap<String, Alias> aliases) {
		this.aliases = aliases;
	}

	public ArrayList<Node> select(Selector selector) {
		ArrayList<Node> list = new ArrayList<>();
		forEachSelected(selector, n -> {
			list.add(n);
			return null;
		});
		return list;
	}

}
