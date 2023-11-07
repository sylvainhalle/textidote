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
import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;

public class CheckSubsectionsTest 
{
	@Test
	public void test1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner(CheckSubsectionsTest.class.getResourceAsStream("data/test-subsec-1.tex")));
		Rule r = new CheckSubsections();
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, ad_list.size());
		Advice ad = ad_list.get(0);
		assertEquals(2, in_string.getPosition(ad.getRange().getStart()).getLine());
	}
	
	@Test
	public void test2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner(CheckSubsectionsTest.class.getResourceAsStream("data/test-subsec-2.tex")));
		Rule r = new CheckSubsections();
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void test3()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner(CheckSubsectionsTest.class.getResourceAsStream("data/test-subsec-3.tex")));
		Rule r = new CheckSubsections();
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, countAdviceWithLabel(ad_list, "sh:secorder"));
	}
	
	@Test
	public void testSingleSectionUsages()
	{
		assertTrue(new CheckSubsections().evaluate(new AnnotatedString("\\chapter{Foo}")).isEmpty());
		assertTrue(new CheckSubsections().evaluate(new AnnotatedString("\\section{Foo}")).isEmpty());
		assertTrue(new CheckSubsections().evaluate(new AnnotatedString("\\subsection{Foo}")).isEmpty());
		assertTrue(new CheckSubsections().evaluate(new AnnotatedString("\\subsubsection{Foo}")).isEmpty());
		assertTrue(new CheckSubsections().evaluate(new AnnotatedString("\\paragraph{Foo}")).isEmpty());
		assertTrue(new CheckSubsections().evaluate(new AnnotatedString("\\part{Foo}")).isEmpty());
	}
	
	@Test
	public void testExtensiveStressTest()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner(CheckSubsectionsTest.class.getResourceAsStream("data/test-subsec-stress.tex")));
		Rule r = new CheckSubsections();
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(10, countAdviceWithLabel(ad_list, "sh:secskip"));
		assertEquals(0, countAdviceWithLabel(ad_list, "sh:nsubdiv"));
		assertEquals(0, countAdviceWithLabel(ad_list, "sh:secorder"));
	}
	
	@Test
	public void testSectionInfoToString()
	{
		assertEquals("\\chapter{} I1-8 (0)", new SectionInfo("chapter", new Range(1, 8)).toString());
	}

	/**
	 * Checks if the list of advice contains one with a given label
	 * @param list The list of advice
	 * @param The label to look for
	 * @return the number of occurences of the label in the list
	 */
	protected static int countAdviceWithLabel(List<Advice> list, String label)
	{
		int count = 0;
		for (Advice a : list)
		{
			if (a.getRule().getName().compareToIgnoreCase(label) == 0)
			{
				count++;
			}
		}
		return count;
	}
}
