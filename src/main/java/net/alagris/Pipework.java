package net.alagris;

import java.util.Map;

/**
 * Pipework is a mature version of Node. Each Pipework contains and supervises
 * one {@link Pipe}. It is not mutable. Multiple Pipeworks make up a
 * {@link Group}.
 */
public class Pipework<Cargo> implements AutoCloseable {

	private final Map<String, Group<Cargo>> alternatives;
	private final Pipe<Cargo> pipe;
	private final String id;
	private final PipeLog<Cargo> logger;

	public Pipework(Map<String, Group<Cargo>> alternatives, Pipe<Cargo> pipe, String id, PipeLog<Cargo> logger) {
		this.alternatives = alternatives;
		this.pipe = pipe;
		this.id = id;
		this.logger = logger;
	}

	public Map<String, Group<Cargo>> getAlternatives() {
		return alternatives;
	}

	public Pipe<Cargo> getPipe() {
		return pipe;
	}

	public Cargo process(Cargo input) throws Exception {
		Output<Cargo> out = pipe.process(input);
		logger.log(this, out);
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

	/**
	 * If present, the title is the same as ID but if no ID was supplied, the title
	 * becomes name of {@link Pipe}'s class
	 */
	public String getTitle() {
		return getId() == null ? getPipe().getClass().getSimpleName() : getId();
	}

}
