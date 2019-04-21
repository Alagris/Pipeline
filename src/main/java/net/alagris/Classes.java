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
        if(val.getClass().isPrimitive() && isPrimitiveWrapper(type)) {
        	try {
                return wrapPrimitive(val);
            } catch (ClassCastException e) {
            }
        }
        if(type.isPrimitive() && isPrimitiveWrapper(val.getClass())) {
        	try {
                return unwrapPrimitive(val);
            } catch (ClassCastException e) {
            }
        }
        try {
            return type.cast(val);
        } catch (ClassCastException e) {
        }
        
        throw new UnsupportedOperationException(
                "Could not find any way to convert " + val.getClass().getName() + " to " + type.getName());
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
        }
    	throw new ClassCastException(type + " is not primitive!");
    }
    
    private static Object unwrapPrimitive(Object value) {
    	Class<?> type = value.getClass();
    	if(Float.class.equals(type)) {
            return ((Float) value).floatValue();
        }else if(Double.class.equals(type)) {
            return ((Double) value).doubleValue();
        }else if(Integer.class.equals(type)) {
            return ((Integer) value).intValue();
        }else if(Short.class.equals(type)) {
            return ((Short) value).shortValue();
        }else if(Byte.class.equals(type)) {
            return ((Byte) value).byteValue();
        }else if(Character.class.equals(type)) {
            return ((Character) value).charValue();
        }else if(Long.class.equals(type)) {
            return ((Long) value).longValue();
        }
    	throw new ClassCastException(type + " is not primitive wrapper!");
    }
    private static boolean isPrimitiveWrapper(Class c) {
    	return Float.class.equals(c) 
    			||Double.class.equals(c)
    			||Integer.class.equals(c)
    			||Short.class.equals(c)
    			||Byte.class.equals(c)
    			||Character.class.equals(c)
    			||Long.class.equals(c);
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
