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
	private final boolean runAllAlternatives;

	public Pipework(Map<String, Group<Cargo>> alternatives, Pipe<Cargo> pipe, String id, PipeLog<Cargo> logger,
			boolean runAllAlternatives) {
		this.alternatives = alternatives;
		this.pipe = pipe;
		this.id = id;
		this.logger = logger;
		this.runAllAlternatives = runAllAlternatives;
	}

	public Map<String, Group<Cargo>> getAlternatives() {
		return alternatives;
	}

	public Pipe<Cargo> getPipe() {
		return pipe;
	}

	public Cargo process(Cargo input, ResultReceiver<Cargo> resultReceiver) throws Exception {
		Output<Cargo> out = pipe.process(input);
		logger.log(this, out);
		if (out.getResults() != null) {
			for (Result<Cargo> result : out.getResults()) {
				resultReceiver.receive(result);
			}
		}
		if (isRunAllAlternatives()) {
			for (Group<Cargo> alt : alternatives.values()) {
				alt.process(out.getValue(), resultReceiver);
			}
		} else {
			Group<Cargo> alt = alternatives.get(out.getAlternative());
			if (alt != null) {
				return alt.process(out.getValue(), resultReceiver).getValue();
			}
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

    public boolean isRunAllAlternatives() {
        return runAllAlternatives;
    }

}
