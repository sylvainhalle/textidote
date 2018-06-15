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

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;

/**
 * Removes LaTeX markup from an input string, and generates an annotated
 * string in return.
 * @author Sylvain Hallé
 *
 */
public class Detexer
{
	/**
	 * Removes LaTeX markup from the annotated string
	 * @param as The annotated string
	 * @return A string without markup
	 */
	public AnnotatedString detex(AnnotatedString as)
	{
		AnnotatedString new_as = new AnnotatedString(as);
		new_as = removeComments(new_as);
		as = removeEnvironments(new_as);
		new_as = removeAllMarkup(new_as);
		//new_as = simplifySpaces(new_as);
		return new_as;
	}

	/**
	 * Remove environments that are not likely to be interpreted as text
	 * (tables, verbatim, equations, figures)
	 * @param as The string to clean
	 * @return 
	 */
	protected AnnotatedString removeEnvironments(AnnotatedString as)
	{
		int in_environment = 0;
		for (int i = 0; i < as.lineCount(); i++)
		{
			String line = as.getLine(i);
			if (line.matches(".*\\\\begin\\s*\\{\\s*(equation|table|tabular|verbatim|lstlisting|IEEEkeywords|figure).*") || line.matches(".*\\\\\\[.*"))
			{
				in_environment++;
			}
			if (in_environment > 0)
			{
				as.removeLine(i);
				i--; // Step counter back so next loop is at same index
			}
			if (line.matches(".*\\\\end\\s*\\{\\s*(equation|table|tabular|verbatim|lstlisting|IEEEkeywords|figure).*") || line.matches(".*\\\\\\].*"))
			{
				in_environment--;
			}
		}
		return as;
	}

	/**
	 * Remove comments from the file
	 * @param as The string to clean
	 * @return The string without comments
	 */
	protected AnnotatedString removeComments(AnnotatedString as)
	{
		for (int i = 0; i < as.lineCount(); i++)
		{
			String line = as.getLine(i);
			if (line.trim().startsWith("%"))
			{
				as.removeLine(i);
				i--; // Step counter back so next loop is at same index
			}
			else
			{
				for (int pos = 0; pos < line.length(); pos++)
				{
					pos = line.indexOf("%", pos);
					if (pos < 0)
					{
						// No more % in the line
						break;
					}
					if (line.substring(pos - 1, pos).compareTo("\\") != 0)
					{
						as = as.trimFrom(new Position(i, pos));
						break;
					}
				}
			}
		}
		return as;
	}

	/**
	 * 
	 * @param as
	 * @return
	 */
	protected AnnotatedString removeAllMarkup(AnnotatedString as)
	{
		AnnotatedString as_out = new AnnotatedString();
		boolean first_line = true;
		for (int line_pos = 0; line_pos < as.lineCount(); line_pos++)
		{
			String orig_source_line = as.getLine(line_pos);
			if (orig_source_line.trim().isEmpty())
			{
				continue;
			}
			AnnotatedString source_line = as.substring(new Position(line_pos, 0), new Position(line_pos, orig_source_line.length()));
			AnnotatedString clean_line = removeMarkup(source_line, line_pos);
			if (clean_line.toString().trim().isEmpty())
			{
				// Ignore completely blank lines
				continue;
			}
			if (first_line)
			{
				first_line = false;
			}
			else
			{
				as_out.appendNewLine();
			}
			as_out.append(clean_line);
		}
		return as_out;
	}

	protected AnnotatedString removeMarkup(AnnotatedString as_out, int line_pos)
	{
		// Common environments
		as_out = as_out.replaceAll("\\\\(begin|end)\\{(itemize|enumerate|document|thm|abstract|eqnarray|compactitem|query)\\}", "");
		// List items
		as_out = as_out.replaceAll("\\\\item\\s*", "");
		// Images
		as_out = as_out.replaceAll("\\\\includegraphics.*$", "");
		// Commands that don't produce text
		as_out = as_out.replaceAll("\\\\(label)\\{.*?\\}", "");
		// Footnotes (ignore)
		as_out = as_out.replaceAll("\\\\footnote\\{.*?\\}", "");
		// Replace citations by dummy placeholder
		as_out = as_out.replaceAll("\\\\(cite|citep|citel)\\{.*?\\}", "[0]");
		// Replace verbatim by dummy placeholder
		as_out = as_out.replaceAll("\\\\verb\\+[^\\+]*?\\+", "[0]");
		// Replace references and URLs by dummy placeholder
		as_out = as_out.replaceAll("\\\\(ref|url)\\{.*?\\}", "X");
		// Titles
		as_out = as_out.replaceAll("\\\\maketitle", "");
		// Inputs and includes
		as_out = as_out.replaceAll("\\\\(input|include|documentclass|usepackage|noindent|vskip|rule).*$", "");
		// Inline equations
		as_out = as_out.replaceAll("([^\\\\])\\$.*?[^\\\\]\\$", "$1X");
		as_out = as_out.replaceAll("^\\$.*?[^\\\\]\\$", "X");
		// Commands we can ignore
		as_out = as_out.replaceAll("\\\\\\w+\\{", "");
		//as_out = as_out.replaceAll("\\\\(title|textbf|textit|emph|uline|section|subsection|subsubsection|paragraph)", "");
		// Curly brackets
		as_out = as_out.replaceAll("\\{|\\}", "");
		return as_out;
	}

	protected AnnotatedString simplifySpaces(AnnotatedString s)
	{
		s = s.replaceAll("\\t", " ");
		s = s.replaceAll("[ ]+", " ");
		return s;
	}
}
