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
package ca.uqac.lif.textidote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.util.AnsiPrinter;

/**
 * Renders a list of advice in a special format
 * @author Sylvain Hallé
 */
public abstract class AdviceRenderer 
{
	/**
	 * The printer where the renderer will print its results
	 */
	/*@ non_null @*/ protected AnsiPrinter m_printer;
	
	/**
	 * A map between filenames and the list of advice computed for that
	 * file
	 */
	/*@ non_null @*/ protected Map<String,List<Advice>> m_advice;
	
	/**
	 * A map associating each filename to its (annotated) contents
	 */
	/*@ non_null @*/ protected Map<String,AnnotatedString> m_originalStrings;
	
	/**
	 * Creates a new advice renderer
	 * @param printer The printer where the renderer will print its
	 * results
	 */
	public AdviceRenderer(/*@ non_null @*/ AnsiPrinter printer)
	{
		super();
		m_printer = printer;
		m_advice = new HashMap<String,List<Advice>>();
		m_originalStrings = new HashMap<String,AnnotatedString>();
	}
	
	/**
	 * Associate a list of advice to a filename
	 * @param filename The filename
	 * @param contents The contents of the corresponding file
	 * @param advice The list of advice
	 */
	public void addAdvice(/*@ non_null @*/ String filename, /*@ non_null @*/ AnnotatedString contents, /*@ non_null @*/ List<Advice> advice)
	{
		m_advice.put(filename, advice);
		m_originalStrings.put(filename, contents);
	}
	
	/**
	 * Renders the list of advice for each of the files given to the
	 * renderer, and prints them to the {@link AnsiPrinter} associated to
	 * it when it was instantiated.
	 */
	public abstract void render();
}
