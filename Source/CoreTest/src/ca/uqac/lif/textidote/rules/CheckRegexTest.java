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
import java.util.Map;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Main;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.rules.RegexRule;

/**
 * Unit tests on regex rules
 */
public class CheckRegexTest
{
	/**
	 * A map of all regex rule names to rule instances
	 */
	protected final Map<String,RegexRule> m_rules = Main.readRules(Main.REGEX_FILENAME);
	
	@Test
	public void testGeneric1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem ipsum dolor sit amet"));
		Rule r = new RegexRule("name:foo", "foo", "Message foo");
		List<Advice> ad_list = r.evaluate(in_string, in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void testGeneric2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem foo ipsum dolor foo sit amet"));
		Rule r = new RegexRule("name:foo", "foo", "Message foo");
		List<Advice> ad_list = r.evaluate(in_string, in_string);
		assertEquals(2, ad_list.size());
	}
	
	@Test
	public void testCmul1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("\\cite{foo} Lorem ipsum \\cite{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string, in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void testCmul2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("\\citep{foo} Lorem ipsum \\citet{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string, in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void testCmul3()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem ipsum  \\cite{foo} \\cite{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string, in_string);
		assertEquals(1, ad_list.size());
	}
	
	@Test
	public void testCmul4()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem ipsum  \\cite{foo} \\citep{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string, in_string);
		assertEquals(1, ad_list.size());
	}
	
	@Test
	public void testCmul5()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem ipsum  \\cite{foo}, \\cite{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string, in_string);
		assertEquals(1, ad_list.size());
	}
}
