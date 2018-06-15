/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018  Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.textidote.as;

import static ca.uqac.lif.textidote.as.AnnotatedString.CRLF;
import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.textidote.Detexer;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;

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
		assertEquals("Hello " + CRLF + "World", as.toString());
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
