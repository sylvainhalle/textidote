package ca.uqac.lif.texlint;

import ca.uqac.lif.texlint.as.AnnotatedString;
import ca.uqac.lif.texlint.as.Range;

/**
 * Removes LaTeX markup from an input string, and generates an annotated
 * string in return.
 * @author sylvain
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
		as = removeAllMarkup(as);
		as = simplifySpaces(as);
		return as;
	}
	
	protected AnnotatedString removeAllMarkup(AnnotatedString as)
	{
		AnnotatedString as_out = new AnnotatedString();
		boolean first_line = true;
		for (int line_pos = 0; line_pos < as.getLines().size(); line_pos++)
		{
			String source_line = as.getLines().get(line_pos);
			if (source_line.trim().isEmpty())
			{
				continue;
			}
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
	
	protected AnnotatedString removeMarkup(String line, int line_pos)
	{
		AnnotatedString as_out = new AnnotatedString();
		as_out.append(line, Range.make(line_pos, 0, line.length() - 1));
		// Common environments
		as_out = as_out.replaceAll("\\\\(begin|end)\\{(itemize|enumerate|document|thm|abstract)\\}", "");
		// List items
		as_out = as_out.replaceAll("\\\\item\\s*", "");
		// Images
		as_out = as_out.replaceAll("\\\\includegraphics.*$", "");
		// Commands that don't produce text
		as_out = as_out.replaceAll("\\\\(label)\\{.*?\\}", "");
		// Replace citations by dummy placeholder
		as_out = as_out.replaceAll("\\\\(cite|citep|citel)\\{.*?\\}", "[0]");
		// Replace references by dummy placeholder
		as_out = as_out.replaceAll("\\\\(ref)\\{.*?\\}", "X");
		// Titles
		as_out = as_out.replaceAll("\\\\maketitle", "");
		// Inputs and includes
		as_out = as_out.replaceAll("\\\\(input|include|documentclass|usepackage).*$", "");
		// Commands we can ignore
		as_out = as_out.replaceAll("\\\\(title|textbf|textit|emph|uline|section|subsection|paragraph)\\{(.*?)\\}", "$2");
		// Comments
		as_out = as_out.replaceAll("%%.*$", "");
		as_out = as_out.replaceAll("([^\\\\%])%.*$", "$1");
		return as_out;
	}
	
	protected AnnotatedString simplifySpaces(AnnotatedString s)
	{
		s = s.replaceAll("\\t", " ");
		s = s.replaceAll("[ ]+", " ");
		return s;
	}
}
