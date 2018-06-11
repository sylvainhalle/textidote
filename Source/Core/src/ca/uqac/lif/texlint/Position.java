package ca.uqac.lif.texlint;

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
		return "L" + m_line + "C" + m_column;
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
