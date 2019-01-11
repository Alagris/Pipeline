package net.alagris;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BlueprintFailTest {

	Group<String> gr;
	Blueprint<GlobalCnfg> blueprint;
	BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(
			BlueprintFailTest.class, String.class, GlobalCnfg.class);

	public BlueprintFailTest()
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		blueprint = loader.load(TestConstants.FAILING);
	}

	@Test
	public void parsing() {
	}

	@Test
	public void injection() {
	}

	@Test
	public void process() throws Exception {
		final ArrayList<Class<?>> loadFails = new ArrayList<>();
		loader.setLoadFailCallback(new LoadFailCallback() {
			@Override
			public <Cargo> void fail(Pipe<Cargo> pipe, Class<Pipe<Cargo>> pipeClass, Map<String, Object> cnfg,
					String id, Exception e) {
				loadFails.add(pipeClass);
			}
		});
		final ArrayList<Class<?>> processingFails = new ArrayList<>();
		loader.setProcessingExceptionCallback(new ProcessingExceptionCallback<String>() {
			@Override
			public void fail(Exception e, String input, Pipework<String> pipework) {
				processingFails.add(pipework.getPipe().getClass());
			}
		});

		gr = loader.make(blueprint);
		assertEquals(1, loadFails.size());
		assertEquals(InstaFail.class, loadFails.get(0));
		assertNotNull(gr);
		gr.process("");
		assertEquals(1, processingFails.size());
		assertEquals(InstaFail.class, processingFails.get(0));
		gr.close();
	}

}
