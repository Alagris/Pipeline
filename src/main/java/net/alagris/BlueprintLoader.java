package net.alagris;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * BlueprintLoader can turn your {@link Blueprint } to {@link Group} . You
 * cannot modify configurations of {@link Group} but you can execute it.
 */
public class BlueprintLoader {

	private final String modulesPackage;

	public BlueprintLoader(String modulesPackage) {
		this.modulesPackage = modulesPackage;
	}

	public BlueprintLoader(Package modulesPackage) {
		this(modulesPackage.getName());
	}

	public BlueprintLoader(Class<?> modulesPackageForClass) {
		this(modulesPackageForClass.getPackage());
	}

	public <T, C extends GlobalConfig> Group<T> make(String json, Class<T> cargo, Class<C> config,
			ProcessingCallback<T> processing)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(Blueprint.load(json, config), cargo, processing);
	}

	public <T, C extends GlobalConfig> Group<T> make(String json, Class<T> cargo, Class<C> config)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(json, cargo, config, null);
	}

	public <T, C extends GlobalConfig> Group<T> make(File f, Class<T> cargo, Class<C> config,
			ProcessingCallback<T> processing)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(Blueprint.load(f, config), cargo, processing);
	}

	public <T, C extends GlobalConfig> Group<T> make(File f, Class<T> cargo, Class<C> config)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(f, cargo, config, null);
	}

	public <T, C extends GlobalConfig> Group<T> make(Blueprint<C> blueprint, Class<T> cargo,
			ProcessingCallback<T> processing) {
		return make(blueprint.getPipeline(), cargo, blueprint.getGlobal(), processing);
	}

	public <T, C extends GlobalConfig> Group<T> make(Blueprint<C> blueprint, Class<T> cargo) {
		return make(blueprint, cargo, null);
	}

	private <Cargo, C extends GlobalConfig> Group<Cargo> make(ArrayList<Node> pipeline, final Class<Cargo> cargo,
			final C globalConfig, final ProcessingCallback<Cargo> processing) {

		final ArrayList<Pipework<Cargo>> gr = ArrayLists.convert(new Converter<Node, Pipework<Cargo>>() {

			@Override
			public Pipework<Cargo> convert(Node f) {
				final Map<String, Object> cnfg = Collections.unmodifiableMap(f.getConfig());
				final HashMap<String, Group<Cargo>> alts = makeAlternatives(cargo, globalConfig, f);
				final Map<String, Group<Cargo>> unmodAlts = Collections.unmodifiableMap(alts);
				final String className = modulesPackage + "." + f.getName();
				final Pipe<Cargo> pipe = buildPipe(globalConfig, cnfg, className);
				return new Pipework<Cargo>(cnfg, unmodAlts, pipe, f.getId());
			}

			private Pipe<Cargo> buildPipe(final C globalConfig, final Map<String, Object> cnfg,
					final String className) {
				try {
					@SuppressWarnings("unchecked")
					final Class<Pipe<Cargo>> pipeClass = (Class<Pipe<Cargo>>) Class.forName(className);
					final Pipe<Cargo> pipe = pipeClass.newInstance();
					injectFields(globalConfig, cnfg, pipe, pipeClass);
					pipe.onLoad();
					return pipe;
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}

			}

			private HashMap<String, Group<Cargo>> makeAlternatives(final Class<Cargo> cargo, final C globalConfig,
					Node f) {
				return HashMaps.convert(new Converter<ArrayList<Node>, Group<Cargo>>() {
					@Override
					public Group<Cargo> convert(ArrayList<Node> f) {
						return make(f, cargo, globalConfig, processing);
					}
				}, f.getAlternatives());
			}

			private void injectFields(final C globalConfig, final Map<String, Object> cnfg, final Pipe<Cargo> pipe,
					final Class<Pipe<Cargo>> pipeClass) {
				ReflectionUtils.doWithFields(pipeClass, new FieldCallback() {

					@SuppressWarnings("unchecked")
					<T2> void setField(Class<T2> c, Object value, Field field)
							throws IllegalArgumentException, IllegalAccessException {
						field.set(pipe, (T2) value);
					}

					<T2> void setField(Class<T2> c, Field field, Config annotation)
							throws IllegalArgumentException, IllegalAccessException {
						try {
							setField(c, makeObject(globalConfig, field, annotation), field);
						} catch (NoSuchElementException e) {
							// Nothing to inject and that's ok
						}
					}

					@Override
					public void doFor(Field field) {
						Config annotation = field.getAnnotation(Config.class);
						if (annotation != null) {
							try {
								setField(field.getType(), field, annotation);
							} catch (Exception e) {
								throw new RuntimeException(
										"Failed injecting " + pipeClass.getName() + "." + field.getName(), e);
							}
						}
					}

					private Object makeObject(final C globalConfig, Field field, Config annotation) {
						String name = annotation.value();
						if (name == null || name.equals("")) {
							name = field.getName();
						}
						return makeObject(globalConfig, field, name);
					}

					private Object makeObject(final C globalConfig, Field field, final String name) {
						return makeObject(globalConfig, field, name, cnfg.get(name));
					}

					private Object makeObject(final C globalConfig, Field field, final String name, final Object val) {
						if (val == null) {
							if (globalConfig == null) {
								throw new NoSuchElementException();
							}
							return globalConfig.get(name, field.getType());
						} else {
							Object out = Classes.parseObject(field.getType(), val);
							if (out == null) {
								throw new NoSuchElementException();
							}
							return out;
						}
					}
				});
			}
		}, pipeline);

		return new Group<Cargo>(gr, processing);
	}

	public <Cargo, TestUnit, Verifier extends PipeTestVerifier<Cargo, TestUnit>, Cnfg extends GlobalConfig> GroupTest<Cargo, TestUnit> makeTest(
			BlueprintLoader loader, Verifier verifier, Class<Cargo> cargo, Blueprint<Cnfg> blueprint) {
		TestProcessingCallback<Cargo, TestUnit, PipeTestVerifier<Cargo, TestUnit>> processing = new TestProcessingCallback<Cargo, TestUnit, PipeTestVerifier<Cargo, TestUnit>>(
				verifier);
		Group<Cargo> test = loader.make(blueprint, cargo, processing);
		return new GroupTest<>(test, processing);
	}
}
