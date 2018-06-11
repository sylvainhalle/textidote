package ca.uqac.lif.texlint;

/**
 * Represents a contiguous interval of characters in a text file.
 * A range is defined by a "start" and an "end" position.
 * @author sylvain
 */
public class Range implements Comparable<Range>
{
	/**
	 * The start position of the range
	 */
	protected Position m_start;
	
	/**
	 * The end position of the range
	 */
	protected Position m_end;
	
	/**
	 * The length of the range
	 */
	protected int m_length;
	
	/**
	 * Creates a new range
	 * @param start The start position of the range
	 * @param end The end position of the range
	 * @param length The declared length of the range
	 */
	public Range(Position start, Position end, int length)
	{
		super();
		m_start = start;
		m_end = end;
		m_length = length;
	}
	
	/**
	 * Creates a new range
	 * @param start The start position of the range
	 * @param end The end position of the range
	 */
	public Range(Position start, Position end)
	{
		this(start, end, guessLength(start, end));
	}
	
	/**
	 * Gets the start position of the range
	 * @return The position
	 */
	public Position getStart()
	{
		return m_start;
	}
	
	/**
	 * Gets the end position of the range
	 * @return The position
	 */
	public Position getEnd()
	{
		return m_end;
	}

	/**
	 * Gets the length of the range
	 * @return The length (in number of characters)
	 */
	public int getLength()
	{
		return m_length;
	}
	
	/**
	 * Attempts to guess the length of a range
	 * @param start The start position of the range
	 * @param end The end position of the range
	 * @return The length, or {@code -1} if no length could be guessed
	 */
	protected static int guessLength(Position start, Position end)
	{
		if (start.m_line != end.m_line)
		{
			// Cannot guess length for ranges that span multiple lines
			return -1;
		}
		return (end.m_column - start.m_column + 1);
	}
	
	@Override
	public String toString()
	{
		return m_start + "-" + m_end;
	}
	
	/**
	 * Checks if a position is within the current range.
	 * @param p The position
	 * @return {@code true} if the position is in the range,
	 * {@code false} if it lies outside the range
	 */
	public boolean isWithin(Position p)
	{
		return m_start.compareTo(p) <= 0 && m_end.compareTo(p) >= 0;
	}
	
	@Override
	public int compareTo(Range r)
	{
		if (r == null)
		{
			return Integer.MIN_VALUE;
		}
		// Ranges are only compared with respect to their start position
		int c = m_start.compareTo(r.m_start);
		if (c != 0)
		{
			return c;
		}
		// Except if they have the same start position
		return m_end.compareTo(r.m_end);
	}
	
	/**
	 * Intersects the current range with the one given as argument
	 * @param r The range to intersect with
	 * @return The range corresponding to the intersection, or {@code null}
	 * if the two ranges do not overlap
	 */
	public Range intersectWith(/*@ non_null @*/ Range r)
	{
		int start_l = m_start.getLine();
		int start_c = m_start.getColumn();
		int end_l = m_end.getLine();
		int end_c = m_end.getColumn();
		Position r_start = r.getStart();
		Position r_end = r.getEnd();
		int r_start_line = r_start.getLine();
		if (r_start_line == start_l)
		{
			start_c = Math.max(start_c, r_start.getColumn());
		}
		else if (r_start_line > start_l)
		{
			if (r_start_line > end_l)
			{
				// These ranges don't overlap
				return null;
			}
			start_l = r_start_line;
			start_c = r_start.getColumn();
		}
		int r_end_line = r_end.getLine();
		if (r_end_line == end_l)
		{
			end_c = Math.min(end_c, r_end.getColumn());
		}
		else if (r_end_line < end_l)
		{
			if (r_end_line < start_l)
			{
				// These ranges don't overlap
				return null;
			}
			end_l = r_end_line;
			end_c = r_end.getColumn();
		}
		return Range.make(start_l, start_c, end_l, end_c);
	}
	
	/**
	 * Offsets the start/end position of the range by using the given position
	 * as (0,0)
	 * @param zero The position to be used as the "zero" position
	 * @return This range
	 */
	public Range setZero(/*@ non_null @*/ Position zero)
	{
		int start_l = 0, start_c = 0, end_l = 0, end_c = 0;
		int zero_l = zero.getLine(), zero_c = zero.getColumn();
		if (zero_l == m_start.getLine())
		{
			start_l = 0;
			start_c = m_start.getColumn() - zero_c;
		}
		else
		{
			start_l = m_start.getLine() - zero_l;
			start_c = m_start.getColumn();
		}
		if (zero_l == m_end.getLine()) 
		{
			end_l = 0;
			end_c = m_end.getColumn() - zero_c;
		}
		else
		{
			end_l = m_end.getLine() - zero_l;
			end_c = m_end.getColumn();
		}
		m_start = new Position(start_l, start_c);
		m_end = new Position(end_l, end_c);
		return this;
	}
	
	@Override
	public int hashCode()
	{
		return m_start.hashCode() + m_end.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Range))
		{
			return false;
		}
		Range r = (Range) o;
		return m_start.equals(r.m_start) && m_end.equals(r.m_end);
	}
	
	/**
	 * Determines if a range spans multiple lines
	 * @return {@code true} if the range spans multiple lines,
	 * {@code false} otherwise
	 */
	public boolean isMultiLine()
	{
		return m_start.m_line != m_end.m_line;
	}
	
	/**
	 * Creates a range
	 * @param start_l The start line
	 * @param start_c The start column
	 * @param end_l The end line
	 * @param end_c The end column
	 * @return The range
	 */
	public static Range make(int start_l, int start_c, int end_l, int end_c)
	{
		return new Range(new Position(start_l, start_c), new Position(end_l, end_c));
	}
	
	/**
	 * Creates a single-line range
	 * @param line The line
	 * @param start_c The start column
	 * @param end_c The end column
	 * @return The range
	 */
	public static Range make(int line, int start_c, int end_c)
	{
		return make(line, start_c, line, end_c);
	}
}
