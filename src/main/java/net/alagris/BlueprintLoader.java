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

	private <T, C extends GlobalConfig> Group<T> make(ArrayList<Node> pipeline, Class<T> workType, C globalConfig) {

		ArrayList<Pipework<T>> gr = ArrayLists.convert(new Converter<Node, Pipework<T>>() {

			@Override
			public Pipework<T> convert(Node f) {
				Map<String, String> cnfg = Collections.unmodifiableMap(f.getConfig());
				HashMap<String, Group<T>> alts = HashMaps.convert(new Converter<ArrayList<Node>, Group<T>>() {
					@Override
					public Group<T> convert(ArrayList<Node> f) {
						return make(f, workType, globalConfig);
					}
				}, f.getAlternatives());
				Map<String, Group<T>> unmodAlts = Collections.unmodifiableMap(alts);
				String className = modulesPackage + "." + f.getName();
				Pipe<T> pipe;
				try {
					@SuppressWarnings("unchecked")
					Class<Pipe<T>> pipeClass = (Class<Pipe<T>>) Class.forName(className);
					pipe = pipeClass.newInstance();
					ReflectionUtils.doWithFields(pipeClass, new FieldCallback() {
						@Override
						public void doFor(Field field) {
							if (field.isAnnotationPresent(Config.class)) {
								try {
									String val = cnfg.get(field.getName());
									Object obj = val == null ? globalConfig.get(field.getName(), field.getType())
											: Classes.parseString(field.getType(), val);
									if (obj == null)
										return;
									field.set(pipe, obj);
								} catch (IllegalArgumentException | IllegalAccessException e) {
									throw new RuntimeException(e);
								}
							}
						}
					});
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
