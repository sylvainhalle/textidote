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
package ca.uqac.lif.textidote.cleaning;

import static ca.uqac.lif.textidote.as.AnnotatedString.CRLF;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.cleaning.CompositeCleaner;
import ca.uqac.lif.textidote.cleaning.ReplacementCleaner;
import ca.uqac.lif.textidote.cleaning.TextCleanerException;
import ca.uqac.lif.textidote.cleaning.latex.LatexCleaner;

public class CleanerTest
{
	@Test
	public void testRemoveMarkup1() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("abc" + CRLF + "def")));
		assertEquals("abc" + CRLF + "def", as.toString());
	}
	
	@Test
	public void testRemoveMarkup2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(CleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertEquals("Hello " + CRLF + "World", as.toString());
		Position p = as.getSourcePosition(new Position(1, 1));
		assertEquals(2, p.getLine());
		assertEquals(9, p.getColumn());
	}
	
	@Test
	public void testRemoveMarkup3() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(CleanerTest.class.getResourceAsStream("data/test2.tex"))));
		Position p = as.getSourcePosition(new Position(5, 1));
		assertEquals(22, p.getLine());
		assertEquals(9, p.getColumn());
	}
	
	@Test
	public void testRemoveEnvironments1() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(CleanerTest.class.getResourceAsStream("data/test3.tex"))));
		assertTrue(as.isEmpty());
	}
	
	@Test
	public void testRemoveEnvironments2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(CleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertFalse(as.isEmpty());
		detexer.ignoreEnvironment("itemize");
		as = detexer.clean(AnnotatedString.read(new Scanner(CleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertTrue(as.isEmpty());
	}
	
	@Test
	public void testRemoveEnvironments3() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(CleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertFalse(as.isEmpty());
		HashSet<String> envs = new HashSet<String>();
		envs.add("itemize");
		detexer.ignoreEnvironments(envs);
		as = detexer.clean(AnnotatedString.read(new Scanner(CleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertTrue(as.isEmpty());
	}
	
	@Test
	public void testRemoveAccents1() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Blabla")));
		assertEquals("Blabla", as.toString());
	}
	
	@Test
	public void testRemoveAccents2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Fr\\'{e}chet")));
		assertEquals("Fréchet", as.toString());
	}
	
	@Test
	public void testRemoveEquations1() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("A $k$-uniform graph")));
		assertEquals("A k-uniform graph", as.toString());
	}
	
	@Test
	public void testRemoveEquations2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("$k$-uniform graph")));
		assertEquals("k-uniform graph", as.toString());
	}
	
	@Test
	public void testRemoveEquations3() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("A $12$-uniform graph")));
		assertEquals("A 12-uniform graph", as.toString());
	}
	
	@Test
	public void testRemoveEquations4() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("$12$-uniform graph")));
		assertEquals("12-uniform graph", as.toString());
	}
	
	@Test
	public void testRemoveLabels1() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\caption{Hello world. }")));
		assertEquals("Hello world. ", as.toString());	
	}
	
	@Test
	public void testRemoveLabels2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\caption{Hello world. \\label{foo}}")));
		assertEquals("Hello world. ", as.toString());
	}

	@Test
	public void testReplacementCleaner1() throws TextCleanerException
	{
		ReplacementCleaner cleaner = ReplacementCleaner.create(new Scanner(CleanerTest.class.getResourceAsStream("data/replacements-1.txt")));
		AnnotatedString as = cleaner.clean(AnnotatedString.read(new Scanner("foo")));
		assertEquals("bar", as.toString());
	}
	
	@Test
	public void testReplacementCleaner2() throws TextCleanerException
	{
		ReplacementCleaner cleaner = ReplacementCleaner.create(new Scanner(CleanerTest.class.getResourceAsStream("data/replacements-1.txt")));
		AnnotatedString as = cleaner.clean(AnnotatedString.read(new Scanner("foo baz foo")));
		assertEquals("bar baz bar", as.toString());
	}
	
	@Test(expected=TextCleanerException.class)
	public void testReplacementCleaner3() throws TextCleanerException
	{
		ReplacementCleaner cleaner = ReplacementCleaner.create(new Scanner(CleanerTest.class.getResourceAsStream("data/replacements-3.txt")));
		cleaner.clean(AnnotatedString.read(new Scanner("foo baz foo")));
	}
	
	@Test
	public void testCompositeCleaner1() throws TextCleanerException
	{
		ReplacementCleaner cleaner1 = ReplacementCleaner.create(new Scanner(CleanerTest.class.getResourceAsStream("data/replacements-1.txt")));
		ReplacementCleaner cleaner2 = ReplacementCleaner.create(new Scanner(CleanerTest.class.getResourceAsStream("data/replacements-2.txt")));
		CompositeCleaner cc = new CompositeCleaner(cleaner1, cleaner2);
		AnnotatedString as = cc.clean(AnnotatedString.read(new Scanner("foo baz foo")));
		assertEquals("baz baz baz", as.toString());
	}
	
	@Test
	public void testIncludes0() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		@SuppressWarnings("unused")
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\caption{Hello world. \\label{ foo}}\nSomething.")));
		List<String> inner_files = detexer.getInnerFiles();
		assertEquals(0, inner_files.size());
	}
	
	@Test
	public void testIncludes2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		@SuppressWarnings("unused")
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\caption{Hello world. \\label{ foo}}\n\\input{foo.tex}\nSomething.")));
		List<String> inner_files = detexer.getInnerFiles();
		assertEquals(1, inner_files.size());
		String s_file = inner_files.get(0);
		assertEquals("foo.tex", s_file);
	}
}
