package net.alagris;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Provides BlueprintLoader with automatically filled type parameters. Just a
 * convenience class.
 */
public class BlueprintTypedLoader<Cargo, Cnfg extends GlobalConfig> extends BlueprintLoader {

	private final Class<Cargo> cargo;
	private final Class<Cnfg> config;

	public BlueprintTypedLoader(String modulesPackage, Class<Cargo> cargo, Class<Cnfg> config) {
		super(modulesPackage);
		this.cargo = cargo;
		this.config = config;
	}

	public BlueprintTypedLoader(Class<?> modulesPackageForClass, Class<Cargo> cargo, Class<Cnfg> config) {
		super(modulesPackageForClass);
		this.cargo = cargo;
		this.config = config;
	}

	public BlueprintTypedLoader(Package modulesPackage, Class<Cargo> cargo, Class<Cnfg> config) {
		super(modulesPackage);
		this.cargo = cargo;
		this.config = config;
	}

	public Blueprint<Cnfg> load(File f) throws JsonProcessingException, IOException, DuplicateIdException {
		return Blueprint.load(f, getConfig());
	}

	public Blueprint<Cnfg> load(String s) throws JsonProcessingException, IOException, DuplicateIdException {
		return Blueprint.load(s, getConfig());
	}

	public Group<Cargo> make(String json) throws JsonProcessingException, IOException, DuplicateIdException {
		return make(json, getCargo(), getConfig());
	}

	public Group<Cargo> make(File f) throws JsonProcessingException, IOException, DuplicateIdException {
		return make(f, getCargo(), getConfig());
	}

	public Group<Cargo> make(Blueprint<Cnfg> blueprint) {
		return make(blueprint, getCargo());
	}

	public <UnitTest> BlueprintTest<Cargo, UnitTest> loadTest(File testFile, Class<UnitTest> unit)
			throws JsonProcessingException, IOException {
		return BlueprintTest.load(testFile, cargo, unit);
	}

	public <UnitTest> BlueprintTest<Cargo, UnitTest> loadTest(String testJson, Class<UnitTest> unit)
			throws JsonProcessingException, IOException {
		return BlueprintTest.load(testJson, cargo, unit);
	}

	public <UnitTest> GroupTest<Cargo, UnitTest> makeTest(File blueprintFile,
			PipeTestVerifier<Cargo, UnitTest> verifier, Class<UnitTest> unit)
			throws JsonProcessingException, IOException, DuplicateIdException {
		return makeTest(load(blueprintFile), verifier);
	}

	public <UnitTest> GroupTest<Cargo, UnitTest> makeTest(String blueprintJson,
			PipeTestVerifier<Cargo, UnitTest> verifier, Class<UnitTest> unit)
			throws JsonProcessingException, IOException, DuplicateIdException {
		return makeTest(load(blueprintJson), verifier);
	}

	public <UnitTest> GroupTest<Cargo, UnitTest> makeTest(Blueprint<Cnfg> blueprint,
			PipeTestVerifier<Cargo, UnitTest> verifier, Class<UnitTest> unit) {
		return makeTest(blueprint, verifier);
	}

	public <UnitTest> GroupTest<Cargo, UnitTest> makeTest(Blueprint<Cnfg> blueprint,
			PipeTestVerifier<Cargo, UnitTest> verifier) {
		return makeTest(this, verifier, cargo, blueprint);
	}

	public BlueprintCover<Cnfg> makeCover(String... args)
			throws ParseException, DuplicateIdException, InstantiationException, IllegalAccessException {
		return makeCover(new CommandLineToCover(args));
	}

	public BlueprintCover<Cnfg> makeCover(CommandLineToCover cmd)
			throws ParseException, DuplicateIdException, InstantiationException, IllegalAccessException {
		return cmd.make(getConfig());
	}

	public void applyCover(Blueprint<Cnfg> blueprint, File cover) throws IOException {
		blueprint.applyCover(cover, getConfig());
	}

	public void applyCover(Blueprint<Cnfg> blueprint, String cover) throws IOException {
		blueprint.applyCover(cover, getConfig());
	}

	public void applyCover(Blueprint<Cnfg> blueprint, BlueprintCover<Cnfg> cover) {
		blueprint.applyCover(cover);
	}

	public BlueprintCover<Cnfg> applyCover(Blueprint<Cnfg> blueprint, CommandLineToCover cmd)
			throws ParseException, DuplicateIdException, InstantiationException, IllegalAccessException {
		BlueprintCover<Cnfg> cover = makeCover(cmd);
		applyCover(blueprint, cover);
		return cover;
	}

	public Class<Cargo> getCargo() {
		return cargo;
	}

	public Class<Cnfg> getConfig() {
		return config;
	}

}
