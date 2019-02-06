package net.alagris;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.alagris.TestConstants.Pair;

public class BlueprintTypedLoaderAbsTest {

	Group<String> gr;
	Blueprint<GlobalCnfg> blueprint;

	public BlueprintTypedLoaderAbsTest()
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(
		        BlueprintTypedLoaderAbsTest.class, String.class, GlobalCnfg.class);
		blueprint = loader.load(TestConstants.DEEPER);
		gr = loader.make(blueprint);
	}

	@Test
	public void parsing() {
	}

	@Test
	public void injection() {
	}

	@Test
	public void process() {
		Pair[] testSet = new Pair[] { new Pair("", " ABS1 DEEPER1"), new Pair("a", "A ABS1 DEEPER1"),
				new Pair("hello world", "HELLO WORLD ABS1 DEEPER1"), new Pair("6237584", "6237584 ABS1 DEEPER1"),
				new Pair("_^$@(%#", "_^$@(%# ABS1 DEEPER1"), };
		for (Pair p : testSet) {
			assertEquals(p.output, gr.process(p.intput).getValue());
		}
	}

	@Test
	public void destroy() throws Exception {
		gr.close();
	}
}
