package net.alagris;

import java.util.Map;

/**
 * Pipework is a mature version of Node. Each Pipework contains and supervises
 * one {@link Pipe}. It is not mutable. Multiple Pipeworks make up a {@link Group}.
 */
public class Pipework<T> implements AutoCloseable {

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

	// Not part of public interface
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

	@Override
	public void close() throws Exception {
		pipe.close();
	}

}
