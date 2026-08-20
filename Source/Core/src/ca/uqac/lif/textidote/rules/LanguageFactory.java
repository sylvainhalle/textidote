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

import java.util.ArrayList;
import java.util.List;

import org.languagetool.Language;
import org.languagetool.Languages;

/**
 * Factory class whose sole purpose is to provide instances of {@code Language}
 * objects.
 * @author Sylvain Hallé
 */
public class LanguageFactory 
{
	/**
	 * Instantiates a Language object based on a string
	 * @param s The string
	 * @return A Language object, or {@code null} if no language could be
	 * instantiated from the string
	 */
	/*@ nullable @*/ public static Language getLanguageFromString(String s)
	{
		String code = getLanguageToolCode(s);
		if (code == null)
		{
			return null;
		}
		try
		{
			// Languages.getLanguageForShortCode delegates to LanguageTool's own
			// singleton-managed registry (built from language-module.properties),
			// instead of instantiating Language subclasses directly with "new".
			// Doing the latter throws under LT 6.x once JLanguageTool has already
			// instantiated the same language internally.
			return Languages.getLanguageForShortCode(code);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	/**
	 * Converts a comma-separated list of TeXtidote-style language codes
	 * (e.g. as passed to --check) into their LanguageTool short-code
	 * equivalents, skipping any that are unrecognized.
	 * @param s The comma-separated list of TeXtidote-style codes
	 * @return The list of corresponding LanguageTool short codes (possibly
	 * shorter than the input if some codes were unrecognized)
	 */
	/*@ non_null @*/ public static List<String> getLanguageToolCodes(/*@ non_null @*/ String s)
	{
		List<String> out = new ArrayList<String>();
		for (String part : s.split(","))
		{
			String code = getLanguageToolCode(part.trim());
			if (code != null)
			{
				out.add(code);
			}
		}
		return out;
	}

	/**
	 * Converts TeXtidote's historical underscore-separated language argument
	 * (e.g. "en_US", as typed after --check) into the hyphen-separated short
	 * code expected by LanguageTool's {@code Languages.getLanguageForShortCode}.
	 * @param s The TeXtidote-style language string
	 * @return The corresponding LanguageTool short code, or {@code null} if
	 * unrecognized
	 */
	public static String getLanguageToolCode(String s)
	{
		if (s.compareToIgnoreCase("en") == 0 || s.compareToIgnoreCase("en_US") == 0)
		{
			return "en-US";
		}
		if (s.compareToIgnoreCase("ar") == 0)
		{
			return "ar";
		}
		if (s.compareToIgnoreCase("en_CA") == 0)
		{
			return "en-CA";
		}
		if (s.compareToIgnoreCase("en_UK") == 0)
		{
			return "en-GB";
		}
		if (s.compareToIgnoreCase("pl") == 0)
		{
			return "pl";
		}
		if (s.compareToIgnoreCase("fr") == 0 || s.compareToIgnoreCase("fr_CA") == 0)
		{
			return "fr";
		}
		if (s.compareToIgnoreCase("es") == 0)
		{
			return "es";
		}
		if (s.compareToIgnoreCase("de") == 0 || s.compareToIgnoreCase("de_DE") == 0)
		{
			return "de-DE";
		}
		if (s.compareToIgnoreCase("de_CH") == 0)
		{
			return "de-CH";
		}
		if (s.compareToIgnoreCase("de_AT") == 0)
		{
			return "de-AT";
		}
		if (s.compareToIgnoreCase("nl") == 0)
		{
			return "nl";
		}
		if (s.compareToIgnoreCase("pt") == 0)
		{
			return "pt";
		}
		if (s.compareToIgnoreCase("pt_BR") == 0)
		{
			return "pt-BR";
		}
		return null;
	}
}
