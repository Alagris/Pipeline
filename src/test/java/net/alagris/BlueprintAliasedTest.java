package net.alagris;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.alagris.TestConstants.Pair;

public class BlueprintAliasedTest {

	Group<String> gr;
	Blueprint<GlobalCnfg> blueprint;

	public BlueprintAliasedTest() throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(
				BlueprintAliasedTest.class, String.class, GlobalCnfg.class);
		blueprint = loader.load(TestConstants.ALIASED);
		loader.applyCover(blueprint, TestConstants.ALIASED_COVER);
		gr = loader.make(blueprint);
	}

	
	@Test
	public void parsing() {
		HashMap<String, ArrayList<Node>> nodes = blueprint.collectByAlias();
		ArrayList<Node> preprocessors = nodes.get("preprocessors");
		assertEquals(find(preprocessors,"Preprocessor-id0").getConfig().get("suffix"),"-s");
		assertEquals(find(preprocessors,"Preprocessor-id1").getConfig().get("suffix"),"-s");
		assertEquals(find(preprocessors,"Preprocessor-id2").getConfig().get("suffix"),"-s");
		ArrayList<Node> truecasers = nodes.get("truecasers");
		assertEquals(find(truecasers,"Lowercase-id").getConfig().get("enabled"),"false");
		assertEquals(find(truecasers,"Truecaser-id").getConfig().get("enabled"),"false");
		assertEquals(find(truecasers,"Uppercase-id").getConfig().get("enabled"),"false");
		
	}

	private Node find(ArrayList<Node> aliased, String id) {
		for(Node a:aliased) {
			if(id.equals(a.getId())) {
				return a;
			}
		}
		return null;
	}


	@Test
	public void injection() {
	}

	@Test
	public void process() {
		Pair[] testSet = new Pair[] { new Pair("", "-S-S-S"), new Pair("a", "A-S-S-S"),
				new Pair("hello world", "HELLO WORLD-S-S-S"), new Pair("6237584", "6237584-S-S-S"),
				new Pair("_^$@(%#", "_^$@(%#-S-S-S"), };
		for (Pair p : testSet) {
			assertEquals(p.output, gr.process(p.intput).getValue());
		}
	}

	@Test
	public void destroy() throws Exception {
		gr.close();
	}
}
