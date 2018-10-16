package net.alagris;

public class Logger {

	public static interface Log {
		void log(String str);
	}

	/** Logs steps of pipeline */
	public static Log pipeline = new Log() {
		@Override
		public void log(String str) {
			System.out.println(str);
		}
	};

	/**
	 * Logs warnings while parsing and checking JSON configurations. Notice that
	 * more serious issues will throw exceptions. Only those negligible issues are
	 * logged as warning.
	 */
	public static Log jsonWarnings = new Log() {
		@Override
		public void log(String str) {
			System.err.println(str);
		}
	};

}
