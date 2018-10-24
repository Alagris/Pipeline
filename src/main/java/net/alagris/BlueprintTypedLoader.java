package net.alagris;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Provides BlueprintLoader with automatically filled type parameters. Just a
 * convenience class.
 */
public class BlueprintTypedLoader<Cargo, Cnfg extends GlobalConfig> extends BlueprintLoader
		implements BlueprintLoader.Callbacks<Cargo> {

	private final Class<Cargo> cargo;
	private final Class<Cnfg> config;

	private PipeLog<Cargo> logger = new PipeLog<Cargo>() {

		@Override
		public void log(Pipework<Cargo> pipework, Output<Cargo> out) {
			System.out.println(pipework.getTitle() + "\t\t" + out.getValue().toString());
		}
	};

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

	public Blueprint<Cnfg> load(File f)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return Blueprint.load(f, getConfig());
	}

	public Blueprint<Cnfg> load(String s)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return Blueprint.load(s, getConfig());
	}

	public Blueprint<Cnfg> load(InputStream i)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return Blueprint.load(i, getConfig());
	}

	public Group<Cargo> make(InputStream in)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(in, getCargo(), getConfig(), this);
	}

	public Group<Cargo> make(String json)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(json, getCargo(), getConfig(), this);
	}

	public Group<Cargo> make(File f)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(f, getCargo(), getConfig(), this);
	}

	public Group<Cargo> make(Blueprint<Cnfg> blueprint) {
		return make(blueprint, getCargo(), this);
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
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return makeTest(load(blueprintFile), verifier);
	}

	public <UnitTest> GroupTest<Cargo, UnitTest> makeTest(String blueprintJson,
			PipeTestVerifier<Cargo, UnitTest> verifier, Class<UnitTest> unit)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return makeTest(load(blueprintJson), verifier);
	}

	public <UnitTest> GroupTest<Cargo, UnitTest> makeTest(Blueprint<Cnfg> blueprint,
			PipeTestVerifier<Cargo, UnitTest> verifier, Class<UnitTest> unit) {
		return makeTest(blueprint, verifier);
	}

	public <UnitTest> GroupTest<Cargo, UnitTest> makeTest(Blueprint<Cnfg> blueprint,
			PipeTestVerifier<Cargo, UnitTest> verifier) {
		return makeTest(verifier, cargo, blueprint, this);
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

	@Override
	public PipeLog<Cargo> getLogger() {
		return logger;
	}

	public void setLogger(PipeLog<Cargo> logger) {
		this.logger = logger;
	}


}
