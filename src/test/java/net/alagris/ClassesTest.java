package net.alagris;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
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
}
