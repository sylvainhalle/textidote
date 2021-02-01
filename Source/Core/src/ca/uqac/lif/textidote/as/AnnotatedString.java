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
package ca.uqac.lif.textidote.as;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A multi-line character string that keeps references to portions of
 * another piece of text.
 * @author Sylvain Hallé
 */
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
	 * The name of the resource (e.g. filename) this string comes from
	 */
	protected String m_resourceName;

	/**
	 * The OS-dependent line separator
	 */
	public static final transient String CRLF = System.getProperty("line.separator");

	/**
	 * The size (in bytes) of the OS-dependent line separator
	 */
	public static final transient int CRLF_SIZE = System.getProperty("line.separator").length();

	/**
	 * Creates a new empty annotated string
	 */
	public AnnotatedString()
	{
		super();
		m_map = new HashMap<Range,Range>();
		m_builder = new StringBuilder();
		m_lines = new ArrayList<String>();
		m_resourceName = "";
	}

	/**
	 * Creates a new annotated string by copying the contents of
	 * another
	 * @param s The string to copy
	 */
	public AnnotatedString(AnnotatedString s)
	{
		super();
		m_map = new HashMap<Range,Range>(s.m_map.size());
		m_map.putAll(s.m_map);
		m_builder = new StringBuilder();
		m_builder.append(s.m_builder);
		m_lines = new ArrayList<String>(s.m_lines.size());
		m_lines.addAll(s.m_lines);
		m_resourceName = s.m_resourceName;
		m_currentLine = s.m_currentLine;
		m_currentColumn = s.m_currentColumn;
	}

	/**
	 * Gets the name of the resource (e.g. filename) this string comes from
	 * @return The name
	 */
	public String getResourceName()
	{
		return m_resourceName;
	}

	/**
	 * Gets the associations between character ranges in the string and
	 * character ranges in the source text
	 * @return The map of associations
	 */
	/*@ pure non_null @*/ public Map<Range,Range> getMap()
	{
		return m_map;
	}

	/**
	 * Sets the name of the resource (e.g. filename) this string comes from
	 * @param name The name
	 * @return This string
	 */
	public AnnotatedString setResourceName(String name)
	{
		m_resourceName = name;
		return this;
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

	/*@ pure @*/ public /*@ non_null @*/ Position getSourcePosition(/*@ non_null @*/ Position p)
	{
		Range r = findKeyRangeFor(p);
		if (r == null)
		{
			return Position.NOWHERE;
		}
		Range r_source = m_map.get(r);
		if (r_source.isMultiLine())
		{
			// Cannot guess source position in a multi-line range
			return null;
		}
		int col_offset = p.getColumn() - r.getStart().getColumn();
		Position p_source_start = r_source.getStart();
		Position out_p = new Position(p_source_start.getLine(), p_source_start.getColumn() + col_offset);
		return out_p;
	}

	/*@ pure @*/ public /*@ non_null @*/ Position getTargetPosition(/*@ non_null @*/ Position p)
	{
		Range key_r = findKeyRangeForValue(p);
		if (key_r == null)
		{
			return Position.NOWHERE;
		}
		Range value_r = m_map.get(key_r);
		if (key_r.isMultiLine())
		{
			// Cannot guess source position in a multi-line range
			return Position.NOWHERE;
		}
		int col_offset = p.getColumn() - value_r.getStart().getColumn();
		Position p_source_start = key_r.getStart();
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
	/*@ pure @*/ protected Range findKeyRangeFor(/*@ non_null @*/ Position p)
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

	/**
	 * Retrieves the range in the map's keys, whose corresponding value
	 * contains the given position
	 * @param p The position
	 * @return The range, if {@code null} if no range contains this
	 * position
	 */
	/*@ pure @*/ protected Range findKeyRangeForValue(/*@ non_null @*/ Position p)
	{
		for (Map.Entry<Range,Range> entry : m_map.entrySet())
		{
			if (entry.getValue().isWithin(p))
			{
				return entry.getKey();
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

	/**
	 * Returns a substring of the current annotated string.
	 * @param r The range representing the part of the current string to keep.
	 * @return The substring. All references to the external text file are
	 * preserved (and their positions shifted accordingly) in the output
	 * string.
	 */
	/*@ pure non_null @*/ public AnnotatedString substring(/*@ non_null @*/ Range r)
	{
		return substring(r.getStart(), r.getEnd());
	}

	/**
	 * Returns a substring of the current annotated string.
	 * @param start The start position of the portion of the current
	 * string to keep
	 * @param end The end position of the portion of the current
	 * string to keep
	 * @return The substring. All references to the external text file are
	 * preserved (and their positions shifted accordingly) in the output
	 * string.
	 */
	/*@ pure non_null @*/ public AnnotatedString substring(/*@ non_null @*/ Position start, /*@ non_null @*/ Position end)
	{
		if (start.getLine() >= lineCount())
		{
			throw new StringIndexOutOfBoundsException("Line " + start.getLine() + " does not exist");
		}
		AnnotatedString out_as = new AnnotatedString();
		out_as.m_resourceName = m_resourceName;
		// First, create list of lines corresponding to truncated string
		for (int i = start.getLine(); i <= Math.min(m_currentLine, end.getLine()); i++)
		{
			if (i == m_currentLine)
			{
				String builder_string = m_builder.toString();
				if (i == start.getLine())
				{
					out_as.m_builder.append(builder_string.substring(start.getColumn(), Math.min(builder_string.length(), end.getColumn() + 1)));
				}
				else
				{
					out_as.m_builder.append(builder_string.substring(0, Math.min(builder_string.length(), end.getColumn() + 1)));
				}
			}
			else
			{
				String line = m_lines.get(i);
				if (i == start.getLine())
				{
					if (start.getLine() == end.getLine())
					{
						int end_p = end.getColumn() + 1;
						if (end_p < 0 || start.getColumn() < 0)
						{
							System.out.println("HE");
						}
						String truncated_line = line.substring(Math.min(line.length(), start.getColumn()), Math.min(line.length(), end_p));
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
					if (i == end.getLine())
					{
						if (end.getColumn() + 1 < 0)
						{
							System.out.println("HE");
						}
						String truncated_line = line.substring(0, Math.min(line.length(), end.getColumn() + 1));
						//out_as.m_lines.add(truncated_line);
						out_as.m_builder.append(truncated_line);
					}
					else
					{
						out_as.m_lines.add(line);
					}
				}
			}
		}
		out_as.m_currentLine = out_as.m_lines.size();
		out_as.m_currentColumn = out_as.m_builder.toString().length();
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
	 * Returns a substring of the current annotated string.
	 * @param start The start position of the portion of the current
	 * string to keep
	 * @return The substring, from the start position to the very end of
	 * the string
	 */
	/*@ pure non_null @*/ public AnnotatedString substring(/*@ non_null @*/ Position start)
	{
		return substring(start, new Position(Integer.MAX_VALUE - 10, Integer.MAX_VALUE - 10));
	}

	/**
	 * Gets the length of the string, in number of characters. If the string
	 * contains multiple lines, this also counts the length of the new line
	 * delimiter at the end of each line except the last.
	 * @return The length of the string
	 */
	/*@ pure @*/ public int length()
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
	 * Attempts to match a regular expression to this string.
	 * @param regex The regex to find. The pattern must not span multiple lines.
	 * @param start The start position
	 * @return A match, or {@code null} if the string cannot be found or
	 * the position is outside the boundaries of the string
	 */
	public Match find(/* @non_null @*/ String regex, /* @non_null @*/ Position start)
	{
		/*@ nullable @*/ String line_to_find = null;
		int col = start.getColumn();
		for (int start_l = start.getLine(); start_l <= m_lines.size(); start_l++)
		{
			if (start_l < m_lines.size())
			{
				line_to_find = m_lines.get(start_l);
			}
			else if (start_l == m_lines.size())
			{
				line_to_find = m_builder.toString();
			}
			else
			{
				continue;
			}
			Pattern pat = Pattern.compile(regex);
			Matcher mat = pat.matcher(line_to_find);
			if (col >= line_to_find.length())
			{
				// Beyond end of line: move to next line
				col = 0;
				continue;
			}
			if (!mat.find(col))
			{
				col = 0;
				continue;
			}
			Match m = new Match(mat.group(0), new Position(start_l, mat.start()));
			for (int i = 0; i <= mat.groupCount(); i++)
			{
				m.addGroup(mat.group(i));
			}
			return m;
		}
		return null;
	}

	/**
	 * Attempts to match a regular expression to this string. 
	 * @param regex The string to find. The pattern must not span multiple lines.
	 * @return A match, or {@code null} if the string cannot be found
	 */
	public Match find(/* @non_null @*/ String regex)
	{
		return find(regex, Position.ZERO);
	}

	/**
	 * Replaces a pattern by another in the string.
	 * @param regex The pattern to find. The pattern must not span multiple
	 * lines.
	 * @param to The string to replace it with. It must not span multiple
	 * lines. If the replacement contains
	 * occurrences of capture groups, and those capture groups were linked to
	 * character ranges in the external text file, this link will be lost in
	 * the resulting string. 
	 * @param start The position where to start searching
	 * @return A <em>new</em> instance of annotated string with the
	 * replacement being made.
	 */
	public /*@ non_null @*/ AnnotatedString replace(String regex, String to, Position start)
	{
		Match m = find(regex, start);
		if (m == null)
		{
			// No match; just return a copy of the whole string
			return new AnnotatedString(this); 
		}
		Position found_pos = m.getPosition();
		AnnotatedString part_left = null;
		if (m.getPosition().equals(Position.ZERO))
		{
			// Match found right at the beginning
			part_left = new AnnotatedString();
		}
		else
		{
			if (found_pos.getColumn() == 0)
			{
				part_left = substring(Position.ZERO, new Position(found_pos.getLine() - 1, getLine(found_pos.getLine() - 1).length()));
			}
			else
			{
				part_left = substring(Position.ZERO, new Position(found_pos.getLine(), found_pos.getColumn() - 1));
			}
		}
		for (int i = 1; i < m.groupCount(); i++)
		{
			if (m.group(i) != null)
			{
				to = to.replace("$" + i, m.group(i));
			}
			else
			{
				to = to.replace("$" + i, "");
			}
		}
		Position target_p = getSourcePosition(found_pos);
		if (!target_p.equals(Position.NOWHERE))
		{
			Range r = Range.make(target_p.getLine(), target_p.getColumn(), target_p.getColumn() + m.getMatch().length() - 1);
			part_left.append(to, r);
		}
		else
		{
			// Cannot locate where this is in the original string
			part_left.append(to);
		}
		AnnotatedString part_right = substring(new Position(found_pos.getLine(), found_pos.getColumn() + m.getMatch().length()));
		part_left.append(part_right);
		return part_left;
	}

	public /*@ non_null @*/ AnnotatedString replace(String regex, String to)
	{
		return replace(regex, to, Position.ZERO);
	}

	public /*@ non_null @*/ AnnotatedString replaceAll(String regex, String to)
	{
		int max_iterations = 10000;
		AnnotatedString replaced = this;
		Position last_pos = Position.ZERO;
		int i = 0;
		for (; i < max_iterations; i++)
		{
			Match m = replaced.find(regex, last_pos);
			if (m == null)
			{
				// No cigarettes, no matches
				break;
			}
			Position new_pos = m.getPosition();
			AnnotatedString rep = replaced.replace(regex, to, last_pos);
			replaced = rep;
			last_pos = new_pos.moveBy(1);
		}
		return replaced;
	}

	/**
	 * Gets the number of lines in this string
	 * @return The number of lines
	 */
	/*@ pure @*/ public int lineCount()
	{
		return m_lines.size() + 1;
	}

	/**
	 * Gets the n-th line of the string
	 * @param line_nb The number of the line
	 * @return The line; an exception if thrown if the argument is out
	 * of bounds
	 */
	/*@ pure non_null @*/ public String getLine(int line_nb)
	{
		if (line_nb >= 0 && line_nb < m_lines.size())
		{
			return m_lines.get(line_nb);
		}
		if (line_nb == m_lines.size())
		{
			return m_builder.toString();
		}
		throw new ArrayIndexOutOfBoundsException("Line " + line_nb + " does not exist");
	}

	/**
	 * Computes the position (line/column) of the n-th character in the
	 * string. If the string contains multiple
	 * lines, the size of each line separator is also included in the count.
	 * @param nb_chars The number of characters from the beginning of the
	 * string
	 * @return The position; you get {@link Position#NOWHERE}
	 * if {@code nb_chars} lies beyond the string boundaries
	 * @see #getOffset(Position)
	 */
	/*@ pure non_null @*/ public Position getPosition(int nb_chars)
	{
		if (nb_chars < 0)
		{
			return Position.NOWHERE;
		}
		int char_count = 0, line_count = 0;
		for (String line : m_lines)
		{
			if (nb_chars < char_count + line.length())
			{
				// It's on this line
				return new Position(line_count, nb_chars - char_count);
			}
			char_count += line.length() + CRLF_SIZE;
			line_count++;
		}
		String line = m_builder.toString();
		if (nb_chars < char_count + line.length())
		{
			// It's on the last line
			return new Position(line_count, nb_chars - char_count);
		}
		return Position.NOWHERE;
	}

	/**
	 * Removes a complete line from the string.
	 * @param line_nb The line number
	 * @return This string
	 */
	public AnnotatedString removeLine(int line_nb)
	{
		// Step 1: remove line from list
		if (line_nb < 0 || line_nb >= lineCount())
		{
			// Nothing to do
			return this;
		}
		if (m_currentLine > 0)
		{
			m_currentLine--;
		}
		if (line_nb < m_lines.size())
		{
			m_lines.remove(line_nb);
		}
		else if (line_nb == m_lines.size())
		{
			m_builder = new StringBuilder();
			if (line_nb > 0)
			{
				String l = m_lines.remove(line_nb - 1);
				m_builder.append(l);
				m_currentColumn = l.length();
			}
			else
			{
				// line_nb == 0
				m_builder = new StringBuilder();
				m_currentColumn = 0;
			}
		}
		// Step 2: adjust all positions on lines below line_nb
		Map<Range,Range> new_ranges = new HashMap<Range,Range>();
		for (Map.Entry<Range,Range> entry : m_map.entrySet())
		{
			int l_pos_start, l_pos_end;
			Range r_key = entry.getKey();
			l_pos_start = r_key.getStart().getLine();
			l_pos_end = r_key.getEnd().getLine();
			if (l_pos_end < line_nb)
			{
				// This range is completely above the cut line: nothing to change
				new_ranges.put(entry.getKey(), entry.getValue());
				continue;
			}
			if (l_pos_start > line_nb)
			{
				// This range is completely below the cut line: simply offset
				// key range by one line up
				Range new_r_key = Range.make(l_pos_start - 1, r_key.getStart().getColumn(), l_pos_end - 1, r_key.getEnd().getColumn());
				new_ranges.put(new_r_key, entry.getValue());
				continue;
			}
			// If we get here, the key range is across the cut line
			if (l_pos_start == line_nb)
			{
				// Range starts on line to be cut
				if (l_pos_end == line_nb)
				{
					// Single line range: just remove it
					continue;
				}
				else
				{
					// TODO. This is the case of a multi-line range that starts on
					// the cut line, and ends strictly below the cut line
					throw new UnsupportedOperationException("Method removeLine does not work on multi-line character ranges");
				}
			}
			else
			{
				// TODO. This is the case of a multi-line range that starts strictly
				// above the cut line, and ends on the cut line or below
				throw new UnsupportedOperationException("Method removeLine does not work on multi-line character ranges");
			}
		}
		m_map = new_ranges;
		return this;
	}

	/**
	 * Resizes a source range, by mirroring the resizing done to the key
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

	/**
	 * Trims a line of the string from a given position
	 * @param pos The position. All characters on the same line,
	 * starting from this position on to the end of the string,
	 * will be removed.
	 * @return A new string trimmed accordingly
	 */
	public AnnotatedString trimFrom(/*@ non_null @*/ Position pos)
	{
		int line_pos = pos.getLine();
		int col_pos = pos.getColumn();
		if (line_pos < m_lines.size())
		{
			String line = m_lines.get(line_pos);
			line = line.substring(0, col_pos);
			m_lines.set(line_pos, line);
		}
		else if (line_pos == m_lines.size())
		{
			String line = m_builder.toString();
			line = line.substring(0, col_pos);
			m_builder = new StringBuilder();
			m_builder.append(line);
			m_currentColumn = col_pos;
		}
		return this;
	}

	/**
	 * Creates an annotated string from a scanner
	 * @param scanner The scanner
	 * @return The annotated string
	 */
	/*@ non_null @*/ public static AnnotatedString read(/*@ non_null @*/ Scanner scanner)
	{
		AnnotatedString as = new AnnotatedString();
		int line_pos = -1;
		while (scanner.hasNextLine())
		{
			line_pos++;
			if (line_pos > 0)
			{
				as.appendNewLine();
			}
			String line = scanner.nextLine();
			as.append(line, Range.make(line_pos, 0, line.length() - 1));
		}
		return as;
	}

	/**
	 * Determines if the string is empty, i.e. contains no characters.
	 * @return {@code true} if the string is empty, {@code false}
	 * otherwise
	 */
	public boolean isEmpty()
	{
		return m_lines.isEmpty() && m_builder.toString().isEmpty();
	}
	
	/**
	 * Gets the offset (in characters from the beginning) corresponding to
	 * a position
	 * @param p The position
	 * @return The offset; -1 if the position does not correspond to a valid
	 * offset in the text
	 * @see #getPosition(int)
	 */
	public int getOffset(Position p)
	{
		int char_count = 0, line_count = 0;
		int p_line = p.getLine();
		int p_col = p.getColumn();
		for (String line : m_lines)
		{
			if (line_count != p_line)
			{
				// It's not on this line
				char_count += line.length() + CRLF_SIZE;
				line_count++;
				continue;
			}
			else
			{
				// It's on this line
				char_count += p_col;
				return char_count;
			}
		}
		return -1;
	}
}
