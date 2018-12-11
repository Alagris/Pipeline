package net.alagris;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ObservableConfigTest {

	private static class GlobalCnfg extends DoubleHashGlobalConfig{
		@Override
		public void onLoad() {
			ObservableConfig<String> observed = new ObservableConfig<>();
			setProgrammaticOpt("observed",observed);
		}
		@Override
		public void onMake() {
			String def = get("defaultObserved",String.class);
			@SuppressWarnings("unchecked")
			ObservableConfig<String> observed = get("observed", ObservableConfig.class);
			observed.setValue(def);
			
		}
	}
	
	Group<String> gr;
	Blueprint<GlobalCnfg> blueprint;

	public ObservableConfigTest()
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(
				BlueprintTypedLoaderTest.class, String.class, GlobalCnfg.class);
		blueprint = loader.load(TestConstants.OBSERVER_PIPELINE);
		gr = loader.make(blueprint);
	}
	
	@Test
	public void test() {
		GlobalCnfg cnfg = blueprint.getGlobal();
		@SuppressWarnings("unchecked")
		ObservableConfig<String> observed = cnfg.get("observed", ObservableConfig.class);
		assertEquals("initial", observed.getValue());
		Observer observerPipe = (Observer) gr.getById("Observer-id").getPipe();
		assertEquals("initial", observerPipe.observed.getValue());
		assertEquals("initial", observerPipe.lastValue);
		assertEquals("hello initial", gr.process("hello").getValue());
		observed.setValue("2");
		assertEquals("hello 2", gr.process("hello").getValue());
		observed.setValue("");
		assertEquals("hello ", gr.process("hello").getValue());
		observed.setValue("jger");
		assertEquals("hello jger", gr.process("hello").getValue());
	}
}
