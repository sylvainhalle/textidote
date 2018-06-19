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

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.as.Range;

/**
 * Checks that text paragraphs do not contain forced line breaks.
 * 
 * @author Sylvain Hallé
 *
 */
public class CheckNoBreak extends Rule 
{
	/**
	 * The pattern for finding figure labels
	 */
	Pattern m_breakPattern = Pattern.compile("\\\\\\\\");

	public CheckNoBreak()
	{
		super("sh:nobreak");
	}

	@Override
	public List<Advice> evaluate(AnnotatedString s, AnnotatedString original)
	{
		List<Advice> out_list = new ArrayList<Advice>();
		List<String> lines = s.getLines();
		int env_level = 0;
		for (int line_cnt = 0; line_cnt < lines.size(); line_cnt++)
		{
			String line = lines.get(line_cnt);
			if (line.matches(".*\\\\begin\\s*\\{\\s*(equation|table|tabular|verbatim|lstlisting|IEEEkeywords|figure).*") || line.matches(".*\\\\\\[.*"))
			{
				env_level++;
			}
			if (env_level == 0)
			{
				Matcher mat = m_breakPattern.matcher(line);
				if (mat.find())
				{
					// Forced break
					Position start_pos = s.getSourcePosition(new Position(line_cnt, mat.start()));
					Position end_pos = s.getSourcePosition(new Position(line_cnt, mat.start() + mat.group(0).length()));
					Range r = new Range(start_pos, end_pos);
					out_list.add(new Advice(this, r, "You should not break lines manually in a paragraph. Either start a new paragraph or stay in the current one.", original.getResourceName(), original.getLine(start_pos.getLine())));	
				}
			}
			if (line.matches(".*\\\\end\\s*\\{\\s*(equation|table|tabular|verbatim|lstlisting|IEEEkeywords|figure).*") || line.matches(".*\\\\\\].*"))
			{
				env_level--;
			}
		}
		return out_list;
	}
}
