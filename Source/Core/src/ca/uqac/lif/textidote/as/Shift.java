package ca.uqac.lif.textidote.as;

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.petitpoucet.function.strings.StringMappingFunction;

class Shift extends StringMappingFunction
{
	protected int m_by;
	
	public Shift(int by)
	{
		super();
		m_by = by;
	}

	@Override
	protected String transformString(String s)
	{
		m_mapping.add(new Range(m_by, m_by + s.length() - 1), new Range(0, s.length() - 1));
		return s;
	}
}
