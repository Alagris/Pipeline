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

	private ProcessingExceptionCallback processingExceptionCallback = new DefaultProcessingExceptionCallback();

	public ProcessingExceptionCallback getProcessingExceptionCallback() {
		return processingExceptionCallback;
	}

	public void setProcessingExceptionCallback(ProcessingExceptionCallback processingExceptionCallback) {
		this.processingExceptionCallback = processingExceptionCallback;
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
		public ResultReceiver<Cargo> getResultReceiver();

		public PipeLog<Cargo> getLogger();
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
		return make(blueprint.getPipeline(), cargo, blueprint.getGlobal(), processing, loadFailCallback,
				callbacks.getLogger(), callbacks.getResultReceiver());
	}

	public <T, C extends GlobalConfig> Group<T> make(Blueprint<C> blueprint, Class<T> cargo, Callbacks<T> callbacks) {
		return make(blueprint, cargo, new DefaultProcessing<T>(processingExceptionCallback), callbacks);
	}

	private <Cargo, C extends GlobalConfig> Group<Cargo> make(ArrayList<Node> pipeline, final Class<Cargo> cargo,
			final C globalConfig, final ProcessingCallback<Cargo> processing, final LoadFailCallback loadFailCallback,
			final PipeLog<Cargo> logger, final ResultReceiver<Cargo> resultReceiver) {

		final ArrayList<Pipework<Cargo>> gr = ArrayLists.convert(new Converter<Node, Pipework<Cargo>>() {

			@Override
			public Pipework<Cargo> convert(Node f) {
				final Map<String, Object> cnfg = Collections.unmodifiableMap(f.getConfig());
				final HashMap<String, Group<Cargo>> alts = makeAlternatives(cargo, globalConfig, f);
				final Map<String, Group<Cargo>> unmodAlts = Collections.unmodifiableMap(alts);
				final String className = modulesPackage + "." + f.getName();
				final Pipe<Cargo> pipe = buildPipe(globalConfig, cnfg, className, f.getId());
				return new Pipework<Cargo>(unmodAlts, pipe, f.getId(), logger, resultReceiver,
						f.isRunAllAlternatives());
			}

			private Pipe<Cargo> buildPipe(final C globalConfig, final Map<String, Object> cnfg, final String className,
					String id) {
				try {
					@SuppressWarnings("unchecked")
					final Class<Pipe<Cargo>> pipeClass = (Class<Pipe<Cargo>>) Class.forName(className);
					final Pipe<Cargo> pipe = pipeClass.getDeclaredConstructor().newInstance();
					injectFields(globalConfig, cnfg, pipe, pipeClass);
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
						return make(f, cargo, globalConfig, processing, loadFailCallback, logger, resultReceiver);
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
			Verifier verifier, Class<Cargo> cargo, Blueprint<Cnfg> blueprint, Callbacks<Cargo> callbacks) {
		TestProcessingCallback<Cargo, TestUnit, PipeTestVerifier<Cargo, TestUnit>> processing = new TestProcessingCallback<Cargo, TestUnit, PipeTestVerifier<Cargo, TestUnit>>(
				verifier, processingExceptionCallback);
		Group<Cargo> test = make(blueprint, cargo, processing, callbacks);
		return new GroupTest<>(test, processing);
	}

}
