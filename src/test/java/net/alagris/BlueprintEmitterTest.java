package net.alagris;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class BlueprintEmitterTest {

	BlueprintTypedLoader<String, GlobalCnfg> loader;

	public BlueprintEmitterTest() {
		loader = new BlueprintTypedLoader<String, GlobalCnfg>(BlueprintEmitterTest.class, String.class,
				GlobalCnfg.class);

	}

	@Test
	public void parsing() {

	}

	@Test
	public void injection() {

	}

	@Test
	public void process() throws Exception {
		final HashMap<String, Result<String>> results = new HashMap<>();
		ResultReceiver<String> receiver = new ResultReceiver<String>() {

			@Override
			public void receive(Result<String> result) {
				results.put((String) result.getCode(), result);
			}
		};
		Blueprint<GlobalCnfg> blueprint = loader.load(TestConstants.EMITTING_PIPELINE);
		Group<String> gr = loader.make(blueprint);
		gr.process("eRatRl  ", receiver);

		Result<String> upperFirst = results.get("upperFirst");
		assertEquals("Eratrl  ", upperFirst.getResult());
		assertEquals("test1", upperFirst.getDescription());

		Result<String> upperAll = results.get("upperAll");
		assertEquals("ERATRL  ", upperAll.getResult());
		assertEquals("test2", upperAll.getDescription());

		Result<String> trim = results.get("trim");
		assertEquals("eRatRl", trim.getResult());
		assertEquals("test3", trim.getDescription());

		Result<String> prefix = results.get("prefix");
		assertEquals("eRatRl  -t", prefix.getResult());
		assertEquals("test4", prefix.getDescription());

		gr.close();
	}

}
