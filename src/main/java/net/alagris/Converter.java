package net.alagris;

//Not part of public interface
interface Converter<From, To> {
	To convert(From f);
}