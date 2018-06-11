package ca.uqac.lif.texlint;

/**
 * Represents a match by a regular expression find
 * @author sylvain
 *
 */
public class Match 
{
	/**
	 * The position where the match occurred 
	 */
	private final Position m_position;
	
	/**
	 * The string that matched the regular expression
	 */
	private final String m_match;
	
	/**
	 * Creates a new match object
	 * @param match The position where the match occurred
	 * @param pos The string that matched the regular expression
	 */
	public Match(String match, Position pos)
	{
		super();
		m_match = match;
		m_position = pos;
	}
	
	/**
	 * Gets the position where the match occurred
	 * @return The position
	 */
	public Position getPosition()
	{
		return m_position;
	}
	
	/**
	 * Gets the string that matched the regular expression
	 * @return The string
	 */
	public String getMatch()
	{
		return m_match;
	}
	
	@Override
	public String toString()
	{
		return m_match + " (" + m_position + ")";
	}
}
