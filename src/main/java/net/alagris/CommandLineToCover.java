package net.alagris;

import java.text.ParseException;

/**
 * Allows for turning command-line arguments into BlueprintCover
 */
public class CommandLineToCover {

	private final String[] args;

	public CommandLineToCover(String[] args) {
		this.args = args;
	}

	public <T extends GlobalConfig> BlueprintCover<T> make(Class<T> config)
			throws ParseException, DuplicateIdException, InstantiationException, IllegalAccessException {
		BlueprintCover<T> cover = new BlueprintCover<>();
		cover.setGlobal(config.newInstance());
		NodeCover currentNode = null; // global is default
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (arg.startsWith("--")) { // new pipe id
				final String id = arg.substring(2);
				if (id.equals("")) {
					throw new ParseException("missing ID at argument " + i + "!", 0);
				}
				currentNode = new NodeCover();
				if (cover.getCover().put(id, currentNode) != null) {
					throw new DuplicateIdException("selector \"" + id + "\" is duplicated!");
				}
			} else { // new parameter
				int equalsSign = arg.indexOf('=');
				if (equalsSign < 0) {
					throw new ParseException("missing equal sign: " + arg, 0);
				}
				final String variable = arg.substring(0, equalsSign).trim();
				final String value = arg.substring(equalsSign + 1).trim();
				if (variable.equals("")) {
					throw new ParseException("missing variable name: " + arg, 0);
				}
				final Object objVal;
				if (value.equals("")) {
					objVal = null;
				} else if (value.startsWith("[") && value.endsWith("]")) {
					objVal = value.substring(1, value.length() - 1).split("\\s*,\\s*");
				} else {
					objVal = value;
				}
				if (currentNode == null) { // global
					cover.getGlobal().put(variable, objVal);
				} else { // current pipe
					currentNode.getConfig().put(variable, objVal);
				}
			}

		}
		return BlueprintCover.afterParsing(cover);
	}
}
