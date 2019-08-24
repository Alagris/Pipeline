package net.alagris;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({ "selectors" })
public class BlueprintCover<T extends GlobalConfig> {
	private T global;

	private LinkedHashMap<String, NodeCover> cover = new LinkedHashMap<>();

	private static class SelectorAndCover {
		Selector selector;
		NodeCover cover;

		public SelectorAndCover(String selector, NodeCover cover) {
			this.cover = cover;
			this.selector = Selector.compile(selector);
		}

	}

	private ArrayList<SelectorAndCover> selectors;

	static <T extends GlobalConfig> BlueprintCover<T> afterParsing(BlueprintCover<T> blueprint) {
		if (blueprint.getGlobal() != null)
			blueprint.getGlobal().onLoad();

		blueprint.selectors = new ArrayList<>(blueprint.cover.size());
		for (Entry<String, NodeCover> entry : blueprint.cover.entrySet()) {
			blueprint.selectors.add(new SelectorAndCover(entry.getKey(), entry.getValue()));
		}
		return blueprint;
	}

	public static <T extends GlobalConfig> BlueprintCover<T> load(InputStream in, Class<T> config)
			throws JsonProcessingException, IOException {
		BlueprintCover<T> blueprint = makeReader(config).readValue(in);
		return afterParsing(blueprint);
	}

	public static <T extends GlobalConfig> BlueprintCover<T> load(File f, Class<T> config)
			throws JsonProcessingException, IOException {
		BlueprintCover<T> blueprint = makeReader(config).readValue(f);
		return afterParsing(blueprint);
	}

	public static <T extends GlobalConfig> BlueprintCover<T> load(String s, Class<T> config)
			throws JsonProcessingException, IOException {
		BlueprintCover<T> blueprint = makeReader(config).readValue(s);
		return afterParsing(blueprint);
	}

	private static <T extends GlobalConfig> ObjectReader makeReader(Class<T> config) {
		ObjectMapper mapper = new ObjectMapper();
		JavaType type = mapper.getTypeFactory().constructParametricType(BlueprintCover.class, config);
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

	public T getGlobal() {
		return global;
	}

	public void setGlobal(T global) {
		this.global = global;
	}

	LinkedHashMap<String, NodeCover> getCover() {
		return cover;
	}

	void setCover(LinkedHashMap<String, NodeCover> cover) {
		this.cover = cover;
	}

	public <Cnfg extends GlobalConfig> void applyToBlueprint(final Blueprint<Cnfg> blueprint) {
		if (getGlobal() != null && blueprint.getGlobal() != null) {
			blueprint.getGlobal().applyCover(getGlobal());
		}
		for (SelectorAndCover selectorAndCover : selectors) {
			blueprint.forEachSelected(selectorAndCover.selector, n -> {
				n.applyCover(selectorAndCover.cover);
				return null;
			});
		}

	}
}
