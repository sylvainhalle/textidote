/*
    TexLint, a linter for LaTeX documents
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
package ca.uqac.lif.texlint;

import java.util.List;

import ca.uqac.lif.texlint.as.AnnotatedString;

/**
 * Description of a condition that must apply on a piece of text.
 * That condition can take multiple forms: a regular expression pattern
 * that must/must not be found, etc.
 * 
 * @author Sylvain Hallé
 */
public abstract class Rule
{
	/**
	 * A unique name given to the rule
	 */
	protected String m_name;
	
	/**
	 * Evaluates the rule on a string
	 * @param s The string on which to evaluate the rule
	 * @param original The original (untransformed) piece of text
	 * @return A list of advice generated from the evaluation of the rule
	 */
	public abstract List<Advice> evaluate(/*@ non_null @*/ AnnotatedString s, 
			/*@ non_null @*/ AnnotatedString original);
	
	/**
	 * Creates a new rule
	 * @param name A unique name given to the rule
	 */
	public Rule(/*@ non_null @*/ String name)
	{
		super();
		m_name = name;
	}
	
	/**
	 * Gets the name given to the rule
	 * @return The name
	 */
	/*@ pure non_null @*/ public String getName()
	{
		return m_name;
	}
	
	@Override
	public String toString()
	{
		return m_name;
	}
}
