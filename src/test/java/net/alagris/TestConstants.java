package net.alagris;

import java.io.File;

class TestConstants {
	static final File PIPELINE = new File("src/test/res/pipeline.json");
	static final File OBSERVER_PIPELINE = new File("src/test/res/observer_pipeline.json");
	static final File COVER = new File("src/test/res/cover.json");
	static final File ALIASED = new File("src/test/res/aliased.json");
	static final File FAILING = new File("src/test/res/failing_pipeline.json");
	static final File ALIASED_COVER = new File("src/test/res/aliased_cover.json");
	static final File TEST = new File("src/test/res/test.json");
	static final File TEST2 = new File("src/test/res/test2.json");
	static final File TEST3 = new File("src/test/res/test3.json");
	static final File EMITTING_PIPELINE = new File("src/test/res/emitting_pipeline.json");

	static final class Pair {
		String intput, output;

		public Pair(String intput, String output) {
			this.intput = intput;
			this.output = output;
		}
	}

}
