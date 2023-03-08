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
package ca.uqac.lif.textidote.rules;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;

public class CheckLevelSkipTest 
{
	@Test
	public void test1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("\\section{foo}\n" + 
				"\\subsection{bar}\n" + 
				"\\chapter{baz}"));
		Rule r = new CheckSubsections();
		List<Advice> ad_list = r.evaluate(in_string);
		assertFalse(containsSkipAdvice(ad_list));
	}
	
	@Test
	public void test2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("\\section{foo}\n" + 
				"\\subsubsection{bar}\n" + 
				"\\section{baz}"));
		Rule r = new CheckSubsections();
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(containsSkipAdvice(ad_list));
	}
	
	/**
	 * Checks if the list of advice contains one of type "section skip"
	 * @param list The list of advice
	 * @return {@code true} if the list contains a "section skip" advice,
	 * {@code false} otherwise
	 */
	protected static boolean containsSkipAdvice(List<Advice> list)
	{
		for (Advice a : list)
		{
			if (a.getRule().getName().compareToIgnoreCase("sh:secskip") == 0)
			{
				return true;
			}
		}
		return false;
	}
}
