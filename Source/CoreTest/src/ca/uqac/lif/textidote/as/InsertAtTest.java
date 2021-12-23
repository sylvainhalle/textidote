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

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.function.strings.RangeMapping;
import ca.uqac.lif.petitpoucet.function.strings.RangeMapping.RangePair;

public class InsertAtTest
{
	@Test
	public void test1()
	{
		InsertAt f = new InsertAt("foo", 0);
		String out = (String) f.evaluate("Hello")[0];
		assertEquals("fooHello", out);
		assertEquals(new RangeMapping(
				new RangePair(0, 4, 3, 7)), f.getMapping());
	}
	
	@Test
	public void test2()
	{
		InsertAt f = new InsertAt("foo", 1);
		String out = (String) f.evaluate("Hello")[0];
		assertEquals("Hfooello", out);
		assertEquals(new RangeMapping(
				new RangePair(0, 0, 0, 0),
				new RangePair(1, 4, 4, 7)), f.getMapping());
	}
	
	@Test
	public void test3()
	{
		InsertAt f = new InsertAt("foo", 5);
		String out = (String) f.evaluate("Hello")[0];
		assertEquals("Hellofoo", out);
		assertEquals(new RangeMapping(
				new RangePair(0, 4, 0, 4)), f.getMapping());
	}
	
	@Test
	public void test4()
	{
		InsertAt f = new InsertAt("foo", 9);
		String out = (String) f.evaluate("Hello")[0];
		assertEquals("Hellofoo", out);
		assertEquals(new RangeMapping(
				new RangePair(0, 4, 0, 4)), f.getMapping());
	}
}
