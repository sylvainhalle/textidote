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
package ca.uqac.lif.textidote.cleaning.latex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Match;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.cleaning.TextCleaner;
import ca.uqac.lif.textidote.cleaning.TextCleanerException;

/**
 * Removes LaTeX markup from an input string, and generates an annotated
 * string in return.
 * @author Sylvain Hallé
 *
 */
public class LatexCleaner extends TextCleaner
{
	/**
	 * The string to look for to tell TeXtidote to start ignoring lines
	 */
	public static final String IGNORE_BEGIN = "textidote: ignore begin";

	/**
	 * The string to look for to tell TeXtidote to stop ignoring lines
	 */
	public static final String IGNORE_END = "textidote: ignore end";

	/**
	 * Whether the detexer will remove all lines before seeing
	 * <tt>\begin{document}</tt>
	 */
	protected boolean m_ignoreBeforeDocument = true;
	
	/**
	 * A set of additional environment names to remove when cleaning up
	 */
	/*@ non_null @*/ protected final Set<String> m_environmentsToIgnore = new HashSet<String>();
	
	/**
	 * A set of additional macro names to remove when cleaning up
	 */
	/*@ non_null @*/ protected final Set<String> m_macrosToIgnore = new HashSet<String>();
	
	/**
	 * A list of <em>non-commented</em> <tt>input</tt> and <tt>include</tt>
	 * declarations found in the file to be cleaned.
	 */
	protected final List<String> m_innerFiles = new ArrayList<String>();

	/**
	 * A regex pattern matching the <tt>input</tt> and <tt>include</tt>
	 * declarations in a line of markup.
	 */
	protected static final transient Pattern m_includePattern = Pattern.compile("^.*\\\\(input|include)\\s*\\{(.*?)\\}.*$");
	
	/**
	 * Adds a new environment name to remove when cleaning up
	 * @param e_name The name of the environment
	 * @return This cleaner
	 */
	public LatexCleaner ignoreEnvironment(/*@ non_null @*/ String e_name)
	{
		m_environmentsToIgnore.add(e_name);
		return this;
	}
	
	/**
	 * Adds new environment names to remove when cleaning up
	 * @param e_names A collection of environment names
	 * @return This cleaner
	 */
	public LatexCleaner ignoreEnvironments(/*@ non_null @*/ Collection<String> e_names)
	{
		m_environmentsToIgnore.addAll(e_names);
		return this;
	}
	
	/**
	 * Adds a new macro name to remove when cleaning up
	 * @param m_name The name of the macro
	 * @return This cleaner
	 */
	public LatexCleaner ignoreMacro(/*@ non_null @*/ String m_name)
	{
		m_macrosToIgnore.add(m_name);
		return this;
	}
	
	/**
	 * Adds new macro names to remove when cleaning up
	 * @param m_names A collection of macro names
	 * @return This cleaner
	 */
	public LatexCleaner ignoreMacros(/*@ non_null @*/ Collection<String> m_names)
	{
		m_macrosToIgnore.addAll(m_names);
		return this;
	}

	@Override
	/*@ non_null @*/ public AnnotatedString clean(/*@ non_null @*/ AnnotatedString as) throws TextCleanerException
	{
		// Reset list of inner files every time we clean
		m_innerFiles.clear();
		AnnotatedString new_as = new AnnotatedString(as);
		new_as = cleanComments(new_as);
		new_as = removeEnvironments(new_as);
		new_as = removeMacros(new_as);
		fetchIncludes(new_as);
		new_as = removeAllMarkup(new_as);
		//new_as = simplifySpaces(new_as);
		return new_as;
	}
	
	/**
	 * Remove user-defined macros that should not be interpreted as text
	 * @param as The string to clean
	 * @return A string with the macros removed
	 */
	protected AnnotatedString removeMacros(AnnotatedString as)
	{
		AnnotatedString new_as = new AnnotatedString(as);
		for (String macro : m_macrosToIgnore)
		{
			new_as = new_as.replaceAll("\\\\" + macro + "\\s", "");
			new_as = new_as.replaceAll("\\\\" + macro + "\\{.*?\\}\\s", "");
			new_as = new_as.replaceAll("\\\\" + macro + "\\{.*?\\}(\\b)", "$1");
			new_as = new_as.replaceAll("\\\\" + macro + "\\[.*?\\]\\{.*?\\}\\s", "");
			new_as = new_as.replaceAll("\\\\" + macro + "\\[.*?\\]\\{.*?\\}(\\b)", "$1");
			new_as = new_as.replaceAll("\\\\" + macro + "(\\b)", "$1");
		}
		return new_as;
	}

	/**
	 * Remove environments that are not likely to be interpreted as text
	 * (tables, verbatim, equations, figures)
	 * @param as The string to clean
	 * @return A string with the environments removed
	 */
	protected AnnotatedString removeEnvironments(AnnotatedString as)
	{
		int in_environment = 0;
		boolean in_document = false;
		for (int i = 0; i < as.lineCount() && i >= 0; i++)
		{
			// If we keep removing line 0, eventually we'll come to an empty string
			if (as.isEmpty())
			{
				break;
			}
			String line = as.getLine(i);
			if (m_ignoreBeforeDocument && !in_document)
			{
				if (line.matches("[^%]*\\\\begin\\s*\\{\\s*document.*"))
				{
					// We have seen the beginning of the document
					in_document = true;
				}
				// All the lines up to the one that has \begin{document} are removed
				as.removeLine(i);
				i--; // Step counter back so next loop is at same index
			}
			else
			{
				if (isEnvironmentStart(line))
				{
					in_environment++;
				}
				if (in_environment > 0)
				{
					as.removeLine(i);
					i--; // Step counter back so next loop is at same index
				}
				if (isEnvironmentEnd(line))
				{
					in_environment--;
				}
			}
		}
		return as;
	}
	
	/**
	 * Determines if the current line contains the start of an environment
	 * to remove from the markup
	 * @param line The text line
	 * @return {@code true} if the line contains the start of an environment,
	 * {@code false} otherwise
	 */
	protected boolean isEnvironmentStart(/*@ non_null @*/ String line)
	{
		if (line.matches(".*\\\\begin\\s*\\{\\s*(align|equation|table|tabular|verbatim|lstlisting|IEEEkeywords|figure|wrapfigure).*") || line.matches(".*\\\\\\[[^\\]]*"))
		{
			return true;
		}
		for (String e_name : m_environmentsToIgnore)
		{
			// Also loop through user-specified environments
			if (line.matches(".*\\\\begin\\s*\\{\\s*" + e_name + "\\s*\\}.*"))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines if the current line contains the end of an environment
	 * to remove from the markup
	 * @param line The text line
	 * @return {@code true} if the line contains the end of an environment,
	 * {@code false} otherwise
	 */
	protected boolean isEnvironmentEnd(/*@ non_null @*/ String line)
	{
		if (line.matches(".*\\\\end\\s*\\{\\s*(align|equation|table|tabular|verbatim|lstlisting|IEEEkeywords|figure|wrapfigure).*") || line.matches(".*\\\\\\].*"))
		{
			return true;
		}
		for (String e_name : m_environmentsToIgnore)
		{
			// Also loop through user-specified environments
			if (line.matches(".*\\\\end\\s*\\{\\s*" + e_name + "\\s*\\}.*"))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public AnnotatedString cleanComments(AnnotatedString as)
	{
		boolean in_comment = false;
		for (int i = 0; i < as.lineCount(); i++)
		{
			String line = as.getLine(i);
			if (line.matches(".*\\\\begin\\s*\\{\\s*comment.*") || line.matches("\\s*%+.*" + IGNORE_BEGIN + ".*"))
			{
				in_comment = true;
			}
			if (in_comment || line.trim().startsWith("%"))
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
			if (in_comment && line.matches(".*\\\\end\\s*\\{\\s*comment.*") || line.matches("\\s*%+.*" + IGNORE_END + ".*"))
			{
				in_comment = false;
			}
		}
		return as;
	}

	/**
	 * Removes LaTeX commands from a string 
	 * @param as The string to clean
	 * @return A string with the environments removed
	 */
	protected AnnotatedString removeAllMarkup(AnnotatedString as)
	{
		AnnotatedString as_out = new AnnotatedString();
		boolean first_line = true;
		for (int line_pos = 0; line_pos < as.lineCount(); line_pos++)
		{
			String orig_source_line = as.getLine(line_pos);
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

	protected AnnotatedString removeMarkup(AnnotatedString as_out, int line_pos)
	{
		// French quotes
		as_out = replaceAccents(as_out, line_pos);
		as_out = as_out.replaceAll("\\\\og\\{\\}", "«");
		as_out = as_out.replaceAll("\\\\fg\\{\\}", "»");
		// Ligatures
		as_out = as_out.replaceAll("\\\\oe\\{\\}", "œ");
		as_out = as_out.replaceAll("\\\\ae\\{\\}", "æ");
		// Escaped braces
		as_out = as_out.replaceAll("\\\\\\{", "{");
		as_out = as_out.replaceAll("\\\\\\}", "}");
		// Line breaks and paragraphs
		as_out = as_out.replaceAll("\\\\\\\\", "");
		as_out = as_out.replaceAll("\\\\par(\\b)", "$1");
		// Common environments
		as_out = as_out.replaceAll("\\\\(begin|end)\\{(itemize|enumerate|inparaenum|document|thm|abstract|eqnarray|compactitem|query|center|minipage|quote|frame)\\}", "");
		// List items
		as_out = as_out.replaceAll("\\\\item\\s*", "");
		// Images
		as_out = as_out.replaceAll("\\\\includegraphics.*$", "");
		// Commands that don't produce text
		as_out = as_out.replaceAll("\\\\(label)\\{[^\\}]*?\\}", "");
		// Footnotes (ignore)
		as_out = as_out.replaceAll("\\\\footnote\\{.*?\\}", "");
		// Replace citations by dummy placeholder
		as_out = as_out.replaceAll("\\\\(cite|citep|citel)(\\[.*?\\])*\\{.*?\\}", "[0]");
		// Replace verbatim by dummy placeholder
		as_out = as_out.replaceAll("\\\\verb\\+[^\\+]*?\\+", "[0]");
		as_out = as_out.replaceAll("\\\\verb\"[^\"]*?\"", "[0]");
		// Replace references and URLs by dummy placeholder
		as_out = as_out.replaceAll("\\\\(ref|url|eqref|cref|Cref)\\{.*?\\}", "X");
		// Titles
		as_out = as_out.replaceAll("\\\\maketitle|\\\\newpage", "");
		// Font commands
		as_out = as_out.replaceAll("\\\\(tiny|scriptsize|footnotesize|small|normalsize|large|Large|LARGE|huge|Huge)", "");
		// Inputs and includes
		as_out = as_out.replaceAll("\\\\(input|include|documentclass|usepackage|noindent|vskip|vspace|vskip|hspace|rule|urlstyle|fancyfoot|fancyhead|pagestyle|thispagestyle|newcommand|renewcommand|bibliographystyle|bibliography|scalebox|printbibliography).*$", "");
		// Conditional hyphens
		as_out = as_out.replaceAll("\\\\\\-", "");
		// Non-breaking spaces
		as_out = as_out.replaceAll("~", " ");
		// Dots
		as_out = as_out.replaceAll("\\\\(dots|cdots|ldots)", "...");
		// Inline display math with only digits and letters
		as_out = as_out.replaceAll("\\\\\\(([A-Za-z0-9,\\.]*?)\\\\\\)", "$1");
		// Otherwise, replace by X
		as_out = as_out.replaceAll("\\\\\\(.*?\\\\\\)", "X");
		// Equations are removed
		as_out = as_out.replaceAll("\\\\\\[.*?\\\\\\]", "");
		// Inline equations in old TeX style ("$foo$")
		as_out = replaceInlineEquations(as_out, line_pos);
		/*as_out = as_out.replaceAll("([^\\\\])\\$.*?[^\\\\]\\$", "$1X");
		//as_out = as_out.replaceAll("^\\$.*?[^\\\\]\\$", "X");
		as_out = as_out.replaceAll("^\\$([^\\$]|\\.)*\\$", "X");
		as_out = as_out.replaceAll("\\\\\\(.*?\\\\\\)", "X");*/
		// Commands we can ignore
		as_out = as_out.replaceAll("\\\\\\w+\\{", "");
		//as_out = as_out.replaceAll("\\\\(title|textbf|textit|emph|uline|section|subsection|subsubsection|paragraph)", "");
		// Curly brackets
		as_out = as_out.replaceAll("\\{|\\}", "");
		return as_out;
	}
	
	protected AnnotatedString replaceInlineEquations(AnnotatedString as_out, int line_pos)
	{
		Match m = null;
		Position p = Position.ZERO;
		do
		{
			m = as_out.find("[^\\\\]\\$.*?[^\\\\]\\$", p);
			if (m == null)
			{
				break;
			}
			p = m.getPosition();
			String s_from = m.getMatch();
			String s_to = s_from.substring(0, 1) + "X";
			String s_inside = s_from.substring(2, s_from.length() - 1);
			if (s_inside.matches("[\\dA-Za-z\\.,]+"))
			{
				s_to = s_from.substring(0, 1) + s_inside;
			}
			as_out = as_out.replaceAll(Pattern.quote(s_from), s_to);
			p = p.moveBy(1); // To ensure progress
		} while (m != null);
		// Do it one last time for equations at the beginning of a line		
		m = as_out.find("^\\$.*?[^\\\\]\\$", p);
		if (m != null)
		{
			p = m.getPosition();
			String s_from = m.getMatch();
			String s_to = "X";
			String s_inside = s_from.substring(1, s_from.length() - 1);
			if (s_inside.matches("[\\dA-Za-z\\.,]+"))
			{
				s_to = s_inside;
			}
			as_out = as_out.replaceAll(Pattern.quote(s_from), s_to);
		}
		return as_out;
	}
	
	/**
	 * Replaces escaped accented character sequences by their proper character
	 * @param as_out The string to replace from
	 * @param line_pos The line number of the line to process
	 * @return The replaced string
	 */
	protected AnnotatedString replaceAccents(AnnotatedString as_out, int line_pos)
	{
		// With braces
		as_out = as_out.replaceAll("\\\\`\\{A\\}", "À");
		as_out = as_out.replaceAll("\\\\'\\{A\\}", "Á");
		as_out = as_out.replaceAll("\\\\^\\{A\\}", "Â");
		as_out = as_out.replaceAll("\\\\~\\{A\\}", "Ã");
		as_out = as_out.replaceAll("\\\\`\\{a\\}", "à");
		as_out = as_out.replaceAll("\\\\'\\{a\\}", "à");
		as_out = as_out.replaceAll("\\\\^\\{a\\}", "â");
		as_out = as_out.replaceAll("\\\\~\\{a\\}", "ã");
		as_out = as_out.replaceAll("\\\\`\\{E\\}", "È");
		as_out = as_out.replaceAll("\\\\'\\{E\\}", "É");
		as_out = as_out.replaceAll("\\\\^\\{E\\}", "Ê");
		as_out = as_out.replaceAll("\\\\~\\{E\\}", "Ẽ");
		as_out = as_out.replaceAll("\\\\`\\{e\\}", "è");
		as_out = as_out.replaceAll("\\\\'\\{e\\}", "é");
		as_out = as_out.replaceAll("\\\\^\\{e\\}", "ê");
		as_out = as_out.replaceAll("\\\\~\\{e\\}", "ẽ");
		as_out = as_out.replaceAll("\\\\`\\{I\\}", "Ì");
		as_out = as_out.replaceAll("\\\\'\\{I\\}", "Í");
		as_out = as_out.replaceAll("\\\\^\\{I\\}", "Î");
		as_out = as_out.replaceAll("\\\\~\\{I\\}", "Ĩ");
		as_out = as_out.replaceAll("\\\\`\\{i\\}", "ì");
		as_out = as_out.replaceAll("\\\\'\\{i\\}", "í");
		as_out = as_out.replaceAll("\\\\^\\{i\\}", "î");
		as_out = as_out.replaceAll("\\\\~\\{i\\}", "ĩ");
		as_out = as_out.replaceAll("\\\\`\\{O\\}", "Ò");
		as_out = as_out.replaceAll("\\\\'\\{O\\}", "Ó");
		as_out = as_out.replaceAll("\\\\^\\{O\\}", "ô");
		as_out = as_out.replaceAll("\\\\~\\{O\\}", "Õ");
		as_out = as_out.replaceAll("\\\\`\\{o\\}", "ò");
		as_out = as_out.replaceAll("\\\\'\\{o\\}", "ó");
		as_out = as_out.replaceAll("\\\\^\\{o\\}", "ô");
		as_out = as_out.replaceAll("\\\\~\\{o\\}", "õ");
		as_out = as_out.replaceAll("\\\\`\\{U\\}", "Ù");
		as_out = as_out.replaceAll("\\\\'\\{U\\}", "Ú");
		as_out = as_out.replaceAll("\\\\^\\{U\\}", "Û");
		as_out = as_out.replaceAll("\\\\~\\{U\\}", "Ũ");
		as_out = as_out.replaceAll("\\\\`\\{u\\}", "ù");
		as_out = as_out.replaceAll("\\\\'\\{u\\}", "ú");
		as_out = as_out.replaceAll("\\\\^\\{u\\}", "û");
		as_out = as_out.replaceAll("\\\\~\\{u\\}", "ũ");
		
		// Without braces
		as_out = as_out.replaceAll("\\\\`A", "À");
		as_out = as_out.replaceAll("\\\\'A", "Á");
		as_out = as_out.replaceAll("\\\\^A", "Â");
		as_out = as_out.replaceAll("\\\\~A", "Ã");
		as_out = as_out.replaceAll("\\\\`a", "à");
		as_out = as_out.replaceAll("\\\\'a", "à");
		as_out = as_out.replaceAll("\\\\^a", "â");
		as_out = as_out.replaceAll("\\\\~a", "ã");
		as_out = as_out.replaceAll("\\\\`E", "È");
		as_out = as_out.replaceAll("\\\\'E", "É");
		as_out = as_out.replaceAll("\\\\^E", "Ê");
		as_out = as_out.replaceAll("\\\\~E", "Ẽ");
		as_out = as_out.replaceAll("\\\\`e", "è");
		as_out = as_out.replaceAll("\\\\'e", "é");
		as_out = as_out.replaceAll("\\\\^e", "ê");
		as_out = as_out.replaceAll("\\\\~e", "ẽ");
		as_out = as_out.replaceAll("\\\\`I", "Ì");
		as_out = as_out.replaceAll("\\\\'I", "Í");
		as_out = as_out.replaceAll("\\\\^I", "Î");
		as_out = as_out.replaceAll("\\\\~I", "Ĩ");
		as_out = as_out.replaceAll("\\\\`i", "ì");
		as_out = as_out.replaceAll("\\\\'i", "í");
		as_out = as_out.replaceAll("\\\\^i", "î");
		as_out = as_out.replaceAll("\\\\~i", "ĩ");
		as_out = as_out.replaceAll("\\\\`O", "Ò");
		as_out = as_out.replaceAll("\\\\'O", "Ó");
		as_out = as_out.replaceAll("\\\\^O", "ô");
		as_out = as_out.replaceAll("\\\\~O", "Õ");
		as_out = as_out.replaceAll("\\\\`o", "ò");
		as_out = as_out.replaceAll("\\\\'o", "ó");
		as_out = as_out.replaceAll("\\\\^o", "ô");
		as_out = as_out.replaceAll("\\\\~o", "õ");
		as_out = as_out.replaceAll("\\\\`U", "Ù");
		as_out = as_out.replaceAll("\\\\'U", "Ú");
		as_out = as_out.replaceAll("\\\\^U", "Û");
		as_out = as_out.replaceAll("\\\\~U", "Ũ");
		as_out = as_out.replaceAll("\\\\`u", "ù");
		as_out = as_out.replaceAll("\\\\'u", "ú");
		as_out = as_out.replaceAll("\\\\^u", "û");
		as_out = as_out.replaceAll("\\\\~u", "ũ");
		return as_out;
	}

	protected AnnotatedString simplifySpaces(AnnotatedString s)
	{
		s = s.replaceAll("\\t", " ");
		s = s.replaceAll("[ ]+", " ");
		return s;
	}

	/**
	 * Sets whether the detexer will remove all lines before seeing
	 * <tt>\begin{document}</tt>, without even attempting to interpret
	 * them
	 * @param b Set to {@code true} to remove the lines (the default),
	 * {@code false} otherwise
	 * @return This detexer
	 */
	public LatexCleaner setIgnoreBeforeDocument(boolean b)
	{
		m_ignoreBeforeDocument = b;
		return this;
	}
	
	/**
	 * Populates a list of <em>non-commented</em> <tt>input</tt> and
	 * <tt>include</tt> declarations found in the file to be cleaned.
	 * @param as The contents of the file (where environments and
	 * comments have already been removed).
	 */
	protected void fetchIncludes(/*@ non_null @*/ AnnotatedString as)
	{
		for (String line : as.getLines())
		{
			Matcher mat = m_includePattern.matcher(line);
			if (mat.find())
			{
				String filename = mat.group(2).trim();
				if (!filename.endsWith(".tex"))
				{
					filename += ".tex";
				}
				m_innerFiles.add(filename);
			}
		}
	}
	
	/**
	 * Returns the list of <em>non-commented</em> <tt>input</tt> and
	 * <tt>include</tt> declarations found in the file to be cleaned.
	 * This result will be non-empty only after
	 * {@link #clean(AnnotatedString) clean()} has been called.
	 * @return The list of filenames
	 */
	@Override
	/*@ pure non_null @*/ public List<String> getInnerFiles()
	{
		return m_innerFiles;
	}
}