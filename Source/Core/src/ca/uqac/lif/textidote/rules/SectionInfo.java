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
package ca.uqac.lif.textidote.rules;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.petitpoucet.function.strings.Range;

/**
 * Information on a section heading inside a document.
 * @author Sylvain Hallé
 */
class SectionInfo
{
	/**
	 * The name of the section heading
	 */
	protected String m_sectionName;

	/**
	 * The range of characters corresponding to this heading in the source
	 * document
	 */
	protected Range m_range;

	/**
	 * The approximate size (in number of words) of this section.
	 * This includes the contents of all subsections contained within
	 * the section.
	 */
	protected int m_size = 0;
	
	protected static final List<String> s_headings = createHeadings();

	/**
	 * Creates a new section info structure
	 * @param section_name The name of the section heading
	 * @param range The range of characters corresponding to this heading
	 * in the source document
	 */
	public SectionInfo(String section_name, Range range)
	{
		super();
		m_sectionName = section_name;
		m_range = range;
		m_size = 0;
	}
	
	/**
	 * Returns the distance between two successive headings. For example,
	 * the distance between a section and a subsection is -1 (moving down
	 * one level); the distance between a subsection and a chapter is +2
	 * (moving up two levels).
	 * @param last_heading The last heading seen
	 * @param heading The heading currently seen
	 * @return The distance
	 */
	public static int distance(String last_heading, String heading)
	{
		return s_headings.indexOf(last_heading) - s_headings.indexOf(heading);
	}
	
	/**
	 * Determines if the new heading is a move down in the heading hierarchy,
	 * with respect to the last heading. For example, seeing a "section"
	 * followed by a "subsection" is a move down.
	 * @param last_heading The last heading seen
	 * @param heading The heading currently seen
	 * @return {@code true} if this is a move down, {@code false} otherwise
	 */
	// TODO: eventually, this method could be replaced with a call to distance()
	public static boolean isMoveDown(String last_heading, String heading)
	{
		if (heading.compareTo("chapter") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("part") == 0;
		}
		if (heading.compareTo("section") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("part") == 0 || last_heading.compareTo("chapter") == 0;
		}
		if (heading.compareTo("subsection") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("part") == 0 || last_heading.compareTo("chapter") == 0 || last_heading.compareTo("section") == 0;
		}
		if (heading.compareTo("subsubsection") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("part") == 0 || last_heading.compareTo("chapter") == 0 || last_heading.compareTo("section") == 0 || last_heading.compareTo("subsection") == 0;
		}
		if (heading.compareTo("paragraph") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("part") == 0 || last_heading.compareTo("chapter") == 0 || last_heading.compareTo("section") == 0 || last_heading.compareTo("subsection") == 0 || last_heading.compareTo("subsubsection") == 0;
		}
		return last_heading.compareTo("") == 0;
	}
	
	public static boolean isMoveDown(SectionInfo last, SectionInfo current)
	{
		return isMoveDown(last.m_sectionName, current.m_sectionName);
	}
	
	/**
	 * Creates an ordered list of headings in LaTeX documents
	 * @return The list of headings
	 */
	protected static List<String> createHeadings()
	{
		List<String> list = new ArrayList<String>(6);
		list.add("part");
		list.add("chapter");
		list.add("section");
		list.add("subsection");
		list.add("subsubsection");
		list.add("paragraph");
		return list;
	}
	
	@Override
	public String toString()
	{
		return "\\" + m_sectionName + "{} " + m_range + " (" + m_size + ")";
	}
}