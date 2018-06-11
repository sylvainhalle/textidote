package ca.uqac.lif.texlint;

import java.util.Scanner;

/**
 * Removes LaTeX markup from an input string, and generates an annotated
 * string in return.
 * @author sylvain
 *
 */
public class Detexer
{
	public AnnotatedString removeAllMarkup(/*@ non_null @*/ Scanner scanner)
	{
		AnnotatedString as_out = new AnnotatedString();
		while (scanner.hasNextLine())
		{
			String source_line = scanner.nextLine();
			if (source_line.trim().isEmpty())
			{
				as_out.appendNewLine();
				continue;
			}
			AnnotatedString clean_line = removeMarkup(source_line);
			as_out.append(clean_line);
		}
		return as_out;
	}
	
	public AnnotatedString removeMarkup(String line)
	{
		AnnotatedString as_out = new AnnotatedString();
		return as_out;
	}
}
