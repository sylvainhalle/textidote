package ca.uqac.lif.texlint;

import ca.uqac.lif.texlint.as.Range;

public class Advice 
{
	/**
	 * The range in the file where the advice applies
	 */
	protected Range m_range;
	
	/**
	 * The message associated with the advice
	 */
	protected String m_message;
	
	/**
	 * The rule from which this advice was generated
	 */
	protected Rule m_rule;
	
	/**
	 * The name of the resource (e.g. filename) this advice refers to 
	 */
	protected String m_resource;
	
	/**
	 * The line where the advice applies
	 */
	protected String m_line;
	
	/**
	 * Creates a new advice
	 * @param rule The rule from which this advice was generated
	 * @param range The range in the file where the advice applies
	 * @param message The message associated with the advice
	 * @param resource The name of the resource (e.g. filename) this
	 * advice refers to
	 */
	public Advice(/*@ non_null @*/ Rule rule, /*@ non_null @*/ Range range, 
			/*@ non_null @*/ String message, /*@ non_null @*/ String resource,
			/*@ non_null @*/ String line)
	{
		super();
		m_rule = rule;
		m_range = range;
		m_message = message;
		m_resource = resource;
		m_line = line;
	}
	
	/**
	 * Gets the range in the file where the advice applies
	 * @return The range
	 */
	/*@ pure non_null @*/ public Range getRange()
	{
		return m_range;
	}
	
	/**
	 * Gets the line in the file where the advice applies
	 * @return The line
	 */
	/*@ pure non_null @*/ public String getLine()
	{
		return m_line;
	}
	
	/**
	 * Gets the message for this advice
	 * @return The message
	 */
	/*@ pure non_null @*/ public String getMessage()
	{
		return m_message;
	}
	
	/**
	 * Gets the rule from which this advice was generated
	 * @return The rule
	 */
	/*@ pure non_null @*/ public Rule getRule()
	{
		return m_rule;
	}
	
	/**
	 * Gets the name of the resource (e.g. filename) this advice refers to
	 * @return The name of the resource
	 */
	/*@ pure non_null @*/ public String getResource()
	{
		return m_resource;
	}
	
	@Override
	/*@ pure non_null @*/ public String toString()
	{
		return m_range + " " + m_message;
	}
	
	@Override
	/*@ pure @*/ public int hashCode()
	{
		return m_range.hashCode() + m_rule.hashCode();
	}
	
	@Override
	/*@ pure @*/ public boolean equals(Object o)
	{
		if (!(o instanceof Advice))
		{
			return false;
		}
		Advice a = (Advice) o;
		return m_range.equals(a.m_range) && m_rule.equals(a.m_rule) 
				&& m_resource.compareTo(a.m_resource) == 0;
	}
}
