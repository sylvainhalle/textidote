/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2023  Sylvain Hallé

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ca.uqac.lif.util.NullPrintStream;

/**
 * Attempts to run the main loop using each language code in succession.
 * This is to make sure that all advertised languages are indeed usable and
 * don't throw an exception.
 * 
 * @author Sylvain Hallé
 *
 */
@RunWith(Parameterized.class)
public class LanguageCodeTest 
{
	/**
	 * The langauge code of the current test
	 */
	protected String m_languageCode;
	
	/**
	 * A nonexistent language code
	 */
	protected static final String FAKE_LANG = "zzz";
	
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object> languageCodes() {
		return Arrays.asList(new Object[] {
				"de", "de_AT", "de_CH", "de_DE",
				"en", "en_CA", "en_UK", "en_US",
				"es",
				"fr",
				"nl",
				"pt",
				FAKE_LANG // We add a non existent language, to make sure the CLI
				// does report an error in such a case
		});
	}
	
	/**
	 * Creates a new instance of the language code test
	 * @param language_code The language code to check
	 */
	public LanguageCodeTest(String language_code)
	{
		super();
		m_languageCode = language_code;
	}

	@Test(timeout = 15000)
	public void testEachLanguage() throws IOException
	{
		InputStream in = LanguageCodeTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--check", m_languageCode}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		if (m_languageCode.compareTo(FAKE_LANG) == 0)
		{
			// For an unsupported language, the return code is negative
			assertTrue(ret_code == Main.ERR_UNKNOWN_LANGUAGE);
		}
		else
		{
			// Otherwise the code is null or positive
			assertTrue(ret_code >= 0);
		}
	}
}
