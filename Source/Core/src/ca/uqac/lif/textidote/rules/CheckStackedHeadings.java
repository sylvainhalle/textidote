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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.AnnotatedString.Line;

/**
 * Checks that stacked headings are not present.
 * @author Sylvain Hallé
 */
public class CheckStackedHeadings extends Rule
{
	/**
	 * The pattern to detect a new section heading.
	 */
	protected Pattern m_headingPattern = Pattern.compile("\\\\(part|chapter|section|subsection|subsubsection|paragraph)\\s*\\{");

	/**
	 * Creates a new instance of the rule
	 */
	public CheckStackedHeadings()
	{
		super("sh:nsubdiv");
	}

	@Override
	public List<Advice> evaluate(AnnotatedString s) 
	{
		List<Advice> out_list = new ArrayList<Advice>();
		List<Line> lines = s.getLines();
		boolean found_text = true;
		for (int line_cnt = 0; line_cnt < lines.size(); line_cnt++)
		{
			Line l = lines.get(line_cnt);
			String line = l.toString();
			Matcher mat = m_headingPattern.matcher(line);
			if (mat.find())
			{
				if (!found_text)
				{
					int start_pos = l.getOffset() + mat.start(1);
					int end_pos = l.getOffset() + mat.start(1) + mat.group(1).length();
					Range r = new Range(start_pos, end_pos);
					out_list.add(new Advice(CheckStackedHeadingsAdvice.instance, r, "Avoid stacked headings, i.e. consecutive headings without text in between.", s, l));
				}
				found_text = false;
			}
			else if (!line.trim().isEmpty())
			{
				found_text = true;
			}
		}
		return out_list;
	}

	/**
	 * A placeholder class for a sub-rule checked by {@link CheckStackedHeadings}.
	 * This class exists only to have a different rule ID.
	 */
	public static class CheckStackedHeadingsAdvice extends Rule
	{
		/**
		 * Reference to a single instance of this class
		 */
		protected static final CheckStackedHeadingsAdvice instance = new CheckStackedHeadingsAdvice();
		
		private CheckStackedHeadingsAdvice()
		{
			super("sh:stacked");
		}
		
		@Override
		public List<Advice> evaluate(AnnotatedString s) 
		{
			// Do nothing; this is a placeholder
			return new ArrayList<Advice>(0);
		}

		@Override
		public String getDescription()
		{
			return "Stacked headings";
		}
	}
	
	@Override
	public String getDescription()
	{
		return "Stacked headings";
	}
}