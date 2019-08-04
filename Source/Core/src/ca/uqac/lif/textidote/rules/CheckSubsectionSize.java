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
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.as.Range;

/**
 * Checks that each sub-division in the text (section, sub-section, etc.) has
 * a minimum number of words.
 * @author Sylvain Hallé
 */
public class CheckSubsectionSize extends Rule
{
	/**
	 * The pattern to detect a new section heading.
	 */
	protected Pattern m_headingPattern = Pattern.compile("\\\\(chapter|section|subsection|subsubsection)");

	/**
	 * The minimum number of words in a subdivision
	 */
	protected int m_minNumWords = 150;
	
	/**
	 * Ignore this rule for sections named "Conclusion"
	 */
	protected boolean m_ignoreConclusion = true;

	/**
	 * Creates a new instance of the rule
	 */
	public CheckSubsectionSize()
	{
		super("sh:seclen");
	}
	
	/**
	 * Sets the minimum number of words a section requires to avoid a
	 * warning.
	 * @param n The minimum number of words
	 */
	/*@ requires n > 0 @*/
	public void setMinNumWords(int n)
	{
		m_minNumWords = n;
	}

	@Override
	public List<Advice> evaluate(AnnotatedString s, AnnotatedString original) 
	{
		Stack<SectionInfo> sections = new Stack<SectionInfo>();
		List<Advice> out_list = new ArrayList<Advice>();
		List<String> lines = s.getLines();
		sections.push(new SectionInfo("", new Range(Position.ZERO, Position.ZERO)));
		for (int line_cnt = 0; line_cnt < lines.size(); line_cnt++)
		{
			String line = lines.get(line_cnt);
			Matcher mat = m_headingPattern.matcher(line);
			if (mat.find())
			{
				Position start_pos = s.getSourcePosition(new Position(line_cnt, mat.start(1)));
				Position end_pos = s.getSourcePosition(new Position(line_cnt, mat.start(1) + mat.group(1).length()));
				String heading = mat.group(1).trim();
				SectionInfo si = new SectionInfo(heading, new Range(start_pos, end_pos));
				SectionInfo si_last = sections.peek();
				if (SectionInfo.isMoveDown(si_last, si))
				{
					sections.push(si);
				}
				else
				{
					// Move up or same level
					if (si_last.m_sectionName.compareTo(heading) == 0)
					{
						// Same heading
						SectionInfo si_sibling = sections.pop();
						SectionInfo si_parent = sections.peek();
						si_parent.m_size += si_sibling.m_size;
						sections.push(si);
					}
					else
					{
						// Move up
						while (!sections.isEmpty() && si_last.m_sectionName.compareTo(si.m_sectionName) != 0)
						{
							si_last = sections.pop();
							if (si_last.m_size < m_minNumWords && !si_last.m_sectionName.isEmpty())
							{
								Range r2 = si_last.m_range;
								out_list.add(new Advice(this, r2, "This " + si_last.m_sectionName + " is very short (about " + si_last.m_size + " words). You should consider merging it with another section or make it longer.", original.getResourceName(), original.getLine(si_last.m_range.getStart().getLine()), original.getOffset(r2.getStart())));
							}
						}
						sections.push(si);
					}
				}
			}
			else
			{
				// Just increment number of words
				int num_words = countWords(line);
				SectionInfo si_last = sections.peek();
				si_last.m_size += num_words;
			}
		}
		// End
		while (!sections.isEmpty())
		{
			SectionInfo si_last = sections.pop();
			if (!si_last.m_sectionName.isEmpty() && si_last.m_size < m_minNumWords)
			{
				Range r2 = si_last.m_range;
				out_list.add(new Advice(this, r2, "This section is very short (about " + si_last.m_size + " words). You should consider merging it with another section or make it longer.", original.getResourceName(), original.getLine(si_last.m_range.getStart().getLine()), original.getOffset(r2.getStart())));
			}
		}
		return out_list;
	}

	/**
	 * Counts the words in a line. This performs a very crude estimation of
	 * the number of words, by simply counting the number of character
	 * blobs that are separated by spaces. That's good enough for the "size"
	 * rule we are evaluating here.
	 * @param line The line to count words in
	 * @return The number of words
	 */
	protected static int countWords(/*@ non_null @*/ String line)
	{
		return line.split("\\s+").length;
	}
	
	@Override
	public String getDescription()
	{
		return "Short sub-sections";
	}
}
