package net.alagris;

import java.util.List;

public class Output<Cargo> {
	private final Cargo value;
	private final String alternative;
	private final List<Result<Cargo>> results;

	public static <Cargo> Output<Cargo> left(Cargo value) {
		return left(value, null);
	}

	public static <Cargo> Output<Cargo> right(Cargo value) {
		return right(value, null);
	}

	public static <Cargo> Output<Cargo> none(Cargo value) {
		return none(value, null);
	}

	public static <Cargo> Output<Cargo> left(Cargo value, List<Result<Cargo>> resultsToEmit) {
		return new Output<Cargo>(value, "left", resultsToEmit);
	}

	public static <Cargo> Output<Cargo> right(Cargo value, List<Result<Cargo>> resultsToEmit) {
		return new Output<Cargo>(value, "right", resultsToEmit);
	}

	public static <Cargo> Output<Cargo> none(Cargo value, List<Result<Cargo>> resultsToEmit) {
		return new Output<Cargo>(value, null, resultsToEmit);
	}

	public Output(Cargo value) {
		this(value, null);
	}

	public Output(Cargo value, String alternative) {
		this(value, alternative, null);
	}

	public Output(Cargo value, String alternative, List<Result<Cargo>> resultsToEmit) {
		this.value = value;
		this.alternative = alternative;
		this.results = resultsToEmit;
	}

	public Cargo getValue() {
		return value;
	}

	public String getAlternative() {
		return alternative;
	}

	/** Never returns null (all nulls are converted into empty list) */
	public List<Result<Cargo>> getResults() {
		return results;
	}

}
