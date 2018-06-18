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
package ca.uqac.lif.textidote.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.RuleMatch;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.as.Range;

/**
 * Checks the text for spelling, grammar and style errors. This rule is a
 * wrapper around the <a href="https://www.languagetool.org/dev">Language
 * Tool</a> Java library, which does all the work.
 * @author Sylvain Hallé
 */
public class CheckLanguage extends Rule
{
	/**
	 * The language tool object used to check the language
	 */
	/*@ non_null @*/ protected JLanguageTool m_languageTool;

	/**
	 * A set of words that should be ignored by spell checking
	 */
	/*@ non_null @*/ protected Set<String> m_dictionary;
	
	/**
	 * Whether to disable Language Tool's "whitespace rule". Since the detexed
	 * string may contain extra spaces (due to the removal of markup), and
	 * that LaTeX ignores multiple spaces anyway, it is advisable to
	 *  turn it off.
	 */
	protected boolean m_disableWhitespace = true; 

	/**
	 * Creates a new rule for checking a specific language
	 * @param lang The language to check. If {@code null}, the
	 * constructor will throw an exception
	 * @param dictionary A set of words that should be ignored by
	 * spell checking
	 * @throws UnsupportedLanguageException If {@code lang} is null
	 */
	public CheckLanguage(/*@ nullable @*/ Language lang, /*@ non_null @*/ Set<String> dictionary) throws UnsupportedLanguageException
	{
		super("lt:");
		if (lang == null)
		{
			throw new UnsupportedLanguageException();
		}
		setName("lt:" + lang.getShortCode());
		m_languageTool = new MultiThreadedJLanguageTool(lang);
		if (m_disableWhitespace)
		{
			m_languageTool.disableRule("WHITESPACE_RULE");
		}
		m_dictionary = dictionary;
	}

	/**
	 * Creates a new rule for checking a specific language
	 * @param lang The language to check
	 * @throws UnsupportedLanguageException If {@code lang} is null
	 */
	public CheckLanguage(/*@ nullable @*/ Language lang) throws UnsupportedLanguageException
	{
		this(lang, new HashSet<String>());
	}

	@Override
	public List<Advice> evaluate(AnnotatedString s, AnnotatedString original) 
	{
		List<Advice> out_list = new ArrayList<Advice>();
		String s_to_check = s.toString();
		List<RuleMatch> matches = null;
		try 
		{
			matches = m_languageTool.check(s_to_check);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (matches == null)
		{
			return out_list;
		}
		for (RuleMatch rm : matches)
		{
			String line = "";
			Position start_src_pos = null, end_src_pos = null;
			Position start_pos = s.getPosition(rm.getFromPos());
			if (start_pos != null)
			{
				start_src_pos = s.getSourcePosition(start_pos);				
			}
			Position end_pos = s.getPosition(rm.getToPos());
			if (end_pos != null)
			{
				end_src_pos = s.getSourcePosition(end_pos);
			}
			Range r = null;
			boolean original_range = true;
			if (start_src_pos == null)
			{
				original_range = false;
				if (start_pos != null)
				{
					// Can't find the text in the original: used detexed
					line = s.getLine(start_pos.getLine());
					start_src_pos = start_pos;
					end_src_pos = end_pos;
				}
				r = Range.make(0, 0, 0);
			}
			else
			{
				line = original.getLine(start_src_pos.getLine());
				if (end_src_pos == null)
				{
					r = Range.make(start_src_pos.getLine(), start_src_pos.getColumn(), start_src_pos.getColumn());
				}
				else
				{
					r = new Range(start_src_pos, end_src_pos);
				}
			}
			// Exception if spelling mistake and a dictionary is provided
			String clean_line = s.getLine(start_pos.getLine());
			int end_p = end_pos.getColumn();
			if (end_p > 0)
			{
				if (end_p > clean_line.length() - 1)
				{
					end_p = clean_line.length() - 1;
				}
				if (rm.getRule().getId().startsWith("MORFOLOGIK"))
				{
					// This is a spelling mistake
					int from = Math.min(clean_line.length() - 1, start_pos.getColumn());
					String word = clean_line.substring(from, end_p);

					word = cleanup(word);
					if (m_dictionary.contains(word))
					{
						// Word is in dictionary: ignore
						continue;
					}
				}
			}
			// Exception for false alarm regarding "smart quotes"
			end_p = r.getEnd().getColumn();
			if (end_p > line.length() - 1)
			{
				end_p = line.length() - 1;
			}
			if (rm.getRule().getId().startsWith("EN_QUOTES") && rm.getMessage().contains("Use a smart opening quote"))
			{
				if (line.length() > 0)
				{
					String word = line.substring(Math.min(line.length() - 1, r.getStart().getColumn()), end_p).trim();
					if (word.contains("``"))
					{
						// This type of quote is OK in LaTeX: ignore 
						continue;
					}
				}
			}
			Advice ad = new Advice(new CheckLanguageSpecific(rm.getRule().getId()), r, rm.getMessage() + " (" + rm.getFromPos() + ")", s.getResourceName(), line);
			ad.setOriginal(original_range);
			out_list.add(ad);
		}
		return out_list;
	}

	/**
	 * Cleans a word by removing spaces at the beginning and the end, and
	 * punctuation symbols at the end
	 * @param s The word to clean
	 * @return The cleaned word
	 */
	protected static String cleanup(String s)
	{
		s = s.replaceAll("^[\\s]*", "");
		s = s.replaceAll("[\\s\\.,':;]*$", "");
		return s;
	}

	public class CheckLanguageSpecific extends Rule
	{
		public CheckLanguageSpecific(String id)
		{
			super(CheckLanguage.this.getName() + ":" + id);
		}

		@Override
		public List<Advice> evaluate(AnnotatedString s, AnnotatedString original)
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*@ pure non_null @*/ public CheckLanguage getParent()
		{
			return CheckLanguage.this;
		}
	}

	public static class UnsupportedLanguageException extends Exception
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;

	}
}
