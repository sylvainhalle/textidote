package ca.uqac.lif.texlint;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.texlint.as.AnnotatedString;
import ca.uqac.lif.texlint.as.Match;
import ca.uqac.lif.texlint.as.Position;
import ca.uqac.lif.texlint.as.Range;

/**
 * Rule based on a regular expression pattern to be found in the text.
 * @author Sylvain Hallé
 */
public class RegexRule extends Rule 
{
	/**
	 * The pattern to find in the text
	 */
	protected String m_pattern;
	
	/**
	 * The message template to generate when the pattern is found
	 */
	protected String m_message;
	
	/**
	 * The maximum number of times the rule can look for the pattern in
	 * the text
	 */
	protected static final transient int MAX_ITERATIONS = 100;
	
	/**
	 * Creates a new regex rule
	 * @param name The name given to the rule
	 * @param pattern The pattern to find in the text
	 * @param message The message template to generate when the pattern
	 * is found. If the pattern contains capture groups, the message can
	 * refer to these capture groups in the usual way (i.e. "$1" refers to
	 * the first group, etc.).
	 */
	public RegexRule(String name, String pattern, String message)
	{
		super(name);
		m_pattern = pattern;
		m_message = message;
	}

	@Override
	/*@ non_null @*/ public List<Advice> evaluate(/*@ non_null @*/ AnnotatedString s,
			/*@ non_null @*/ AnnotatedString original)
	{
		List<Advice> out_list = new ArrayList<Advice>();
		Position pos = Position.ZERO;
		for (int num_iterations = 0; num_iterations < MAX_ITERATIONS; num_iterations++)
		{
			Match match = s.find(m_pattern, pos);
			if (match == null)
			{
				// No cigarettes, no matches
				break;
			}
			String message = createMessage(match);
			Position start_pos = match.getPosition();
			Position end_pos = new Position(start_pos.getLine(), start_pos.getColumn() + match.getMatch().length() - 1);
			Position source_start_pos = s.getSourcePosition(start_pos);
			Position source_end_pos = s.getSourcePosition(end_pos);
			if (source_end_pos == null)
			{
				source_end_pos = source_start_pos;
			}
			Range r = new Range(source_start_pos, source_end_pos);
			String original_line = original.getLines().get(source_start_pos.getLine());
			out_list.add(new Advice(this, r, message, s.getResourceName(), original_line));
			pos = new Position(start_pos.getLine(), start_pos.getColumn() + match.getMatch().length());;
		}
		return out_list;
	}

	/**
	 * Creates the message for a specific advice, by replacing references
	 * to capture groups in the message template by the actual strings that
	 * matched these capture groups
	 * @param match A match object containing data about the regex match
	 * @return The formatted message
	 */
	protected String createMessage(Match match)
	{
		String out = m_message;
		for (int i = 1; i < match.groupCount(); i++)
		{
			String s = match.group(i);
			if (s != null)
			{
				out = out.replace("$" + i, match.group(i));
			}
			else
			{
				out = out.replace("$" + i, "");
			}
		}
		return out;
	}
}
