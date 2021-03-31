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
import static org.junit.Assert.assertEquals;

import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.cleaning.markdown.MarkdownCleaner;

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
}
