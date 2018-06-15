/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018  Sylvain Hallé

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

import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.textidote.as.Range;

public class RangeTest 
{
	@Test
	public void testCompare1()
	{
		Range r1 = new Range(new Position(0, 0), new Position(0, 0));
		Range r2 = new Range(new Position(0, 0), new Position(0, 0));
		assertTrue(r1.compareTo(r2) == 0);
	}
	
	@Test
	public void testCompare2()
	{
		Range r1 = new Range(new Position(0, 0), new Position(1, 0));
		Range r2 = new Range(new Position(0, 0), new Position(1, 0));
		assertTrue(r1.compareTo(r2) == 0);
	}
	
	@Test
	public void testCompare3()
	{
		Range r1 = new Range(new Position(0, 0), new Position(0, 0));
		Range r2 = new Range(new Position(1, 0), new Position(0, 0));
		assertTrue(r1.compareTo(r2) < 0);
	}
	
	@Test
	public void testCompare4()
	{
		Range r1 = new Range(new Position(1, 0), new Position(0, 0));
		Range r2 = new Range(new Position(0, 0), new Position(0, 0));
		assertTrue(r1.compareTo(r2) > 0);
	}
	
	@Test
	public void testCompare5()
	{
		Range r1 = new Range(new Position(0, 0), new Position(0, 5));
		Range r2 = new Range(new Position(0, 0), new Position(0, 6));
		assertTrue(r1.compareTo(r2) < 0);
	}
	
	@Test
	public void testCompare6()
	{
		Range r1 = new Range(new Position(0, 0), new Position(0, 6));
		Range r2 = new Range(new Position(0, 0), new Position(0, 5));
		assertTrue(r1.compareTo(r2) > 0);
	}
	
	@Test
	public void testWithin1()
	{
		Range r1 = new Range(new Position(0, 0), new Position(0, 5));
		assertTrue(r1.isWithin(new Position(0, 4)));
	}
	
	@Test
	public void testWithin2()
	{
		Range r1 = new Range(new Position(0, 0), new Position(0, 5));
		assertFalse(r1.isWithin(new Position(0, 8)));
	}
}
