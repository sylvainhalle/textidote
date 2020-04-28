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
package ca.uqac.lif.textidote.render;

import java.util.List;
import java.util.Map;

import org.languagetool.Language;

import ca.uqac.lif.json.JsonFalse;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.AdviceRenderer;
import ca.uqac.lif.textidote.Main;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.Range;
import ca.uqac.lif.textidote.rules.LanguageFactory;
import ca.uqac.lif.util.AnsiPrinter;

/**
 * Renders advice in LanguageTool's JSON format.
 * <p>
 * <b>Caveat emptor:</b> since LT works on single files, its JSON format
 * does not have a structure that supports outputting info for multiple
 * files at once. Hence this advice renderer will only work on the first
 * file it is given.
 * 
 * @author Sylvain Hallé
 */
public class JsonAdviceRenderer extends AdviceRenderer 
{
	public JsonAdviceRenderer(/*@ non_null @*/ AnsiPrinter printer, String lang_code)
	{
		super(printer, lang_code);
	}

	@Override
	public void render()
	{
		JsonMap root = new JsonMap();
		{
			JsonMap sw = new JsonMap();
			sw.put("name", "TeXtidote");
			sw.put("version", Main.VERSION_STRING);
			sw.put("buildDate", "2019-08-03 10:08"); // Dummy date
			sw.put("apiVersion", 1);
			root.put("software", sw);
		}
		{
			JsonMap warn = new JsonMap();
			root.put("warnings", warn);
		}
		{
			JsonMap lang = new JsonMap();
			String name = "", code = "";
			if (!m_languageCode.isEmpty())
			{
				Language l = LanguageFactory.getLanguageFromString(m_languageCode);
				if (l != null)
				{
					code = l.getShortCode();
					name = l.getName();
				}
			}
			lang.put("name", name);
			lang.put("code", code);
			root.put("language", lang);
		}
		JsonList matches = new JsonList();
		for (Map.Entry<String,List<Advice>> entry : m_advice.entrySet())
		{
			for (Advice a : entry.getValue())
			{
				int count = 1;
				JsonMap match = new JsonMap();
				match.put("message", a.getMessage());
				match.put("shortMessage", a.getShortMessage());
				JsonList replacements = new JsonList();
				List<String> repls = a.getReplacements();
				if (repls != null)
				{
					for (String repl : repls)
					{
						JsonMap j_rep = new JsonMap();
						j_rep.put("value", repl);
						replacements.add(j_rep);
					}
				}
				match.put("replacements", replacements);
				match.put("offset", a.getOffset());
				match.put("length", a.getRange().getLength());
				String excerpt = renderExcerpt(a.getLine(), a.getRange(), 80);
				{
					JsonMap context = new JsonMap();
					context.put("text", excerpt);
					context.put("offset", a.getOffset());
					context.put("length", a.getRange().getLength());
					match.put("context", context);
				}
				match.put("sentence", excerpt);
				{
					JsonMap type = new JsonMap();
					type.put("typeName", "Other"); // Haven't seen any other value
					match.put("type", type);
				}
				{
					JsonMap j_rule = new JsonMap();
					Rule rule = a.getRule();
					j_rule.put("id", rule.getName());
					j_rule.put("description", rule.getDescription());
					j_rule.put("issueType", "language");
					{
						JsonMap category = new JsonMap();
						category.put("id", "MISC");
						category.put("name", "Miscellaneous");
						j_rule.put("category", category);
					}
					match.put("rule", j_rule);
				}
				match.put("ignoreForIncompleteSentence", JsonFalse.instance);
				match.put("contextForSureMatch", count);
				matches.add(match);	
				count++;
			}
			// Break after the first file; see comment at top of this class
			break;
		}
		root.put("matches", matches);
		m_printer.print(root.toString());
	}
	
	/**
	 * Renders a partial line of text surrounding the location of the advice.
	 * @param line The line of text
	 * @param range The range to highlight
	 * @param line_width An approximate length for the line of text to produce
	 * @return The excerpt
	 */
	protected String renderExcerpt(/*@ non_null @*/ String line, /*@ non_null @*/ Range range, int line_width)
	{
		int left = range.getStart().getColumn();
		int right = range.getEnd().getColumn();
		int range_width = right - left;
		int mid_point = left + range_width / 2;
		int offset = 0;
		if (range_width < line.length())
		{
			if (mid_point + line_width / 2 >= line.length())
			{
				int char_dif = (mid_point + line_width / 2) - line.length();
				offset = Math.max(0, (mid_point - line_width / 2) - char_dif);
			}
			else
			{
				offset = Math.max(0, mid_point - line_width / 2);
			}
		}
		String line_to_display = line.substring(offset, Math.min(line.length(), offset + line_width));
		return line_to_display;
	}
}
