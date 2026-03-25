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
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Match;

/**
 * Checks that a document does not mix occurrences of <code>\cite</code>
 * and <code>\citep</code>/<code>\citet</code>. * 
 * @author Sylvain Hallé
 *
 */
public class CheckCiteMix extends Rule 
{
	public CheckCiteMix()
	{
		super("sh:c:itemix");
	}

	@Override
	public List<Advice> evaluate(AnnotatedString s)
	{
		List<Advice> out_list = new ArrayList<Advice>();
		Match m1 = s.find("\\\\cite(p|t)");
		Match m2 = s.find("\\\\cite[^pt]");
		if (m1 != null && m2 != null)
		{
			Range r = s.findOriginalRange(new Range(m1.getPosition(), m1.getPosition() + m1.getMatch().length()));
			out_list.add(new Advice(this, r, "Do not mix \\cite with \\citep or \\citet in the same document.", s, s.findOriginalLineOf(m1.getPosition())));
		}
		return out_list;
	}
	
	@Override
	public String getDescription()
	{
		return "No mix of citation styles";
	}
}
