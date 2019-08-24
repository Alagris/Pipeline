package net.alagris;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;


public class SelectorTest {

	@Test
	public void test() {
		String[] cases = { "A", "A+1", "A-1", "A||B", "A&&B-1", "A+1&&B", "((A+1&&C)&&D)&&B", "(A+1&&C)&&(D&&B)",
				"(A+1&&(C&&D))&&B", "A+1&&((C&&D)&&B)", "(0||1)||(2||3)" };

		for (String caze : cases) {
			assertEquals(caze, Selector.compile(caze).toString());
		}
	}

	@Test
	public void testFail() {
		String[] cases = {  "75 87 43 53", "1+a",  "a a", "a%$^#"};

		for (String caze : cases) {
			try {
				assertNull(Selector.compile(caze));
				fail(caze);
			} catch (ParseCancellationException e) {
				continue;
			}
			fail(caze);
		}
	}

	Blueprint<GlobalCnfg> blueprint;

	public SelectorTest() throws JsonProcessingException, IOException, DuplicateIdException, UndefinedAliasException,
			IllegalIdException, IllegalAliasException {
		BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(
				BlueprintTypedLoaderTest.class, String.class, GlobalCnfg.class);
		blueprint = loader.load(TestConstants.LARGE);
	}

	@Test
	public void testPipeline0() {
		Selector a = Selector.compile("0");
		ArrayList<Node> a_ = blueprint.select(a);
		assertEquals(1, a_.size());
		assertEquals("a", a_.get(0).getId());
	}

	@Test
	public void testPipeline1() {
		Selector b = Selector.compile("1");
		ArrayList<Node> b_ = blueprint.select(b);
		assertEquals(1, b_.size());
		assertEquals("b", b_.get(0).getId());
	}

	@Test
	public void testPipeline2() {
		Selector c = Selector.compile("2");
		ArrayList<Node> c_ = blueprint.select(c);
		assertEquals(1, c_.size());
		assertEquals("x", c_.get(0).getId());
	}

	@Test
	public void testPipeline3() {
		Selector d = Selector.compile("3");
		ArrayList<Node> d_ = blueprint.select(d);
		assertEquals(1, d_.size());
		assertEquals("y", d_.get(0).getId());
	}

	@Test
	public void testPipeline4() {
		Selector e = Selector.compile("0||1||2||3");
		ArrayList<Node> e_ = blueprint.select(e);
		assertEquals(4, e_.size());
		assertEquals("a", e_.get(0).getId());
		assertEquals("b", e_.get(1).getId());
		assertEquals("x", e_.get(2).getId());
		assertEquals("y", e_.get(3).getId());
	}

	@Test
	public void testPipeline5() {
		Selector e = Selector.compile("a+1");
		ArrayList<Node> e_ = blueprint.select(e);
		assertEquals(e_.toString(), 1, e_.size());
		assertEquals("b", e_.get(0).getId());
	}

	@Test
	public void testPipeline6() {
		Selector e = Selector.compile("a+1&&x-1");
		ArrayList<Node> e_ = blueprint.select(e);
		assertEquals(e_.toString(), 1, e_.size());
		assertEquals("b", e_.get(0).getId());
	}

	@Test
	public void testPipeline7() {
		Selector e = Selector.compile("a+1||x-1");
		ArrayList<Node> e_ = blueprint.select(e);
		assertEquals(e_.toString(), 1, e_.size());
		assertEquals("b", e_.get(0).getId());
	}

	@Test
	public void testPipeline8() {
		Selector e = Selector.compile("!a");
		ArrayList<Node> e_ = blueprint.select(e);
		assertTrue(notContains(e_, "a"));
	}

	private static boolean contains(ArrayList<Node> list, String id) {
		return list.stream().filter(n -> n.getId().equals(id)).count() == 1;
	}

	private static boolean notContains(ArrayList<Node> list, String id) {
		return list.stream().filter(n -> n.getId().equals(id)).count() == 0;
	}

	@Test
	public void testPipeline9() {
		Selector e = Selector.compile("!k1<=*");
		ArrayList<Node> e_ = blueprint.select(e);
		assertTrue(contains(e_, "a"));
		assertTrue(contains(e_, "b"));
		assertTrue(contains(e_, "bb"));
		assertTrue(contains(e_, "c"));
		assertTrue(contains(e_, "bb1"));
		assertTrue(contains(e_, "b1"));
		assertTrue(contains(e_, "j1"));
		assertTrue(notContains(e_, "k1"));
		assertTrue(notContains(e_, "l1"));
		assertTrue(notContains(e_, "m1"));
		assertTrue(notContains(e_, "e"));
		assertTrue(notContains(e_, "f"));
		assertTrue(notContains(e_, "g"));
		assertTrue(contains(e_, "k"));
		assertTrue(contains(e_, "l"));
		assertTrue(contains(e_, "m"));
		assertTrue(notContains(e_, "x"));
		assertTrue(notContains(e_, "y"));
		assertTrue(notContains(e_, "z"));
	}

	@Test
	public void testPipeline10() {
		Selector e = Selector.compile("k1>*");
		ArrayList<Node> e_ = blueprint.select(e);
		assertTrue(contains(e_, "a"));
		assertTrue(contains(e_, "b"));
		assertTrue(contains(e_, "bb"));
		assertTrue(contains(e_, "c"));
		assertTrue(contains(e_, "bb1"));
		assertTrue(notContains(e_, "b1"));
		assertTrue(notContains(e_, "j1"));
		assertTrue(notContains(e_, "k1"));
		assertTrue(notContains(e_, "l1"));
		assertTrue(notContains(e_, "m1"));
		assertTrue(notContains(e_, "e"));
		assertTrue(notContains(e_, "f"));
		assertTrue(notContains(e_, "g"));
		assertTrue(notContains(e_, "k"));
		assertTrue(notContains(e_, "l"));
		assertTrue(notContains(e_, "m"));
		assertTrue(notContains(e_, "x"));
		assertTrue(notContains(e_, "y"));
		assertTrue(notContains(e_, "z"));
	}

	@Test
	public void testPipeline11() {
		Selector e = Selector.compile("m1>*");
		ArrayList<Node> e_ = blueprint.select(e);
		assertTrue(contains(e_, "a"));
		assertTrue(contains(e_, "b"));
		assertTrue(contains(e_, "bb"));
		assertTrue(contains(e_, "c"));
		assertTrue(contains(e_, "bb1"));
		assertTrue(notContains(e_, "b1"));
		assertTrue(notContains(e_, "j1"));
		assertTrue(contains(e_, "k1"));
		assertTrue(contains(e_, "l1"));
		assertTrue(notContains(e_, "m1"));
		assertTrue(notContains(e_, "e"));
		assertTrue(notContains(e_, "f"));
		assertTrue(notContains(e_, "g"));
		assertTrue(notContains(e_, "k"));
		assertTrue(notContains(e_, "l"));
		assertTrue(notContains(e_, "m"));
		assertTrue(notContains(e_, "x"));
		assertTrue(notContains(e_, "y"));
		assertTrue(notContains(e_, "z"));
	}

	@Test
	public void testPipeline12() {
		Selector e = Selector.compile("alias alias_a");
		ArrayList<Node> e_ = blueprint.select(e);
		assertEquals(e_.toString(), 1, e_.size());
		assertEquals("a", e_.get(0).getId());
	}

	@Test
	public void testPipeline13() {
		Selector e = Selector.compile("alias alias_b");
		ArrayList<Node> e_ = blueprint.select(e);
		assertEquals(e_.toString(), 2, e_.size());
		assertEquals("b1", e_.get(0).getId());
		assertEquals("h1", e_.get(1).getId());
	}

	@Test
	public void testPipeline14() {
		Selector e = Selector.compile("alias alias_b<*&&*<alias alias_b");
		ArrayList<Node> e_ = blueprint.select(e);
		assertTrue(notContains(e_, "a"));
		assertTrue(notContains(e_, "b"));
		assertTrue(notContains(e_, "bb"));
		assertTrue(notContains(e_, "c"));
		assertTrue(notContains(e_, "bb1"));
		assertTrue(notContains(e_, "b1"));
		assertTrue(contains(e_, "c1"));
		assertTrue(contains(e_, "d1"));
		assertTrue(contains(e_, "e1"));
		assertTrue(contains(e_, "f1"));
		assertTrue(contains(e_, "g1"));
		assertTrue(notContains(e_, "h1"));
		assertTrue(notContains(e_, "j1"));
		assertTrue(notContains(e_, "k1"));
		assertTrue(notContains(e_, "l1"));
		assertTrue(notContains(e_, "m1"));
		assertTrue(notContains(e_, "e"));
		assertTrue(notContains(e_, "f"));
		assertTrue(notContains(e_, "g"));
		assertTrue(notContains(e_, "k"));
		assertTrue(notContains(e_, "l"));
		assertTrue(notContains(e_, "m"));
		assertTrue(notContains(e_, "x"));
		assertTrue(notContains(e_, "y"));
		assertTrue(notContains(e_, "z"));
	}

	@Test
	public void testPipeline15() {
		Selector e = Selector.compile("name Uppercase");
		ArrayList<Node> e_ = blueprint.select(e);
		assertTrue(notContains(e_, "a"));
		assertTrue(notContains(e_, "b"));
		assertTrue(notContains(e_, "bb"));
		assertTrue(notContains(e_, "c"));
		assertTrue(notContains(e_, "bb1"));
		assertTrue(notContains(e_, "b1"));
		assertTrue(notContains(e_, "c1"));
		assertTrue(notContains(e_, "d1"));
		assertTrue(notContains(e_, "e1"));
		assertTrue(notContains(e_, "f1"));
		assertTrue(notContains(e_, "g1"));
		assertTrue(notContains(e_, "h1"));
		assertTrue(notContains(e_, "j1"));
		assertTrue(contains(e_, "k1"));
		assertTrue(contains(e_, "l1"));
		assertTrue(contains(e_, "m1"));
		assertTrue(notContains(e_, "e"));
		assertTrue(notContains(e_, "f"));
		assertTrue(notContains(e_, "g"));
		assertTrue(contains(e_, "k"));
		assertTrue(contains(e_, "l"));
		assertTrue(contains(e_, "m"));
		assertTrue(notContains(e_, "x"));
		assertTrue(notContains(e_, "y"));
		assertTrue(notContains(e_, "z"));
	}
}
