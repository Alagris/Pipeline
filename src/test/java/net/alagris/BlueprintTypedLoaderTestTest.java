package net.alagris;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BlueprintTypedLoaderTestTest {

	Blueprint<GlobalCnfg> blueprint;
	BlueprintTest<String, String> blueprintTest;
	PipeTestVerifier<String, String> verifier = new PipeStringTestVerifier();
	BlueprintTypedLoader<String, GlobalCnfg> loader;
	GroupTest<String, String> test;

	public BlueprintTypedLoaderTestTest() throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		loader = new BlueprintTypedLoader<String, GlobalCnfg>(BlueprintTypedLoaderTestTest.class, String.class,
				GlobalCnfg.class);
		blueprint = loader.load(TestConstants.PIPELINE);
		blueprintTest = BlueprintTest.load(TestConstants.TEST, String.class, String.class);
		test = loader.makeTest(blueprint, verifier);
	}

	@Test
	public void parsing() {
	}

	@Test
	public void injection() {
	}

	@Test
	public void process() throws JsonProcessingException, IOException {
		test.runWith(blueprintTest);
		test.runWith(TestConstants.TEST2, String.class, String.class);
		test.runWith(TestConstants.TEST3, String.class, String.class);
	}

	@Test
	public void destroy() throws Exception {
		test.close();
	}
}
