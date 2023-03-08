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

/**
 * Unit tests on regex rules
 */
public class CheckRegexTest
{
	/**
	 * A map of all regex rule names to rule instances
	 */
	protected final Map<String,RegexRule> m_rules = Main.readRules(Main.REGEX_FILENAME);

	/**
	 * A map of all detex regex rule names to rule instances
	 */
	protected final Map<String, RegexRule> m_detex_rules = Main.readRules(Main.REGEX_FILENAME_DETEX);

	@Test
	public void testGeneric1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem ipsum dolor sit amet"));
		Rule r = new RegexRule("name:foo", "foo", "Message foo");
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}

	@Test
	public void testGeneric2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem foo ipsum dolor foo sit amet"));
		Rule r = new RegexRule("name:foo", "foo", "Message foo");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(2, ad_list.size());
	}

	@Test
	public void testCmul1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("\\cite{foo} Lorem ipsum \\cite{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}

	@Test
	public void testCmul2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("\\citep{foo} Lorem ipsum \\citet{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}

	@Test
	public void testCmul3()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem ipsum  \\cite{foo} \\cite{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, ad_list.size());
	}

	@Test
	public void testCmul4()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem ipsum  \\cite{foo} \\citep{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, ad_list.size());
	}

	@Test
	public void testCmul5()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Lorem ipsum  \\cite{foo}, \\cite{foo}"));
		Rule r = m_rules.get("sh:c:mul");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, ad_list.size());
	}
	
	@Test
	public void testCmulP1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Antarctica mass change shows clear regional differences between \\gls{eais},\\gls{wais}, and \\gls{ap} \\citep{schroder_four_2019}"));
		Rule r = m_rules.get("sh:c:mulp");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}

	@Test
	public void test010_1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("et al. foo"));
		Rule r = m_rules.get("sh:010");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, ad_list.size());
	}

	@Test
	public void test010_2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("et al.\\ foo"));
		Rule r = m_rules.get("sh:010");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}

	@Test
	public void test010_3()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("et al., foo"));
		Rule r = m_rules.get("sh:010");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}

	@Test
	public void test011_1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("i.e. foo"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, ad_list.size());
	}

	@Test
	public void test011_2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("i.e.\\ foo"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}

	@Test
	public void test011_3()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("i.e., foo"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}

	@Test
	public void test011_4()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("e.g. foo"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, ad_list.size());
	}

	@Test
	public void test011_5()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("e.g.\\ foo"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}

	@Test
	public void test011_6()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("e.g., foo"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}

	@Test
	public void testD002_1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Hello world.Bye."));
		Rule r = m_detex_rules.get("sh:d:002");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(1, ad_list.size());
	}

	@Test
	public void testD002_2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Foo e.g. bar"));
		Rule r = m_detex_rules.get("sh:d:002");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}
	
	@Test
	public void testD003_1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("The number is 0.5."));
		Rule r = m_detex_rules.get("sh:d:003");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}
	
	@Test
	public void testD003_2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("The number is .5."));
		Rule r = m_detex_rules.get("sh:d:003");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}
	
	@Test
	public void testD004_1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("The number is 0,5."));
		Rule r = m_detex_rules.get("sh:d:003");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}
	
	@Test
	public void testNoin1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("from \\cite{foo}"));
		Rule r = m_rules.get("sh:c:noin");
		List<Advice> ad_list = r.evaluate(in_string);
		assertFalse(ad_list.isEmpty());
	}
	
	@Test
	public void testNoin2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("from \\citet{foo}"));
		Rule r = m_rules.get("sh:c:noin");
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void testNoin3()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("from \\cite[Section 1.2]{foo}"));
		Rule r = m_rules.get("sh:c:noin");
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void testSH011_1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("blabla, i.e. something"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertFalse(ad_list.isEmpty());
	}
	
	@Test
	public void testSH011_2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("blabla, i.e.: something"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void testSH011_3()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("blabla, i.e.; something"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void testSH011_4()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("blabla, i.e.\\ something"));
		Rule r = m_rules.get("sh:011");
		List<Advice> ad_list = r.evaluate(in_string);
		assertTrue(ad_list.isEmpty());
	}
	
	@Test
	public void testD008_1()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("Hello \"World\""));
		Rule r = m_detex_rules.get("sh:d:008");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(2, ad_list.size());
	}

	@Test
	public void testD008_2()
	{
		AnnotatedString in_string = AnnotatedString.read(new Scanner("na\\\"{\\i}ve"));
		Rule r = m_detex_rules.get("sh:d:008");
		List<Advice> ad_list = r.evaluate(in_string);
		assertEquals(0, ad_list.size());
	}
}
