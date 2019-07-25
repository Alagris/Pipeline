package net.alagris;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.alagris.TestConstants.Pair;

public class BlueprintTypedLoaderCmdCoverTest {

	Group<String> gr;
	Blueprint<GlobalCnfg> blueprint;

	public BlueprintTypedLoaderCmdCoverTest() throws JsonProcessingException, IOException, DuplicateIdException,
			ParseException, InstantiationException, IllegalAccessException, UndefinedAliasException {
		BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(
				BlueprintTypedLoaderCmdCoverTest.class, String.class, GlobalCnfg.class);
		CommandLineToCover cmd = new CommandLineToCover(
				new String[] { "lang=en-GB", "--Preprocessor-id", "suffix=-cmd", "paths=[one,two,three]" });
		blueprint = loader.load(TestConstants.PIPELINE);
		loader.applyCover(blueprint, cmd);
		gr = loader.make(blueprint);
	}

	@Test
	public void parsing() {
		assertEquals("Lang code wrong!", "en-GB", blueprint.getGlobal().get("lang", String.class));
		assertEquals("Country code wrong!", "GB", blueprint.getGlobal().get("country", String.class));
		assertArrayEquals("Ints wrong!", new int[] { 0, 14 }, blueprint.getGlobal().get("ints", int[].class));
		assertArrayEquals("Paths wrong!", new String[] { "tre", "tre4" },
				blueprint.getGlobal().get("paths", String[].class));

		HashMap<String, Node> nodes = blueprint.collectById();
		Node preprocessor = nodes.get("Preprocessor-id");
		HashMap<String, Object> preprocessorC = preprocessor.getConfig();
		assertEquals("Not enabled!", "true", preprocessorC.get("enabled"));
		assertEquals("Suffix wrong!", "-cmd", preprocessorC.get("suffix"));
		assertArrayEquals("Paths wrong!", new String[] { "one", "two", "three" },
				(String[]) preprocessorC.get("paths"));

	}

	@Test
	public void injection() {
		Preprocessor preprocessor = (Preprocessor) gr.findPipeworkById("Preprocessor-id").getPipe();

		assertEquals("Country code wrong!", "GB", preprocessor.country);
		assertEquals("Not enabled!", true, preprocessor.isEnabled());
		assertArrayEquals("Ints wrong!", new int[] { 0, 14 }, preprocessor.ints);
		assertArrayEquals("Paths wrong!", new String[] { "one", "two", "three" }, preprocessor.paths);
		assertEquals("dynPaths wrong!", "re", preprocessor.dynPaths.get(0));
		assertEquals("dynPaths wrong!", "move", preprocessor.dynPaths.get(1));
		assertEquals("dynPaths wrong!", "bugs", preprocessor.dynPaths.get(2));
		assertEquals("dynPaths wrong!", 3, preprocessor.dynPaths.size());
		assertEquals("Suffix wrong!", "-cmd", preprocessor.suffix);
		assertEquals("id wrong!", "Preprocessor-id", preprocessor.name);
        assertEquals("id wrong!", "Preprocessor-id", preprocessor.nameDuplicate);
	}

	@Test
	public void process() {
		Pair[] testSet = new Pair[] { new Pair("", "-CMD"), new Pair("a", "A-CMD"),
				new Pair("hello world", "HELLO WORLD-CMD"), new Pair("6237584", "6237584-CMD"),
				new Pair("_^$@(%#", "_^$@(%#-CMD"), };
		for (Pair p : testSet) {
			assertEquals(p.output, gr.process(p.intput).getValue());
		}
	}

	@Test
	public void destroy() throws Exception {
		gr.close();
	}
}
