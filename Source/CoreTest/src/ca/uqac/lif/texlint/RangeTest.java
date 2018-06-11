package ca.uqac.lif.texlint;

import static org.junit.Assert.*;

import org.junit.Test;

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
