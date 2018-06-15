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
package ca.uqac.lif.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Print stream that prints nothing
 * @author Sylvain Hallé
 */
public class NullPrintStream extends PrintStream 
{
	/**
	 * Creates a new null print stream
	 */
	public NullPrintStream() 
	{
		super(new NullByteArrayOutputStream());
	}

	/**
	 * Output stream that prints nothing
	 */
	private static class NullByteArrayOutputStream extends ByteArrayOutputStream 
	{
		@Override
		public void write(int b)
		{
			// do nothing
		}

		@Override
		public void write(byte[] b, int off, int len) 
		{
			// do nothing
		}

		@Override
		public void writeTo(OutputStream out) throws IOException 
		{
			// do nothing
		}
	}

}