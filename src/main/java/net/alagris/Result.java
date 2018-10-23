package net.alagris;

/**
 * Represents result emitted from pipeline. Pipeline could emit multiple results
 * and any point of its processing. Every {@link Result} is identified by
 * <code>code</code> and may also contain optional human-readable description
 * (great for debugging).
 */
public class Result<Cargo> {
	private Cargo result;
	private String description;
	private Object code;

	public Result(Cargo result, Object code) {
		this(result, "", code);
	}

	public Result(Cargo result, String description, Object code) {
		this.result = result;
		this.description = description;
		this.code = code;
	}

	public Cargo getResult() {
		return result;
	}

	public void setResult(Cargo result) {
		this.result = result;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object getCode() {
		return code;
	}

	public void setCode(Object code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "(" + code.toString() + ") " + result.toString();
	}

}
