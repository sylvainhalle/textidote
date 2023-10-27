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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.AnnotatedString.Line;

/**
 * Checks that every figure with a label is mentioned in the text
 * at least once.
 * <p>
 * Formally, this rule checks that for every occurrence of <tt>\label{X}</tt>
 * within a <tt>figure</tt> environment (that is not commented out), there
 * exists a <tt>\ref{X}</tt> somewhere in the text (that is not commented
 * out).
 * 
 * @author Sylvain Hallé
 *
 */
public class CheckFigureReferences extends Rule 
{
	/**
	 * The pattern for finding figure labels
	 */
	Pattern m_figurePattern = Pattern.compile("\\\\label\\s*\\{(.*?)\\}");
	
	public CheckFigureReferences()
	{
		super("sh:figref");
	}
	
	@Override
	public List<Advice> evaluate(AnnotatedString s)
	{
		List<Advice> out_list = new ArrayList<Advice>();
		boolean in_figure = false;
		Map<String,Integer> figure_defs = new HashMap<String,Integer>();
		List<Line> lines = s.getLines();
		boolean found_label = false;
		// Step 1: find all figure labels
		for (int line_cnt = 0; line_cnt < lines.size(); line_cnt++)
		{
			Line l = lines.get(line_cnt);
			String line = l.toString();
			if (line.matches(".*\\\\begin\\s*\\{\\s*(figure|wrapfigure).*"))
			{
				in_figure = true;
				found_label = false;
				continue;
			}
			if (line.matches(".*\\\\end\\s*\\{\\s*(figure|wrapfigure).*"))
			{
				in_figure = false;
				if (!found_label)
				{
					// This figure is missing a label
					int start_pos = l.getOffset();
					int end_pos = start_pos + line.length();
					Range r = new Range(start_pos, end_pos);
					out_list.add(new Advice(this, r, "This figure is missing a label", s, l));	
				}
				continue;
			}
			if (in_figure)
			{
				Matcher mat = m_figurePattern.matcher(line);
				if (mat.find())
				{
					String fig_name = mat.group(1).trim();
					int fig_pos = l.getOffset() + mat.start(1);
					figure_defs.put(fig_name, fig_pos);
					found_label = true;
				}
			}
		}
		// Step 2: find references to these figures
		for (String fig_name : figure_defs.keySet())
		{
			if (s.find("\\\\(C|c){0,1}ref\\s*\\{.*" + fig_name + ".*?\\}") == null)
			{
				// This figure is not referenced
				int start_pos = figure_defs.get(fig_name);
				int end_pos = start_pos + fig_name.length() - 1;
				Range r = s.findOriginalRange(start_pos, end_pos);
				Line original_line = s.getOriginalLineOf(start_pos);
				out_list.add(new Advice(this, r, "Figure " + fig_name + " is never referenced in the text", s, original_line));
			}
		}
		return out_list;
	}

	@Override
	public String getDescription()
	{
		return "Every figure must be referenced";
	}
}
