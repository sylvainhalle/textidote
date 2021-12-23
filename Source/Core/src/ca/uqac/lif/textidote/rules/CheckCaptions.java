/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2019  Sylvain Hallé

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
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.AnnotatedString.Line;
import ca.uqac.lif.textidote.as.Position;

/**
 * Checks that captions end with a period. This rule does not evaluate a
 * regular expression, as the nesting of commands within the caption creates
 * lots of false positives. Rather, it finds an occurrence of
 * <tt>\caption</tt>, and then keeps track of the nesting level of opening
 * and closing braces.
 * 
 * @author Sylvain Hallé
 *
 */
public class CheckCaptions extends Rule 
{
	public CheckCaptions()
	{
		super("sh:capperiod");
	}

	@Override
	public List<Advice> evaluate(AnnotatedString s)
	{
		List<Advice> out_list = new ArrayList<Advice>();
		List<Line> lines = s.getLines();
		for (int line_cnt = 0; line_cnt < lines.size(); line_cnt++)
		{
			Line l = lines.get(line_cnt);
			String line = l.toString();
			int start_pos = line.indexOf("\\caption");
			if (start_pos < 0)
			{
				continue;
			}
			if (line.substring(start_pos).matches("\\\\captionsetup.*"))
			{
				// We consider caption, but we ignore captionsetup
				continue;
			}
			boolean period_seen = false;
			int level = 0;
			for (int i = start_pos + 1; i < line.length(); i++)
			{
				String c = line.substring(i, i + 1);
				if (c.compareTo("{") == 0)
				{
					level++;
					period_seen = false;
				}
				else if (c.compareTo("}") == 0)
				{
					level--;
					if (level == 0 && !period_seen)
					{
						int start_p = s.findOriginalIndex(new Position(line_cnt, start_pos));
						int end_p = s.findOriginalIndex(new Position(line_cnt, i));
						Range r = new Range(start_p, end_p);
						out_list.add(new Advice(this, r, "A caption should end with a period", s, s.findOriginalLine(line_cnt)));
						break;
					}
					period_seen = false;
				}
				else if (c.compareTo(".") == 0)
				{
					period_seen = true;
				}
				else if (c.compareTo(" ") != 0)
				{
					period_seen = false;
				}
			}
		}
		return out_list;
	}

	@Override
	public String getDescription()
	{
		return "Period at the end of table and figure captions";
	}
}
