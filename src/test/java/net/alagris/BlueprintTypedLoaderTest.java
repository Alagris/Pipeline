package net.alagris;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.alagris.TestConstants.Pair;

public class BlueprintTypedLoaderTest {

	Group<String> gr;
	Blueprint<GlobalCnfg> blueprint;

	public BlueprintTypedLoaderTest() throws JsonProcessingException, IOException, DuplicateIdException {
		BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(
				BlueprintTypedLoaderTest.class, String.class, GlobalCnfg.class);
		blueprint = loader.load(TestConstants.PIPELINE);
		gr = loader.make(blueprint);
	}

	@Test
	public void parsing() {
		assertEquals("Lang code wrong!", "es-ES", blueprint.getGlobal().get("lang", String.class));
		assertEquals("Country code wrong!", "ES", blueprint.getGlobal().get("country", String.class));
		assertArrayEquals("Ints wrong!", new int[] { 0, 14 }, blueprint.getGlobal().get("ints", int[].class));
		assertArrayEquals("Paths wrong!", new String[] { "tre", "tre4" },
				blueprint.getGlobal().get("paths", String[].class));

		HashMap<String, Node> nodes = blueprint.collectById();
		Node preprocessor = nodes.get("Preprocessor-id");
		HashMap<String, Object> preprocessorC = preprocessor.getConfig();
		assertEquals("Not enabled!", "true", preprocessorC.get("enabled"));
		assertEquals("Suffix wrong!", "-t", preprocessorC.get("suffix"));
	}

	@Test
	public void injection() {
		Preprocessor preprocessor = (Preprocessor) gr.findPipeworkById("Preprocessor-id").getPipe();
		assertEquals("Country code wrong!", "ES", preprocessor.country);
		assertEquals("Not enabled!", true, preprocessor.enabled);
		assertArrayEquals("Ints wrong!", new int[] { 0, 14 }, preprocessor.ints);
		assertArrayEquals("Paths wrong!", new String[] { "tre", "tre4" }, preprocessor.paths);
		assertEquals("Suffix wrong!", "-t", preprocessor.suffix);
		assertEquals("dynPaths wrong!", "move", preprocessor.dynPaths.get(1));
		assertEquals("dynPaths wrong!", "bugs", preprocessor.dynPaths.get(2));
		assertEquals("dynPaths wrong!", 3, preprocessor.dynPaths.size());
	}

	@Test
	public void process() {
		Pair[] testSet = new Pair[] { new Pair("", "-T"), new Pair("a", "A-T"),
				new Pair("hello world", "HELLO WORLD-T"), new Pair("6237584", "6237584-T"),
				new Pair("_^$@(%#", "_^$@(%#-T"), };
		for (Pair p : testSet) {
			assertEquals(p.output, gr.process(p.intput).getValue());
		}
	}

	@Test
	public void destroy() throws Exception {
		gr.close();
	}
}
