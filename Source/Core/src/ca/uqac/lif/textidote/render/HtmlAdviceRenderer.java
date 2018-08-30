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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.AdviceRenderer;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.as.Range;
import ca.uqac.lif.util.AnsiPrinter;

public class HtmlAdviceRenderer extends AdviceRenderer 
{
	/*@ non_null @*/ protected AnnotatedString m_originalString;
	
	/**
	 * Creates a new HTML advice renderer
	 * @param original The original string
	 */
	public HtmlAdviceRenderer(AnsiPrinter printer, /*@ non_null @*/ AnnotatedString original)
	{
		super(printer);
		m_originalString = original;
	}
	
	@Override
	public void render(/*@ non_null @*/List<Advice> list) 
	{
		Map<Integer,List<Advice>> map = groupAdviceByStartLine(list);
		printFromInternalFile("preamble.html");
		m_printer.println("<p>Found " + list.size() + " warning(s)</p>");
		m_printer.println("<div class=\"original-file\">");
		int num_digits = (int) Math.ceil((Math.log10(m_originalString.lineCount())));
		for (int cur_line_nb = 0; cur_line_nb < m_originalString.lineCount(); cur_line_nb++)
		{
			String cur_line = m_originalString.getLine(cur_line_nb);
			m_printer.print(printLineNumber(cur_line_nb + 1, num_digits)); // +1 since 1st line is 1, not 0
			m_printer.print("<div class=\"codeline\">");
			if (!map.containsKey(cur_line_nb))
			{
				// No advice on this line: print it as is
				m_printer.print(highlightLatex(escape(cur_line)));
				m_printer.println("</div><div class=\"clear\"></div>");
				continue;
			}
			AnnotatedString a_cur_line = new AnnotatedString().append(cur_line, Range.make(cur_line_nb, 0, cur_line.length() - 1));
			a_cur_line = escape(a_cur_line);
			List<Advice> ad_list = map.get(cur_line_nb);
			//Collections.sort(ad_list, Collections.reverseOrder());
			// Go through advice, starting from the end of the line
			for (Advice ad : ad_list)
			{
				Range r = ad.getRange();
				Position start_tgt_pos = a_cur_line.getTargetPosition(r.getStart());
				if (start_tgt_pos.equals(Position.NOWHERE))
				{
					// This is a corner case where the first character of the
					// region to find is "&", "<" or ">"; since it has been escaped,
					// the connection to the original location is lost. Hack:
					// let's try to find the position of the character just before and
					// increment it by 1.
					start_tgt_pos = a_cur_line.getTargetPosition(r.getStart().moveBy(-1));
					if (start_tgt_pos.equals(Position.NOWHERE))
					{
						// Still no luck: give up
						continue;
					}
					start_tgt_pos = start_tgt_pos.moveBy(1);
				}
				assert start_tgt_pos != null;
				// Reset line nb to 0, as we operate on a line-by-line basis
				start_tgt_pos = new Position(0, start_tgt_pos.getColumn());
				Position end_tgt_pos = a_cur_line.getTargetPosition(r.getEnd());
				if (end_tgt_pos.equals(Position.NOWHERE))
				{
					end_tgt_pos = start_tgt_pos.moveBy(1);
				}
				assert end_tgt_pos != null;
				// Reset line nb to 0, as we operate on a line-by-line basis
				end_tgt_pos = new Position(0, end_tgt_pos.getColumn());
				AnnotatedString as_left = a_cur_line.substring(Position.ZERO, start_tgt_pos.moveBy(-1));
				AnnotatedString as_middle = a_cur_line.substring(start_tgt_pos, end_tgt_pos.moveBy(-1));
				AnnotatedString as_right = a_cur_line.substring(end_tgt_pos);
				if (as_middle.toString().startsWith("&"))
				{
					// This is an HTML entity; the first "character" spans up to the semicolon
					int semicolon_index = as_right.toString().indexOf(";");
					end_tgt_pos = end_tgt_pos.moveBy(semicolon_index);
					as_middle = a_cur_line.substring(start_tgt_pos, end_tgt_pos);
					as_right = a_cur_line.substring(end_tgt_pos.moveBy(1));
				}
				as_left.append(getOpeningSpan(ad));
				as_left.append(as_middle);
				as_left.append("</span>");
				as_left.append(as_right);
				a_cur_line = as_left;
			}
			m_printer.print(highlightLatex(a_cur_line.toString()));
			m_printer.println("</div><div class=\"clear\"></div>");
		}
		m_printer.println("</div>");
		printFromInternalFile("postamble.html");
	}
	
	/**
	 * Creates the opening &lt;span&gt; tag corresponding to a specific advice
	 * @param ad The advice
	 * @return A string with the concents of the opening &lt;span&gt; tag
	 */
	/*@ non_null @*/ protected static String getOpeningSpan(/*@ non_null @*/ Advice ad)
	{
		String category = "";
		String rule_name = ad.getRule().getName();
		if (rule_name.startsWith("sh"))
		{
			category = "-sh";
		}
		if (rule_name.contains("MORFOLOGIK"))
		{
			category = "-spelling";
		}
		StringBuilder span = new StringBuilder();
		span.append("<span class=\"highlight").append(category).append("\" ");
		String message = ad.getMessage() + " [" + ad.getRule().getName() + "]";
		message = message.replaceAll("<suggestion>|</suggestion>|\"", "'");
		span.append("title=\"").append(escape(message)).append("\"");
		span.append(">");
		return span.toString();
	}
	
	/**
	 * Writes to the print stream the contents of an internal file
	 * @param filename The name of the internal file
	 */
	protected void printFromInternalFile(/*@ non_null @*/ String filename)
	{
		Scanner scanner = new Scanner(HtmlAdviceRenderer.class.getResourceAsStream(filename));
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			m_printer.println(line);
		}
		scanner.close();	
	}
	
	/**
	 * Groups a list of advice by the line number of the start position of
	 * their character range.
	 * @param list The list of advice
	 * @return A map from line numbers to a list of advice
	 */
	/*@ pure non_null @*/ protected Map<Integer,List<Advice>> groupAdviceByStartLine(/*@ non_null @*/ List<Advice> list)
	{
		Map<Integer,List<Advice>> map = new HashMap<Integer,List<Advice>>(); 
		for (Advice ad : list)
		{
			Position start_pos = ad.getRange().getStart();
			Position start_src_pos = m_originalString.getSourcePosition(start_pos);
			if (start_src_pos != null)
			{
				int line = start_src_pos.getLine();
				List<Advice> ad_list = null;
				if (map.containsKey(line))
				{
					ad_list = map.get(line);
				}
				else
				{
					ad_list = new ArrayList<Advice>();
				}
				ad_list.add(ad);
				map.put(line, ad_list);
			}
		}
		return map;
	}
	
	/**
	 * Escapes the special HTML characters in the string
	 * @param s The string
	 * @return The escaped string
	 */
	protected static String escape(String s)
	{
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}
	
	/**
	 * Escapes the special HTML characters in the string
	 * @param s The string
	 * @return The escaped string
	 */
	protected static AnnotatedString escape(AnnotatedString s)
	{
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}
	
	/**
	 * Performs a basic syntax highlighting of LaTeX markup in the string.
	 * @param s The string to highlight
	 * @return The new string, with &lt;span&gt; tags inserted around some
	 * LaTeX keywords
	 */
	protected static String highlightLatex(String s)
	{
		if (s.isEmpty())
		{
			return "&nbsp;";
		}
		s = s.replaceAll("\\\\(textbf|emph|textit|section|subsection|subsubsection|paragraph|includegraphics|caption|label|maketitle|documentclass|item|documentclass|usepackage|title)", "<span class=\"keyword1\">\\\\$1</span>");
		s = s.replaceAll("\\\\(begin|end)(\\{.*?\\})", "<span class=\"keyword2\">\\\\$1$2</span>");
		s = s.replaceAll("(%.*)$", "<span class=\"comment\">$1</span>");
		return s;
	}
	
	protected static String printLineNumber(int n, int width)
	{
		StringBuilder out = new StringBuilder(); 
		out.append("<div class=\"linenb\">");
		String number = String.format("%" + width + "d", n);
		number = number.replaceAll(" ", "&nbsp;");
		out.append(number);
		out.append("</div>");
		return out.toString();
	}

}
