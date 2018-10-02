package net.alagris;

import java.util.Map;

public class Pipework<T> {

	private final Map<String, String> config;
	private final Map<String, Group<T>> alternatives;
	private final Pipe<T> pipe;
	private final String id;

	public Pipework(Map<String, String> config, Map<String, Group<T>> alternatives, Pipe<T> pipe, String id) {
		this.config = config;
		this.alternatives = alternatives;
		this.pipe = pipe;
		this.id = id;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public Map<String, Group<T>> getAlternatives() {
		return alternatives;
	}

	public Pipe<T> getPipe() {
		return pipe;
	}

	T process(T input) {
		Output<T> out = pipe.process(input);
		if (Logger.verbose) {
			System.out.println(pipe.getClass().getSimpleName() + ":\t" + out.getValue().toString());
		}
		Group<T> alt = alternatives.get(out.getAlternative());
		if (alt != null) {
			return alt.process(out.getValue()).getValue();
		}
		return out.getValue();
	}

	public String getId() {
		return id;
	}

}
