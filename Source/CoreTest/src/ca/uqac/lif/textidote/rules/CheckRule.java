/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2019  Sylvain Hallé

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
package ca.uqac.lif.textidote.rules;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;

public class CheckRule 
{
	@Test
	public void testToString()
	{
		// Simple test to check that the toString method
		// produces a non-empty string
		Rule r = new DummyRule("foo");
		assertEquals("foo", r.toString());
	}
	
	public static class DummyRule extends Rule
	{
		public DummyRule(String name)
		{
			super(name);
		}
		
		@Override
		public List<Advice> evaluate(AnnotatedString s)
		{
			// Don't care
			return null;
		}
		
		@Override
		public String getDescription()
		{
			// Don't care
			return "";
		}
	}
}
