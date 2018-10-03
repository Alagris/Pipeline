package net.alagris;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BlueprintLoader {

	private final String modulesPackage;

	public BlueprintLoader(String modulesPackage) {
		this.modulesPackage = modulesPackage;
	}

	public <T, C extends GlobalConfig> Group<T> make(String json, Class<T> workType, Class<C> config)
			throws JsonProcessingException, IOException {
		return make(Blueprint.load(json, config), workType);
	}

	public <T, C extends GlobalConfig> Group<T> make(File f, Class<T> workType, Class<C> config)
			throws JsonProcessingException, IOException {
		return make(Blueprint.load(f, config), workType);
	}

	public <T, C extends GlobalConfig> Group<T> make(Blueprint<C> blueprint, Class<T> workType) {
		return make(blueprint.getPipeline(), workType, blueprint.getGlobal());
	}

	private <T, C extends GlobalConfig> Group<T> make(ArrayList<Node> pipeline, final Class<T> workType,
			final C globalConfig) {

		ArrayList<Pipework<T>> gr = ArrayLists.convert(new Converter<Node, Pipework<T>>() {

			@Override
			public Pipework<T> convert(Node f) {
				final Map<String, String> cnfg = Collections.unmodifiableMap(f.getConfig());
				HashMap<String, Group<T>> alts = HashMaps.convert(new Converter<ArrayList<Node>, Group<T>>() {
					@Override
					public Group<T> convert(ArrayList<Node> f) {
						return make(f, workType, globalConfig);
					}
				}, f.getAlternatives());
				Map<String, Group<T>> unmodAlts = Collections.unmodifiableMap(alts);
				String className = modulesPackage + "." + f.getName();
				final Pipe<T> pipe;
				try {
					@SuppressWarnings("unchecked")
					final Class<Pipe<T>> pipeClass = (Class<Pipe<T>>) Class.forName(className);
					pipe = pipeClass.newInstance();
					ReflectionUtils.doWithFields(pipeClass, new FieldCallback() {
						@SuppressWarnings("unchecked")
						<T2> void setField(Class<T2> c, Object value, Field field)
								throws IllegalArgumentException, IllegalAccessException {
							field.set(pipe, (T2) value);
						}

						@Override
						public void doFor(Field field) {
							if (field.isAnnotationPresent(Config.class)) {
								try {
									final String name = field.getName();
									final String val = cnfg.get(name);
									final Object obj;
									if (val == null) {
										obj = globalConfig.get(name, field.getType());
									} else {
										obj = Classes.parseObject(field.getType(), val);
									}
									setField(field.getType(), obj, field);

								} catch (Exception e) {
									throw new RuntimeException(
											"Failed injecting " + pipeClass.getName() + "." + field.getName(), e);
								}
							}
						}
					});
					pipe.onLoad();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				Pipework<T> pipework = new Pipework<T>(cnfg, unmodAlts, pipe, f.getId());
				return pipework;
			}
		}, pipeline);

		return new Group<T>(gr);
	}
}
