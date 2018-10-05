package net.alagris;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Every Group consists of multiple Pipe components. Executing a group causes
 * each {@link Pipe} to process given input one by one. You cannot modify, add
 * or remove Group members.
 **/
public class Group<T> implements Pipe<T> {
	private final ArrayList<Pipework<T>> group;

	public int size() {
		return group.size();
	}

	public boolean isEmpty() {
		return group.isEmpty();
	}

	public Pipework<T> get(int index) {
		return group.get(index);
	}

	public Group(ArrayList<Pipework<T>> group) {
		this.group = group;
	}

	@Override
	public Output<T> process(T input) {
		for (Pipework<T> pipe : group) {
			input = pipe.process(input);
		}
		return new Output<T>(input);
	}

	public List<Pipework<T>> getGroup() {
		return Collections.unmodifiableList(group);
	}

	public Pipework<T> findPipeworkById(String id) {
		for (Pipework<T> pipe : group) {
			if (pipe.getId().equals(id)) {
				return pipe;
			} else {
				for (Group<T> gr : pipe.getAlternatives().values()) {
					Pipework<T> p = gr.findPipeworkById(id);
					if (p != null) {
						return p;
					}
				}
			}
		}
		return null;
	}

	/** Recursively searches for pipe with given ID */
	public Pipework<T> getById(String id) {
		for (Pipework<T> pipe : group) {
			if (id.equals(pipe.getId())) {
				return pipe;
			}
			for (Group<T> alt : pipe.getAlternatives().values()) {
				Pipework<T> deeper = alt.getById(id);
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
		for (Pipework<T> pipe : group) {
			pipe.close();
		}
	}
}
