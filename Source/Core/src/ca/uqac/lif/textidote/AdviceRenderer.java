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
package ca.uqac.lif.textidote;

import java.util.List;

import ca.uqac.lif.util.AnsiPrinter;

/**
 * Renders a list of advice in a special format
 * @author Sylvain Hallé
 */
public abstract class AdviceRenderer 
{
	/*@ non_null @*/ protected AnsiPrinter m_printer;
	
	public AdviceRenderer(/*@ non_null @*/ AnsiPrinter printer)
	{
		super();
		m_printer = printer;
	}
	
	/**
	 * Renders the list of advice
	 * @param out A print stream where to send the text
	 * @param list The list of advice to render
	 */
	public abstract void render(/*@ non_null @*/ List<Advice> list);
}
