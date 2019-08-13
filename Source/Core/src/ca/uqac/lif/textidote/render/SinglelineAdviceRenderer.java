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
package ca.uqac.lif.textidote.render;

import java.util.List;
import java.util.Map;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.AdviceRenderer;
import ca.uqac.lif.textidote.as.Range;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.util.AnsiPrinter.Color;

/**
 * Renders advice to a terminal (such as {@code stdin}), printing a single line
 * per advice, using colored output.
 * @author toolcreator
 */
public class SinglelineAdviceRenderer extends AdviceRenderer {
	/**
	 * Creates a new advice renderer
	 *
	 * @param printer The printer to which the advice will be printed
	 */
	public SinglelineAdviceRenderer(AnsiPrinter printer) {
		super(printer);
	}

	@Override
	public void render() {
		for (Map.Entry<String, List<Advice>> entry : m_advice.entrySet()) {
			String filename = entry.getKey();
			List<Advice> list = entry.getValue();
			if (!list.isEmpty()) {
				for (Advice ad : list) {
					m_printer.setForegroundColor(Color.YELLOW);
					m_printer.print(filename + "(" + ad.getRange() + ")");
					m_printer.resetColors();
					m_printer.print(": ");
					m_printer.print(ad.getMessage().replaceAll("<suggestion>", "").replaceAll("</suggestion", "").trim());
					renderExcerpt(ad.getLine(), ad.getRange());
					m_printer.println();
				}
			}
		}
	}

	/**
	 * Renders a line of text and "highlights" a portion of it. The highlight
	 * here is represented by printing the text red:
	 * @param line The line of text
	 * @param range The range to highlight
	 */
	protected void renderExcerpt(/*@ non_null @*/ String line, /*@ non_null @*/ Range range)
	{
		m_printer.print(" \"");
		m_printer.setForegroundColor(Color.WHITE);
		Position start = range.getStart();
		Position end = range.getEnd();
		if (start.compareTo(end) < 0 && start.getColumn() <= line.length())
		{
			m_printer.print(line.substring(0, start.getColumn()));
			m_printer.setForegroundColor(Color.LIGHT_RED);
			if (start.getLine() != end.getLine() || end.getColumn() + 1 >= line.length())
			{
				m_printer.print(line.substring(start.getColumn(), line.length()));
			}
			else
			{
				m_printer.print(line.substring(start.getColumn(), end.getColumn() + 1));
				m_printer.setForegroundColor(Color.WHITE);
				m_printer.print(line.substring(end.getColumn() + 1, line.length()));
			}
		}
		else
		{
			m_printer.print(line);
		}
		m_printer.resetColors();
		m_printer.print("\"");
	}
}
