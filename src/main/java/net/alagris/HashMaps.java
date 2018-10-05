package net.alagris;

import java.util.HashMap;
import java.util.Map.Entry;


//Not part of public interface
class HashMaps {

	public static <From, To, Key> HashMap<Key, To> convert(Converter<From, To> c, HashMap<Key, From> a) {
		HashMap<Key, To> out = new HashMap<>(a.size());
		for (Entry<Key, From> entry : a.entrySet()) {
			out.put(entry.getKey(), c.convert(entry.getValue()));
		}
		return out;
	}
}
