package ca.uqac.lif.texlint;

import java.util.List;

import ca.uqac.lif.texlint.as.AnnotatedString;

/**
 * Description of a condition that must apply on a piece of text.
 * That condition can take multiple forms: a regular expression pattern
 * that must/must not be found, etc.
 * 
 * @author Sylvain Hallé
 */
public abstract class Rule
{
	/**
	 * A unique name given to the rule
	 */
	protected String m_name;
	
	/**
	 * Evaluates the rule on a string
	 * @param s The string on which to evaluate the rule
	 * @param original The original (untransformed) piece of text
	 * @return A list of advice generated from the evaluation of the rule
	 */
	public abstract List<Advice> evaluate(/*@ non_null @*/ AnnotatedString s, 
			/*@ non_null @*/ AnnotatedString original);
	
	/**
	 * Creates a new rule
	 * @param name A unique name given to the rule
	 */
	public Rule(/*@ non_null @*/ String name)
	{
		super();
		m_name = name;
	}
	
	/**
	 * Gets the name given to the rule
	 * @return The name
	 */
	/*@ pure non_null @*/ public String getName()
	{
		return m_name;
	}
	
	@Override
	public String toString()
	{
		return m_name;
	}
}
