package ca.uqac.lif.textidote.as;

import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.petitpoucet.function.strings.RangeMapping.RangePair;
import ca.uqac.lif.petitpoucet.function.strings.StringMappingFunction;

public class PassthroughFunction extends StringMappingFunction
{
	public PassthroughFunction(StringMappingFunction f, int offset)
	{
		super();
		m_mapping.add(new Range(0, offset - 1), new Range(0, offset - 1));
		for (RangePair rp : f.getMapping().getPairs())
		{
			m_mapping.add(rp.getFrom().shift(offset), rp.getTo().shift(offset));
		}
	}

	@Override
	protected String transformString(String s)
	{
		return s;
	}
}
