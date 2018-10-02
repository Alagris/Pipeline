package net.alagris;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class Blueprint {

	private ArrayList<Node> pipeline = new ArrayList<>();

	public ArrayList<Node> getPipeline() {
		return pipeline;
	}

	public void setPipeline(ArrayList<Node> pipeline) {
		this.pipeline = pipeline;
	}

	public static Blueprint load(File f) throws JsonProcessingException, IOException {
		return makeReader().readValue(f);
	}

	public static Blueprint load(String s) throws JsonProcessingException, IOException {
		return makeReader().readValue(s);
	}

	private static ObjectReader makeReader() {
		return new ObjectMapper().reader(Blueprint.class);
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

}
