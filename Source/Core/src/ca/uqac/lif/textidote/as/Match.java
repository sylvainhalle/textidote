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
import java.util.List;

/**
 * Represents a match by a regular expression find.
 * @author Sylvain Hallé
 */
public class Match 
{
	/**
	 * The position where the match occurred 
	 */
	private final int m_position;
	
	/**
	 * The string that matched the regular expression
	 */
	private final String m_match;
	
	/**
	 * A list with the capture groups for the regex
	 */
	private List<String> m_groups;
	
	/**
	 * Creates a new match object
	 * @param match The position where the match occurred
	 * @param pos The string that matched the regular expression
	 */
	public Match(String match, int pos)
	{
		super();
		m_match = match;
		m_position = pos;
		m_groups = new ArrayList<String>();
	}
	
	/**
	 * Gets the linear position where the match occurred
	 * @return The position
	 */
	public int getPosition()
	{
		return m_position;
	}
	
	/**
	 * Gets the string that matched the regular expression
	 * @return The string
	 */
	public String getMatch()
	{
		return m_match;
	}
	
	/**
	 * Adds the string for a new capture group. Capture groups should
	 * be added in the order they are named in the corresponding regex.
	 * @param s The string
	 */
	public void addGroup(String s)
	{
		m_groups.add(s);
	}
	
	/**
	 * Gets the capture group with given index 
	 * @param index The index of the capture group
	 * @return The string corresponding to the capture group
	 */
	public String group(int index)
	{
		return m_groups.get(index);
	}
	
	/**
	 * Gets the number of capture groups mentioned in this match
	 * @return The number of capture groups
	 */
	public int groupCount()
	{
		return m_groups.size();
	}
	
	@Override
	public String toString()
	{
		return m_match + " (" + m_position + ")";
	}
}
