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

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.petitpoucet.function.strings.StringMappingFunction;

/**
 * Inserts a string at a given location in another string.
 */
public class InsertAt extends StringMappingFunction
{
	/**
	 * The string to insert.
	 */
	/*@ non_null @*/ protected String m_toInsert;
	
	/**
	 * The index where to insert the string.
	 */
	protected int m_index;
	
	/**
	 * Creates a new instance of the function.
	 * @param s The string to insert
	 * @param index The index where to insert the string. The string will be
	 * inserted before this index; hence setting it to 0 means it is
	 * inserted at the beginning.
	 */
	public InsertAt(String s, int index)
	{
		super();
		m_toInsert = s;
		m_index = index;
	}
	
	@Override
	protected String transformString(String s)
	{
		int insertion_point = Math.min(m_index, s.length());
		int inserted_length = m_toInsert.length();
		StringBuilder out = new StringBuilder();
		if (insertion_point > 0)
		{
			out.append(s.substring(0, insertion_point));
			m_mapping.add(new Range(0, insertion_point - 1), new Range(0, insertion_point - 1));
		}
		out.append(m_toInsert);
		if (insertion_point < s.length())
		{
			int remaining = s.length() - insertion_point;
			out.append(s.substring(insertion_point));
			m_mapping.add(new Range(insertion_point, insertion_point + remaining - 1), new Range(insertion_point + inserted_length, insertion_point + inserted_length + remaining - 1));
		}
		return out.toString();
	}

}
