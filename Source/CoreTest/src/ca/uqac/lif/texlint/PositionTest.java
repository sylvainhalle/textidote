package ca.uqac.lif.texlint;

import static org.junit.Assert.*;

import org.junit.Test;

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
