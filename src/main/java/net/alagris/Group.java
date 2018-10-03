package net.alagris;

import java.util.ArrayList;

public class Group<T> implements Pipe<T> {
	private final ArrayList<Pipework<T>> group;

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

	public ArrayList<Pipework<T>> getGroup() {
		return group;
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

	@Override
	public void onLoad() {
	}
}
