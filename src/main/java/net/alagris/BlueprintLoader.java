package net.alagris;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

	public static final LoadFailCallback DEFAULT_LOAD_FAIL_CALLBACK = new LoadFailCallback() {
		@Override
		public <Cargo> void fail(Pipe<Cargo> pipe, Class<Pipe<Cargo>> pipeClass, Map<String, Object> cnfg, String id,
				Exception e) {
			e.printStackTrace();
		}
	};

	private LoadFailCallback loadFailCallback = DEFAULT_LOAD_FAIL_CALLBACK;

	public LoadFailCallback getLoadFailCallback() {
		return loadFailCallback;
	}

	public void setLoadFailCallback(LoadFailCallback loadFailCallback) {
		this.loadFailCallback = loadFailCallback;
	}

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

	/** Contains set of callback functions that operate on Cargo generic type */
	public static interface Callbacks<Cargo> {
//		public ResultReceiver<Cargo> getResultReceiver();

		public PipeLog<Cargo> getLogger();

		public ProcessingExceptionCallback<Cargo> getProcessingExceptionCallback();
	}

	public <T, C extends GlobalConfig> Group<T> make(InputStream in, Class<T> cargo, Class<C> config,
			ProcessingCallback<T> processing, Callbacks<T> callbacks)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(Blueprint.load(in, config), cargo, processing, callbacks);
	}

	public <T, C extends GlobalConfig> Group<T> make(InputStream in, Class<T> cargo, Class<C> config,
			Callbacks<T> callbacks)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(Blueprint.load(in, config), cargo, callbacks);
	}

	public <T, C extends GlobalConfig> Group<T> make(String json, Class<T> cargo, Class<C> config,
			ProcessingCallback<T> processing, Callbacks<T> callbacks)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(Blueprint.load(json, config), cargo, processing, callbacks);
	}

	public <T, C extends GlobalConfig> Group<T> make(String json, Class<T> cargo, Class<C> config,
			Callbacks<T> callbacks)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(Blueprint.load(json, config), cargo, callbacks);
	}

	public <T, C extends GlobalConfig> Group<T> make(File f, Class<T> cargo, Class<C> config,
			ProcessingCallback<T> processing, Callbacks<T> callbacks)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(Blueprint.load(f, config), cargo, processing, callbacks);
	}

	public <T, C extends GlobalConfig> Group<T> make(File f, Class<T> cargo, Class<C> config, Callbacks<T> callbacks)
			throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException {
		return make(Blueprint.load(f, config), cargo, callbacks);
	}

	public <Cargo, C extends GlobalConfig> Group<Cargo> make(Blueprint<C> blueprint, Class<Cargo> cargo,
			ProcessingCallback<Cargo> processing, Callbacks<Cargo> callbacks) {
		if (blueprint.getGlobal() != null)
			blueprint.getGlobal().onMake();
		return make(blueprint.getPipeline(), cargo, blueprint.getGlobal(), processing, loadFailCallback,
				callbacks.getLogger());
	}

	public <T, C extends GlobalConfig> Group<T> make(Blueprint<C> blueprint, Class<T> cargo, Callbacks<T> callbacks) {
		return make(blueprint, cargo, new DefaultProcessing<T>(callbacks.getProcessingExceptionCallback()), callbacks);
	}

	/**
	 * IMPORTANT!! Remember to always call blueprint.getGlobal().onMake(); before
	 * executing this method
	 **/
	private <Cargo, C extends GlobalConfig> Group<Cargo> make(ArrayList<Node> pipeline, final Class<Cargo> cargo,
			final C globalConfig, final ProcessingCallback<Cargo> processing, final LoadFailCallback loadFailCallback,
			final PipeLog<Cargo> logger) {

		final ArrayList<Pipework<Cargo>> gr = ArrayLists.convert(new Converter<Node, Pipework<Cargo>>() {

			@Override
			public Pipework<Cargo> convert(Node f) {
				final Map<String, Object> cnfg = Collections.unmodifiableMap(f.getConfig());
				final HashMap<String, Group<Cargo>> alts = makeAlternatives(cargo, globalConfig, f);
				final Map<String, Group<Cargo>> unmodAlts = Collections.unmodifiableMap(alts);
				//if f.getName() contains . then it's probably absolute path to class
				final String className = f.getName().contains(".") ? f.getName() : modulesPackage + "." + f.getName();
				final Pipe<Cargo> pipe = buildPipe(globalConfig, cnfg, className, f.getId());
				return new Pipework<Cargo>(unmodAlts, pipe, f.getId(), logger, f.isRunAllAlternatives());
			}

			private Pipe<Cargo> buildPipe(final C globalConfig, final Map<String, Object> cnfg, final String className,
					String id) {
				try {
					@SuppressWarnings("unchecked")
					final Class<Pipe<Cargo>> pipeClass = (Class<Pipe<Cargo>>) Class.forName(className);
					final Pipe<Cargo> pipe = pipeClass.getDeclaredConstructor().newInstance();
					injectFields(globalConfig, cnfg, pipe, pipeClass, id);
					try {
						pipe.onLoad();
					} catch (Exception e) {
						loadFailCallback.fail(pipe, pipeClass, cnfg, id, e);
					}
					return pipe;
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | NoSuchMethodException
						| SecurityException e) {
					throw new RuntimeException(e);
				}

			}

			private HashMap<String, Group<Cargo>> makeAlternatives(final Class<Cargo> cargo, final C globalConfig,
					Node f) {
				return HashMaps.convert(new Converter<ArrayList<Node>, Group<Cargo>>() {
					@Override
					public Group<Cargo> convert(ArrayList<Node> f) {
						return make(f, cargo, globalConfig, processing, loadFailCallback, logger);
					}
				}, f.getAlternatives());
			}

			private void injectFields(final C globalConfig, final Map<String, Object> cnfg, final Pipe<Cargo> pipe,
					final Class<Pipe<Cargo>> pipeClass, final String id) {
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
						Config configAnnotation = field.getAnnotation(Config.class);
						PipeID nameAnnotation = field.getAnnotation(PipeID.class);
						if (configAnnotation != null) {
						    if(nameAnnotation!=null) {
						        throw new RuntimeException(
                                        "Field " + pipeClass.getName() + "." + field.getName()+" has both @Config and @PipeName. Choose only one of them!");
						    }
							try {
								setField(field.getType(), field, configAnnotation);
							} catch (Exception e) {
								throw new RuntimeException(
										"Failed injecting " + pipeClass.getName() + "." + field.getName(), e);
							}
						}else {
						    if(nameAnnotation!=null) {
						        if(field.getType().equals(String.class)){
						            try {
                                        field.set(pipe, id);
                                    } catch (IllegalArgumentException | IllegalAccessException e) {
                                        throw new RuntimeException(
                                                "Failed injecting " + pipeClass.getName() + "." + field.getName(), e);
                                    }
						        }else {
						            throw new RuntimeException(
	                                        "Field " + pipeClass.getName() + "." + field.getName()+" has @PipeName therefore it must be of String type!");
						        }
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

	public <Cargo, TestUnit, Cnfg extends GlobalConfig> GroupTest<Cargo, TestUnit> makeTest(
	        PipeTestVerifier<Cargo, TestUnit> verifier, Class<Cargo> cargo, Blueprint<Cnfg> blueprint, Callbacks<Cargo> callbacks) {
		TestProcessingCallback<Cargo, TestUnit, PipeTestVerifier<Cargo, TestUnit>> processing = new TestProcessingCallback<Cargo, TestUnit, PipeTestVerifier<Cargo, TestUnit>>(
				verifier, callbacks.getProcessingExceptionCallback());
		Group<Cargo> test = make(blueprint, cargo, processing, callbacks);
		return new GroupTest<>(test, processing);
	}

}
