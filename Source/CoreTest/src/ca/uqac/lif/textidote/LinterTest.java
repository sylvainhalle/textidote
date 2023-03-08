/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2023  Sylvain Hallé

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
package ca.uqac.lif.textidote;

import static ca.uqac.lif.textidote.as.AnnotatedString.CRLF;

import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.cleaning.TextCleanerException;
import ca.uqac.lif.textidote.cleaning.latex.LatexCleaner;

public class LinterTest
{	
	@Test(expected=LinterException.class)
	public void testNoBeginDocument1() throws TextCleanerException, LinterException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(true);
		Linter l = new Linter(detexer);
		l.evaluateAll(AnnotatedString.read(new Scanner("abc" + CRLF + "def")));
	}
	
	@Test
	public void testNoBeginDocument2() throws TextCleanerException, LinterException
	{
		LatexCleaner detexer = new LatexCleaner().setIgnoreBeforeDocument(false);
		Linter l = new Linter(detexer);
		l.evaluateAll(AnnotatedString.read(new Scanner("abc" + CRLF + "def")));
	}
}
