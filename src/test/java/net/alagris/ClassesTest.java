package net.alagris;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class ClassesTest {

	public enum TestEnum {
		TEST1, TEST2, TEST3
	}

	@Test
	public void parseObjectForEnum() {
		assertEquals(TestEnum.TEST1, Classes.parseObject(TestEnum.class, "TEST1"));
		assertEquals(TestEnum.TEST2, Classes.parseObject(TestEnum.class, "TEST2"));
		assertEquals(TestEnum.TEST3, Classes.parseObject(TestEnum.class, "TEST3"));
	}
	

	@Test
	public void parseObjectArrays() {
		Integer[] integers = {1 ,4 ,75, -54};
		int[] ints = {1 ,4 ,75, -54};
		Float[] floatsC = {1.4f ,0f ,75f, -54.53f};
		float[] floats = {1.4f ,0f ,75f, -54.53f};
		String[] strings = {"try", "me"};
		Object[] objects = {"try", "me"};
		List<Integer> integersA = Arrays.asList(1 ,4 ,75, -54);
		List<Float> floatsCA = Arrays.asList(1.4f ,0f ,75f, -54.53f);
		List<String> stringsA = Arrays.asList("try", "me");
		List<Object> objectsA = Arrays.asList((Object)"try", (Object)"me");
		assertArrayEquals(integers, (Integer[]) Classes.parseObject(Integer[].class, ints));
		assertArrayEquals(ints, (int[]) Classes.parseObject(int[].class, integers));
		assertArrayEquals(floatsC, (Float[]) Classes.parseObject(Float[].class, floats));
		assertArrayEquals(floats, (float[]) Classes.parseObject(float[].class, floatsC), 0);
		assertArrayEquals(floatsC, (Float[]) Classes.parseObject(Float[].class, floatsCA));
		assertArrayEquals(integers, (Integer[]) Classes.parseObject(Integer[].class, integersA));
		assertArrayEquals(objects, (Object[]) Classes.parseObject(Object[].class, strings));
		assertArrayEquals(strings, (String[]) Classes.parseObject(String[].class, objects));
		assertArrayEquals(strings, (String[]) Classes.parseObject(String[].class, objectsA));
		assertArrayEquals(objects, (Object[]) Classes.parseObject(Object[].class, objectsA));
	}
	
	@Test
    public void convertObjectArrays() {
        Integer[] integers = {0, 1 ,4 ,75, -54};
        int[] ints = {0, 1 ,4 ,75, -54};
        Character[] characters = {0, 1 ,4 ,75, (char)-54};
        char[] chars = {0, 1 ,4 ,75, (char)-54};
        Boolean[] booleans = {false,true ,true ,true, true};
        boolean[] bools = {false,true ,true ,true, true};
        Float[] floatsC = {0f, 1f ,4f ,75f, -54f};
        float[] floats = {0f, 1f ,4f ,75f, -54f};
        
        List<Integer> integersA = Arrays.asList(0, 1 ,4 ,75, -54);
        List<Float> floatsCA = Arrays.asList(0f, 1f ,4f ,75f, -54f);
        assertArrayEquals(floats, (float[]) Classes.parseObject(float[].class, floats), 0);
        assertArrayEquals(floatsC, (Float[]) Classes.parseObject(Float[].class, floats));
        assertArrayEquals(bools, (boolean[]) Classes.parseObject(boolean[].class, floats));
        assertArrayEquals(booleans, (Boolean[]) Classes.parseObject(Boolean[].class, floats));
        assertArrayEquals(chars, (char[]) Classes.parseObject(char[].class, floats));
        assertArrayEquals(characters, (Character[]) Classes.parseObject(Character[].class, floats));
        
        assertArrayEquals(floats, (float[]) Classes.parseObject(float[].class, ints), 0);
        assertArrayEquals(floatsC, (Float[]) Classes.parseObject(Float[].class, ints));
        assertArrayEquals(bools, (boolean[]) Classes.parseObject(boolean[].class, ints));
        assertArrayEquals(booleans, (Boolean[]) Classes.parseObject(Boolean[].class, ints));
        assertArrayEquals(chars, (char[]) Classes.parseObject(char[].class, ints));
        assertArrayEquals(characters, (Character[]) Classes.parseObject(Character[].class, ints));
        
    }

	private <T> void assertListsEquals(List<T> a, List<T> b) {
		Iterator<T> ia = a.iterator();
		Iterator<T> ib = b.iterator();
		while(ia.hasNext()&&ib.hasNext()) {
			assertEquals(ia.next(), ib.next());
		}
		if(ia.hasNext()||ib.hasNext()) {
			fail("expected " + (ia.hasNext()? ia.next() : "end") + " but found " + (ib.hasNext()? ib.next() : "end"));
		}
	}

	public static class ClassWithStringConstructor {
		final String s;

		public ClassWithStringConstructor(String s) {
			this.s = s;
		}
	}

	@Test
	public void parseObjectForCustom() {
		ClassWithStringConstructor c = (ClassWithStringConstructor) Classes
				.parseObject(ClassWithStringConstructor.class, "hello");
		assertEquals("hello", c.s);
		assertEquals(new File("/etc/fstab"), Classes.parseObject(File.class, "/etc/fstab"));
	}

	@Test
	public void parseObjectForEnumArray() {
		List<String> in = Arrays.asList("TEST1", "TEST2", "TEST3");
		List<TestEnum> exp = Arrays.asList(TestEnum.TEST1, TestEnum.TEST2, TestEnum.TEST3);
		TestEnum[] out = (TestEnum[]) Classes.parseObject(TestEnum[].class, in);
		for (int i = 0; i < out.length; i++) {
			assertEquals(exp.get(i), out[i]);
		}
	}

	@Test
	public void parseObjectForCustomArray() {
		List<String> in = Arrays.asList("hel", "lo", "world");
		ClassWithStringConstructor[] c = (ClassWithStringConstructor[]) Classes
				.parseObject(ClassWithStringConstructor[].class, in);
		assertEquals("hel", c[0].s);
		assertEquals("lo", c[1].s);
		assertEquals("world", c[2].s);
	}
	
	@Test
	public void deepToString() {
	    String[] str = {"a", "b"};
	    assertEquals("[a, b]",Classes.deepToString(str, String[].class));
	    float[] f = {0.1f, 54.76f};
	    assertEquals("[0.1, 54.76]",Classes.deepToString(f, float[].class));
	    Path[] p = {Paths.get("he", "llo"), Paths.get("w", "o","rld")};
        assertEquals("[he/llo, w/o/rld]",Classes.deepToString(p, Path[].class));
        Float[] f2 = {0.1f, 54.76f};
        assertEquals("[0.1, 54.76]",Classes.deepToString(f2, Float[].class));
	}
	
}
