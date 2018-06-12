package ca.uqac.lif.texlint.as;

import static ca.uqac.lif.texlint.as.AnnotatedString.CRLF;
import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.texlint.Detexer;
import ca.uqac.lif.texlint.as.AnnotatedString;
import ca.uqac.lif.texlint.as.Position;

public class DetexerTest
{
	@Test
	public void testRemoveMarkup1()
	{
		Detexer detexer = new Detexer();
		AnnotatedString as = detexer.detex(AnnotatedString.read(new Scanner("abc" + CRLF + "def")));
		assertEquals("abc" + CRLF + "def", as.toString());
	}
	
	@Test
	public void testRemoveMarkup2()
	{
		Detexer detexer = new Detexer();
		AnnotatedString as = detexer.detex(AnnotatedString.read(new Scanner(DetexerTest.class.getResourceAsStream("data/test1.tex"))));
		assertEquals("Hello" + CRLF + "World", as.toString());
		Position p = as.getSourcePosition(new Position(1, 1));
		assertEquals(2, p.getLine());
		assertEquals(9, p.getColumn());
	}
	
	@Test
	public void testRemoveMarkup3()
	{
		Detexer detexer = new Detexer();
		AnnotatedString as = detexer.detex(AnnotatedString.read(new Scanner(DetexerTest.class.getResourceAsStream("data/test2.tex"))));
		System.out.println(as);
		Position p = as.getSourcePosition(new Position(5, 1));
		System.out.println(p);
	}
}
