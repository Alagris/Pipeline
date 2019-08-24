package net.alagris;

interface SearchCallback<T, SearchResult> {
	/**
	 * Various iterators and for-each methods may use this callback. The standard
	 * behavior is to keep iterating until user decided to return something else
	 * than null. If non-null result is returned, iterators will stop and just
	 * hand this value back to caller.
	 */
	SearchResult doFor(T t);
}
