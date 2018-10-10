package net.alagris;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.alagris.TestConstants.Pair;

public class BlueprintTypedLoaderCoverTest {

	Group<String> gr;
	Blueprint<GlobalCnfg> blueprint;

	public BlueprintTypedLoaderCoverTest() throws JsonProcessingException, IOException, DuplicateIdException {
		BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(
				BlueprintTypedLoaderCoverTest.class, String.class, GlobalCnfg.class);
		blueprint = loader.load(TestConstants.PIPELINE);
		loader.applyCover(blueprint, TestConstants.COVER);
		gr = loader.make(blueprint);
	}

	@Test
	public void parsing() {
		assertEquals("Lang code wrong!", "pl-PL", blueprint.getGlobal().get("lang", String.class));
		assertEquals("Country code wrong!", "PL", blueprint.getGlobal().get("country", String.class));
		assertArrayEquals("Ints wrong!", new int[] { 99, 1499, 43 }, blueprint.getGlobal().get("ints", int[].class));
		assertArrayEquals("Paths wrong!", new String[] { "tap1", "tap2" },
				blueprint.getGlobal().get("paths", String[].class));

		HashMap<String, Node> nodes = blueprint.collectById();
		Node preprocessor = nodes.get("Preprocessor-id");
		HashMap<String, Object> preprocessorC = preprocessor.getConfig();
		assertEquals("Not enabled!", "true", preprocessorC.get("enabled"));
		assertEquals("Suffix wrong!", "-new", preprocessorC.get("suffix"));
	}

	@Test
	public void injection() {
		Preprocessor preprocessor = (Preprocessor) gr.findPipeworkById("Preprocessor-id").getPipe();

		assertEquals("Country code wrong!", "PL", preprocessor.country);
		assertEquals("Not enabled!", true, preprocessor.enabled);
		assertArrayEquals("Ints wrong!", new int[] { 99, 1499, 43 }, preprocessor.ints);
		assertArrayEquals("Paths wrong!", new String[] { "tap1", "tap2" }, preprocessor.paths);
		assertEquals("dynPaths wrong!", "re", preprocessor.dynPaths.get(0));
		assertEquals("dynPaths wrong!", "move", preprocessor.dynPaths.get(1));
		assertEquals("dynPaths wrong!", "bugs", preprocessor.dynPaths.get(2));
		assertEquals("dynPaths wrong!", 3, preprocessor.dynPaths.size());
		assertEquals("Suffix wrong!", "-new", preprocessor.suffix);
	}

	@Test
	public void process() {
		Pair[] testSet = new Pair[] { new Pair("", "-NEW"), new Pair("a", "A-NEW"),
				new Pair("hello world", "HELLO WORLD-NEW"), new Pair("6237584", "6237584-NEW"),
				new Pair("_^$@(%#", "_^$@(%#-NEW"), };
		for (Pair p : testSet) {
			assertEquals(p.output, gr.process(p.intput).getValue());
		}
	}

	@Test
	public void destroy() throws Exception {
		gr.close();
	}
}
