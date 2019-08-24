package net.alagris;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BlueprintTypedLoaderTestTest {

	Blueprint<GlobalCnfg> blueprint;
	BlueprintTest<String,DefaultStringBuilder, String> blueprintTest;
	PipeTestVerifier<String, String> verifier = new PipeStringTestVerifier();
	BlueprintTypedLoader<String, GlobalCnfg> loader;
	GroupTest<String, String> test;

	public BlueprintTypedLoaderTestTest() throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException, IllegalIdException, IllegalAliasException {
		loader = new BlueprintTypedLoader<String, GlobalCnfg>(BlueprintTypedLoaderTestTest.class, String.class,
				GlobalCnfg.class);
		blueprint = loader.load(TestConstants.PIPELINE);
		blueprintTest = BlueprintTest.load(TestConstants.TEST, DefaultStringBuilder.class, String.class, String.class);
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
		test.runWith(TestConstants.TEST2,DefaultStringBuilder.class, String.class, String.class);
		test.runWith(TestConstants.TEST3,DefaultStringBuilder.class, String.class, String.class);
	}

	@Test
	public void destroy() throws Exception {
		test.close();
	}
}
