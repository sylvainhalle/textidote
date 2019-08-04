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
 * Checks that a document does not mix occurrences of <tt>\cite</tt>
 * and <tt>\citep</tt>/<tt>\citet</tt>. * 
 * @author Sylvain Hallé
 *
 */
public class CheckCiteMix extends Rule 
{
	/**
	 * The pattern for finding {@code \cite} references
	 */
	Pattern m_citePattern = Pattern.compile("\\\\cite[^pt]");
	
	/**
	 * The pattern for finding {@code \citep} and {@code \citet}
	 * references
	 */
	Pattern m_citepPattern = Pattern.compile("\\\\cite(p|t)");

	public CheckCiteMix()
	{
		super("sh:c:itemix");
	}

	@Override
	public List<Advice> evaluate(AnnotatedString s, AnnotatedString original)
	{
		List<Advice> out_list = new ArrayList<Advice>();
		boolean found_citep = false, found_cite = false;
		List<String> lines = s.getLines();
		for (int line_cnt = 0; line_cnt < lines.size(); line_cnt++)
		{
			String line = lines.get(line_cnt);
			Matcher mat = m_citePattern.matcher(line);
			if (mat.find())
			{
				found_cite = true;
				if (found_citep && found_cite)
				{
					Position start_pos = s.getSourcePosition(new Position(line_cnt, mat.start()));
					Position end_pos = s.getSourcePosition(new Position(line_cnt, mat.start() + 6)); // 6 = length("\cite")
					Range r = new Range(start_pos, end_pos);
					out_list.add(new Advice(this, r, "Do not mix \\cite with \\citep or \\citet in the same document.", original.getResourceName(), original.getLine(start_pos.getLine()), original.getOffset(start_pos)));
					break; // A single warning is enough
				}
			}
			mat = m_citepPattern.matcher(line);
			if (mat.find())
			{
				found_citep = true;
				if (found_citep && found_cite)
				{
					Position start_pos = s.getSourcePosition(new Position(line_cnt, mat.start()));
					Position end_pos = s.getSourcePosition(new Position(line_cnt, mat.start() + 7)); // 6 = length("\citep")
					Range r = new Range(start_pos, end_pos);
					out_list.add(new Advice(this, r, "Do not mix \\cite with \\citep or \\citet in the same document.", original.getResourceName(), original.getLine(start_pos.getLine()), original.getOffset(start_pos)));
					break; // A single warning is enough
				}
			}
		}
		return out_list;
	}
	
	@Override
	public String getDescription()
	{
		return "No mix of citation styles";
	}
}
