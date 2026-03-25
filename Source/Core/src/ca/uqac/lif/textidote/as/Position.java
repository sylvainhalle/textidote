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
 * A position in the string, expressed in terms of lines and columns.
 * Contrary to a linear index, a Position displays lines and
 * column indices starting at 1 instead of 0. However they are internally
 * stored as 0-based indices, and {@link #getLine()} and
 * {@link #getColumn()} return 0-based locations.
 */
public class Position implements Comparable<Position>
{
	/**
	 * The position of the first character of the string.
	 */
	public static final Position ZERO = new Position(0, 0);
	
	/**
	 * A fictive position representing no location.
	 */
	public static final Position NOWHERE = new Position(-1, -1);
	
	/**
	 * The line corresponding to the position.
	 */
	private final int m_line;

	/**
	 * The column corresponding to the position.
	 */
	private final int m_column;

	/**
	 * Creates a new position.
	 * @param line The line corresponding to the position
	 * @param column The column corresponding to the position
	 */
	public Position(int line, int column)
	{
		super();
		m_line = line;
		m_column = column;
	}

	/**
	 * Gets the line coordinate of the position.
	 * @return The coordinate
	 */
	/*@ pure @*/ public int getLine()
	{
		return m_line;
	}

	/**
	 * Gets the column coordinate of the position.
	 * @return The coordinate
	 */
	/*@ pure @*/ public int getColumn()
	{
		return m_column;
	}

	@Override
	public String toString()
	{
		return "L" + (m_line + 1) + "C" + (m_column + 1);
	}

	@Override
	public int hashCode()
	{
		return m_line * m_column;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof Position))
		{
			return false;
		}
		Position p = (Position) o;
		return p.m_column == m_column && p.m_line == m_line;
	}

	@Override
	public int compareTo(Position p)
	{
		if (p.m_line > m_line)
		{
			return -1;
		}
		if (p.m_line == m_line)
		{
			if (p.m_column > m_column)
			{
				return -1;
			}
			if (p.m_column == m_column)
			{
				return 0;
			}
		}
		return 1;
	}
}
