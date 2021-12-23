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

/**
 * An interval of characters in a string represented by a start and end
 * position.
 */
public class PositionRange implements Comparable<PositionRange>
{
	/**
	 * The start position of the range.
	 */
	/*@ non_null @*/ private final Position m_start;

	/**
	 * The end position of the range.
	 */
	/*@ non_null @*/ private final Position m_end;

	/**
	 * Creates a new position range.
	 * @param start The start position of the range
	 * @param end The end position of the range
	 */
	public PositionRange(/*@ non_null @*/ Position start, /*@ non_null @*/ Position end)
	{
		super();
		m_start = start;
		m_end = end;
	}
	
	/**
	 * Gets the start position of the range.
	 * @return The position
	 */
	public Position getStart()
	{
		return m_start;
	}
	
	/**
	 * Gets the end position of the range.
	 * @return The position
	 */
	public Position getEnd()
	{
		return m_end;
	}

	@Override
	public String toString()
	{
		return m_start + "-" + m_end;
	}

	@Override
	public int compareTo(PositionRange o)
	{
		if (m_start.compareTo(o.m_start) < 0)
		{
			return -1;
		}
		if (m_start.compareTo(o.m_start) == 0)
		{
			if (m_end.compareTo(o.m_end) < 0)
			{
				return -1;
			}
			if (m_end.equals(o.m_end))
			{
				return 0;
			}
		}
		return 1;
	}
}

