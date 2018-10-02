package net.alagris;

import java.lang.reflect.Field;

public final class ReflectionUtils {
	private ReflectionUtils() {
	}

	public static void doWithFields(Class<?> baseClass, FieldCallback callback) {
		while (baseClass != null) {
			for (Field field : baseClass.getDeclaredFields()) {
				callback.doFor(field);
			}
			baseClass = baseClass.getSuperclass();
		}
	}

}
