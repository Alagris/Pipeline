package net.alagris;

import java.util.ArrayList;

public final class ArrayLists {

	private ArrayLists() {
	}

	public static <From, To> ArrayList<To> convert(Converter<From, To> c, ArrayList<From> a) {
		ArrayList<To> out = new ArrayList<>(a.size());
		for (From from : a) {
			out.add(c.convert(from));
		}
		return out;
	}
}
