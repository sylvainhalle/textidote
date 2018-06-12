package ca.uqac.lif.texlint;

import java.util.List;

import ca.uqac.lif.texlint.as.Range;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.util.AnsiPrinter.Color;

public class AnsiAdviceRenderer extends AdviceRenderer 
{
	/**
	 * The number of characters to display when showing an excerpt
	 * from the file
	 */
	protected int m_lineWidth = 50;
	
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
			m_printer.println("Found " + list.size() + " warning(s)");
			m_printer.println();
			for (Advice ad : list)
			{
				m_printer.setForegroundColor(Color.YELLOW);
				m_printer.print("* " + ad.getRange());
				m_printer.resetColors();
				m_printer.println(" " + ad.getMessage());
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
		m_printer.setForegroundColor(Color.RED);
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
}
