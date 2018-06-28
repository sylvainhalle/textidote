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

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Match;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.as.Range;

/**
 * Rule based on a regular expression pattern to be found in the text.
 * @author Sylvain Hallé
 */
public class RegexRule extends Rule 
{
	/**
	 * The pattern to find in the text
	 */
	protected String m_pattern;
	
	/**
	 * If this pattern is found, the rule does not apply
	 */
	protected String m_exceptionPattern;
	
	/**
	 * The message template to generate when the pattern is found
	 */
	protected String m_message;
	
	/**
	 * The maximum number of times the rule can look for the pattern in
	 * the text
	 */
	protected static final transient int MAX_ITERATIONS = 100;
	
	/**
	 * Creates a new regex rule
	 * @param name The name given to the rule
	 * @param pattern The pattern to find in the text
	 * @param message The message template to generate when the pattern
	 * is found. If the pattern contains capture groups, the message can
	 * refer to these capture groups in the usual way (i.e. "$1" refers to
	 * the first group, etc.).
	 */
	public RegexRule(String name, String pattern, String message)
	{
		this(name, pattern, null, message);
	}
	
	/**
	 * Creates a new regex rule
	 * @param name The name given to the rule
	 * @param pattern The pattern to find in the text
	 * @param exception If this pattern is found, the rule does not apply
	 * @param message The message template to generate when the pattern
	 * is found. If the pattern contains capture groups, the message can
	 * refer to these capture groups in the usual way (i.e. "$1" refers to
	 * the first group, etc.).
	 */
	public RegexRule(String name, String pattern, String exception, String message)
	{
		super(name);
		m_pattern = pattern;
		m_message = message;
		m_exceptionPattern = exception;
	}

	@Override
	/*@ non_null @*/ public List<Advice> evaluate(/*@ non_null @*/ AnnotatedString s,
			/*@ non_null @*/ AnnotatedString original)
	{
		List<Advice> out_list = new ArrayList<Advice>();
		Position pos = Position.ZERO;
		for (int num_iterations = 0; num_iterations < MAX_ITERATIONS; num_iterations++)
		{
			Match match = s.find(m_pattern, pos);
			if (match == null)
			{
				// No cigarettes, no matches
				break;
			}
			if (m_exceptionPattern != null && match.getMatch().matches(m_exceptionPattern))
			{
				// Rule does not apply
				continue;
			}
			String message = createMessage(match);
			Position start_pos = match.getPosition();
			Position end_pos = new Position(start_pos.getLine(), start_pos.getColumn() + match.getMatch().length());
			Position source_start_pos = s.getSourcePosition(start_pos);
			Position source_end_pos = s.getSourcePosition(end_pos);
			Range r = null;
			String original_line = "";
			if (source_start_pos.equals(Position.NOWHERE))
			{
				r = Range.make(0, 0, 0);
			}
			else
			{
				original_line = original.getLine(source_start_pos.getLine());
				if (source_end_pos.equals(Position.NOWHERE))
				{
					source_end_pos = source_start_pos;
				}
				r = new Range(source_start_pos, source_end_pos);
			}
			assert r != null;
			out_list.add(new Advice(this, r, message, s.getResourceName(), original_line));
			pos = new Position(start_pos.getLine(), start_pos.getColumn() + match.getMatch().length());;
		}
		return out_list;
	}

	/**
	 * Creates the message for a specific advice, by replacing references
	 * to capture groups in the message template by the actual strings that
	 * matched these capture groups
	 * @param match A match object containing data about the regex match
	 * @return The formatted message
	 */
	protected String createMessage(Match match)
	{
		String out = m_message;
		for (int i = 1; i < match.groupCount(); i++)
		{
			String s = match.group(i);
			if (s != null)
			{
				out = out.replace("$" + i, match.group(i));
			}
			else
			{
				out = out.replace("$" + i, "");
			}
		}
		return out;
	}
}
