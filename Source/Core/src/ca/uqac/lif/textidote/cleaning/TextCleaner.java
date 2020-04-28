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
package ca.uqac.lif.textidote.cleaning;

import java.util.List;

import ca.uqac.lif.textidote.as.AnnotatedString;

/**
 * Removes markup from a text file. A text cleaner can perform two kinds
 * of "cleanup" on a text file:
 * <ul>
 * <li>A full cleanup that removes all markup. Portions of the file that do
 * not contain meaningful text (for example, LaTeX equations) may also be
 * removed. As a rule, do not consider the output string as a completely
 * faithful clear-text rendition of the original document.</li>
 * <li>A cleanup that only removes blocks of text identified as comments.
 * What a "comment" means depends on the markup language, and some languages
 * may not have comments at all.</li>
 * </ul> 
 * @author Sylvain Hallé
 */
public abstract class TextCleaner 
{
	/**
	 * Removes markup from a string.
	 * @param s The original string. Note that this string can be modified
	 * by the method.
	 * @return The new string. Whether this object is a copy of {@code s}
	 * or {@code s} itself is left undefined.
	 * @throws TextCleanerException If a problem occurs when cleaning
	 */
	/*@ non_null @*/ public abstract AnnotatedString clean(/*@ non_null @*/ AnnotatedString s) throws TextCleanerException;
	
	/**
	 * Removes portions of the string identified as comments, but keeps all
	 * other markup.
	 * @param s The original string. Note that this string can be modified
	 * by the method.
	 * @return The new string. Whether this object is a copy of {@code s}
	 * or {@code s} itself is left undefined.
	 * @throws TextCleanerException If a problem occurs when cleaning
	 */
	/*@ non_null @*/ public abstract AnnotatedString cleanComments(/*@ non_null @*/ AnnotatedString s) throws TextCleanerException;
	
	/**
	 * Returns the list of inner files included in the file to be cleaned.
	 * Currently, this only has a meaning for cleaners based on LaTeX,
	 * which has <tt>include</tt> and <tt>input</tt> instructions.
	 * This result will be non-empty only after
	 * {@link #clean(AnnotatedString) clean()} has been called.
	 * @return The list of filenames
	 */
	/*@ non_null @*/ public abstract List<String> getInnerFiles();
}
