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
package ca.uqac.lif.textidote.cleaning.markdown;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.cleaning.TextCleaner;
import ca.uqac.lif.textidote.cleaning.TextCleanerException;

public class MarkdownCleaner extends TextCleaner
{
	/**
	 * The string to look for to tell TeXtidote to start ignoring lines
	 */
	public static final String IGNORE_BEGIN = "<!-- textidote: ignore begin -->";

	/**
	 * The string to look for to tell TeXtidote to stop ignoring lines
	 */
	public static final String IGNORE_END = "<!-- textidote: ignore end -->";

	@Override
	/*@ non_null @*/ public AnnotatedString clean(/*@ non_null @*/ AnnotatedString as) throws TextCleanerException
	{
		AnnotatedString new_as = new AnnotatedString(as);
		new_as = cleanComments(new_as);
		as = removeEnvironments(new_as);
		new_as = removeAllMarkup(new_as);
		//new_as = simplifySpaces(new_as);
		return new_as;
	}

	/*@ non_null @*/ public AnnotatedString cleanComments(AnnotatedString in)
	{
		in = in.replaceAll("<!--.*?-->", "");
		return in;
	}

	/**
	 * Remove environments that are not likely to be interpreted as text
	 * (mostly code)
	 * @param as The string to clean
	 * @return A string with the environments removed
	 */
	/*@ non_null @*/ protected AnnotatedString removeEnvironments(AnnotatedString as)
	{
		boolean in_environment = false;
		for (int i = 0; i < as.lineCount() && i >= 0; i++)
		{
			// If we keep removing line 0, eventually we'll come to an empty string
			if (as.isEmpty())
			{
				break;
			}
			String line = as.getLine(i);
			if (line.trim().startsWith("```"))
			{
				in_environment = !in_environment;
			}
			if (in_environment)
			{
				as.removeLine(i);
				i--; // Step counter back so next loop is at same index
			}
		}
		return as;
	}
	
	/**
	 * Removes Markdown specific markup
	 * @param as The string to replace from
	 * @return The replaced string
	 */
	/*@ non_null @*/ protected AnnotatedString removeAllMarkup(AnnotatedString as)
	{
		AnnotatedString as_out = new AnnotatedString();
		boolean first_line = true;
		for (int line_pos = 0; line_pos < as.lineCount(); line_pos++)
		{
			String orig_source_line = as.getLine(line_pos);
			if (orig_source_line.trim().isEmpty())
			{
				as_out.appendNewLine();
				continue;
			}
			AnnotatedString source_line = as.substring(new Position(line_pos, 0), new Position(line_pos, orig_source_line.length()));
			AnnotatedString clean_line = removeMarkup(source_line, line_pos);
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
	
	/*@ non_null @*/ protected AnnotatedString removeMarkup(/*@ non_null @*/ AnnotatedString as_out, int line_nb)
	{
		as_out = as_out.replaceAll("\\*", "");
		as_out = as_out.replaceAll("`.*?`", "X");
		as_out = as_out.replaceAll("!\\[(.*?)\\]\\(.*?\\)", "$1"); // images
		as_out = as_out.replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1"); // links
		as_out = as_out.replaceAll("^\\s*?- ", "• ");
		as_out = as_out.replaceAll("^\\s*#*\\s*", "");
		as_out = as_out.replaceAll("^\\s*=*\\s*$", "");
		return as_out;
	}

	@Override
	/*@ pure non_null @*/ public List<String> getInnerFiles()
	{
		return new ArrayList<String>(0);
	}
}
