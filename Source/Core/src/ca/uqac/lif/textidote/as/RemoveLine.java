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
package ca.uqac.lif.textidote.as;

import java.util.regex.Pattern;

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.petitpoucet.function.strings.StringMappingFunction;

/**
 * Removes a line from a string.
 */
public class RemoveLine extends StringMappingFunction
{
	/**
	 * The OS-dependent new line sequence.
	 */
	/*@ non_null @*/ public static final String CRLF = System.getProperty("line.separator");

	/**
	 * The OS-dependent length of the new line sequence.
	 */
	/*@ non_null @*/ public static final int CRLF_S = CRLF.length();

	/**
	 * The regex pattern matching a new line.
	 */
	/*@ non_null @*/ protected static final Pattern s_line = Pattern.compile(CRLF);
	
	/**
	 * The number of the line to remove.
	 */
	protected final int m_lineNb;
	
	/**
	 * Creates a new instance of the function.
	 * @param line_nb The number of the line to remove
	 */
	public RemoveLine(int line_nb)
	{
		super();
		m_lineNb = line_nb;
	}
	
	@Override
	protected String transformString(String s)
	{
		int pos = 0, line = 0;
		while (pos < s.length() && line < m_lineNb)
		{
			int next_pos = s.indexOf(CRLF, pos);
			if (next_pos < 0)
			{
				break;
			}
			if (next_pos < s.length())
			{
				pos = next_pos + CRLF_S;
				line++;
			}
		}
		if (line != m_lineNb)
		{
			m_mapping.add(new Range(0, s.length() - 1), new Range(0, s.length() - 1));
			return s;
		}
		int next_pos = s.indexOf(CRLF, pos);
		int start = pos, end = next_pos + CRLF_S;
		if (next_pos < 0)
		{
			end = s.length() + 1;
		}
		StringBuilder out = new StringBuilder();
		if (start > 0)
		{
			if (start == s.length())
			{
				start--;
			}
			m_mapping.add(new Range(0, start - 1), new Range(0, start - 1));
			out.append(s.substring(0, start));
		}
		if (end < s.length())
		{
			int remaining = s.length() - end - 1;
			m_mapping.add(new Range(end, end + remaining), new Range(start, start + remaining));
			out.append(s.substring(end));
		}
		return out.toString();
	}

}
