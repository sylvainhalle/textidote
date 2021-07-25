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

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.cleaning.markdown.MarkdownCleaner;
import org.junit.Test;

import java.util.Objects;
import java.util.Scanner;

import static ca.uqac.lif.textidote.as.AnnotatedString.CRLF;
import static org.junit.Assert.assertEquals;

public class MarkdownCleanerTest
{
	@Test
	public void testRemoveBold1() throws TextCleanerException
	{
		MarkdownCleaner detexer = new MarkdownCleaner();
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("This is **bold**.")));
		assertEquals("This is bold.", as.toString());
	}

	@Test
	public void testRemoveBackticks1() throws TextCleanerException
	{
		MarkdownCleaner detexer = new MarkdownCleaner();
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("This is `foo`.")));
		assertEquals("This is X.", as.toString());
	}

	@Test
	public void testRemoveIndentedBlocks1() throws TextCleanerException
	{
		MarkdownCleaner detexer = new MarkdownCleaner();
		AnnotatedString as = detexer.clean(AnnotatedString.read(new Scanner("Here are a few words." + CRLF + CRLF + "    Some code block" + CRLF + CRLF + "Here are some more.")));
		assertEquals("Here are a few words." + CRLF + CRLF + CRLF + CRLF + "Here are some more.", as.toString());
	}

    @Test
    public void testMarkdownCommentsAndFrontMatter() throws TextCleanerException {
        MarkdownCleaner markdownCleaner = new MarkdownCleaner();
        AnnotatedString as =
                markdownCleaner.clean(AnnotatedString.read(new Scanner(Objects.requireNonNull(MarkdownCleanerTest.class.getResourceAsStream("data" +
                        "/markdown-test-1.md")))));
        assertEquals("Test with comments" + CRLF  + CRLF + "foo  bar" + CRLF + "Begin of multiline " + " end " +
						"comment" + CRLF + CRLF +
						"Second " +
						"comment" + CRLF + CRLF +
                        "Some " +
                        "other text",
                as.toString());
    }

    @Test
    public void testMarkdownIgnoreComments() throws TextCleanerException {
        MarkdownCleaner markdownCleaner = new MarkdownCleaner();
        AnnotatedString as =
                markdownCleaner.clean(AnnotatedString.read(new Scanner(Objects.requireNonNull(MarkdownCleanerTest.class.getResourceAsStream("data" +
                        "/markdown-test-2.md")))));
        assertEquals("TexTidote ignore test file" + CRLF + CRLF + "some words" + CRLF + CRLF + "Ignore everything " +
                        "below" + CRLF,
                as.toString());
    }
}
