/*
    TexLint, a linter for LaTeX documents
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
package ca.uqac.lif.texlint.languagetool;

import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.AustrianGerman;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.CanadianEnglish;
import org.languagetool.language.Dutch;
import org.languagetool.language.French;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.Portuguese;
import org.languagetool.language.Spanish;
import org.languagetool.language.SwissGerman;

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
	 * @return A Language object, or {@cde null} if no language could be
	 * instantiated from the string
	 */
	/*@ nullable @*/ public static Language getLanguageFromString(String s)
	{
		if (s.compareToIgnoreCase("en") == 0 || s.compareToIgnoreCase("en_US") == 0)
		{
			return new AmericanEnglish();
		}
		if (s.compareToIgnoreCase("en_CA") == 0)
		{
			return new CanadianEnglish();
		}
		if (s.compareToIgnoreCase("en_UK") == 0)
		{
			return new BritishEnglish();
		}
		if (s.compareToIgnoreCase("fr") == 0 || s.compareToIgnoreCase("fr_CA") == 0)
		{
			return new French();
		}
		if (s.compareToIgnoreCase("es") == 0)
		{
			return new Spanish();
		}
		if (s.compareToIgnoreCase("de") == 0 || s.compareToIgnoreCase("de_DE") == 0)
		{
			return new GermanyGerman();
		}
		if (s.compareToIgnoreCase("de_CH") == 0)
		{
			return new SwissGerman();
		}
		if (s.compareToIgnoreCase("de_AT") == 0)
		{
			return new AustrianGerman();
		}
		if (s.compareToIgnoreCase("nl") == 0)
		{
			return new Dutch();
		}
		if (s.compareToIgnoreCase("pt") == 0)
		{
			return new Portuguese();
		}
		return null;
	}
}
