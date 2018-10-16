package net.alagris;

import java.util.Map;

/**
 * Pipework is a mature version of Node. Each Pipework contains and supervises
 * one {@link Pipe}. It is not mutable. Multiple Pipeworks make up a
 * {@link Group}.
 */
public class Pipework<Cargo> implements AutoCloseable {

	private final Map<String, Object> config;
	private final Map<String, Group<Cargo>> alternatives;
	private final Pipe<Cargo> pipe;
	private final String id;

	public Pipework(Map<String, Object> config, Map<String, Group<Cargo>> alternatives, Pipe<Cargo> pipe, String id) {
		this.config = config;
		this.alternatives = alternatives;
		this.pipe = pipe;
		this.id = id;
	}

	public Map<String, Object> getConfig() {
		return config;
	}

	public Map<String, Group<Cargo>> getAlternatives() {
		return alternatives;
	}

	public Pipe<Cargo> getPipe() {
		return pipe;
	}

	public Cargo process(Cargo input) throws Exception {
		Output<Cargo> out = pipe.process(input);
		Logger.pipeline.log(pipe.getClass().getSimpleName() + ":\t" + out.getValue().toString());
		Group<Cargo> alt = alternatives.get(out.getAlternative());
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
