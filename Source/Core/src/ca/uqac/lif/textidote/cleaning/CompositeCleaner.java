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

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.textidote.as.AnnotatedString;

/**
 * Text cleaner that calls multiple other cleaners in succession.
 * @author Sylvain Hallé
 */
public class CompositeCleaner extends TextCleaner
{
	/**
	 * The list of cleaners to call
	 */
	protected List<TextCleaner> m_cleaners;
	
	/**
	 * Creates a new composite text cleaner from a list of
	 * other cleaners.
	 * @param cleaners The list of cleaner to give to this composite cleaner
	 */
	public CompositeCleaner(TextCleaner ... cleaners)
	{
		super();
		m_cleaners = new ArrayList<TextCleaner>(cleaners.length);
		for (TextCleaner tc : cleaners)
		{
			m_cleaners.add(tc);
		}
	}
	
	/**
	 * Creates a new copy of a composite cleaner
	 * @param c The cleaner to copy
	 */
	public CompositeCleaner(/*@ non_null @*/ CompositeCleaner c)
	{
		super();
		m_cleaners = new ArrayList<TextCleaner>(c.m_cleaners.size());
		m_cleaners.addAll(c.m_cleaners);
	}
	
	@Override
	/*@ pure non_null @*/ public AnnotatedString clean(/*@ non_null @*/ AnnotatedString s) throws TextCleanerException
	{
		for (TextCleaner tc : m_cleaners)
		{
			s = tc.clean(s);
		}
		return s;
	}
	
	@Override
	/*@ pure non_null @*/ public AnnotatedString cleanComments(/*@ non_null @*/ AnnotatedString s) throws TextCleanerException
	{
		for (TextCleaner tc : m_cleaners)
		{
			s = tc.cleanComments(s);
		}
		return s;
	}
	
	/**
	 * Adds a new cleaner to the list of cleaners
	 * @param tc The cleaner to add
	 * @return This composite cleaner
	 */
	/*@ non_null @*/ public CompositeCleaner add(/*@ non_null @*/ TextCleaner tc)
	{
		m_cleaners.add(tc);
		return this;
	}

	@Override
	/*@ pure non_null @*/ public List<String> getInnerFiles()
	{
		ArrayList<String> files = new ArrayList<String>();
		for (TextCleaner tc : m_cleaners)
		{
			files.addAll(tc.getInnerFiles());
		}
		return files;
	}
}
