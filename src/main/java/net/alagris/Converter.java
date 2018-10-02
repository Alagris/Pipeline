package net.alagris;

public interface Converter<From, To> {
	To convert(From f);
}