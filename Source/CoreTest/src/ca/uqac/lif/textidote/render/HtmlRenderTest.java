/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2019  Sylvain Hallé

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
package ca.uqac.lif.textidote.render;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.rules.CheckFigureReferences;
import ca.uqac.lif.textidote.rules.CheckFigureReferencesTest;
import ca.uqac.lif.util.AnsiPrinter;

public class HtmlRenderTest 
{
	@Test
	public void test1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner(CheckFigureReferencesTest.class.getResourceAsStream("data/test4.tex")));
		Rule r = new CheckFigureReferences();
		List<Advice> ad_list = r.evaluate(in_string, in_string);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		AnsiPrinter ansi_p = new AnsiPrinter(out);
		HtmlAdviceRenderer har = new HtmlAdviceRenderer(ansi_p, in_string);
		har.render(ad_list);
		String html_render = new String(baos.toByteArray());
		assertNotNull(html_render);
		assertFalse(html_render.isEmpty());
	}
}
