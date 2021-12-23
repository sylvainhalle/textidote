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

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.AdviceRenderer;
import ca.uqac.lif.textidote.as.AnnotatedString.Line;
import ca.uqac.lif.textidote.as.PositionRange;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.util.AnsiPrinter.Color;

/**
 * Renders a list of advice to a terminal (such as {@code stdin}), using
 * colored output.
 * @author Sylvain Hallé
 */
public class AnsiAdviceRenderer extends AdviceRenderer 
{
	/**
	 * The number of characters to display when showing an excerpt
	 * from the file
	 */
	protected int m_lineWidth = 50;

	/**
	 * The width of the line in the terminal (in number of characters)
	 */
	protected int m_terminalLineWidth = 78;

	/**
	 * Creates a new advice renderer
	 * @param printer The printer to which the advice will be printed
	 */
	public AnsiAdviceRenderer(AnsiPrinter printer)
	{
		super(printer);
	}

	@Override
	public void render()
	{
		boolean map_single = m_advice.size() <= 1;
		for (Map.Entry<String,List<Advice>> entry : m_advice.entrySet())
		{
			String filename = entry.getKey();
			List<Advice> list = entry.getValue();
			if (!map_single)
			{
				m_printer.println(filename);
				m_printer.println();
			}
			if (list.isEmpty())
			{
				if (!map_single)
				{
					m_printer.print("* ");
				}
				m_printer.println("Everything is OK!");
			}
			else
			{
				for (Advice ad : list)
				{
					PositionRange pr = ad.getPositionRange();
					m_printer.setForegroundColor(Color.YELLOW);
					m_printer.print("* " + pr);
					m_printer.resetColors();
					m_printer.print(" ");
					wrap(ad.getMessage() + " [" + ad.getRule().getName() + "]", "  ", pr.toString().length() + 2);
					m_printer.println();
					m_printer.setForegroundColor(Color.WHITE);
					renderExcerpt(ad, ad.getLine(), ad.getRange());
				}
			}
		}
	}

	/**
	 * Renders a line of text and "highlights" a portion of it. The highlight
	 * here is simulated with a series of "^" characters, like this:
	 * <pre>
	 * the quick brown fox jumps over the lazy dog
	 *     ^^^^^^^^^^^^^^^
	 * </pre>
	 * @param line The line of text
	 * @param range The range to highlight
	 */
	protected void renderExcerpt(/*@ non_null @*/ Advice ad, /*@ non_null @*/ Line l, /*@ non_null @*/ Range range)
	{
		String line = l.toString();
		int indent = 2;
		int left = ad.getReferenceString().getOriginalPosition(range.getStart()).getColumn();
		int right = ad.getReferenceString().getOriginalPosition(range.getEnd()).getColumn();
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

	/**
	 * Prints some spaces
	 * @param n The number of spaces to print
	 */
	protected void printSpaces(int n)
	{
		for (int i = 0; i < n; i++)
		{
			m_printer.print(" ");
		}	
	}

	/**
	 * Prints a sequence of words, using word wrapping and indenting each
	 * new line
	 * @param message The sequence of words to print
	 * @param indent The indent (a sequence of spaces) to append at the
	 * beginning of each new line
	 * @param start_pos The start position on the first line (set to greater
	 * than 0 to append text to an existing line)
	 */
	protected void wrap(/*@ non_null @*/ String message, /*@ non_null @*/ String indent, int start_pos)
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