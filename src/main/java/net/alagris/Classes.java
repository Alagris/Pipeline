package net.alagris;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        if (type.isArray()) {
            List<String> strList = null;
            try {
                strList = (List<String>) val;
            } catch (ClassCastException e) {
            }
            if (strList != null) {
                final Class<?> comp = type.getComponentType();
                final List<?> list = parseStringArray(comp, strList);
                return toArray(list, comp);
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
        if (type.isAssignableFrom(Boolean.class) || type == boolean.class) {
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

}
