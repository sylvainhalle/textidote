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
package ca.uqac.lif.texlint.as;

/**
 * Represents a position in a text file. This position is expressed
 * in *characters*, not in *bytes*.
 */
public class Position implements Comparable<Position>
{
	/**
	 * The line number in the file
	 */
	private final int m_line;
	
	/**
	 * The column number in the file
	 */
	private final int m_column;
	
	/**
	 * An object representing the position (0, 0)
	 */
	public static final Position ZERO = new Position(0, 0);
	
	/**
	 * Creates a new position object.
	 * @param line The line number in the file
	 * @param column The column number in the file
	 */
	public Position(int line, int column)
	{
		super();
		m_line = line;
		m_column = column;
	}
	
	public int getLine()
	{
		return m_line;
	}
	
	public int getColumn()
	{
		return m_column;
	}
	
	@Override
	public String toString()
	{
		return "L" + (m_line + 1) + "C" + (m_column + 1);
	}

	@Override
	public int compareTo(Position p) 
	{
		if (p == null)
		{
			return Integer.MIN_VALUE;
		}
		if (m_line < p.m_line)
		{
			return -1;
		}
		if (m_line > p.m_line)
		{
			return 1;
		}
		// m_line == p.m_line
		return m_column - p.m_column;
	}
	
	@Override
	public int hashCode()
	{
		// 80 has no special meaning
		return m_line * 80 + m_column;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Position))
		{
			return false;
		}
		Position p = (Position) o;
		return m_line == p.m_line && m_column == p.m_column;
	}
}
