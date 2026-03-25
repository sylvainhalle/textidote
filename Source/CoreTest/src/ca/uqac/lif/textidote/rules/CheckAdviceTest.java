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
package ca.uqac.lif.textidote.rules;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.as.AnnotatedString;

public class CheckAdviceTest
{
	@Test
	public void testToString()
	{
		// Simple test to check that the toString method of Advice
		// produces a non-empty string
		AnnotatedString in_string = AnnotatedString.read(new Scanner(CheckAdviceTest.class.getResourceAsStream("data/test-subsec-1.tex")));
		CheckSubsectionSize r = new CheckSubsectionSize();
		r.setMinNumWords(40);
		List<Advice> ad_list = r.evaluate(in_string);
		Advice ad = ad_list.get(0);
		String s = ad.toString();
		assertNotNull(s);
		assertFalse(s.isEmpty());
	}

	@Test
	public void testEquals()
	{
		Advice ad1 = new Advice(new CheckNoBreak(), new Range(0, 10), "message", new AnnotatedString("resource"), new AnnotatedString("line").getLine(0));
		Advice ad2 = new Advice(new CheckNoBreak(), new Range(1, 10), "message", new AnnotatedString("resource"), new AnnotatedString("line").getLine(0));
		Advice ad3 = new Advice(new CheckStackedHeadings(), new Range(0, 10), "message", new AnnotatedString("resource"), new AnnotatedString("line").getLine(0));
		assertTrue(ad1.equals(ad1));
		assertFalse(ad1.equals(ad2));
		assertFalse(ad1.equals(ad3));
	}
}
