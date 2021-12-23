/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2021  Sylvain Hallé

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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.AdviceRenderer;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.util.AnsiPrinter;

public class HtmlAdviceRenderer extends AdviceRenderer
{
	/**
	 * Creates a new HTML advice renderer
	 * @param printer The printer where the renderer will print its
	 * results
	 */
	public HtmlAdviceRenderer(/*@ non_null @*/ AnsiPrinter printer)
	{
		super(printer);
	}

	@Override
	public void render()
	{
		printFromInternalFile("preamble.html");
		boolean map_single = m_advice.size() <= 1;
		for (Map.Entry<String,List<Advice>> entry : m_advice.entrySet())
		{
			String filename = entry.getKey();
			AnnotatedString original_string = m_originalStrings.get(filename);
			escape(original_string);
			List<Advice> list = entry.getValue();
			if (!map_single)
			{
				m_printer.println("<h2 class=\"filename\">" + escape(filename) + "</h2>");
				m_printer.println("");
			}
			m_printer.println("<p>Found " + list.size() + " warning(s)</p>");
			m_printer.println("<div class=\"original-file\">");
			for (Advice ad : list)
			{
				Range r = original_string.findCurrentRange(ad.getRange());
				if (r == null)
				{
					// For some reason, this advice has no range; nothing to do
					continue;
				}
				original_string.insertAt("</span>", r.getEnd() + 1);
				original_string.insertAt(getOpeningSpan(ad), r.getStart());
			}
			// At this point we no longer need provenance marking; 
			// flatten to a plain string to speed things up
			String markup = original_string.toString();
			markup = highlightLatex(markup);
			markup = indent(markup);
			markup = markup.replaceAll("(?m)^", "<div class=\"linenb\">#NB</div><div class=\"codeline\">");
			markup = markup.replaceAll("(?m)$", "</div><div class=\"clear\"></div>");
			int num_digits = (int) Math.ceil((Math.log10(original_string.lineCount())));
			if (num_digits == 0)
			{
				num_digits = 1;
			}

			int line_cnt = original_string.lineCount();
			for (int i = 0; i < line_cnt; i++)
			{
				markup = markup.replaceFirst("#NB", printLineNumber(i + 1, num_digits));
			}
			m_printer.println(markup);
			m_printer.println("</div>");
		}
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
		s = s.replaceAll("(?m)^(%.*?)$", "<span class=\"comment\">$1</span>");
		//s = s.replaceAll("(?m)([^\\\\])(%.*?)$", "$1<span class=\"comment\">$2</span>");
		return s;
	}

	protected static String indent(String s)
	{
		Pattern pat = Pattern.compile("(?m)^([ ]+?)([^ ])");
		boolean matched = true;
		while (matched)
		{
			Matcher mat = pat.matcher(s);
			matched = mat.find();
			if (matched)
			{
				String pad = "";
				for (int i = 0; i < mat.group(1).length(); i++)
				{
					pad += "&nbsp;";
				}
				pad += mat.group(2);
				String new_s = s.substring(0, mat.start()) + pad + s.substring(mat.end());
				s = new_s;
			}
		}
		return s;
	}

	protected static String printLineNumber(int n, int width)
	{
		String number = String.format("%" + width + "d", n);
		number = number.replaceAll(" ", "&nbsp;");
		return number;
	}

}
