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

import java.io.IOException;

import org.junit.Test;

import ca.uqac.lif.textidote.Main;

public class MainTest 
{
	@Test(timeout = 1000)
	public void test1() throws IOException
	{
		Main.mainLoop(new String[] {"--help"});
	}
	
	@Test(timeout = 1000)
	public void test2() throws IOException
	{
		Main.mainLoop(new String[] {"--help", "--quiet"});
	}
}
