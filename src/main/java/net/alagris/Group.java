package net.alagris;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Every Group consists of multiple Pipe components. Executing a group causes
 * each {@link Pipe} to process given input one by one. You cannot modify, add
 * or remove Group members.
 **/
public class Group<Cargo> implements Pipe<Cargo> {
	private final ArrayList<Pipework<Cargo>> group;
	private final ProcessingCallback<Cargo> callback;

	public int size() {
		return group.size();
	}

	public boolean isEmpty() {
		return group.isEmpty();
	}

	public Pipework<Cargo> get(int index) {
		return group.get(index);
	}

	public Group(ArrayList<Pipework<Cargo>> group) {
		this(group, null);
	}

	public Group(ArrayList<Pipework<Cargo>> group, ProcessingCallback<Cargo> callback) {
		this.group = group;
		this.callback = callback == null ? new DefaultProcessing<Cargo>() : callback;
	}

	@Override
	public Output<Cargo> process(Cargo input) {
		for (Pipework<Cargo> pipework : group) {
			input = callback.process(pipework, input);
		}
		return new Output<Cargo>(input);
	}

	public List<Pipework<Cargo>> getGroup() {
		return Collections.unmodifiableList(group);
	}

	public Pipework<Cargo> findPipeworkById(String id) {
		for (Pipework<Cargo> pipe : group) {
			if (pipe.getId().equals(id)) {
				return pipe;
			} else {
				for (Group<Cargo> gr : pipe.getAlternatives().values()) {
					Pipework<Cargo> p = gr.findPipeworkById(id);
					if (p != null) {
						return p;
					}
				}
			}
		}
		return null;
	}

	/** Recursively searches for pipe with given ID */
	public Pipework<Cargo> getById(String id) {
		for (Pipework<Cargo> pipe : group) {
			if (id.equals(pipe.getId())) {
				return pipe;
			}
			for (Group<Cargo> alt : pipe.getAlternatives().values()) {
				Pipework<Cargo> deeper = alt.getById(id);
				if (deeper != null)
					return deeper;
			}
		}
		return null;
	}

	/**
	 * Don't use this method. Pipes are loaded automatically by
	 * {@link BlueprintLoader}
	 */
	@Override
	public void onLoad() {
	}

	@Override
	public void close() throws Exception {
		for (Pipework<Cargo> pipe : group) {
			pipe.close();
		}
	}
}
