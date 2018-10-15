package net.alagris;

public class Logger {

	public static interface Log {
		void log(String str);
	}

	public static Log pipeline = new Log() {
		@Override
		public void log(String str) {
			System.out.println(str);
		}
	};
	
	public static Log jsonWarnings = new Log() {
		@Override
		public void log(String str) {
			System.err.println(str);
		}
	};
}
