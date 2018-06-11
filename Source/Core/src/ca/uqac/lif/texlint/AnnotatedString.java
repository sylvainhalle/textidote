package ca.uqac.lif.texlint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotatedString
{
	/**
	 * A map storing the correspondences between ranges of characters in
	 * the string and ranges of characters in an external text file
	 */
	/*@ non_null @*/ protected Map<Range,Range> m_map;

	/**
	 * A string builder object used to append characters to the current line
	 */
	/*@ non_null @*/ protected StringBuilder m_builder;

	/**
	 * The current line position in the string
	 */
	protected int m_currentLine = 0;

	/**
	 * The current column position in the string
	 */
	protected int m_currentColumn = 0;

	/**
	 * A list of lines contained in the string
	 */
	/*@ non_null @*/ protected List<String> m_lines;

	/**
	 * The OS-dependent line separator
	 */
	public static final transient String CRLF = System.getProperty("line.separator");

	/**
	 * The size (in bytes) of the OS-dependent line separator
	 */
	public static final transient int CRLF_SIZE = System.getProperty("line.separator").length();

	public AnnotatedString()
	{
		super();
		m_map = new HashMap<Range,Range>();
		m_builder = new StringBuilder();
		m_lines = new ArrayList<String>();
	}

	/**
	 * Appends a new string to the current annotated string, at the
	 * current position on the current line; keeps an association to
	 * a range of characters in the external text file.
	 * @param s The string to append
	 * @param r The range in the external text file from which the
	 * characters in {@code s} come
	 * @return This annotated string
	 */
	public /*@ non_null @*/ AnnotatedString append(/*@ non_null @*/ String s, /*@ non_null @*/ Range r)
	{
		Position start = new Position(m_currentLine, m_currentColumn);
		// Update column position
		m_currentColumn += s.length();
		Position end = new Position(m_currentLine, m_currentColumn - 1);
		Range source_r = new Range(start, end);
		m_map.put(source_r, r);
		m_builder.append(s);
		return this;
	}

	/**
	 * Appends a new string to the current annotated string, at the
	 * current position on the current line.
	 * @param s The string to append
	 * @return This annotated string
	 */
	public /*@ non_null @*/ AnnotatedString append(/*@ non_null @*/ String s)
	{
		m_builder.append(s);
		m_currentColumn += s.length();
		return this;
	}

	/**
	 * Appends a new line to this string
	 * @return This annotated string
	 */
	public /*@ non_null @*/ AnnotatedString appendNewLine()
	{
		m_currentColumn = 0;
		m_currentLine++;
		m_lines.add(m_builder.toString());
		m_builder = new StringBuilder();
		return this;
	}

	/**
	 * Appends an annotated string to the current one. If the annotated string
	 * passed as an argument has associations, these associations will be
	 * transferred to the current annotated string, by offsetting their
	 * positions relative to the current position in the current string.
	 * @param as The annotated string to append
	 * @return This annotated string
	 */
	public /*@ non_null @*/ AnnotatedString append(/*@ non_null @*/ AnnotatedString as)
	{
		int start_line = m_currentLine;
		int start_column = m_currentColumn;
		// First, append all lines in as
		List<String> lines = as.getLines();
		for (int i = 0; i < lines.size(); i++)
		{
			if (i > 0)
			{
				m_currentLine++;
				m_currentColumn = 0;
				m_lines.add(m_builder.toString());
				m_builder = new StringBuilder();
			}
			String line = lines.get(i);
			m_builder.append(line);
			m_currentColumn += line.length();
		}
		// Second, transfer all associations from as
		for (Map.Entry<Range,Range> entry : as.m_map.entrySet())
		{
			Range key_r = entry.getKey();
			Range new_key_r = null;
			if (key_r.getStart().getLine() == 0)
			{
				// On first line, we append at the current column position

				if (key_r.isMultiLine())
				{
					// Start and end are on different lines, so we offset only start column by start_column
					new_key_r = Range.make(key_r.getStart().getLine() + start_line, key_r.getStart().getColumn() + start_column, key_r.getEnd().getLine() + start_line, key_r.getEnd().getColumn());
				}
				else
				{
					// Start and end are on the same line, so we offset both start/end columns by start_column 
					new_key_r = Range.make(key_r.getStart().getLine() + start_line, key_r.getStart().getColumn() + start_column, key_r.getEnd().getLine() + start_line, key_r.getEnd().getColumn() + start_column);
				}
			}
			else
			{
				// Not on first line; only offset start/end lines by current_line
				new_key_r = Range.make(key_r.getStart().getLine() + start_line, key_r.getStart().getColumn(), key_r.getEnd().getLine() + start_line, key_r.getEnd().getColumn());
			}
			assert new_key_r != null;
			m_map.put(new_key_r, entry.getValue());
		}
		return this;
	}

	/**
	 * Gets the list of text lines in the current string
	 * @return The list of lines
	 */
	public List<String> getLines()
	{
		List<String> lines = new ArrayList<String>(m_lines.size() + 1);
		lines.addAll(m_lines);
		lines.add(m_builder.toString());
		return lines;
	}

	/*@ pure @*/ public Position getSourcePosition(/*@ non_null @*/ Position p)
	{
		Range r = findRangeFor(p);
		if (r == null)
		{
			return null;
		}
		Range r_source = m_map.get(r);
		if (r_source.isMultiLine())
		{
			// Cannot guess source position in a multi-line range
			return null;
		}
		int col_offset = p.m_column - r.getStart().m_column;
		Position p_source_start = r_source.getStart();
		Position out_p = new Position(p_source_start.getLine(), p_source_start.getColumn() + col_offset);
		return out_p;
	}

	/**
	 * Retrieves the range in the map's keys that contains the given
	 * position
	 * @param p The position
	 * @return The range, if {@code null} if no range contains this
	 * position
	 */
	/*@ pure @*/ protected Range findRangeFor(/*@ non_null @*/ Position p)
	{
		for (Range key_r : m_map.keySet())
		{
			if (key_r.isWithin(p))
			{
				return key_r;
			}
		}
		return null;
	}

	@Override
	/*@ pure @*/ public String toString()
	{
		StringBuilder out = new StringBuilder();
		for (String line : m_lines)
		{
			out.append(line).append(CRLF);
		}
		out.append(m_builder.toString());
		return out.toString();
	}

	public AnnotatedString substring(/*@ non_null @*/ Range r)
	{
		return substring(r.getStart(), r.getEnd());
	}

	public AnnotatedString substring(/*@ non_null @*/ Position start, /*@ non_null @*/ Position end)
	{
		AnnotatedString out_as = new AnnotatedString();
		// First, create list of lines corresponding to truncated string
		for (int i = start.getLine(); i <= Math.min(m_currentLine, end.getLine()); i++)
		{
			if (i == m_currentLine)
			{
				String builder_string = m_builder.toString();
				out_as.m_builder.append(builder_string.substring(0, Math.min(builder_string.length(), end.getColumn() + 1)));
			}
			else
			{
				String line = m_lines.get(i);
				if (i == start.getLine())
				{
					if (start.getLine() == end.getLine())
					{
						String truncated_line = line.substring(Math.min(line.length(), start.getColumn()), Math.min(line.length(), end.getColumn() + 1));
						out_as.m_builder.append(truncated_line);
					}
					else
					{
						String truncated_line = line.substring(Math.min(line.length(), start.getColumn()));
						out_as.m_lines.add(truncated_line);
					}
				}
				else
				{
					out_as.m_lines.add(line);
				}
			}
		}
		// Second, remap associations
		Range in_range = new Range(start, end);
		for (Map.Entry<Range,Range> entry : m_map.entrySet())
		{
			Range key_r = entry.getKey();
			Range new_key_r = key_r.intersectWith(in_range);
			if (new_key_r != null)
			{
				// This range overlaps with the target substring: intersect it with argument range
				Range new_source_r = resizeSourceRange(key_r, new_key_r, entry.getValue());
				new_key_r.setZero(start);
				out_as.m_map.put(new_key_r, new_source_r);
			}
		}
		return out_as;
	}

	/**
	 * Gets the length of the string, in number of characters. If the string
	 * contains multiple lines, this also counts the length of the new line
	 * delimiter at the end of each line except the last.
	 * @return The length of the string
	 */
	public int length()
	{
		int length = 0;
		for (String line : m_lines)
		{
			length += line.length() + CRLF_SIZE;
		}
		length += m_builder.toString().length();
		return length;
	}

	/**
	 * Resizes a source range, by mirrorring the resizing done to the key
	 * range after an intersection
	 * @param orig_key_range The original key range
	 * @param resized_key_range The resized key range
	 * @param orig_source_range The original source range (the one to be
	 * resized)
	 * @return The resized source range
	 */
	/*@ non_null @*/ protected static Range resizeSourceRange(/*@ non_null @*/ Range orig_key_range, /*@ non_null @*/ Range resized_key_range, /*@ non_null @*/ Range orig_source_range)
	{
		int okr_start_l = orig_key_range.getStart().getLine();
		int rkr_start_l = resized_key_range.getStart().getLine();
		Position new_start = null, new_end = null;
		if (okr_start_l == rkr_start_l)
		{
			// Range starts at the same line: only offset by columns
			int col_offset = resized_key_range.getStart().getColumn() - orig_key_range.getStart().getColumn();
			new_start = new Position(orig_source_range.getStart().getLine(), orig_source_range.getStart().getColumn() + col_offset);
		}
		else
		{
			// Range starts at lower line
			int line_offset = rkr_start_l - okr_start_l;
			new_start = new Position(orig_source_range.getStart().getLine() + line_offset, resized_key_range.getStart().getColumn());
		}
		assert new_start != null;
		int okr_end_l = orig_key_range.getEnd().getLine();
		int rkr_end_l = resized_key_range.getEnd().getLine();
		if (okr_end_l == rkr_end_l)
		{
			// Range ends at same line: only offset by columns
			int col_offset = resized_key_range.getEnd().getColumn() - orig_key_range.getEnd().getColumn();
			new_end = new Position(orig_source_range.getEnd().getLine(), orig_source_range.getEnd().getColumn() + col_offset);
		}
		else
		{
			// Range ends at higher line
			int line_offset = rkr_end_l - okr_end_l;
			new_end = new Position(orig_source_range.getEnd().getLine() + line_offset, resized_key_range.getStart().getColumn());
		}
		return new Range(new_start, new_end);
	}
}
