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
package ca.uqac.lif.textidote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.cleaning.TextCleaner;
import ca.uqac.lif.textidote.cleaning.TextCleanerException;

/**
 * Object in charge of evaluating a set of rules on a piece of text.
 * 
 * @author Sylvain Hallé
 *
 */
public class Linter 
{
	/**
	 * The list of rules that will be evaluated on the original text
	 */
	protected List<Rule> m_rules;
	
	/**
	 * The list of rules that will be evaluated on the plain text
	 */
	protected List<Rule> m_rulesDetexed;
	
	/**
	 * The detexer used to clean the document
	 */
	protected TextCleaner m_cleaner;
	
	/**
	 * A list of rules to ignore
	 */
	protected List<String> m_blacklist;
	
	/**
	 * The markup language used in the source file
	 */
	public enum Language {LATEX, MARKDOWN, TEXT, UNSPECIFIED}
	
	/**
	 * Creates a new empty linter object
	 * @param cleaner The text cleaner used to clean the text
	 */
	public Linter(/*@ non_null @*/ TextCleaner cleaner)
	{
		super();
		m_rules = new ArrayList<Rule>();
		m_rulesDetexed = new ArrayList<Rule>();
		m_cleaner = cleaner;
		m_blacklist = new ArrayList<String>();
	}
	
	/**
	 * Adds a new rule to the linter, which should apply to the original
	 * text.
	 * @param r The rule to add
	 * @return This linter
	 */
	public Linter add(/*@ non_null @*/ Rule r)
	{
		m_rules.add(r);
		return this;
	}
	
	/**
	 * Adds new rules to the linter, which should apply to the original
	 * text.
	 * @param r The rules to add
	 * @return This linter
	 */
	/*@ non_null @*/ public Linter add(/*@ non_null @*/ Collection<? extends Rule> r)
	{
		m_rules.addAll(r);
		return this;
	}
	
	/**
	 * Adds a new rule to the linter, which should apply on the clean
	 * ("detexed") text.
	 * @param r The rule to add
	 * @return This linter
	 */
	/*@ non_null @*/ public Linter addCleaned(/*@ non_null @*/ Rule r)
	{
		m_rulesDetexed.add(r);
		return this;
	}
	
	/**
	 * Adds new rules to the linter, which should apply on the clean
	 * ("detexed") text.
	 * @param r The rules to add
	 * @return This linter
	 */
	/*@ non_null @*/ public Linter addCleaned(/*@ non_null @*/ Collection<? extends Rule> r)
	{
		m_rulesDetexed.addAll(r);
		return this;
	}
	
	/**
	 * Adds a list of rule IDs to ignore
	 * @param list The list of rule IDs to ignore. IDs can also contain the
	 * wildcard character <tt>*</tt>; this can be used to ignore multiple
	 * rules at once.
	 * @return This linter
	 */
	/*@ non_null @*/ public Linter addToBlacklist(/*@ non_null @*/ List<String> list)
	{
		for (String rule_pat : list)
		{
			if (rule_pat.contains("*"))
			{
				m_blacklist.addAll(getMatchingRules(rule_pat));
			}
			else
			{
				m_blacklist.add(rule_pat);
			}
		}
		return this;
	}
	
	/**
	 * Gets all the rules whose name matches a pattern.
	 * @param rule_pattern The pattern. The only special character allowed is
	 * the wildcard (<tt>*</tt>), which can match zero or more arbitrary
	 * characters in the rule's name.
	 * @return The list of rules matching the pattern
	 */
	protected List<String> getMatchingRules(String rule_pattern)
	{
		List<String> list = new ArrayList<String>();
		String regex_pat = rule_pattern.replaceAll("\\*", ".*");
		for (Rule r : m_rules)
		{
			String r_name = r.getName();
			if (r_name.matches(regex_pat))
			{
				list.add(r_name);
			}
		}
		for (Rule r : m_rulesDetexed)
		{
			String r_name = r.getName();
			if (r_name.matches(regex_pat))
			{
				list.add(r_name);
			}
		}
		return list;
	}
	
	/**
	 * Evaluates all the rules added to the linter on a given string, and
	 * collects the advice these rules generate.
	 * @param s The string on which to evaluate the rules
	 * @return The list of advice generated by these rules
	 * @throws LinterException Thrown if a problem occurs during the linting
	 * process
	 */
	/*@ non_null @*/ List<Advice> evaluateAll(/*@ non_null @*/ AnnotatedString s) throws LinterException
	{
		List<Advice> out_list = new ArrayList<Advice>();
		try
		{
			AnnotatedString s_decommented = m_cleaner.cleanComments(new AnnotatedString(s));
			for (Rule r : m_rules)
			{
				filterAdvice(out_list, r.evaluate(s_decommented));
			}
			AnnotatedString s_detexed = m_cleaner.clean(s);
			if (s_detexed.toString().isBlank())
			{
				throw new LinterException("No text to analyze. Did you omit --read-all?");
			}
			for (Rule r : m_rulesDetexed)
			{
				filterAdvice(out_list, r.evaluate(s_detexed));
			}
			return out_list;
		}
		catch (TextCleanerException e)
		{
			// Abort
			throw new LinterException(e);
		}
	}
	
	/**
	 * Adds to a list of advice only those that don't match a blacklist
	 * @param out_list The list to add to
	 * @param advice The list of advice to add
	 */
	protected void filterAdvice(List<Advice> out_list, List<Advice> advice)
	{
		for (Advice ad : advice)
		{
			String rule_name = ad.getRule().getName();
			if (!m_blacklist.contains(rule_name))
			{
				out_list.add(ad);
			}
		}
	}
	
	/**
	 * Gets the instance of detexer used by this linter.
	 * @return The detexer
	 */
	/*@ pure non_null */ public TextCleaner getTextCleaner()
	{
		return m_cleaner;
	}
}
