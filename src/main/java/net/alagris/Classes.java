package net.alagris;

public final class Classes {
	private Classes() {
	}

	public static Object parseString(Class<?> type, String val) {
		if (type.isAssignableFrom(Boolean.class)) {
			return Boolean.valueOf(val);
		} else if (type.isAssignableFrom(boolean.class)) {
			return Boolean.parseBoolean(val);
		} else if (type.isAssignableFrom(Integer.class)) {
			return Integer.valueOf(val);
		} else if (type.isAssignableFrom(int.class)) {
			return Integer.parseInt(val);
		} else if (type.isAssignableFrom(Float.class)) {
			return Float.valueOf(val);
		} else if (type.isAssignableFrom(float.class)) {
			return Float.parseFloat(val);
		} else if (type.isAssignableFrom(Double.class)) {
			return Double.valueOf(val);
		} else if (type.isAssignableFrom(double.class)) {
			return Double.parseDouble(val);
		} else if (type.isAssignableFrom(Byte.class)) {
			return Byte.valueOf(val);
		} else if (type.isAssignableFrom(byte.class)) {
			return Byte.parseByte(val);
		} else if (type.isAssignableFrom(Short.class)) {
			return Short.valueOf(val);
		} else if (type.isAssignableFrom(short.class)) {
			return Short.parseShort(val);
		} else if (type.isAssignableFrom(Long.class)) {
			return Long.valueOf(val);
		} else if (type.isAssignableFrom(long.class)) {
			return Long.parseLong(val);
		}
		return val;
	}
}
