package ca.uqac.lif.textidote.as;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.function.strings.Range;
import static ca.uqac.lif.textidote.as.AnnotatedString.CRLF;
import static ca.uqac.lif.textidote.as.AnnotatedString.CRLF_S;

public class AnnotatedStringTest
{
	@Test
	public void testLinear1()
	{
		String s = "abcdef";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(-1, as.getIndex(new Position(1, 3)));
	}
	
	@Test
	public void testLinear2()
	{
		String s = "abcdef";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(3, as.getIndex(new Position(0, 3)));
	}
	
	@Test
	public void testLinear3()
	{
		String s = "abcdef";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(-1, as.getIndex(new Position(0, 10)));
	}
	
	@Test
	public void testLinear4()
	{
		String s = "abc" + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(s.indexOf("e"), as.getIndex(new Position(1, 1)));
	}
	
	@Test
	public void testLinear5()
	{
		String s = "abc" + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(s.indexOf("c"), as.getIndex(new Position(0, 2)));
	}
	
	@Test
	public void testLinear6()
	{
		String s = "abc" + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(-1, as.getIndex(new Position(0, 10)));
	}
	
	@Test
	public void testLinear7()
	{
		String s = "abc" + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(-1, as.getIndex(new Position(1, 3)));
	}
	
	@Test
	public void testLinear8()
	{
		String s = "abc" + CRLF + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(s.indexOf("e"), as.getIndex(new Position(2, 1)));
	}
	
	@Test
	public void testLinear9()
	{
		String s = "abc" + CRLF + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(s.indexOf("c"), as.getIndex(new Position(0, 2)));
	}
	
	@Test
	public void testLinear10()
	{
		String s = "abc" + CRLF + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(-1, as.getIndex(new Position(0, 10)));
	}
	
	@Test
	public void testLinear11()
	{
		String s = "abc" + CRLF + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(-1, as.getIndex(new Position(2, 3)));
	}
	
	@Test
	public void testLinear12()
	{
		String s = "abc" + CRLF + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(-1, as.getIndex(new Position(3, 3)));
	}
	
	@Test
	public void testPosition1()
	{
		String s = "abcdef";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(new Position(0, 2), as.getPosition(2));
	}
	
	@Test
	public void testPosition2()
	{
		String s = "abcdef";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(Position.NOWHERE, as.getPosition(-1));
	}
	
	@Test
	public void testPosition3()
	{
		String s = "abcdef";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(Position.NOWHERE, as.getPosition(6));
	}
	
	@Test
	public void testPosition4()
	{
		String s = "abc" + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(new Position(1, 0), as.getPosition(4));
	}
	
	@Test
	public void testPosition5()
	{
		String s = "abc" + CRLF + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(new Position(1, 0), as.getPosition(4));
		assertEquals(new Position(2, 0), as.getPosition(5));
	}
	
	@Test
	public void testPosition6()
	{
		String s = "abc" + CRLF + "defghijklmno" + CRLF + "pqrstuvw" + CRLF;
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(new Position(1, 1 + CRLF_S), as.getPosition(6));
	}
	
	@Test
	public void testPosition7()
	{
		String s = "abc" + CRLF;
		AnnotatedString as = new AnnotatedString(s);
		// The CRLF is the last character of the line
		assertEquals(new Position(0, 3), as.getPosition(3));
	}
	
	@Test
	public void testPosition8()
	{
		String s = "abc";
		AnnotatedString as = new AnnotatedString(s);
		assertEquals(new Position(0, 2), as.getPosition(2));
	}
	
	@Test
	public void testPosition9()
	{
		String s = "abc" + CRLF + "def";
		AnnotatedString as = new AnnotatedString(s);
	// The CRLF is the last character of the line
		assertEquals(new Position(0, 3), as.getPosition(3));
	}
	
	@Test
	public void testLineCount1()
	{
		AnnotatedString as = new AnnotatedString("abc");
		assertEquals(1, as.lineCount());
	}
	
	@Test
	public void testLineCount2()
	{
		AnnotatedString as = new AnnotatedString("abc" + CRLF + "defghijklmno" + CRLF + "pqrstuvw" + CRLF);
		assertEquals(4, as.lineCount());
	}
	
	@Test
	public void testLineCount3()
	{
		AnnotatedString as = new AnnotatedString("abc" + CRLF + "defghijklmno" + CRLF + "pqrstuvw");
		assertEquals(3, as.lineCount());
	}
	
	@Test
	public void testInvert1()
	{
		AnnotatedString as = new AnnotatedString("abcdefg");
		as.replaceAll("def", "foo").substring(2, 4);
		assertEquals("cf", as.toString());
		List<Range> ranges = as.trackToInput(0, 0);
		assertEquals(1, ranges.size());
		assertEquals(new Range(2, 2), ranges.get(0));
	}
	
	@Test
	public void testRegex1()
	{
		AnnotatedString original = AnnotatedString.read(new Scanner("$\\frac{x}{y}$ $x*$"));
		AnnotatedString replaced = original.replaceAll("\\$.*?\\$", "X");
		assertEquals("X X", replaced.toString());
		Range r = replaced.findOriginalRange(new Range(0, original.length() - 1));
		assertEquals(0, r.getStart());
		assertEquals(17, r.getEnd());
	}
	
	@Test
	public void testReplace1()
	{
		AnnotatedString original = AnnotatedString.read(new Scanner("$\\frac{x}{y}$ $x*$"));
		original = original.replaceAll("\\\\frac\\{", "");
		original = original.replaceAll(Pattern.quote("$x}{y}$"), "X");
		original = original.replaceAll(Pattern.quote("$x*$"), "X");
		Range r = original.findOriginalRange(new Range(0, original.length() - 1));
		assertEquals(0, r.getStart());
		assertEquals(17, r.getEnd());
	}
	
	/*@Test
	public void testAppend1()
	{
		AnnotatedString as1 = new AnnotatedString("foobarbaz").substring(3, 6);
		AnnotatedString as2 = new AnnotatedString("Hello world ");
		as2.append(as1);
		assertEquals("Hello world bar", as2.toString());
		List<Range> ranges = as2.invert(12, 14);
		assertEquals(1, ranges.size());
		assertEquals(new Range(15, 17), ranges.get(0));
	}*/
}
