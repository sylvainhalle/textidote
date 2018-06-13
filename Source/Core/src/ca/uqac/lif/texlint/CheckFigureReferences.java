/*
    TexLint, a linter for LaTeX documents
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
package ca.uqac.lif.texlint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.texlint.as.AnnotatedString;
import ca.uqac.lif.texlint.as.Position;
import ca.uqac.lif.texlint.as.Range;

/**
 * Checks that every figure with a label is mentioned in the text
 * at least once.
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
		super("sh:fig:001");
	}
	
	@Override
	public List<Advice> evaluate(AnnotatedString s, AnnotatedString original)
	{
		List<Advice> out_list = new ArrayList<Advice>();
		boolean in_figure = false;
		Map<String,Position> figure_defs = new HashMap<String,Position>();
		List<String> lines = original.getLines();
		// Step 1: find all figure labels
		for (int line_cnt = 0; line_cnt < lines.size(); line_cnt++)
		{
			String line = lines.get(line_cnt);
			if (line.matches(".*\\\\begin\\s*\\{\\s*figure.*"))
			{
				in_figure = true;
				continue;
			}
			if (line.matches(".*\\\\end\\s*\\{\\s*figure.*"))
			{
				in_figure = false;
				continue;
			}
			if (in_figure)
			{
				Matcher mat = m_figurePattern.matcher(line);
				if (mat.find())
				{
					String fig_name = mat.group(1).trim();
					Position fig_pos = new Position(line_cnt, mat.start(1));
					figure_defs.put(fig_name, fig_pos);
				}
			}
		}
		// Step 2: find references to these figures
		for (String fig_name : figure_defs.keySet())
		{
			Pattern ref_pat = Pattern.compile("\\\\ref\\s*\\{.*" + fig_name + ".*?\\}");
			boolean found = false;
			for (String line : lines)
			{
				Matcher mat = ref_pat.matcher(line);
				if (mat.find())
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				Position start_pos = figure_defs.get(fig_name);
				Position end_pos = new Position(start_pos.getLine(), start_pos.getColumn() + fig_name.length());
				Range r = new Range(start_pos, end_pos);
				String original_line = lines.get(start_pos.getLine());
				out_list.add(new Advice(this, r, "Figure " + fig_name + " is never referenced in the text", original.getResourceName(), original_line));
			}
		}
		return out_list;
	}

}
