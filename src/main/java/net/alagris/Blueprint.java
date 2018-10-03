package net.alagris;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class Blueprint<T extends GlobalConfig> {
	private T global;

	private ArrayList<Node> pipeline = new ArrayList<>();

	public ArrayList<Node> getPipeline() {
		return pipeline;
	}

	public void setPipeline(ArrayList<Node> pipeline) {
		this.pipeline = pipeline;
	}

	public static <T extends GlobalConfig> Blueprint<T> load(File f,Class<T> config) throws JsonProcessingException, IOException {
		return makeReader(config).readValue(f);
	}

	public static <T extends GlobalConfig> Blueprint<T> load(String s,Class<T> config) throws JsonProcessingException, IOException {
		
		return makeReader(config).readValue(s);
	}

	private static <T extends GlobalConfig> ObjectReader makeReader(Class<T> config) {
		ObjectMapper mapper = new ObjectMapper();
		JavaType type = mapper.getTypeFactory().constructParametricType(Blueprint.class, config);
		return mapper.reader(type);
	}

	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
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

}
