package net.alagris;

public class Output<T> {
	private final T value;
	private final String alternative;

	public static <T> Output<T> left(T value) {
		return new Output<T>(value, "left");
	}

	public static <T> Output<T> right(T value) {
		return new Output<T>(value, "right");
	}

	public static <T> Output<T> none(T value) {
		return new Output<T>(value);
	}

	public Output(T value) {
		this(value, null);
	}

	public Output(T value, String alternative) {
		this.value = value;
		this.alternative = alternative;
	}

	public T getValue() {
		return value;
	}

	public String getAlternative() {
		return alternative;
	}

}
