package net.alagris;

import java.io.File;

class TestConstants {
	static final File PIPELINE = new File("src/test/res/pipeline.json");
	static final File COVER = new File("src/test/res/cover.json");

	static final class Pair {
		String intput, output;

		public Pair(String intput, String output) {
			this.intput = intput;
			this.output = output;
		}
	}


}
