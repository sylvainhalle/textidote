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

public class PositionTest
{
	@Test
	public void test1()
	{
		Position p1 = new Position(0, 0);
		Position p2 = new Position(0, 0);
		assertTrue(p1.compareTo(p2) == 0);
	}
	
	@Test
	public void test2()
	{
		Position p1 = new Position(0, 0);
		Position p2 = new Position(0, 1);
		assertTrue(p1.compareTo(p2) < 0);
	}
	
	@Test
	public void test3()
	{
		Position p1 = new Position(0, 1);
		Position p2 = new Position(0, 0);
		assertTrue(p1.compareTo(p2) > 0);
	}
	
	@Test
	public void test4()
	{
		Position p1 = new Position(0, 0);
		Position p2 = new Position(1, 0);
		assertTrue(p1.compareTo(p2) < 0);
	}
	
	@Test
	public void test5()
	{
		Position p1 = new Position(1, 0);
		Position p2 = new Position(0, 0);
		assertTrue(p1.compareTo(p2) > 0);
	}
	
	@Test
	public void testToString()
	{
		Position p1 = new Position(1, 0);
		String s = p1.toString();
		assertNotNull(s);
		assertTrue(s.length() > 0);
	}
}
