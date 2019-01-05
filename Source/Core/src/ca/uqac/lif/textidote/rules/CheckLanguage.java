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
package ca.uqac.lif.textidote.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

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
	 * A list of words that should be ignored by spell checking
	 */
	/*@ non_null @*/ protected List<String> m_dictionary;

	/**
	 * Whether to disable Language Tool's "whitespace rule". Since the detexed
	 * string may contain extra spaces (due to the removal of markup), and
	 * that LaTeX ignores multiple spaces anyway, it is advisable to
	 *  turn it off.
	 */
	protected boolean m_disableWhitespace = true;

	/**
	 * Whether to disable Language Tool's "unpaired symbol" rule. The detexing
	 * of the string leaves a few unmatched "}", so we just ignore this rule
	 * when it concerns this particular character.
	 */
	protected boolean m_disableUnpaired = true;

	/**
	 * Creates a new rule for checking a specific language
	 * @param lang The language to check. If {@code null}, the
	 * constructor will throw an exception
	 * @param dictionary A set of words that should be ignored by
	 * spell checking
	 * @throws UnsupportedLanguageException If {@code lang} is null
	 */
	public CheckLanguage(/*@ nullable @*/ Language lang, /*@ non_null @*/ List<String> dictionary) throws UnsupportedLanguageException
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
		for (org.languagetool.rules.Rule rule : m_languageTool.getAllActiveRules())
		{
			if (rule instanceof SpellingCheckRule)
			{
				((SpellingCheckRule) rule).addIgnoreTokens(dictionary);
			}
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
		this(lang, new ArrayList<String>(0));
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
			Position start_src_pos = Position.NOWHERE, end_src_pos = Position.NOWHERE;
			Position start_pos = s.getPosition(rm.getFromPos());
			if (!start_pos.equals(Position.NOWHERE))
			{
				start_src_pos = s.getSourcePosition(start_pos);				
			}
			Position end_pos = s.getPosition(rm.getToPos());
			if (!end_pos.equals(Position.NOWHERE))
			{
				end_src_pos = s.getSourcePosition(end_pos);
			}
			Range r = null;
			boolean original_range = true;
			if (start_src_pos.equals(Position.NOWHERE))
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
				if (end_src_pos.equals(Position.NOWHERE))
				{
					r = Range.make(start_src_pos.getLine(), start_src_pos.getColumn(), start_src_pos.getColumn());
				}
				else
				{
					r = new Range(start_src_pos, end_src_pos);
				}
			}
			// Exception for the disable unpaired rule
			if (m_disableUnpaired && rm.getRule().getId().startsWith("EN_UNPAIRED_BRACKETS"))
			{
				if (rm.getMessage().contains("{") || rm.getMessage().contains("}"))
				{
					// We ignore the unpaired symbol for this character
					continue;
				}
			}
			// Exception if spelling mistake and a dictionary is provided
			String clean_line = s.getLine(start_pos.getLine());
			int end_p = 0;
			if (end_pos != null)
			{
				end_p = end_pos.getColumn();
			}
			if (end_p > 0)
			{
				if (end_p > clean_line.length() - 1)
				{
					end_p = clean_line.length() - 1;
				}
			}
			// Exception for false alarm regarding "smart quotes"
			end_p = r.getEnd().getColumn();
			if (end_p > line.length() - 1)
			{
				end_p = line.length() - 1;
			}
			if (rm.getRule().getId().startsWith("FRENCH_WHITESPACE"))
			{
				// LaTeX takes care of whitespace, so ignore LT's advice
				continue;
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
			StringBuilder advice_message = new StringBuilder();
			advice_message.append(rm.getMessage());
			// Append suggested replacements to advice message, if any
			List<String> replacements = rm.getSuggestedReplacements();
			if (!replacements.isEmpty())
			{
				advice_message.append(". Suggestions: ").append(replacements.toString());
			}
			advice_message.append(" (").append(rm.getFromPos()).append(")");
			Advice ad = new Advice(new CheckLanguageSpecific(rm.getRule().getId()), r, advice_message.toString(), s.getResourceName(), line);
			ad.setOriginal(original_range);
			out_list.add(ad);
		}
		return out_list;
	}
	
	/**
	 * Activate rules that depend on a language model. The language model
	 * currently consists of Lucene indexes with ngram occurrence counts.
	 * @param f_ngram_dir Directory with a '3grams' sub directory which
	 * contains a Lucene index with 3gram occurrence counts
	 * @throws IOException If folder cannot be found
	 */
	public void activateLanguageModelRules(File f_ngram_dir) throws IOException
	{
		m_languageTool.activateLanguageModelRules(f_ngram_dir);
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
