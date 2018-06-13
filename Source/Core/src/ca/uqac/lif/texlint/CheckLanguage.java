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
package ca.uqac.lif.texlint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.RuleMatch;

import ca.uqac.lif.texlint.as.AnnotatedString;
import ca.uqac.lif.texlint.as.Position;
import ca.uqac.lif.texlint.as.Range;

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
	JLanguageTool m_languageTool;
	
	/**
	 * Creates a new rule for checking a specific language
	 * @param lang The language to check
	 */
	public CheckLanguage(/*@ non_null @*/ Language lang)
	{
		super("lt:" + lang.getShortCode());
		m_languageTool = new MultiThreadedJLanguageTool(lang);
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
			if (start_src_pos == null)
			{
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
			out_list.add(new Advice(new CheckLanguageSpecific(rm.getRule().getId()), r, rm.getMessage(), s.getResourceName(), line));
		}
		return out_list;
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
}
