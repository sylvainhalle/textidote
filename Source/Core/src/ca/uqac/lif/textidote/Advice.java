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
package ca.uqac.lif.textidote;

import java.util.List;

import ca.uqac.lif.textidote.as.Range;

/**
 * A comment or suggestion on a portion of text. An advice applies to
 * a specific location in the original file, designated by a {@link Range}
 * object. It is generated from the evaluation of a {@link Rule}, and
 * typically provides a message describing what the problem (or the
 * suggestion) is.
 * @author Sylvain Hallé
 */
public class Advice implements Comparable<Advice>
{
	/**
	 * The range in the file where the advice applies
	 */
	protected Range m_range;

	/**
	 * The message associated with the advice
	 */
	protected String m_message;
	
	/**
	 * The short message associated with the advice
	 */
	protected String m_shortMessage;

	/**
	 * The rule from which this advice was generated
	 */
	protected Rule m_rule;

	/**
	 * The name of the resource (e.g. filename) this advice refers to 
	 */
	protected String m_resource;

	/**
	 * The line where the advice applies
	 */
	protected String m_line;
	
	/**
	 * The number of characters from the beginning of the original
	 * text
	 */
	protected int m_offset = -1;
	
	/**
	 * A list of possible replacements for this message
	 */
	/*@ null @*/ protected List<String> m_replacements;

	/**
	 * Whether the range is on the original string or a sanitized version
	 */
	protected boolean m_originalRange = true;

	/**
	 * Creates a new advice
	 * @param rule The rule from which this advice was generated
	 * @param range The range in the file where the advice applies
	 * @param message The message associated with the advice
	 * @param resource The name of the resource (e.g. filename) this
	 * advice refers to
	 * @param line The line of text on which the advice applies
	 * @param offset The position (in characters from the beginning of the
	 * text) where this advices starts
	 */
	public Advice(/*@ non_null @*/ Rule rule, /*@ non_null @*/ Range range, 
			/*@ non_null @*/ String message, /*@ non_null @*/ String resource,
			/*@ non_null @*/ String line, int offset)
	{
		super();
		m_rule = rule;
		m_range = range;
		m_message = message;
		m_resource = resource;
		m_line = line;
		m_replacements = null;
		m_offset = offset;
		m_shortMessage = "TeXtidote rule";
	}

	/**
	 * Gets the range in the file where the advice applies
	 * @return The range
	 */
	/*@ pure non_null @*/ public Range getRange()
	{
		return m_range;
	}

	/**
	 * Sets whether the range applies on the original text or a sanitized
	 * version
	 * @param b Set to {@code true} to apply to the original text
	 * @return This advice
	 */
	public Advice setOriginal(boolean b)
	{
		m_originalRange = b;
		return this;
	}
	
	/**
	 * Sets a short message for this advice
	 * @param message The message
	 */
	public void setShortMessage(/*@ non_null @*/ String message)
	{
		m_shortMessage = message;
	}
	
	/**
	 * Sets a list of replacements for this advice
	 * @param message The replacements
	 */
	public void setReplacements(/*@ null @*/ List<String> replacements)
	{
		m_replacements = replacements;
	}
	
	/**
	 * Gets a list of replacements for this advice
	 * @return The replacements; may be null
	 */
	/*@ pure null @*/ public List<String> getReplacements()
	{
		return m_replacements;
	}

	/**
	 * Gets the line in the file where the advice applies
	 * @return The line
	 */
	/*@ pure non_null @*/ public String getLine()
	{
		return m_line;
	}

	/**
	 * Gets the message for this advice
	 * @return The message
	 */
	/*@ pure non_null @*/ public String getMessage()
	{
		return m_message;
	}
	
	/**
	 * Gets the short message for this advice
	 * @return The short message
	 */
	/*@ pure non_null @*/ public String getShortMessage()
	{
		return m_shortMessage;
	}

	/**
	 * Gets the rule from which this advice was generated
	 * @return The rule
	 */
	/*@ pure non_null @*/ public Rule getRule()
	{
		return m_rule;
	}

	/**
	 * Gets the name of the resource (e.g. filename) this advice refers to
	 * @return The name of the resource
	 */
	/*@ pure non_null @*/ public String getResource()
	{
		return m_resource;
	}

	@Override
	/*@ pure non_null @*/ public String toString()
	{
		String range_string = m_range.toString();
		if (!m_originalRange)
		{
			range_string = range_string.toLowerCase();
		}
		return range_string + " " + m_message;
	}

	@Override
	/*@ pure @*/ public int hashCode()
	{
		return m_range.hashCode() + m_rule.hashCode();
	}

	@Override
	/*@ pure @*/ public boolean equals(Object o)
	{
		if (!(o instanceof Advice))
		{
			return false;
		}
		Advice a = (Advice) o;
		return m_range.equals(a.m_range) && m_rule.equals(a.m_rule) 
				&& m_resource.compareTo(a.m_resource) == 0;
	}

	@Override
	public int compareTo(Advice a)
	{
		return m_range.compareTo(a.m_range);
	}
	
	/**
	 * Gets the offset corresponding to the start of the advice
	 * @return The number of characters from the beginning of the original
	 * text
	 */
	public int getOffset()
	{
		return m_offset;
	}
}
