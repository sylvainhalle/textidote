/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2023  Sylvain Hallé

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
package ca.uqac.lif.textidote.render;

import java.util.List;
import java.util.Map;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.as.PositionRange;
import ca.uqac.lif.util.AnsiPrinter;

/**
 * Renders advice in a "file:line:column: message" format that can be parsed by
 * many terminals and editors as clickable diagnostics.
 */
public class ClickableAdviceRenderer extends SinglelineAdviceRenderer
{
	/**
	 * Creates a new advice renderer.
	 *
	 * @param printer
	 *          The printer to which the advice will be printed
	 */
	public ClickableAdviceRenderer(AnsiPrinter printer)
	{
		super(printer);
	}

	@Override
	public void render()
	{
		for (Map.Entry<String, List<Advice>> entry : m_advice.entrySet())
		{
			String filename = entry.getKey();
			List<Advice> list = entry.getValue();
			if (!list.isEmpty())
			{
				for (Advice ad : list)
				{
					PositionRange range = ad.getPositionRange();
					Position start = range.getStart();
					int line = Math.max(1, start.getLine() + 1);
					int col = Math.max(1, start.getColumn() + 1);
					m_printer.print(filename + ":" + line + ":" + col + ": ");
					m_printer.print(
							ad.getMessage().replaceAll("<suggestion>", "").replaceAll("</suggestion", "").trim());
					renderExcerpt(ad.getReferenceString(), ad.getLine(), ad.getRange());
					m_printer.println();
				}
			}
		}
	}
}
