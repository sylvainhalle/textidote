package ca.uqac.lif.texlint;

import java.util.List;

import ca.uqac.lif.util.AnsiPrinter;

/**
 * Renders a list of advice in a special format
 * @author Sylvain Hallé
 */
public abstract class AdviceRenderer 
{
	AnsiPrinter m_printer;
	
	public AdviceRenderer(AnsiPrinter printer)
	{
		super();
		m_printer = printer;
	}
	
	/**
	 * Renders the list of advice
	 * @param out A print stream where to send the text
	 * @param list The list of advice to render
	 */
	public abstract void render(List<Advice> list);
}
