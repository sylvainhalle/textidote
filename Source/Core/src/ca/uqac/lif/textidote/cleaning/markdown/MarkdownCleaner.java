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
package ca.uqac.lif.textidote.cleaning.markdown;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.AnnotatedString.Line;
import ca.uqac.lif.textidote.cleaning.TextCleaner;
import ca.uqac.lif.textidote.cleaning.TextCleanerException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownCleaner extends TextCleaner
{

	@Override
	/*@ non_null @*/ public AnnotatedString clean(/*@ non_null @*/ AnnotatedString as) throws TextCleanerException
	{
		AnnotatedString new_as = new AnnotatedString(as);
		new_as = cleanComments(new_as);
		new_as = removeEnvironments(new_as);
		new_as = removeMarkup(new_as);
		//new_as = simplifySpaces(new_as);
		return new_as;
	}

	private enum CommentStates {SINGLE_LINE, MULTILINE, INLINE, IGNORE, NONE}
	
	/**
	 * Clean regular, inline and multiline comments
	 * Also takes into account Ignore comment sections
	 *
	 * @param as Annotated String to clean
	 *
	 * @return Annotated String without comments
	 */
	@Override
	/*@ non_null @*/ public AnnotatedString cleanComments(AnnotatedString as) 
	{
		String singleLineCommentRegEx = "^<!--(.*?)-->$";
		String markdownFrontMatterRegEx = "^---$";

		String singleInlineCommentRegEx = "<!--(.*?)-->";
		Pattern singleInlineCommentPattern = Pattern.compile(singleInlineCommentRegEx);

		String ignoreStartRegEx = "<!--\\s*" + IGNORE_BEGIN + "\\s*-->";
		Pattern ignoreStartPattern = Pattern.compile(ignoreStartRegEx);
		String ignoreEndRegEx = "<!--\\s*" + IGNORE_END + "\\s*-->";
		Pattern ignoreEndRegExPattern = Pattern.compile(ignoreEndRegEx);

		String beginMultilineComment = "<!--";
		Pattern beginMultilinePattern = Pattern.compile(beginMultilineComment);
		String endMultilineComment = "-->";
		Pattern endMultilinePattern = Pattern.compile(endMultilineComment);

		CommentStates commentState = CommentStates.NONE;
		// Tracks whether we are in a front matter block
		boolean inFrontMatterContent = false;

		for (int i = 0; i < as.lineCount(); i++) 
		{
			Line l = as.getLine(i);
			String line = l.toString();

			Matcher singleInlineCommentMatcher = singleInlineCommentPattern.matcher(line);

			Matcher beginMultilineMatcher = beginMultilinePattern.matcher(line);
			Matcher endMultilineMatcher = endMultilinePattern.matcher(line);

			Matcher ignoreStartMatcher = ignoreStartPattern.matcher(line);
			Matcher ignoreEndRegExMatcher = ignoreEndRegExPattern.matcher(line);

			// Check for end of multiline comment or ignore block
			// For that, the state has to fit and pattern catch
			boolean multilineCommentDone = commentState == CommentStates.MULTILINE && endMultilineMatcher.find();

			// Search and handle comment type
			if (commentState == CommentStates.IGNORE || commentState == CommentStates.MULTILINE && !multilineCommentDone)
			{
				// We're in a multiline comment or ignore block. Clean.
				i = cleanLine(as, i);
			}
			else 
			{
				if (ignoreStartMatcher.find() || line.matches(markdownFrontMatterRegEx)) 
				{
					// This case when either front matter section or an ignore comment is found
					commentState = CommentStates.IGNORE;
					i = cleanLine(as, i);
				} 
				else if (line.matches(singleLineCommentRegEx)) 
				{
					commentState = CommentStates.SINGLE_LINE;
					i = cleanLine(as, i);
				} 
				else if (singleInlineCommentMatcher.find()) 
				{
					commentState = CommentStates.INLINE;
					int pos = line.indexOf("<!--", 0);
					if (pos > 0) 
					{
						// Remove inline comment
						as = as.replace(singleInlineCommentRegEx, "", l.getOffset() + pos);
					}
				}
				else if (beginMultilineMatcher.find()) 
				{
					commentState = CommentStates.MULTILINE;
					int pos = line.indexOf("<!--", 0);
					if (pos >= 0)
					{
						// Remove everything from the beginning of the comment till the end of the line
						as = as.replace("<!--.*", "", l.getOffset() + pos);
					}
				}
			}

			// Ignore done when ignore end comment or the second closing front matter comment reached
			boolean ignoreCommentDone =
					(commentState == CommentStates.IGNORE && ignoreEndRegExMatcher.find()) || (inFrontMatterContent && line.matches(markdownFrontMatterRegEx));
			// If we are in front matter content, search for the front matter end block
			if (line.matches(markdownFrontMatterRegEx)) inFrontMatterContent = true;

			if (multilineCommentDone || ignoreCommentDone) 
			{
				// Replace everything till the end of the multiline comment
				if (commentState == CommentStates.MULTILINE) 
				{
					as = as.replace(".*-->", "", l.getOffset() + 0);
				}
				if (line.matches(markdownFrontMatterRegEx)) 
				{
					inFrontMatterContent = false;
				}
				commentState = CommentStates.NONE;
			}
		}
		return as;
	}

	/**
	 * Cleans a line in an Annotated String
	 *
	 * @param as            Reference to the annotated String to clean
	 * @param lineReference Line reference to clean
	 *
	 * @return Line Reference on the removed line
	 */
	private int cleanLine(AnnotatedString as, int lineReference) 
	{
		as.removeLine(lineReference);
		lineReference--; // Step counter back so next loop is at same index
		return lineReference;
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
			Line l = as.getLine(i);
			String line = l.toString();
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

	/*@ non_null @*/ protected AnnotatedString removeMarkup(/*@ non_null @*/ AnnotatedString as_out)
	{
		as_out = as_out.replaceAll("\\*", "");
		as_out = as_out.replaceAll("`.*?`", "X");
		as_out = as_out.replaceAll("!\\[(.*?)\\]\\(.*?\\)", "$1"); // images
		as_out = as_out.replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1"); // links
		as_out = as_out.replaceAll("(?m)^    .*$", ""); // indented code blocks
		as_out = as_out.replaceAll("(?m)^[ \\t]*?- ", "• ");
		as_out = as_out.replaceAll("(?m)^[ \\t]*#+[ \\t]*", "");
		as_out = as_out.replaceAll("(?m)^[ \\\\t]*=+[ \\t]*$", "");
		return as_out;
	}

	@Override
	/*@ pure non_null @*/ public List<String> getInnerFiles()
	{
		return new ArrayList<String>(0);
	}
}
