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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.Part.Self;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.strings.InsertAt;
import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.petitpoucet.function.strings.RangeMapping;
import ca.uqac.lif.petitpoucet.function.strings.RangeMapping.RangePair;
import ca.uqac.lif.petitpoucet.function.strings.Remove;
import ca.uqac.lif.petitpoucet.function.strings.RemoveLine;
import ca.uqac.lif.petitpoucet.function.strings.Replace;
import ca.uqac.lif.petitpoucet.function.strings.StringMappingFunction;
import ca.uqac.lif.petitpoucet.function.strings.Substring;

/**
 * A multi-line character string with facilities for provenance tracking.
 * An annotated string is distinct from a regular character {@link String}
 * in a number of aspects.
 * 
 * <h4>Lines</h4>
 * 
 * First, if supports operations addressing characters either in terms of a
 * linear index (offset from the start of the string), or as a line/column
 * pair (called a {@link Position}). It provides methods to convert to/from
 * indices and positions (namely {@link #getIndex(Position) getIndex}
 * and {@link #getPosition(int) getPosition}). Method {@link #getLine(int)
 * getLine} extracts a given line, while method 
 * {@link #getLineOf(int) getLineOf} extracts the text line containing a given
 * index or position.
 * 
 * <h4>Character tracking</h4>
 * 
 * An annotated string supports typical methods for string manipulation
 * (e.g. {@link #substring(int) substring},
 * {@link #replace(String, String, int) replace}, etc.). The important
 * distinction is that it also keeps track of characters as these
 * operations are performed on the string. This is exemplified by the following
 * piece of code:
 * <pre>
 * AnnotatedString s = new AnnotatedString("Hello worl<u>d</u> foo bar baz.");
 * s.replaceAll("foo", "abcde").substring(9, s.lastIndexOf("d"));
 * System.out.println(s);
 * int x = s.findOriginalIndex(1);
 * </pre>
 * (Note that contrary to a classical String, where transformations return a
 * new object, here operations mutate and return the <em>current</em> object.)
 * The call to <tt>println</tt> produces "ld abc", as expected. The call to
 * {@link #findOriginalIndex(int) findOriginalIndex} then asks the object to
 * retrace the location, in the initial contents of the string, of the
 * character that is currently at index 1. This is letter "d", and we know
 * from the operations we applied to <tt>s</tt> that this "d" corresponds to
 * the end of "world" in the original string (underlined in the code above).
 * Thus, the value of <tt>x</tt> is 10, the index of that letter in the
 * original string.
 * <p>
 * This mechanism can exhibit complex behavior. Consider for example the
 * following code:
 * <pre>
 * AnnotatedString s = 
 *   new AnnotatedString("Compare apples and oranges, kiwis and cherries.")
 *       .replaceAll("(.*?) and (.*?)", "$2 or $1");
 * </pre>
 * The result of this code is the string <tt>"Compare oranges or apples,
 * cherries or kiwis."</tt> The capture groups in the regex pattern are
 * correctly tracked, so that:
 * 
 * <pre>Range r = s.findOriginalRange(8, 14);</pre>
 *  
 * results in a {@link Range} object spanning characters 19-25 (the location of
 * "oranges" in the original string). Here we see an example where, instead of
 * asking for the location of a single character, we rather ask for a whole
 * range.
 * <p>
 * But what if this range does not correspond to a contiguous string of
 * characters in the original string? This is handled by method
 * {@link #trackToInput(int, int)}:
 * 
 * <pre>
 * List&lt;Range&gt; ranges = s.invert(8, 24);</pre>
 * 
 * Method <tt>invert</tt> outputs a list of ranges. The previous call asks for
 * the original character ranges associated to the portion "oranges and apples"
 * of the string. It produces in return these <em>two</em> ranges: [19-25] and
 * [8-13], correctly corresponding to the initial location of "apples" and
 * "oranges" in the string. We also observe that the portion " and " between
 * those two words is not there, as these characters are not present in the
 * output string (having been replaced by another string, " or ").
 * <p>
 * A few corner cases must be considered:
 * <ul>
 * <li>For parts of the replacement string that are outside of a capture group
 * (and hence do not come from the input string), the best <tt>invert</tt> can
 * do is to point to the range that matches the pattern as a whole. Therefore,
 * <tt>s.invert(16, 17)</tt> (location of the word "or") outputs the single
 * range [8,25].</li>
 * <li>For an input range <tt>r</tt> where <tt>invert</tt> returns multiple
 * ranges, the call to {@link #findOriginalRange(Range)} on <tt>r</tt> returns
 * the range that includes them all. In the previous example, this would
 * correspond to the range [8,25].</li>
 * <li>Methods {@link #findOriginalIndex(int)} and
 * {@link #findOriginalPosition(Position)} always return a single character
 * index/position. In cases where a character can only be associated to a range,
 * the first character of that range is taken as its "location".</li>
 * </ul>   
 * 
 * @author Sylvain Hallé
 */
public class AnnotatedString implements ExplanationQueryable
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
	 * The current value of the string.
	 */
	/*@ non_null @*/ protected String m_string;

	/**
	 * The original value of the string.
	 */
	/*@ non_null @*/ protected String m_original;

	/**
	 * A linear sequence of unary functions that have been applied to the string
	 * since its creation.
	 */
	/*@ non_null @*/ protected List<StringMappingFunction> m_operations;

	/**
	 * The name of the resource (e.g. filename) this string comes from.
	 */
	protected String m_resourceName;

	/**
	 * Creates a new annotated string from a plain Java string.
	 * @param s The string
	 */
	public AnnotatedString(/*@ non_null @*/ String s)
	{
		super();
		m_original = s;
		m_string = s;
		m_operations = new ArrayList<StringMappingFunction>();
		m_resourceName = "";
	}

	/**
	 * Creates a new annotated string from another annotated string. This has for
	 * effect of copying the history of operations applied on the original
	 * string.
	 * @param s The annotated string
	 */
	public AnnotatedString(/*@ non_null @*/ AnnotatedString s)
	{
		super();
		m_original = s.m_original;
		m_string = s.m_string;
		m_operations = new ArrayList<StringMappingFunction>(s.m_operations.size());
		m_operations.addAll(s.m_operations);
	}

	/**
	 * Creates a new empty annotated string.
	 */
	public AnnotatedString()
	{
		this("");
	}

	/**
	 * Gets the length of the string.
	 * @return The length of the string
	 */
	/*@ pure @*/ public int length()
	{
		return m_string.length();
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
	 * Gets the number of lines in this string.
	 * @return The number of lines
	 */
	/*@ pure @*/ public int lineCount()
	{
		Matcher mat = s_line.matcher(m_string);
		int cnt = 1;
		while (mat.find())
		{
			cnt++;
		}
		return cnt;
	}

	/**
	 * Determines if the string contains a pattern.
	 * @param pattern The pattern to look for
	 * @return <tt>true</tt> if the pattern is found in the string,
	 * <tt>false</tt> otherwise
	 */
	/*@ pure @*/ public boolean contains(String pattern)
	{
		return m_string.contains(pattern);
	}

	/**
	 * Returns the index within this string of the first occurrence of the
	 * specified substring from a given position.
	 * @param s The substring
	 * @param start The position where to start looking for
	 * @return The index of the starting character of the substring, or -1 if
	 * the substring is not found
	 */
	/*@ pure @*/ public int indexOf(String s, int start)
	{
		return m_string.indexOf(s, start);
	}

	/**
	 * Returns the index within this string of the first occurrence of the
	 * specified substring.
	 * @param s The substring
	 * @return The index of the starting character of the substring, or -1 if
	 * the substring is not found
	 */
	/*@ pure @*/ public int indexOf(String s)
	{
		return indexOf(s, 0);
	}

	/**
	 * Returns the line/column position within this string of the first
	 * occurrence of the specified substring.
	 * @param s The substring
	 * @return The position of the starting character of the substring, or
	 * <tt>null</tt> if the substring is not found
	 */
	/*@ pure null @*/ public Position positionOf(String s)
	{
		return getPosition(m_string.indexOf(s));
	}

	/**
	 * Returns the index within this string of the last occurrence of the
	 * specified substring.
	 * @param s The substring
	 * @return The index of the starting character of the substring, or -1 if
	 * the substring is not found
	 */
	/*@ pure @*/ public int lastIndexOf(String s)
	{
		return m_string.lastIndexOf(s);
	}

	/**
	 * Returns the line/column position within this string of the last
	 * occurrence of the specified substring.
	 * @param s The substring
	 * @return The position of the starting character of the substring, or
	 * <tt>null</tt> if the substring is not found
	 */
	/*@ pure null @*/ public Position lastPositionOf(String s)
	{
		return getPosition(m_string.lastIndexOf(s));
	}

	/**
	 * Gets the n-th line of the current string.
	 * @param line_nb The number of the line
	 * @return The line; an exception if thrown if the argument is out
	 * of bounds
	 */
	/*@ pure non_null @*/ public Line getLine(int line_nb) throws ArrayIndexOutOfBoundsException
	{
		return getLine(m_string, line_nb);
	}

	/**
	 * Gets the line of the original string corresponding to the n-th line of
	 * the current string.
	 * @param line_nb The number of the line
	 * @return The line; an exception if thrown if the argument is out
	 * of bounds
	 */
	/*@ pure non_null @*/ public Line findOriginalLine(int line_nb) throws ArrayIndexOutOfBoundsException
	{
		return getLine(m_original, findOriginalPosition(new Position(line_nb, 0)).getLine());
	}

	/**
	 * Gets the line of the original string containing the n-th character of
	 * the current string.
	 * @param index The character index
	 * @return The line; an exception if thrown if the argument is out
	 * of bounds
	 */
	/*@ pure non_null @*/ public Line findOriginalLineOf(int index) throws ArrayIndexOutOfBoundsException
	{
		return getLineOf(m_original, findOriginalIndex(index));
	}

	/**
	 * Gets the n-th line of the original string.
	 * @param line_nb The number of the line
	 * @return The line; an exception if thrown if the argument is out
	 * of bounds
	 */
	/*@ pure non_null @*/ public Line getOriginalLine(int line_nb) throws ArrayIndexOutOfBoundsException
	{
		return getLine(m_original, line_nb);
	}

	/**
	 * Gets the n-th line of a string.
	 * @param line_nb The number of the line
	 * @return The line
	 * @throws ArrayIndexOutOfBoundsException If the argument is out of bounds
	 */
	/*@ non_null @*/ protected static Line getLine(String s, int line_nb) throws ArrayIndexOutOfBoundsException
	{
		int pos = 0, line = 0;
		while (pos < s.length() && line < line_nb)
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
		if (line != line_nb)
		{
			throw new ArrayIndexOutOfBoundsException("Line " + line_nb + " does not exist");
		}
		int next_pos = s.indexOf(CRLF, pos);
		if (next_pos < 0)
		{
			return new Line(s.substring(pos), pos);
		}
		return new Line(s.substring(pos, next_pos), pos);
	}

	/**
	 * Gets the line of a string containing the n-th character.
	 * @param s The string to search
	 * @param index The number of the character
	 * @return The line
	 * @throws ArrayIndexOutOfBoundsException If the argument is out of bounds
	 */
	/*@ non_null @*/ protected static Line getLineOf(String s, int index) throws ArrayIndexOutOfBoundsException {
		int currentIndex = 0;

		if (index < 0 || index >= s.length())
		{
			throw new ArrayIndexOutOfBoundsException("Character " + index + " out of bounds");
		}

		for (String line : s.lines().toArray(String[]::new)) {
			int lineLength = line.length();

			if (currentIndex + lineLength > index) {
				return new Line(line, currentIndex);
			}

			currentIndex += lineLength + 1; // add 1 for the newline character(s)
		}

		throw new ArrayIndexOutOfBoundsException("Character " + index + " does not exist in string");
	}

	/**
	 * Gets the list of text lines in the current string.
	 * @return The list of lines
	 */
	/*@ pure non_null @*/ public List<Line> getLines()
	{
		List<Line> lines = new ArrayList<Line>();
		int pos = 0;
		while (pos < m_string.length())
		{
			int next_pos = m_string.indexOf(CRLF, pos);
			if (next_pos < 0)
			{
				lines.add(new Line(m_string.substring(pos), pos));
				break;
			}
			if (next_pos < m_string.length())
			{
				lines.add(new Line(m_string.substring(pos, next_pos), pos));
				pos = next_pos + CRLF_S;
			}
		}
		return lines;
	}

	/**
	 * Trims a line of the string from a given position
	 * @param index The position. All characters on the same line,
	 * starting from this position on to the end of the string,
	 * will be removed.
	 * @return This string
	 */
	/*@ non_null @*/ public AnnotatedString trimFrom(int index)
	{
		int crlf_pos = indexOf(CRLF, index);
		if (crlf_pos < 0)
		{
			crlf_pos = m_string.length();
		}
		return addOperation(new Remove(index, crlf_pos));
	}

	/**
	 * Determines if the string is empty, i.e. contains no characters.
	 * @return {@code true} if the string is empty, {@code false}
	 * otherwise
	 */
	/*@ pure @*/ public boolean isEmpty()
	{
		return m_string.isEmpty();
	}

	/**
	 * Gets the linear index in the original string of a given line/column
	 * position in the current string contents.
	 * @param p The position
	 * @return The index in the original string
	 */
	/*@ pure @*/ public int findOriginalIndex(/*@ non_null @*/ Position p)
	{
		return findOriginalIndex(getIndex(p));
	}

	/**
	 * Gets the linear index in the original string of a given index
	 * in the current string contents.
	 * @param p The position
	 * @return The index in the original string
	 */
	/*@ pure @*/ public int findOriginalIndex(int index)
	{
		List<Range> ranges = trackToInput(index, index);
		int src_index = -1;
		for (Range r : ranges)
		{
			int i = r.getStart();
			if (src_index < 0 || i < src_index)
			{
				src_index = i;
			}
		}
		return src_index;
	}

	/**
	 * Gets the linear index (in number of characters) corresponding to a
	 * position expressed as a line and a column.
	 * @param p The position
	 * @return The index, or a negative value if the position does not exist
	 * in the string
	 */
	/*@ pure @*/ public int getIndex(/*@ non_null @*/ Position p)
	{
		int pos = 0;
		for (int i = 0; i < p.getLine(); i++)
		{
			int index = m_string.indexOf(CRLF, pos);
			if (index < 0) // Last line
			{
				return -1;
			}
			else
			{
				pos = index + CRLF_S;
			}
		}
		int next_pos = m_string.indexOf(CRLF, pos);
		int width = m_string.length() - pos;
		if (next_pos > 0)
		{
			width = next_pos - pos;
		}
		if (p.getColumn() >= width)
		{
			return -1;
		}
		return pos + p.getColumn();
	}

	/**
	 * Gets the two-dimensional position corresponding to a linear character
	 * index in the string.
	 * @param index The character index
	 * @return The position, or <tt>null</tt> if the index is out of bounds
	 */
	/*@ pure null @*/ public Position getPosition(int index)
	{
		return getPosition(m_string, index);
	}

	/**
	 * Gets the two-dimensional position of the <em>original</em> string
	 * corresponding to a linear character index in the string.
	 * @param index The character index
	 * @return The position, or <tt>null</tt> if the index is out of bounds
	 */
	/*@ pure null @*/ public Position getOriginalPosition(int index)
	{
		return getPosition(m_original, index);
	}

	/**
	 * Gets the two-dimensional position of the <em>original</em> string
	 * corresponding to a line/column position in the current string.
	 * @param p The position
	 * @return The position, or <tt>null</tt> if the index is out of bounds
	 */
	/*@ pure null @*/ public Position findOriginalPosition(Position p)
	{
		return getPosition(m_original, findOriginalIndex(p));
	}

	/**
	 * Gets the two-dimensional range in the original string corresponding to
	 * a start and end position in the original string.
	 * @param start The start position in the original string
	 * @param end The end position in the original string
	 * @return The position range
	 */
	/*@ pure non_null @*/ public PositionRange getOriginalPositionRange(int start, int end)
	{
		Position p_start = getOriginalPosition(start);
		Position p_end = getOriginalPosition(end);
		return new PositionRange(p_start, p_end);
	}

	/**
	 * Gets the two-dimensional range in the current string corresponding to
	 * a start and end position in the current string.
	 * @param start The start position in the current string
	 * @param end The end position in the current string
	 * @return The position range
	 */
	/*@ pure non_null @*/ public PositionRange getPositionRange(int start, int end)
	{
		return new PositionRange(getPosition(start), getPosition(end));
	}

	/**
	 * Gets the two-dimensional position corresponding to a linear character
	 * index in a string.
	 * @param s The string
	 * @param index The character index
	 * @return The position, or a special position called "nowhere" if the index
	 * is out of bounds
	 */
	/*@ non_null @*/ protected static Position getPosition(String s, int index)
	{
		if (index < 0 || index >= s.length())
		{
			return Position.NOWHERE;
		}
		int pos = 0, line = 0;
		while (pos < index)
		{
			int next_pos = s.indexOf(CRLF, pos);
			if (next_pos < 0)
			{
				break;
			}
			if (next_pos < index)
			{
				pos = next_pos + CRLF_S;
				line++;
			}
			if (next_pos >= index)
			{
				break;
			}
		}
		return new Position(line, index - pos);
	}

	/**
	 * Calculates the range of the original string corresponding to a range of
	 * the current string. If this range corresponds to multiple original ranges,
	 * a single range encompassing all of them is returned.
	 * @param r The range in the current string
	 * @return The range in the original string, or <tt>null</tt>
	 * if no range could be found.
	 */
	/*@ pure null @*/ public Range findOriginalRange(Range r)
	{
		List<Range> ranges = trackToInput(r);
		return uniteRanges(ranges);
	}

	/**
	 * Calculates the range of the original string corresponding to a range of
	 * the current string. If this range corresponds to multiple original ranges,
	 * a single range encompassing all of them is returned.
	 * @param start The start of the range
	 * @param end The end of the range
	 * @return The range in the original string, or <tt>null</tt>
	 * if no range could be found.
	 */
	/*@ pure null @*/ public Range findOriginalRange(int start, int end)
	{
		return findOriginalRange(new Range(start, end));
	}
	
	/**
	 * Calculates the range of the current string corresponding to a range of
	 * the original string. If this range corresponds to multiple current ranges,
	 * a single range encompassing all of them is returned.
	 * @param r The range in the original string
	 * @return The range in the current string, or <tt>null</tt>
	 * if no range could be found.
	 */
	/*@ pure null @*/ public Range findCurrentRange(Range r)
	{
		List<Range> ranges = trackToOutput(r);
		return uniteRanges(ranges);
	}
	
	/**
	 * Calculates the range of the current string corresponding to a range of
	 * the original string. If this range corresponds to multiple current ranges,
	 * a single range encompassing all of them is returned.
	 * @param start The start of the range
	 * @param end The end of the range
	 * @return The range in the current string, or <tt>null</tt>
	 * if no range could be found.
	 */
	/*@ pure null @*/ public Range findCurrentRange(int start, int end)
	{
		return findCurrentRange(new Range(start, end));
	}

	/**
	 * Gets the line containing the character at a given position in the
	 * original string.
	 * @param index The character position
	 * @return The line
	 */
	public Line getOriginalLineOf(int index)
	{
		return getLineOf(m_original, index);
	}

	/**
	 * Gets the line containing the character at a given position in the
	 * current string.
	 * @param index The character position
	 * @return The line
	 */
	public Line getLineOf(int index)
	{
		return getLineOf(m_string, index);
	}

	@Override
	public String toString()
	{
		return m_string;
	}

	/**
	 * Gets the associations between character ranges in the string and
	 * character ranges in the source text
	 * @return The map of associations
	 */
	/*@ pure non_null @*/ public Map<Range,Range> getMap()
	{
		Map<Range,Range> map = new HashMap<Range,Range>();
		if (m_operations.isEmpty())
		{
			map.put(new Range(0, m_string.length() - 1), new Range(0, m_string.length() - 1));
			return map;
		}
		RangeMapping rm = m_operations.get(0).getMapping();
		for (int i = 0; i < m_operations.size() - 1; i++)
		{
			rm = RangeMapping.compose(rm, m_operations.get(i + 1).getMapping());
		}
		for (RangePair rp : rm.getPairs())
		{
			map.put(rp.getFrom(), rp.getTo());
		}
		return map;
	}

	/**
	 * Keeps a substring of the current string contents, defined by a range of
	 * characters.
	 * @param start The position of the first character 
	 * @param end
	 * @return
	 */
	/*@ non_null @*/ public AnnotatedString substring(int start, int end)
	{
		return addOperation(new Substring(start, end));
	}

	/*@ non_null @*/ public AnnotatedString substring(int start)
	{
		return substring(start, m_string.length());
	}

	/*@ non_null @*/ public AnnotatedString substring(/*@ non_null @*/ Position start, /*@ non_null @*/ Position end)
	{
		return substring(getIndex(start), getIndex(end));
	}

	/*@ non_null @*/ public AnnotatedString substring(/*@ non_null @*/ Position start)
	{
		return substring(getIndex(start));
	}

	/*@ non_null @*/ public AnnotatedString replace(String from, String to, int start)
	{
		return addOperation(new Replace(from, to, false, start));
	}

	/*@ non_null @*/ public AnnotatedString replaceAll(String from, String to)
	{
		return addOperation(new Replace(from, to));
	}

	/*@ non_null @*/ public AnnotatedString removeLine(int line_nb)
	{
		return addOperation(new RemoveLine(line_nb));
	}

	/*@ non_null @*/ public AnnotatedString insertAt(String s, int index)
	{
		return addOperation(new InsertAt(s, index));
	}

	/**
	 * Attempts to match a regular expression to this string.
	 * @param regex The regex to find. The pattern must not span multiple lines.
	 * @param start The start position
	 * @return A match, or {@code null} if the string cannot be found or
	 * the position is outside the boundaries of the string
	 */
	/*@ pure null @*/ public Match find(String regex, int start)
	{
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(m_string);
		if (!mat.find(start))
		{
			return null;
		}
		Match m = new Match(mat.group(), mat.start());
		for (int i = 1 ; i < mat.groupCount(); i++)
		{
			m.addGroup(mat.group(i));
		}
		return m;
	}

	/**
	 * Attempts to match a regular expression to this string. 
	 * @param regex The string to find. The pattern must not span multiple lines.
	 * @return A match, or {@code null} if the string cannot be found
	 */
	public Match find(/* @non_null @*/ String regex)
	{
		return find(regex, 0);
	}

	/**
	 * Applies a new operation on the string (thereby transforming its contents)
	 * and adds this operation to its internal history.
	 * @param r The operation to apply
	 * @return The new contents of the string
	 */
	protected AnnotatedString addOperation(StringMappingFunction r)
	{
		m_operations.add(r);
		if (m_operations.size() > 1)
		{
			NodeConnector.connect(m_operations.get(m_operations.size() - 2), 0, r, 0);
		}
		m_string = (String) r.evaluate(m_string)[0];
		return this;
	}

	@Override
	public PartNode getExplanation(Part part)
	{
		return getExplanation(part, NodeFactory.getFactory());
	}

	@Override
	public PartNode getExplanation(Part part, NodeFactory factory)
	{
		Part new_p = part; //replaceSelfBy(part, NthOutput.FIRST);
		if (m_operations.isEmpty())
		{
			return factory.getPartNode(part, this);
		}
		AtomicFunction first = m_operations.get(0);
		Circuit g = new Circuit(1, 1);
		g.addNodes(first);
		g.associateInput(0, first.getInputPin(0));
		AtomicFunction previous = first, current = first;
		for (int i = 1; i < m_operations.size(); i++)
		{
			current = m_operations.get(i);
			NodeConnector.connect(previous, 0, current, 0);
			previous = current;
		}
		g.associateOutput(0, current.getOutputPin(0));
		return g.getExplanation(new_p, factory);
	}

	/**
	 * Finds the ranges of the original string corresponding to a range of
	 * characters in the current contents of the string.
	 * @param r The range of characters in the current contents of the string
	 * @return A list of ranges corresponding to portions of the initial string
	 */
	/*@ non_null @*/ protected List<Range> trackToInput(/*@ non_null @*/ Range r)
	{
		PartNode root = getExplanation(ComposedPart.compose(r, NthOutput.FIRST));
		RangeFetcher crawler = new RangeFetcher(root);
		crawler.crawl();
		List<Range> ranges = crawler.getRanges();
		sortAndMerge(ranges);
		return ranges;
	}

	/**
	 * Finds the ranges of the original string corresponding to a range of
	 * characters in the current contents of the string.
	 * @param start The start index of the range of characters in the current
	 * contents of the string
	 * @param end The end index of the range of characters in the current
	 * contents of the string
	 * @return A list of ranges corresponding to portions of the initial string
	 */
	/*@ non_null @*/ protected List<Range> trackToInput(int start, int end)
	{
		return trackToInput(new Range(start, end));
	}
	
	/**
	 * Finds the ranges of the current string corresponding to a range of
	 * characters in the original contents of the string.
	 * @param r The range of characters in the original contents of the string
	 * @return A list of ranges corresponding to portions of the current string
	 */
	/*@ non_null @*/ protected List<Range> trackToOutput(/*@ non_null @*/ Range r)
	{
		PartNode root = getExplanation(ComposedPart.compose(r, NthInput.FIRST));
		RangeFetcher crawler = new RangeFetcher(root);
		crawler.crawl();
		List<Range> ranges = crawler.getRanges();
		sortAndMerge(ranges);
		return ranges;
	}
	
	/**
	 * Finds the ranges of the current string corresponding to a range of
	 * characters in the original contents of the string.
	 * @param start The start index of the range of characters in the original
	 * contents of the string
	 * @param end The end index of the range of characters in the original
	 * contents of the string
	 * @return A list of ranges corresponding to portions of the current string
	 */
	/*@ non_null @*/ protected List<Range> trackToOutput(int start, int end)
	{
		return trackToOutput(new Range(start, end));
	}

	/**
	 * Creates an annotated string from a scanner.
	 * @param scanner The scanner
	 * @return The annotated string
	 */
	public static AnnotatedString read(Scanner scanner)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		while (scanner.hasNextLine())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(CRLF);
			}
			sb.append(scanner.nextLine());
		}
		return new AnnotatedString(sb.toString());
	}

	/**
	 * Merges all overlapping ranges in a list and sorts the result. For example,
	 * the list [8,13] [0,3] [4,6] [10,15] would become [0,6] [8,15].
	 * @param ranges The list to process
	 */
	protected static void sortAndMerge(List<Range> ranges)
	{
		Collections.sort(ranges);
		int pos = 0;
		while (pos < ranges.size() - 1)
		{
			Range r1 = ranges.get(pos);
			Range r2 = ranges.get(pos + 1);
			if (r1.overlaps(r2))
			{
				Range r = new Range(Math.min(r1.getStart(), r2.getStart()), Math.max(r1.getEnd(), r2.getEnd()));
				ranges.remove(pos + 1);
				ranges.set(pos, r);
			}
			else
			{
				pos++;
			}
		}
	}	
	
	/**
	 * Creates a range that encompasses all the ranges in a list. 
	 * @param ranges The list of ranges
	 * @return The range encompassing them all
	 */
	/*@ null @*/ protected static Range uniteRanges(/*@ non_null @*/ List<Range> ranges)
	{
		int left = -1, right = -1;
		for (Range i_r : ranges)
		{
			if (left < 0 || left > i_r.getStart())
			{
				left = i_r.getStart();
			}
			if (right < 0 || right < i_r.getEnd())
			{
				right = i_r.getEnd();
			}
		}
		if (left < 0 || right < 0)
		{
			return null;
		}
		return new Range(left, right);
	}
	
	/**
	 * Given an arbitrary designator, replaces the first occurrence of
	 * {@link NthOutput} by an instance of {@link NthInput} with given index.
	 * @param from The original part
	 * @param to The part to replace it with
	 * @return The new designator. The input object is not modified if it does
	 * not contain {@code d}
	 */
	/*@ non_null @*/ protected static Part replaceSelfBy(/*@ non_null @*/ Part from, Part to)
	{
		if (from instanceof Self)
		{
			return to;
		}
		if (from instanceof ComposedPart)
		{
			ComposedPart cd = (ComposedPart) from;
			List<Part> desigs = new ArrayList<Part>();
			boolean replaced = false;
			for (int i = 0 ; i < cd.size(); i++)
			{
				Part in_d = cd.get(i);
				if (in_d instanceof Self && !replaced)
				{
					desigs.add(to);
					replaced = true;
				}
				else
				{
					desigs.add(in_d);
				}
			}
			if (!replaced)
			{
				// Return input object if no replacement was done
				return from;
			}
			return ComposedPart.compose(desigs);
		}
		return from;
	}

	public static class Line
	{
		protected final int m_offset;

		protected final String m_string;

		public Line(String s, int offset)
		{
			super();
			m_string = s;
			m_offset = offset;
		}

		public String toString()
		{
			return m_string;
		}

		public int getOffset()
		{
			return m_offset;
		}
	}
}
