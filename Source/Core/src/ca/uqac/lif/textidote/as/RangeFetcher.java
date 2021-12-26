/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2021  Sylvain Hallé

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
	/*@ non_null @*/ protected final List<Range> m_ranges;
	
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