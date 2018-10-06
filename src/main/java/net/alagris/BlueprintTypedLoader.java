package net.alagris;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Provides BlueprintLoader with automatically filled type parameters. Just a
 * convenience class.
 */
public class BlueprintTypedLoader<T, C extends GlobalConfig> extends BlueprintLoader {

	private final Class<T> workType;
	private final Class<C> config;

	public BlueprintTypedLoader(String modulesPackage, Class<T> workType, Class<C> config) {
		super(modulesPackage);
		this.workType = workType;
		this.config = config;
	}

	public BlueprintTypedLoader(Class<?> modulesPackageForClass, Class<T> workType, Class<C> config) {
		super(modulesPackageForClass);
		this.workType = workType;
		this.config = config;
	}

	public BlueprintTypedLoader(Package modulesPackage, Class<T> workType, Class<C> config) {
		super(modulesPackage);
		this.workType = workType;
		this.config = config;
	}

	public Blueprint<C> load(File f) throws JsonProcessingException, IOException, DuplicateIdException {
		return Blueprint.load(f, config);
	}

	public Blueprint<C> load(String s) throws JsonProcessingException, IOException, DuplicateIdException {
		return Blueprint.load(s, config);
	}

	public Group<T> make(String json) throws JsonProcessingException, IOException, DuplicateIdException {
		return make(json, workType, config);
	}

	public Group<T> make(File f) throws JsonProcessingException, IOException, DuplicateIdException {
		return make(f, workType, config);
	}

	public Group<T> make(Blueprint<C> blueprint) throws JsonProcessingException, IOException, DuplicateIdException {
		return make(blueprint, workType);
	}

	public void applyCover(Blueprint<C> blueprint, File cover) throws IOException {
		blueprint.applyCover(cover, config);
	}

	public void applyCover(Blueprint<C> blueprint, String cover) throws IOException {
		blueprint.applyCover(cover, config);
	}

	public void applyCover(Blueprint<C> blueprint, BlueprintCover<C> cover) throws IOException {
		blueprint.applyCover(cover);
	}
}
