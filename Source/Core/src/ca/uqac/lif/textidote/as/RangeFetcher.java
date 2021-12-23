package ca.uqac.lif.textidote.as;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.dag.Crawler;
import ca.uqac.lif.dag.Node;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.strings.Range;

/**
 * A crawler that traverses an explanation graph and fetches all the ranges
 * mentioned in leaf nodes.
 */
class RangeFetcher extends Crawler
{
	/**
	 * The ranges found in leaf nodes.
	 */
	/*@ non_null @*/ protected List<Range> m_ranges;
	
	/**
	 * Creates a new range fetcher.
	 * @param start The starting point of the crawl
	 */
	public RangeFetcher(/*@ non_null @*/ Node start)
	{
		super(start);
		m_ranges = new ArrayList<Range>();
	}
	
	@Override
	public void visit(/*@ non_null @*/ Node n)
	{
		if (isLeaf(n))
		{
			Part p = ((PartNode) n).getPart();
			Range r = Range.mentionedRange(p);
			if (r != null)
			{
				m_ranges.add(r);
			}
		}
	}
	
	/**
	 * Gets the list of ranges fetched during the crawl.
	 * @return The list of ranges
	 */
	/*@ pure non_null @*/ public List<Range> getRanges()
	{
		return m_ranges;
	}
	
	/**
	 * Determines if a node is a leaf PartNode.
	 * @param n The node
	 * @return <tt>true</tt> if n is a part node and a leaf, <tt>false</tt>
	 * otherwise
	 */
	protected static boolean isLeaf(/*@ non_null @*/ Node n)
	{
		if (!(n instanceof PartNode))
		{
			return false;
		}
		for (int i = 0; i < n.getOutputArity(); i++)
		{
			if (n.getOutputLinks(i).size() > 0)
			{
				return false;
			}
		}
		return true;
	}
}