/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2021  Sylvain Hallé

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
import static ca.uqac.lif.textidote.as.AnnotatedString.CRLF_S;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.cleaning.latex.LatexCleaner;

public class LatexCleanerTest
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
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test1.tex"))));
		AnnotatedString original = AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test1.tex")));
		assertEquals(CRLF + "Hello " + CRLF + "World" + CRLF, as.toString());
		int out_index = as.getIndex(new Position(1, 1));
		assertEquals(CRLF_S + 1, out_index);
		int index = as.findOriginalIndex(out_index);
		Position p = original.getPosition(index);
		assertNotNull(p);
		assertEquals(1, p.getLine());
		assertEquals(7, p.getColumn());
	}
	
	@Test
	public void testRemoveMarkup3() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test2.tex"))));
		AnnotatedString original = AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test2.tex")));
		Position p = original.getPosition(as.findOriginalIndex(new Position(12, 1)));
		assertNotNull(p);
		assertEquals(22, p.getLine());
		assertEquals(9, p.getColumn());
	}
	
	@Test
	public void testIssue215() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.ignoreEnvironment("tikzpicture");
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/issue215.tex"))));
		// Check that tikzpicture environment is removed
		assertEquals(as.toString().indexOf("tikzpicture"), -1);
		assertEquals(as.toString().indexOf("draw"), -1);
		// Check that all math enviroments are removed
		assertEquals(as.toString().indexOf("\\["), -1);
		assertEquals(as.toString().indexOf("\\]"), -1);
		assertEquals(as.toString().indexOf("x"), -1);
		// Check that normal text is still present
		assertTrue(as.toString().indexOf("One line math:") != -1);
		assertTrue(as.toString().indexOf("Tikzpicture:") != -1);
		assertTrue(as.toString().indexOf("Multi line math:") != -1);
		assertTrue(as.toString().indexOf("Second one line math:") != -1);
	}
	
	@Test
	public void testRemoveMarkup4() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\section{Hello}")));
		assertEquals("Hello", as.toString());
	}
	
	@Test
	public void testRemoveMarkup5() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\section*{Hello}")));
		assertEquals("Hello", as.toString());
	}
	
	@Test
	public void testRemoveMarkup6() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Something~with tilde")));
		assertEquals("Something with tilde", as.toString());
	}
	
	@Test
	public void testRemoveMarkup7() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Something\\,with comma")));
		assertEquals("Something with comma", as.toString());
	}
	
	@Test
	public void testRemoveMarkup8() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\parencite[p. 12-25]{source}")));
		assertEquals("[0]", as.toString());
	}
	
	@Test
	public void testRemoveMathMarkup1() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello $abc$ world")));
		assertEquals("Hello abc world", as.toString());
	}
	
	@Test
	public void testRemoveMathMarkup2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello $3.5$ world")));
		assertEquals("Hello 3.5 world", as.toString());
	}
	
	@Test
	public void testRemoveMathMarkup3() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello $3.5x$ world")));
		assertEquals("Hello 3.5x world", as.toString());
	}
	
	@Test
	public void testRemoveMathMarkup4() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello $abc\\theta$ world")));
		assertEquals("Hello X world", as.toString());
	}
	
	@Test
	public void testRemoveMathMarkup5() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello $ab\\$$ world")));
		assertEquals("Hello X world", as.toString());
	}
	
	@Test
	public void testRemoveMathMarkup6() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Consider the following: Let" + CRLF + "$x=7$ and blahblah $y=5$ in the above.")));
		assertEquals("Consider the following: Let" + CRLF + "X and blahblah X in the above.", as.toString());
	}
	
	@Test
	public void testRemoveEnvironments1() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test3.tex"))));
		assertTrue(as.isEmpty());
	}
	
	@Test
	public void testRemoveEnvironments2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertFalse(as.isEmpty());
		detexer.ignoreEnvironment("itemize");
		as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertTrue(as.isEmpty());
	}
	
	@Test
	public void testRemoveEnvironments3() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertFalse(as.isEmpty());
		HashSet<String> envs = new HashSet<String>();
		envs.add("itemize");
		detexer.ignoreEnvironments(envs);
		as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test1.tex"))));
		assertTrue(as.isEmpty());
	}
	
	@Test
	public void testRemoveEnvironments4() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		detexer.ignoreEnvironment("IEEEeqnarray*");
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/test4.tex"))));
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
	public void testRemoveGather() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\begin{gather}x=b^2-\\sqrt(2a-b)\\end{gather}")));
		assertEquals("", as.toString());
	}
	
	@Test
	public void testRemoveFlalign() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\begin{flalign}x=b^2-\\sqrt(2a-b)\\end{flalign}")));
		assertEquals("", as.toString());
	}
	
	@Test
	public void testRemoveMultline() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("test:\n\\begin{multline}x=b^2-\\sqrt(2a-b)\\end{multline}")));
		assertEquals("test:\n", as.toString());
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
	public void testRemoveReference() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\href{http://example.org/index.html}{Hello world. }")));
		assertEquals("Hello world. ", as.toString());
	}

	@Test
	public void testRemoveMacros1() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.ignoreMacro("foo");
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello \\foo world.")));
		assertEquals("Hello world.", as.toString());	
	}
	
	@Test
	public void testRemoveMacros2() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.ignoreMacro("foo");
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello \\foob world.")));
		assertEquals("Hello \\foob world.", as.toString());	
	}
	
	@Test
	public void testRemoveMacros3() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.ignoreMacro("foo");
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello \\foo{abc} world.")));
		assertEquals("Hello world.", as.toString());	
	}
	
	@Test
	public void testRemoveMacros4() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.ignoreMacro("foo");
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello \\foo[param=2]{abc} world.")));
		assertEquals("Hello world.", as.toString());	
	}
	
	@Test
	public void testRemoveMacros5() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.ignoreMacro("foo");
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Hello \\foo.")));
		assertEquals("Hello .", as.toString());	
	}
	
	@Test
	public void testRemoveMacros6() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.ignoreMacro("todo");
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("This is a poorly worded sentence \\todo{rewrite this sentence}.")));
		assertEquals("This is a poorly worded sentence .", as.toString());	
	}
	
	@Test
	public void testRemoveMacros7() throws TextCleanerException
	{
		LatexCleaner detexer = new LatexCleaner();
		detexer.ignoreMacro("foo");
		detexer.setIgnoreBeforeDocument(false);
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("\\foo{Hello worlf!}")));
		assertEquals("", as.toString());	
	}

	@Test
	public void testReplacementCleaner1() throws TextCleanerException
	{
		ReplacementCleaner cleaner = ReplacementCleaner.create(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/replacements-1.txt")));
		AnnotatedString as = cleaner.clean(AnnotatedString.read(new Scanner("foo")));
		assertEquals("bar", as.toString());
	}
	
	@Test
	public void testReplacementCleaner2() throws TextCleanerException
	{
		ReplacementCleaner cleaner = ReplacementCleaner.create(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/replacements-1.txt")));
		AnnotatedString as = cleaner.clean(AnnotatedString.read(new Scanner("foo baz foo")));
		assertEquals("bar baz bar", as.toString());
	}
	
	@Test(expected=TextCleanerException.class)
	public void testReplacementCleaner3() throws TextCleanerException
	{
		ReplacementCleaner cleaner = ReplacementCleaner.create(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/replacements-3.txt")));
		cleaner.clean(AnnotatedString.read(new Scanner("foo baz foo")));
	}
	
	@Test
	public void testCompositeCleaner1() throws TextCleanerException
	{
		ReplacementCleaner cleaner1 = ReplacementCleaner.create(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/replacements-1.txt")));
		ReplacementCleaner cleaner2 = ReplacementCleaner.create(new Scanner(LatexCleanerTest.class.getResourceAsStream("data/replacements-2.txt")));
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
