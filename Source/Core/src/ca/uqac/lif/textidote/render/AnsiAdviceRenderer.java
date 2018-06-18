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
package ca.uqac.lif.textidote.render;

import java.util.List;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.AdviceRenderer;
import ca.uqac.lif.textidote.as.Range;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.util.AnsiPrinter.Color;

/**
 * Renders a list of advice to a terminal (such as stdin), using colored
 * output.
 * @author Sylvain Hallé
 */
public class AnsiAdviceRenderer extends AdviceRenderer 
{
	/**
	 * The number of characters to display when showing an excerpt
	 * from the file
	 */
	protected int m_lineWidth = 50;
	
	protected int m_terminalLineWidth = 78;
	
	public AnsiAdviceRenderer(AnsiPrinter printer)
	{
		super(printer);
	}

	@Override
	public void render(List<Advice> list)
	{
		if (list.isEmpty())
		{
			m_printer.println("Everything is OK!");
		}
		else
		{
			for (Advice ad : list)
			{
				m_printer.setForegroundColor(Color.YELLOW);
				m_printer.print("* " + ad.getRange());
				m_printer.resetColors();
				m_printer.print(" ");
				wrap(ad.getMessage() + " [" + ad.getRule().getName() + "]", "  ", ad.getRange().toString().length() + 2);
				m_printer.println();
				m_printer.setForegroundColor(Color.WHITE);
				renderExcerpt(ad.getLine(), ad.getRange());
			}
		}
	}
	
	protected void renderExcerpt(String line, Range range)
	{
		int indent = 2;
		int left = range.getStart().getColumn();
		int right = range.getEnd().getColumn();
		int range_width = right - left;
		int mid_point = left + range_width / 2;
		int offset = 0;
		if (range_width < line.length())
		{
			if (mid_point + m_lineWidth / 2 >= line.length())
			{
				int char_dif = (mid_point + m_lineWidth / 2) - line.length();
				offset = Math.max(0, (mid_point - m_lineWidth / 2) - char_dif);
			}
			else
			{
				offset = Math.max(0, mid_point - m_lineWidth / 2);
			}
		}
		String line_to_display = line.substring(offset, Math.min(line.length(), offset + m_lineWidth));
		printSpaces(indent);
		m_printer.println(line_to_display);
		// Show squiggly line
		printSpaces(indent + Math.max(0, left - offset));
		m_printer.setForegroundColor(Color.LIGHT_RED);
		for (int i = 0; i < range_width + 1; i++)
		{
			m_printer.append("^");
		}
		m_printer.resetColors();
		m_printer.println();
	}
	
	protected void printSpaces(int n)
	{
		for (int i = 0; i < n; i++)
		{
			m_printer.print(" ");
		}	
	}
	
	/*@ pure @*/ protected void wrap(/*@ non_null @*/ String message, String indent, int start_pos)
	{
		int cur_width = start_pos;
		String[] words = message.split(" ");
		for (String word : words)
		{
			cur_width += word.length() + 1;
			if (cur_width > m_terminalLineWidth)
			{
				m_printer.println();
				m_printer.print(indent);
				cur_width = word.length() + 1;
			}
			if (word.startsWith("[") && word.endsWith("]"))
			{
				m_printer.setForegroundColor(Color.BROWN);
				m_printer.print(word + " ");
				m_printer.resetColors();
			}
			else
			{
				// Language Tool advice has this strange markup
				word = word.replaceAll("<suggestion>", "'");
				word = word.replaceAll("</suggestion>", "'");
				m_printer.print(word + " ");
			}
		}
	}
}