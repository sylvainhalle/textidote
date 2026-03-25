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
package ca.uqac.lif.textidote.cleaning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

import ca.uqac.lif.textidote.as.AnnotatedString;

/**
 * Text cleaner that applies a series of find/replace operations on a
 * string.
 * @author Sylvain Hallé
 */
public class ReplacementCleaner extends TextCleaner 
{
	/**
	 * The map of find/replace patterns
	 */
	protected Map<String,String> m_replacements;

	/**
	 * Creates a new replacement cleaner with a map of replacements
	 * @param replacements The map of replacements. Keys in the map
	 * represent the patterns to find, and values represent what they should
	 * be replaced with.
	 */
	public ReplacementCleaner(/*@ non_null @*/ Map<String,String> replacements)
	{
		super();
		m_replacements = replacements;
	}

	/**
	 * Creates a new replacement cleaner.
	 */
	public ReplacementCleaner()
	{
		this(new HashMap<String,String>());
	}

	@Override
	public AnnotatedString clean(/*@ non_null @*/ AnnotatedString s) throws TextCleanerException 
	{
		for (Map.Entry<String,String> entry : m_replacements.entrySet())
		{
			try
			{
				s = s.replaceAll(entry.getKey(), entry.getValue());
			}
			catch (PatternSyntaxException pse)
			{
				throw new TextCleanerException(pse);
			}
		}
		return s;
	}

	@Override
	public AnnotatedString cleanComments(AnnotatedString s) 
	{
		// We do nothing
		return s;
	}

	/**
	 * Creates a new replacement cleaner from a list of find/replace patterns
	 * taken from a text source. The format for the text source is as follows:
	 * <ul>
	 * <li>Lines that begin with "#" or are made only of whitespace
	 * are ignored</li>
	 * <li>Other lines must contain a find pattern, one or more tabs, and a
	 * replacement pattern</li>
	 * </ul>
	 * @param scanner A scanner open on a text source
	 * @return A new replacement cleaner.
	 */
	public static ReplacementCleaner create(/*@ non_null @*/ Scanner scanner)
	{
		Map<String,String> replacements = new HashMap<String,String>();
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			if (line.trim().startsWith("#") || line.trim().isEmpty())
			{
				// Ignore
				continue;
			}
			String[] parts = line.split("\\t+", 2);
			if (parts.length != 2)
			{
				// Malformed line: ignore
				continue;
			}
			replacements.put(parts[0], parts[1]);
		}
		return new ReplacementCleaner(replacements);
	}

	@Override
	/*@ pure non_null @*/ public List<String> getInnerFiles() 
	{
		return new ArrayList<String>(0);
	}
}
