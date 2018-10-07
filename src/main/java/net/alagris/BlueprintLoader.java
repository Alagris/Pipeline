package net.alagris;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

	public <T, C extends GlobalConfig> Group<T> make(String json, Class<T> workType, Class<C> config)
			throws JsonProcessingException, IOException, DuplicateIdException {
		return make(Blueprint.load(json, config), workType);
	}

	public <T, C extends GlobalConfig> Group<T> make(File f, Class<T> workType, Class<C> config)
			throws JsonProcessingException, IOException, DuplicateIdException {
		return make(Blueprint.load(f, config), workType);
	}

	public <T, C extends GlobalConfig> Group<T> make(Blueprint<C> blueprint, Class<T> workType) {
		return make(blueprint.getPipeline(), workType, blueprint.getGlobal());
	}

	private <T, C extends GlobalConfig> Group<T> make(ArrayList<Node> pipeline, final Class<T> workType,
			final C globalConfig) {

		final ArrayList<Pipework<T>> gr = ArrayLists.convert(new Converter<Node, Pipework<T>>() {

			@Override
			public Pipework<T> convert(Node f) {
				final Map<String, Object> cnfg = Collections.unmodifiableMap(f.getConfig());
				final HashMap<String, Group<T>> alts = makeAlternatives(workType, globalConfig, f);
				final Map<String, Group<T>> unmodAlts = Collections.unmodifiableMap(alts);
				final String className = modulesPackage + "." + f.getName();
				final Pipe<T> pipe = buildPipe(globalConfig, cnfg, className);
				return new Pipework<T>(cnfg, unmodAlts, pipe, f.getId());
			}

			private Pipe<T> buildPipe(final C globalConfig, final Map<String, Object> cnfg, final String className) {
				try {
					@SuppressWarnings("unchecked")
					final Class<Pipe<T>> pipeClass = (Class<Pipe<T>>) Class.forName(className);
					final Pipe<T> pipe = pipeClass.newInstance();
					injectFields(globalConfig, cnfg, pipe, pipeClass);
					pipe.onLoad();
					return pipe;
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}

			}

			private HashMap<String, Group<T>> makeAlternatives(final Class<T> workType, final C globalConfig, Node f) {
				return HashMaps.convert(new Converter<ArrayList<Node>, Group<T>>() {
					@Override
					public Group<T> convert(ArrayList<Node> f) {
						return make(f, workType, globalConfig);
					}
				}, f.getAlternatives());
			}

			private void injectFields(final C globalConfig, final Map<String, Object> cnfg, final Pipe<T> pipe,
					final Class<Pipe<T>> pipeClass) {
				ReflectionUtils.doWithFields(pipeClass, new FieldCallback() {

					@SuppressWarnings("unchecked")
					<T2> void setField(Class<T2> c, Object value, Field field)
							throws IllegalArgumentException, IllegalAccessException {
						field.set(pipe, (T2) value);
					}

					<T2> void setField(Class<T2> c, Field field, Config annotation)
							throws IllegalArgumentException, IllegalAccessException {
						setField(c, makeObject(globalConfig, field, annotation), field);
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
							return globalConfig.get(name, field.getType());
						} else {
							return Classes.parseObject(field.getType(), val);
						}
					}
				});
			}
		}, pipeline);

		return new Group<T>(gr);
	}
}
