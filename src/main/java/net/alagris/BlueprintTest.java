package net.alagris;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class BlueprintTest<Cargo, TestUnit> {

	private Cargo input;

	@JsonProperty("test")
	private HashMap<String, NodeTest<TestUnit>> testUnits = new HashMap<>();

	public HashMap<String, NodeTest<TestUnit>> getTestUnits() {
		return testUnits;
	}

	public void setTestUnits(HashMap<String, NodeTest<TestUnit>> testUnits) {
		this.testUnits = testUnits;
	}

	public NodeTest<TestUnit> testForId(String id) {
		return testUnits.get(id);
	}

	public static <Cargo, TestUnit> BlueprintTest<Cargo, TestUnit> load(InputStream in, Class<Cargo> cargo,
			Class<TestUnit> unit) throws JsonProcessingException, IOException {
		BlueprintTest<Cargo, TestUnit> blueprint = makeReader(cargo, unit).readValue(in);
		return blueprint;
	}
	
	public static <Cargo, TestUnit> BlueprintTest<Cargo, TestUnit> load(File f, Class<Cargo> cargo,
			Class<TestUnit> unit) throws JsonProcessingException, IOException {
		BlueprintTest<Cargo, TestUnit> blueprint = makeReader(cargo, unit).readValue(f);
		return blueprint;
	}

	public static <Cargo, TestUnit> BlueprintTest<Cargo, TestUnit> load(String s, Class<Cargo> cargo,
			Class<TestUnit> unit) throws JsonProcessingException, IOException {
		BlueprintTest<Cargo, TestUnit> blueprint = makeReader(cargo, unit).readValue(s);
		return blueprint;
	}

	private static <Cargo, TestUnit> ObjectReader makeReader(Class<Cargo> cargo, Class<TestUnit> unit) {
		ObjectMapper mapper = new ObjectMapper();
		JavaType type = mapper.getTypeFactory().constructParametricType(BlueprintTest.class, cargo, unit);
		return mapper.readerFor(type);
	}

	public Cargo getInput() {
		return input;
	}

	public void setInput(Cargo input) {
		this.input = input;
	}

}
