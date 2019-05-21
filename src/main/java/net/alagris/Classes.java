package net.alagris;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

//Not part of public interface
final class Classes {
    private Classes() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Object toArray(List<?> list, Class<T> componentType) {
        if (list instanceof ArrayList) {
            ArrayList<?> arrList = (ArrayList<?>) list;
            if (componentType.isPrimitive()) {
                return toPrimitiveArray(list, componentType);
            }
            T[] arr0 = (T[]) Array.newInstance(componentType, 0);
            return arrList.toArray(arr0);
        } else {
            T[] arr = (T[]) Array.newInstance(componentType, list.size());
            int i = 0;
            for (Object elem : list) {
                arr[i++] = (T) elem;
            }
            return arr;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> parseStringArray(Class<T> type, List<String> val) {
        if (type == String.class) {
            return (List<T>) val;
        }
        Converter<String, T> parser = parser(type);
        return ArrayLists.convert(parser, val);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object parseObject(Class<?> type, Object val) {
        if (val == null) {
            if (type.isPrimitive()) {
                if (type == boolean.class) {
                    return false;
                } else {
                    return 0;
                }
            } else {
                return null;
            }
        }
        if (type.isAssignableFrom(val.getClass())) {
            return val;
        }
        if(val.getClass().isPrimitive()) {
            if(isPrimitiveWrapper(type)) {
                try {
                    return wrapPrimitive(convertPrimitiveType(type,val));
                } catch (ClassCastException e) {
                    assert false: e.getMessage() + " primitive val: "+val.getClass()+" wrapper type: "+ type  + new Callable<String>() {
                        @Override
                        public String call() {
                            e.printStackTrace();
                            return "";
                        }
                    }.call();
                }
            }else if(type.isPrimitive()) {
                try {
                    return convertPrimitiveType(type,val);
                } catch (ClassCastException e) {
                    assert false: e.getMessage() + " primitive val: "+val.getClass()+" primitive type: "+ type + new Callable<String>() {
                        @Override
                        public String call() {
                            e.printStackTrace();
                            return "";
                        }
                    }.call();
                }
            }
        }else if(isPrimitiveWrapper(val.getClass())) {
            if(type.isPrimitive()) {
                try {
                    return unwrapPrimitive(type, val);
                } catch (ClassCastException e) {
                    assert false: e.getMessage() + " wrapper val: "+val.getClass()+" primitive type: "+ type + new Callable<String>() {
                        @Override
                        public String call() {
                            e.printStackTrace();
                            return "";
                        }
                    }.call();
                }
            }else if(isPrimitiveWrapper(type)) {
                try {
                    return wrapPrimitive(unwrapPrimitive(type, val));
                } catch (ClassCastException e) {
                    assert false: e.getMessage() + " wrapper val: "+val.getClass()+" wapper type: "+ type + new Callable<String>() {
                        @Override
                        public String call() {
                            e.printStackTrace();
                            return "";
                        }
                    }.call();
                }
            }
        }
        if (type.isArray()) { // we need to return array
        	final Class<?> targetComp = type.getComponentType();
        	if(val.getClass().isArray()) { // we get array
        		int len = Array.getLength(val);
        		Object arr = Array.newInstance(targetComp, len);
        		for(int i = 0;i < len;i++) {
        			Array.set(arr, i, parseObject(targetComp, Array.get(val, i)));
        		}
            	return arr;
        	}else if (List.class.isAssignableFrom(val.getClass())) { // we get a list
        		try { // we need to parse strings in given list
                	List<String> strList = (List<String>) val;
                    final List<?> list = parseStringArray(targetComp, strList);
                    return toArray(list, targetComp);
                } catch (ClassCastException e) {
                }
        		//we just need to convert lists
        		List<?> genericList = (List<?>) val;
        		if(targetComp.isPrimitive()) {
        			return toPrimitiveArray(genericList, targetComp);
        		}
        		if(genericList.isEmpty()) {//special case to handle
    				return Collections.emptyList().toArray();
    			}
        		return convertListToArray(targetComp,genericList);
        	}
        } else if (type.isEnum()) {
            try {
                return Enum.valueOf((Class<Enum>) type, (String) val);
            } catch (ClassCastException e) {
            }
        } else if (val.getClass() == String.class) {
            Object out = parseString(type, (String) val);
            if (out != null)
                return out;
        } else if (val.getClass() == ArrayList.class) {
            try {
                Constructor<?> c = type.getConstructor(ArrayList.class);
                return c.newInstance(val);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
            }
        } else if (val.getClass() == HashMap.class) {
            try {
                Constructor<?> c = type.getConstructor(HashMap.class);
                return c.newInstance(val);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
            }
        }
        
        try {
            return type.cast(val);
        } catch (ClassCastException e) {
        }
        
        throw new UnsupportedOperationException(
                "Could not find any way to convert " + val.getClass().getName() + " to " + type.getName());
    }

    public static Object convertPrimitiveType(Class<?> type, Object val) {
        if (type == boolean.class) {
            return (boolean) val;
        } else if (type == int.class) {
            return (int)val;
        } else if (type == float.class) {
            return (float) val;
        } else if (type == double.class) {
            return (double) val;
        } else if (type == byte.class) {
            return (byte)val;
        } else if (type == short.class) {
            return (short)val;
        } else if (type == long.class) {
            return (long)val;
        } else if (type == char.class) {
            return (char)val;
        }
        throw new UnsupportedOperationException(type.getName() + " is not primitive!");
    }
   

	public static Object parseString(Class<?> type, String val) {
        if (type == Boolean.class) {
            return Boolean.valueOf(val);
        } else if (type == boolean.class) {
            return Boolean.parseBoolean(val);
        } else if (type == Integer.class) {
            return Integer.valueOf(val);
        } else if (type == int.class) {
            return Integer.parseInt(val);
        } else if (type == Float.class) {
            return Float.valueOf(val);
        } else if (type == float.class) {
            return Float.parseFloat(val);
        } else if (type == Double.class) {
            return Double.valueOf(val);
        } else if (type == double.class) {
            return Double.parseDouble(val);
        } else if (type == Byte.class) {
            return Byte.valueOf(val);
        } else if (type == byte.class) {
            return Byte.parseByte(val);
        } else if (type == Short.class) {
            return Short.valueOf(val);
        } else if (type == short.class) {
            return Short.parseShort(val);
        } else if (type == Long.class) {
            return Long.valueOf(val);
        } else if (type == long.class) {
            return Long.parseLong(val);
        } else if (type == Character.class) {
            return Character.valueOf(val.charAt(0));
        } else if (type == char.class) {
            return val.charAt(0);
        } else if (type.isAssignableFrom(String.class)) {
            return val;
        } else if (type == Pattern.class) {
            return Pattern.compile(val);
        }
        try {
            Constructor<?> c = type.getConstructor(String.class);
            return c.newInstance(val);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
        }
        return null;
    }

    public static <To> Converter<String, To> parser(final Class<To> type) {
    	if(type.isAssignableFrom(Object.class)) {
    		return new Converter<String, To>() {
				@SuppressWarnings("unchecked")
				@Override
				public To convert(String f) {
					return (To) f;
				}
			};
    	}else if (type.isAssignableFrom(Boolean.class) || type == boolean.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Boolean.valueOf(f);
                }
            };
        } else if (type.isAssignableFrom(Integer.class) || type == int.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Integer.valueOf(f);
                }
            };
        } else if (type.isAssignableFrom(Short.class) || type == short.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Short.valueOf(f);
                }
            };
        } else if (type.isAssignableFrom(Long.class) || type == long.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Long.valueOf(f);
                }
            };
        } else if (type.isAssignableFrom(Byte.class) || type == byte.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Byte.valueOf(f);
                }
            };
        } else if (type.isAssignableFrom(Float.class) || type == float.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Float.valueOf(f);
                }
            };
        } else if (type.isAssignableFrom(Double.class) || type == double.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Float.valueOf(f);
                }
            };
        } else if (type.isAssignableFrom(Character.class) || type == char.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Character.valueOf(f.charAt(0));
                }
            };
        } else if (type.isAssignableFrom(String.class)) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) f;
                }
            };
        } else if (type.isEnum()) {
            return new Converter<String, To>() {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                public To convert(String f) {
                    try {
                        return (To) Enum.valueOf((Class<Enum>) type, f);
                    } catch (ClassCastException e) {
                    }
                    return null;
                }
            };
        } else if (type == Pattern.class) {
            return new Converter<String, To>() {
                @SuppressWarnings("unchecked")
                @Override
                public To convert(String f) {
                    return (To) Pattern.compile(f);
                }
            };
        } else {
            try {
                final Constructor<?> c = type.getConstructor(String.class);
                return new Converter<String, To>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public To convert(String f) {
                        try {
                            return (To) c.newInstance(f);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };
            } catch (NoSuchMethodException | SecurityException e) {
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Object toPrimitiveArray(List<?> list, Class<T> componentType) {
        if (componentType == int.class) {
            return toInts((List<Integer>) list);
        } else if (componentType == char.class) {
            return toChars((List<Character>) list);
        } else if (componentType == byte.class) {
            return toBytes((List<Byte>) list);
        } else if (componentType == double.class) {
            return toDoubles((List<Double>) list);
        } else if (componentType == float.class) {
            return toFloats((List<Float>) list);
        } else if (componentType == short.class) {
            return toShorts((List<Short>) list);
        } else if (componentType == long.class) {
            return toLongs((List<Long>) list);
        } else if (componentType == boolean.class) {
            return toBooleans((List<Boolean>) list);
        }
        throw new UnsupportedOperationException(componentType.getName() + " is not primitive!");
    }

    public static int[] toInts(List<Integer> ints) {
        int[] out = new int[ints.size()];
        int i = 0;
        for (Integer ii : ints) {
            out[i++] = ii;
        }
        return out;
    }
    
    public static boolean[] toBooleans(List<Boolean> bools) {
        boolean[] out = new boolean[bools.size()];
        int i = 0;
        for (Boolean ii : bools) {
            out[i++] = ii;
        }
        return out;
    }

    public static byte[] toBytes(List<Byte> ints) {
        byte[] out = new byte[ints.size()];
        int i = 0;
        for (Byte ii : ints) {
            out[i++] = ii;
        }
        return out;
    }

    public static short[] toShorts(List<Short> ints) {
        short[] out = new short[ints.size()];
        int i = 0;
        for (Short ii : ints) {
            out[i++] = ii;
        }
        return out;
    }

    public static long[] toLongs(List<Long> ints) {
        long[] out = new long[ints.size()];
        int i = 0;
        for (Long ii : ints) {
            out[i++] = ii;
        }
        return out;
    }

    public static float[] toFloats(List<Float> ints) {
        float[] out = new float[ints.size()];
        int i = 0;
        for (Float ii : ints) {
            out[i++] = ii;
        }
        return out;
    }

    public static double[] toDoubles(List<Double> ints) {
        double[] out = new double[ints.size()];
        int i = 0;
        for (Double ii : ints) {
            out[i++] = ii;
        }
        return out;
    }

    public static char[] toChars(List<Character> ints) {
        char[] out = new char[ints.size()];
        int i = 0;
        for (Character ii : ints) {
            out[i++] = ii;
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T[]> getArrayClass(Class<T> componentType) throws ClassNotFoundException {
        ClassLoader classLoader = componentType.getClassLoader();
        String name;
        if (componentType.isArray()) {
            // just add a leading "["
            name = "[" + componentType.getName();
        } else if (componentType == boolean.class) {
            name = "[Z";
        } else if (componentType == byte.class) {
            name = "[B";
        } else if (componentType == char.class) {
            name = "[C";
        } else if (componentType == double.class) {
            name = "[D";
        } else if (componentType == float.class) {
            name = "[F";
        } else if (componentType == int.class) {
            name = "[I";
        } else if (componentType == long.class) {
            name = "[J";
        } else if (componentType == short.class) {
            name = "[S";
        } else {
            // must be an object non-array class
            name = "[L" + componentType.getName() + ";";
        }
        return (Class<T[]>) (classLoader != null ? classLoader.loadClass(name) : Class.forName(name));
    }

    public static String deepToString(Object value, Class<?> fieldType) {
        if(value != null && fieldType.isArray()) {
            if(String.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((String[]) value);
            }else if(float.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((float[]) value);
            }else if(double.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((double[]) value);
            }else if(int.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((int[]) value);
            }else if(short.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((short[]) value);
            }else if(byte.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((byte[]) value);
            }else if(char.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((char[]) value);
            }else if(long.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((long[]) value);
            }else if(boolean.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((boolean[]) value);
            }else if(Float.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((Float[]) value);
            }else if(Double.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((Double[]) value);
            }else if(Integer.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((Integer[]) value);
            }else if(Short.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((Short[]) value);
            }else if(Byte.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((Byte[]) value);
            }else if(Character.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((Character[]) value);
            }else if(Long.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((Long[]) value);
            }else if(Boolean.class.equals(fieldType.getComponentType())) {
                return Arrays.toString((Boolean[]) value);
            }else {
                try {
                    return Arrays.toString((Object[])value);
                }catch (ClassCastException e) {
                    return String.valueOf(value);
                }
            }
        }else {
            return String.valueOf(value);
        }        
    }
    
    private static Object wrapPrimitive(Object value) {
    	Class<?> type = value.getClass();
    	if(isPrimitiveWrapper(value.getClass())) {
    	    return value;
    	}
    	if(float.class.equals(type)) {
            return new Float((float) value);
        }else if(double.class.equals(type)) {
            return new Double((double) value);
        }else if(int.class.equals(type)) {
            return new Integer((int) value);
        }else if(short.class.equals(type)) {
            return new Short((short) value);
        }else if(byte.class.equals(type)) {
            return new Byte((byte) value);
        }else if(char.class.equals(type)) {
            return new Character((char) value);
        }else if(long.class.equals(type)) {
            return new Long((long) value);
        }else if(boolean.class.equals(type)) {
            return new Boolean((boolean) value);
        }
    	throw new ClassCastException(type + " is not primitive!");
    }
    
    private static Object unwrapPrimitive(Class<?> targetPrimitive,Object value) {
        if(value.getClass().isPrimitive()) {
            return convertPrimitiveType(targetPrimitive, value);
        }
    	Class<?> type = value.getClass();
    	if(Float.class.equals(type)) {
    	    if (targetPrimitive == boolean.class || targetPrimitive == Boolean.class) {
                return 0 != ((Float) value).floatValue();
            } else if (targetPrimitive == int.class || targetPrimitive == Integer.class) {
                return (int) ((Float) value).floatValue();
            } else if (targetPrimitive == float.class || targetPrimitive == Float.class) {
                return (float) ((Float) value).floatValue();
            } else if (targetPrimitive == double.class || targetPrimitive == Double.class) {
                return (double) ((Float) value).floatValue();
            } else if (targetPrimitive == byte.class || targetPrimitive == Byte.class) {
                return (byte) ((Float) value).floatValue();
            } else if (targetPrimitive == short.class || targetPrimitive == Short.class) {
                return (short) ((Float) value).floatValue();
            } else if (targetPrimitive == long.class || targetPrimitive == Long.class) {
                return (long) ((Float) value).floatValue();
            } else if (targetPrimitive == char.class || targetPrimitive == Character.class) {
                return (char) ((Float) value).floatValue();
            }
    	    throw new ClassCastException(targetPrimitive + " is not primitive!");
        }else if(Double.class.equals(type)) {
            if (targetPrimitive == boolean.class || targetPrimitive == Boolean.class) {
                return 0 != ((Double) value).doubleValue();
            } else if (targetPrimitive == int.class || targetPrimitive == Integer.class) {
                return (int) ((Double) value).doubleValue();
            } else if (targetPrimitive == float.class || targetPrimitive == Float.class) {
                return (float) ((Double) value).doubleValue();
            } else if (targetPrimitive == double.class || targetPrimitive == Double.class) {
                return (double) ((Double) value).doubleValue();
            } else if (targetPrimitive == byte.class || targetPrimitive == Byte.class) {
                return (byte) ((Double) value).doubleValue();
            } else if (targetPrimitive == short.class || targetPrimitive == Short.class) {
                return (short) ((Double) value).doubleValue();
            } else if (targetPrimitive == long.class || targetPrimitive == Long.class) {
                return (long) ((Double) value).doubleValue();
            } else if (targetPrimitive == char.class || targetPrimitive == Character.class) {
                return (char) ((Double) value).doubleValue();
            }
            throw new ClassCastException(targetPrimitive + " is not primitive!");
        }else if(Integer.class.equals(type)) {
            if (targetPrimitive == boolean.class || targetPrimitive == Boolean.class) {
                return 0 != ((Integer) value).intValue();
            } else if (targetPrimitive == int.class || targetPrimitive == Integer.class) {
                return (int) ((Integer) value).intValue();
            } else if (targetPrimitive == float.class || targetPrimitive == Float.class) {
                return (float) ((Integer) value).intValue();
            } else if (targetPrimitive == double.class || targetPrimitive == Double.class) {
                return (double) ((Integer) value).intValue();
            } else if (targetPrimitive == byte.class || targetPrimitive == Byte.class) {
                return (byte) ((Integer) value).intValue();
            } else if (targetPrimitive == short.class || targetPrimitive == Short.class) {
                return (short) ((Integer) value).intValue();
            } else if (targetPrimitive == long.class || targetPrimitive == Long.class) {
                return (long) ((Integer) value).intValue();
            } else if (targetPrimitive == char.class || targetPrimitive == Character.class) {
                return (char) ((Integer) value).intValue();
            }
            throw new ClassCastException(targetPrimitive + " is not primitive!");
        }else if(Short.class.equals(type)) {
            if (targetPrimitive == boolean.class || targetPrimitive == Boolean.class) {
                return 0 != ((Short) value).shortValue();
            } else if (targetPrimitive == int.class || targetPrimitive == Integer.class) {
                return (int) ((Short) value).shortValue();
            } else if (targetPrimitive == float.class || targetPrimitive == Float.class) {
                return (float) ((Short) value).shortValue();
            } else if (targetPrimitive == double.class || targetPrimitive == Double.class) {
                return (double) ((Short) value).shortValue();
            } else if (targetPrimitive == byte.class || targetPrimitive == Byte.class) {
                return (byte) ((Short) value).shortValue();
            } else if (targetPrimitive == short.class || targetPrimitive == Short.class) {
                return (short) ((Short) value).shortValue();
            } else if (targetPrimitive == long.class || targetPrimitive == Long.class) {
                return (long) ((Short) value).shortValue();
            } else if (targetPrimitive == char.class || targetPrimitive == Character.class) {
                return (char) ((Short) value).shortValue();
            }
            throw new ClassCastException(targetPrimitive + " is not primitive!");
        }else if(Byte.class.equals(type)) {
            if (targetPrimitive == boolean.class || targetPrimitive == Boolean.class) {
                return 0 != ((Byte) value).byteValue();
            } else if (targetPrimitive == int.class || targetPrimitive == Integer.class) {
                return (int) ((Byte) value).byteValue();
            } else if (targetPrimitive == float.class || targetPrimitive == Float.class) {
                return (float) ((Byte) value).byteValue();
            } else if (targetPrimitive == double.class || targetPrimitive == Double.class) {
                return (double) ((Byte) value).byteValue();
            } else if (targetPrimitive == byte.class || targetPrimitive == Byte.class) {
                return (byte) ((Byte) value).byteValue();
            } else if (targetPrimitive == short.class || targetPrimitive == Short.class) {
                return (short) ((Byte) value).byteValue();
            } else if (targetPrimitive == long.class || targetPrimitive == Long.class) {
                return (long) ((Byte) value).byteValue();
            } else if (targetPrimitive == char.class || targetPrimitive == Character.class) {
                return (char) ((Byte) value).byteValue();
            }
            throw new ClassCastException(targetPrimitive + " is not primitive!");
        }else if(Character.class.equals(type)) {
            if (targetPrimitive == boolean.class || targetPrimitive == Boolean.class) {
                return 0 != ((Character) value).charValue();
            } else if (targetPrimitive == int.class || targetPrimitive == Integer.class) {
                return (int) ((Character) value).charValue();
            } else if (targetPrimitive == float.class || targetPrimitive == Float.class) {
                return (float) ((Character) value).charValue();
            } else if (targetPrimitive == double.class || targetPrimitive == Double.class) {
                return (double) ((Character) value).charValue();
            } else if (targetPrimitive == byte.class || targetPrimitive == Byte.class) {
                return (byte) ((Character) value).charValue();
            } else if (targetPrimitive == short.class || targetPrimitive == Short.class) {
                return (short) ((Character) value).charValue();
            } else if (targetPrimitive == long.class || targetPrimitive == Long.class) {
                return (long) ((Character) value).charValue();
            } else if (targetPrimitive == char.class || targetPrimitive == Character.class) {
                return (char) ((Character) value).charValue();
            }
            throw new ClassCastException(targetPrimitive + " is not primitive!");
        }else if(Long.class.equals(type)) {
            if (targetPrimitive == boolean.class || targetPrimitive == Boolean.class) {
                return 0 != ((Long) value).longValue();
            } else if (targetPrimitive == int.class || targetPrimitive == Integer.class) {
                return (int) ((Long) value).longValue();
            } else if (targetPrimitive == float.class || targetPrimitive == Float.class) {
                return (float)  ((Long) value).longValue();
            } else if (targetPrimitive == double.class || targetPrimitive == Double.class) {
                return (double)  ((Long) value).longValue();
            } else if (targetPrimitive == byte.class || targetPrimitive == Byte.class) {
                return (byte) ((Long) value).longValue();
            } else if (targetPrimitive == short.class || targetPrimitive == Short.class) {
                return (short) ((Long) value).longValue();
            } else if (targetPrimitive == long.class || targetPrimitive == Long.class) {
                return (long) ((Long) value).longValue();
            } else if (targetPrimitive == char.class || targetPrimitive == Character.class) {
                return (char) ((Long) value).longValue();
            }
            throw new ClassCastException(targetPrimitive + " is not primitive!");
        }else if(Boolean.class.equals(type)) {
            if (targetPrimitive == boolean.class || targetPrimitive == Boolean.class) {
                return (boolean) ((Boolean) value).booleanValue();
            } else if (targetPrimitive == int.class || targetPrimitive == Integer.class) {
                return (int) (((Boolean) value).booleanValue()?1:0);
            } else if (targetPrimitive == float.class || targetPrimitive == Float.class) {
                return (float) (((Boolean) value).booleanValue()?1:0);
            } else if (targetPrimitive == double.class || targetPrimitive == Double.class) {
                return (double) (((Boolean) value).booleanValue()?1:0);
            } else if (targetPrimitive == byte.class || targetPrimitive == Byte.class) {
                return (byte) (((Boolean) value).booleanValue()?1:0);
            } else if (targetPrimitive == short.class || targetPrimitive == Short.class) {
                return (short) (((Boolean) value).booleanValue()?1:0);
            } else if (targetPrimitive == long.class || targetPrimitive == Long.class) {
                return (long) (((Boolean) value).booleanValue()?1:0);
            } else if (targetPrimitive == char.class || targetPrimitive == Character.class) {
                return (char) (((Boolean) value).booleanValue()?1:0);
            }
            throw new ClassCastException(targetPrimitive + " is not primitive!");
        }
    	throw new ClassCastException(type + " is not primitive wrapper!");
    }
    private static boolean isPrimitiveWrapper(Class<?> c) {
    	return Float.class.equals(c) 
    			||Double.class.equals(c)
    			||Integer.class.equals(c)
    			||Short.class.equals(c)
    			||Byte.class.equals(c)
    			||Character.class.equals(c)
    			||Long.class.equals(c)
    			||Boolean.class.equals(c);
    }
    
    private static Class<?> primitiveWrapperToPrimitive(Class<?> c) {
        if(Float.class.equals(c)) {
            return float.class;
        }
        if(Double.class.equals(c)) {
            return double.class;
        }
        if(Integer.class.equals(c)) {
            return int.class;
        }
        if(Short.class.equals(c)) {
            return short.class;
        }
        if(Byte.class.equals(c)) {
            return byte.class;
        }
        if(Character.class.equals(c)) {
            return char.class;
        }
        if(Long.class.equals(c)) {
            return long.class;
        }
        if(Boolean.class.equals(c)) {
            return boolean.class;
        }
        throw new ClassCastException(c + " is not primitive wrapper!");
    }
    
	private static Object convertListToArray(Class<?> targetComp, @SuppressWarnings("rawtypes") List genericList) {
    	if(float.class.equals(targetComp)) {
    		float[] arr = new float[genericList.size()];
    		@SuppressWarnings("rawtypes")
			Iterator iter = genericList.iterator();
    		for(int i=0;i<arr.length;i++) {
    			arr[i] = (float) parseObject(targetComp, iter.next());
    		}
            return arr;
        }else if(double.class.equals(targetComp)) {
        	double[] arr = new double[genericList.size()];
    		@SuppressWarnings("rawtypes")
			Iterator iter = genericList.iterator();
    		for(int i=0;i<arr.length;i++) {
    			arr[i] = (double) parseObject(targetComp, iter.next());
    		}
            return arr;
        }else if(boolean.class.equals(targetComp)) {
        	boolean[] arr = new boolean[genericList.size()];
    		@SuppressWarnings("rawtypes")
			Iterator iter = genericList.iterator();
    		for(int i=0;i<arr.length;i++) {
    			arr[i] = (boolean) parseObject(targetComp, iter.next());
    		}
            return arr;
        }else if(int.class.equals(targetComp)) {
        	int[] arr = new int[genericList.size()];
    		@SuppressWarnings("rawtypes")
			Iterator iter = genericList.iterator();
    		for(int i=0;i<arr.length;i++) {
    			arr[i] = (int) parseObject(targetComp, iter.next());
    		}
            return arr;
        }else if(short.class.equals(targetComp)) {
        	short[] arr = new short[genericList.size()];
    		@SuppressWarnings("rawtypes")
			Iterator iter = genericList.iterator();
    		for(int i=0;i<arr.length;i++) {
    			arr[i] = (short) parseObject(targetComp, iter.next());
    		}
            return arr;
        }else if(byte.class.equals(targetComp)) {
        	byte[] arr = new byte[genericList.size()];
    		@SuppressWarnings("rawtypes")
			Iterator iter = genericList.iterator();
    		for(int i=0;i<arr.length;i++) {
    			arr[i] = (byte) parseObject(targetComp, iter.next());
    		}
            return arr;
        }else if(char.class.equals(targetComp)) {
        	char[] arr = new char[genericList.size()];
    		@SuppressWarnings("rawtypes")
			Iterator iter = genericList.iterator();
    		for(int i=0;i<arr.length;i++) {
    			arr[i] = (char) parseObject(targetComp, iter.next());
    		}
            return arr;
        }else if(long.class.equals(targetComp)) {
        	long[] arr = new long[genericList.size()];
    		@SuppressWarnings("rawtypes")
			Iterator iter = genericList.iterator();
    		for(int i=0;i<arr.length;i++) {
    			arr[i] = (long) parseObject(targetComp, iter.next());
    		}
            return arr;
        }else {
        	Object[] arr = (Object[]) Array.newInstance(targetComp, genericList.size());
        	int i = 0;
    		for(Object o:genericList) {
    			arr[i++] = parseObject(targetComp, o);
    		}
        	return arr;
        }
	}

}
