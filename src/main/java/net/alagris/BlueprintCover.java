package net.alagris;

import java.io.File;
import java.io.IOException;
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
public class BlueprintCover<T extends GlobalConfig> {
	private T global;

	private HashMap<String, NodeCover> cover = new HashMap<>();

	private static <T extends GlobalConfig> BlueprintCover<T> afterParsing(BlueprintCover<T> blueprint) {
		blueprint.getGlobal().onLoad();
		return blueprint;
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

	public HashMap<String, NodeCover> getCover() {
		return cover;
	}

	public void setCover(HashMap<String, NodeCover> cover) {
		this.cover = cover;
	}

}
