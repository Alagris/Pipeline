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

public class BlueprintTest<Cargo, Builder extends CargoBuilder<Cargo>, TestUnit> {

    private Builder input;

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

    public static <Cargo, Builder extends CargoBuilder<Cargo>,TestUnit> BlueprintTest<Cargo, Builder, TestUnit> load(InputStream in,
            Class<Builder> builder, Class<Cargo> cargo, Class<TestUnit> unit)
            throws JsonProcessingException, IOException {
        BlueprintTest<Cargo, Builder, TestUnit> blueprint = makeReader(builder, cargo, unit).readValue(in);
        return blueprint;
    }

    public static <Cargo, Builder extends CargoBuilder<Cargo>, TestUnit> BlueprintTest<Cargo, Builder, TestUnit> load(File f,
            Class<Builder> builder, Class<Cargo> cargo, Class<TestUnit> unit)
            throws JsonProcessingException, IOException {
        BlueprintTest<Cargo, Builder, TestUnit> blueprint = makeReader(builder, cargo, unit).readValue(f);
        return blueprint;
    }

    public static <Cargo, Builder extends CargoBuilder<Cargo>, TestUnit> BlueprintTest<Cargo, Builder, TestUnit> load(String s,
            Class<Builder> builder, Class<Cargo> cargo, Class<TestUnit> unit)
            throws JsonProcessingException, IOException {
        BlueprintTest<Cargo, Builder, TestUnit> blueprint = makeReader(builder, cargo, unit).readValue(s);
        return blueprint;
    }

    private static <Cargo, Builder extends CargoBuilder<Cargo>, TestUnit> ObjectReader makeReader(Class<Builder> builder, Class<Cargo> cargo,
            Class<TestUnit> unit) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructParametricType(BlueprintTest.class, cargo, builder, unit);
        return mapper.readerFor(type);
    }

    public Builder getInput() {
        return input;
    }

    public void setInput(Builder input) {
        this.input = input;
    }

}
