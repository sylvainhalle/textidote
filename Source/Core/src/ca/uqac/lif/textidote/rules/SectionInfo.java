package ca.uqac.lif.textidote.rules;

import ca.uqac.lif.textidote.as.Range;

class SectionInfo
{
	protected String m_sectionName;

	protected Range m_range;

	protected int m_size = 0;

	public SectionInfo(String section_name, Range range)
	{
		super();
		m_sectionName = section_name;
		m_range = range;
		m_size = 0;
	}
	
	/**
	 * Determines if the new heading is a move down in the heading hierarchy,
	 * with respect to the last heading. For example, seeing a "section"
	 * followed by a "subsection" is a move down.
	 * @param last_heading The last heading seen
	 * @param heading The heading currently seen
	 * @return {@code true} if this is a move down, {@code false} otherwise
	 */
	public static boolean isMoveDown(String last_heading, String heading)
	{
		if (heading.compareTo("section") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("chapter") == 0;
		}
		if (heading.compareTo("subsection") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("chapter") == 0 || last_heading.compareTo("section") == 0;
		}
		if (heading.compareTo("subsubsection") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("chapter") == 0 || last_heading.compareTo("section") == 0 || last_heading.compareTo("subsection") == 0;
		}
		if (heading.compareTo("paragraph") == 0)
		{
			return last_heading.isEmpty() || last_heading.compareTo("chapter") == 0 || last_heading.compareTo("section") == 0 || last_heading.compareTo("subsection") == 0 || last_heading.compareTo("subsubsection") == 0;
		}
		return last_heading.compareTo("") == 0;
	}
	
	public static boolean isMoveDown(SectionInfo last, SectionInfo current)
	{
		return isMoveDown(last.m_sectionName, current.m_sectionName);
	}
	
	@Override
	public String toString()
	{
		return "\\" + m_sectionName + "{} " + m_range + " (" + m_size + ")";
	}
}