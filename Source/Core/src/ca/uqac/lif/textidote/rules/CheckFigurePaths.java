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
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.as.AnnotatedString.Line;

/**
 * Checks that included figures are not referenced  by an absolute local path.
 * <p>
 * For the means of this rule, an absolute path is anything of the form
 * <ul>
 * <li><code>X:...</code> (Windows drive letter)</li>
 * <li><code>/...</code> (Unix absolute path)</li>
 * <li>anything that contains <code>..</code> (moving up in the folder
 * structure is not recommended)</li>
 * </ul>
 * Formally, this rule checks that no such path appears in an
 * <code>\includegraphics</code> command.
 * 
 * @author Sylvain Hallé
 *
 */
public class CheckFigurePaths extends Rule 
{
	/**
	 * The pattern for finding figure labels
	 */
	Pattern m_figurePattern = Pattern.compile("\\\\includegraphics\\s*(\\[.*?\\])*?\\{(.*?)\\}");

	public CheckFigurePaths()
	{
		super("sh:relpath");
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
			Matcher mat = m_figurePattern.matcher(line);
			if (mat.find())
			{
				String path = mat.group(2).trim();
				if (isAbsolute(path))
				{
					// Absolute path
					int start_pos = mat.start(2);
					int end_pos = mat.start(2) + mat.group(2).length() - 1;
					int start_p = s.findOriginalIndex(new Position(line_cnt, start_pos));
					int end_p = s.findOriginalIndex(new Position(line_cnt, end_pos));
					Range r = new Range(start_p, end_p);
					out_list.add(new Advice(this, r, "Do not use an absolute path for a figure", s, l));	
				}
			}
		}
		return out_list;
	}

	/**
	 * Checks if string represents an absolute path (or at least a path
	 * outside the current folder)
	 * @param path The path
	 * @return {@code true} if the path is absolute
	 */
	protected static boolean isAbsolute(/*@ non_null @*/ String path)
	{
		return path.matches("[A-Za-z]\\:.*") || path.matches("/.*") || path.contains("..");
	}
	
	@Override
	public String getDescription()
	{
		return "Absolute paths in figures";
	}
}
