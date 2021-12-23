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

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.AnnotatedString.Line;
import ca.uqac.lif.textidote.as.PositionRange;

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
	 * @param first_lang The first language of the author.
	 * Used by LanguageTool to check for false friends.
	 * @param dictionary A set of words that should be ignored by
	 * spell checking
	 * @throws UnsupportedLanguageException If {@code lang} is null
	 */
	public CheckLanguage(/*@ nullable @*/ Language lang, /*@ nullable @*/ Language first_lang, /*@ non_null @*/ List<String> dictionary) throws UnsupportedLanguageException
	{
		super("lt:");
		if (lang == null)
		{
			throw new UnsupportedLanguageException();
		}
		setName("lt:" + lang.getShortCode());
		if (first_lang == null)
		{
			m_languageTool = new MultiThreadedJLanguageTool(lang);
		}
		else
		{
			m_languageTool = new MultiThreadedJLanguageTool(lang, first_lang);
		}
		if (m_disableWhitespace)
		{
			m_languageTool.disableRule("WHITESPACE_RULE");
		}
		m_dictionary = dictionary;
		handleUserDictionary();
	}

	public void handleUserDictionary()
	{
		for (org.languagetool.rules.Rule rule : m_languageTool.getAllActiveRules())
		{
			if (rule instanceof SpellingCheckRule)
			{
				((SpellingCheckRule) rule).addIgnoreTokens(m_dictionary);
			}
		}
	}

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
		this(lang, null, dictionary);
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
	public List<Advice> evaluate(AnnotatedString s)
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
			Line line = null;
			int start_pos = rm.getFromPos();
			int end_pos = rm.getToPos();
			Range r = s.findOriginalRange(new Range(start_pos, end_pos - 1));
			boolean original_range = true;
			if (r == null)
			{
				// Can't find the text in the original: used detexed
				original_range = false;
				line = s.getLineOf(start_pos);
				r = new Range(-1, -1);
			}
			else
			{
				PositionRange source_pr = s.getOriginalPositionRange(r.getStart(), r.getEnd());
				line = s.getOriginalLine(source_pr.getStart().getLine());
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
			Line cl = s.getLine(s.getPosition(start_pos).getLine());
			String clean_line = cl.toString();
			int end_p = 0;
			if (end_pos >= 0)
			{
				end_p = end_pos;
			}
			if (end_p > 0)
			{
				if (end_p > clean_line.length() - 1)
				{
					end_p = clean_line.length() - 1;
				}
			}
			// Exception for false alarm regarding "smart quotes"
			end_p = r.getEnd();
			/*if (end_p > line.length() - 1)
			{
				end_p = line.length() - 1;
			}*/
			if (rm.getRule().getId().startsWith("FRENCH_WHITESPACE"))
			{
				// LaTeX takes care of whitespace, so ignore LT's advice
				continue;
			}
			if (rm.getRule().getId().startsWith("EN_QUOTES") && rm.getMessage().contains("Use a smart opening quote"))
			{
				if (line.toString().length() > 0)
				{
					int word_start = Math.min(line.toString().length() - 1, r.getStart() - line.getOffset());
					int word_end = end_p - line.getOffset();
					String word = line.toString().substring(word_start, word_end).trim();
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
			Advice ad = new Advice(new CheckLanguageSpecific(rm.getRule().getId(), rm.getRule().getDescription()), r, advice_message.toString(), s, line);
			ad.setOriginal(original_range);
			ad.setShortMessage("LanguageTool rule");
			out_list.add(ad);
		}
		return out_list;
	}

	/**
	 * Activate rules that depend on a language model. The language model
	 * currently consists of Lucene indexes with ngram occurrence counts.
	 * @param f_ngram_dir Directory with a '3grams' sub directory which
	 * contains a Lucene index with 3gram occurrence counts
	 * @throws FolderNotFoundException If folder cannot be found
	 * @throws IncorrectFolderStructureException If folder has the wrong
	 * structure
	 */
	public void activateLanguageModelRules(File f_ngram_dir) throws FolderNotFoundException, IncorrectFolderStructureException
	{
		try
		{
			m_languageTool.activateLanguageModelRules(f_ngram_dir);
			handleUserDictionary();
		}
		catch (IOException e)
		{
			throw new FolderNotFoundException();
		}
		catch (RuntimeException e)
		{
			// This happens if the folder exists, but does not have the
			// right structure
			throw new IncorrectFolderStructureException(e.getMessage());
		}
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
		/**
		 * A description for the rule
		 */
		protected String m_description = "";

		public CheckLanguageSpecific(String id, String description)
		{
			super(CheckLanguage.this.getName() + ":" + id);
			m_description = description;
		}

		@Override
		public List<Advice> evaluate(AnnotatedString s)
		{
			// No need to implement, this is just a placeholder
			return null;
		}

		/*@ pure non_null @*/ public CheckLanguage getParent()
		{
			return CheckLanguage.this;
		}

		@Override
		public String getDescription()
		{
			return m_description;
		}
	}

	public static class UnsupportedLanguageException extends Exception
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;
	}

	public static class IncorrectFolderStructureException extends Exception
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;

		public IncorrectFolderStructureException(String message)
		{
			super(message);
		}
	}

	public static class FolderNotFoundException extends Exception
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;
	}

	@Override
	public String getDescription()
	{
		return "LanguageTool";
	}
}
