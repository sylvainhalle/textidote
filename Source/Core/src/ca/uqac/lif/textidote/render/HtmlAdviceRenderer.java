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

    ---
    PATCH (non officiel) : la boucle de numérotation de lignes dans render()
    a été réécrite pour éviter un comportement en O(n*m) (n = nombre de lignes,
    m = taille du document), qui rendait la génération du rapport HTML très
    lente sur les documents volumineux (plusieurs milliers de lignes).
    L'ancienne version appelait String.replaceFirst() une fois par ligne, ce
    qui force Java à rescanner et recopier l'intégralité du texte à chaque
    itération. La nouvelle version découpe le texte une seule fois et le
    reconstruit en une seule passe avec un StringBuilder.
 */
package ca.uqac.lif.textidote.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

			// --- PATCH : on ne modifie plus original_string un avertissement
			// à la fois via insertAt() (qui recopie toute la chaîne ET
			// recompose toute la table de correspondance de positions à
			// CHAQUE appel - un comportement qui s'aggrave au fil des
			// avertissements). findCurrentRange() ne modifie rien, donc on
			// peut appeler cette méthode pour tous les avertissements sans
			// jamais muter la chaîne, puis insérer toutes les balises <span>
			// en une seule passe à la fin. Le code original indiquait
			// lui-même juste après cette boucle que le suivi de provenance
			// (le mécanisme derrière insertAt) n'était de toute façon plus
			// utile une fois cette étape terminée.
			List<SpanMarker> markers = new ArrayList<SpanMarker>(list.size() * 2);
			for (Advice ad : list)
			{
				Range r = original_string.findCurrentRange(ad.getRange());
				if (r == null)
				{
					// For some reason, this advice has no range; nothing to do
					continue;
				}
				markers.add(new SpanMarker(r.getStart(), getOpeningSpan(ad)));
				markers.add(new SpanMarker(r.getEnd() + 1, "</span>"));
			}
			String markup = insertSpansInOnePass(original_string.toString(), markers);
			// --- FIN PATCH ---

			markup = highlightLatex(markup);
			markup = indent(markup);

			// --- PATCH : numérotation des lignes en une seule passe ---
			// (remplace l'ancien bloc replaceAll("(?m)^", ...) + replaceAll("(?m)$", ...)
			//  + boucle de replaceFirst("#NB", ...) appelée une fois par ligne)
			markup = numberLines(markup, original_string.lineCount());
			// --- FIN PATCH ---

			m_printer.println(markup);
			m_printer.println("</div>");
		}
		printFromInternalFile("postamble.html");
	}

	/**
	 * Wraps every line of the given markup in the "linenb"/"codeline"/"clear"
	 * div structure expected by the report's CSS, and prefixes it with its
	 * line number. This is done in a single pass over the text (using
	 * String.split + StringBuilder) instead of repeatedly scanning and
	 * rebuilding the whole string once per line, which does not scale to
	 * large documents.
	 * @param markup The (already escaped/highlighted/indented) HTML markup
	 * @param line_count The expected number of lines (used only to compute
	 * the width used to pad line numbers; the actual splitting is done on
	 * the markup itself so the two stay consistent even if they differ)
	 * @return The final markup, with line numbering applied
	 */
	protected static String numberLines(/*@ non_null @*/ String markup, int line_count)
	{
		// -1 keeps trailing empty lines (important if the file ends with
		// a blank line, otherwise split() silently drops it)
		String[] lines = markup.split("\n", -1);
		int effective_count = Math.max(lines.length, Math.max(line_count, 1));
		int num_digits = (int) Math.ceil(Math.log10(effective_count));
		if (num_digits == 0)
		{
			num_digits = 1;
		}
		// Pre-size the buffer to roughly the final size to avoid internal
		// re-allocations of the StringBuilder as it grows
		StringBuilder sb = new StringBuilder(markup.length() + lines.length * 96);
		for (int i = 0; i < lines.length; i++)
		{
			sb.append("<div class=\"linenb\">")
			  .append(printLineNumber(i + 1, num_digits))
			  .append("</div><div class=\"codeline\">")
			  .append(lines[i])
			  .append("</div><div class=\"clear\"></div>");
			if (i < lines.length - 1)
			{
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	/**
	 * A small helper class representing a piece of text (e.g. an opening or
	 * closing {@code <span>} tag) to be inserted at a given position of a
	 * base string.
	 */
	protected static class SpanMarker
	{
		final int position;
		final String text;

		SpanMarker(int position, String text)
		{
			this.position = position;
			this.text = text;
		}
	}

	/**
	 * Inserts a set of markers (e.g. opening/closing {@code <span>} tags) into
	 * a base string, in a single left-to-right pass, instead of mutating the
	 * string once per marker (which is what the previous implementation did
	 * via repeated calls to {@code AnnotatedString.insertAt}).
	 * @param content The base string, without any of the markers inserted
	 * @param markers The list of markers to insert; order in the list does
	 * not matter, they are sorted internally by position. Markers with equal
	 * positions keep their relative order from the input list (stable sort).
	 * @return The resulting string, with all markers inserted at their
	 * respective positions
	 */
	protected static String insertSpansInOnePass(/*@ non_null @*/ String content, /*@ non_null @*/ List<SpanMarker> markers)
	{
		List<SpanMarker> sorted = new ArrayList<SpanMarker>(markers);
		sorted.sort((a, b) -> Integer.compare(a.position, b.position));
		int len = content.length();
		StringBuilder sb = new StringBuilder(len + sorted.size() * 40);
		int cursor = 0;
		for (SpanMarker m : sorted)
		{
			int pos = Math.max(0, Math.min(m.position, len));
			if (pos > cursor)
			{
				sb.append(content, cursor, pos);
				cursor = pos;
			}
			sb.append(m.text);
		}
		if (cursor < len)
		{
			sb.append(content, cursor, len);
		}
		return sb.toString();
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
		s = s.replaceAll("(?m)([^\\\\])(%.*?)$", "$1<span class=\"comment\">$2</span>");
		return s;
	}

	/**
	 * PATCH : réécriture en une seule passe. L'ancienne version relançait une
	 * recherche sur l'intégralité du texte pour chaque ligne indentée trouvée
	 * (une ligne traitée par itération), ce qui est en O(nombre de lignes
	 * indentées x taille du document). Ici, on ne parcourt le texte qu'une
	 * seule fois, ligne par ligne.
	 */
	protected static String indent(String s)
	{
		String[] lines = s.split("\n", -1);
		StringBuilder sb = new StringBuilder(s.length() + lines.length * 8);
		for (int li = 0; li < lines.length; li++)
		{
			String line = lines[li];
			int leading = 0;
			while (leading < line.length() && line.charAt(leading) == ' ')
			{
				leading++;
			}
			if (leading > 0)
			{
				for (int j = 0; j < leading; j++)
				{
					sb.append("&nbsp;");
				}
				sb.append(line, leading, line.length());
			}
			else
			{
				sb.append(line);
			}
			if (li < lines.length - 1)
			{
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	protected static String printLineNumber(int n, int width)
	{
		String number = String.format("%" + width + "d", n);
		number = number.replaceAll(" ", "&nbsp;");
		return number;
	}

}
