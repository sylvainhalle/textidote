/*
    Cornipickle, validation of layout bugs in web applications
    Copyright (C) 2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Print stream with facilities for producing colored text using
 * ANSI escape codes.
 * @author sylvain
 */
public class AnsiPrinter extends PrintStream
{
	/**
	 * Default 16-color scheme for foreground and background text
	 */
	public static enum Color {BLACK, BLUE, GREEN, CYAN, RED, PURPLE, BROWN,
		LIGHT_GRAY, DARK_GRAY, LIGHT_BLUE, LIGHT_GREEN, LIGHT_CYAN, LIGHT_RED,
		LIGHT_PURPLE, YELLOW, WHITE};

		/**
		 * Whether ANSI codes are enabled. If set to false (with
		 * {@link #disableColors()}), behaves like a normal PrintStream
		 */
		protected boolean m_enabled = true;

		/**
		 * Instantiates an AnsiPrinter.
		 * @param out The OutputStream where the printer will send its output
		 */
		public AnsiPrinter(OutputStream out)
		{
			super(out);
		}

		/**
		 * A reader from the standard input
		 */
		private final BufferedReader m_console = new BufferedReader(new InputStreamReader(System.in));

		/**
		 * Enables the output of ANSI codes in the output stream
		 */
		public void enableColors()
		{
			m_enabled = true;
		}

		/**
		 * Disables the output of ANSI codes in the output stream
		 */
		public void disableColors()
		{
			m_enabled = false;
		}


		/**
		 * Sets the foreground color for printed text.
		 * Until this color is changed, the text will be printed using
		 * that color.
		 * @param c The color
		 * @return This printer
		 */
		public AnsiPrinter setForegroundColor(AnsiPrinter.Color c)
		{
			if (!m_enabled)
			{
				return this;
			}
			String to_print = "";
			switch (c)
			{
			case BLACK:
				to_print = "\u001B[2;30m";
				break;
			case RED:
				to_print = "\u001B[2;31m";
				break;
			case GREEN:
				to_print = "\u001B[2;32m";
				break;
			case BROWN:
				to_print = "\u001B[2;33m";
				break;
			case BLUE:
				to_print = "\u001B[2;34m";
				break;
			case PURPLE:
				to_print = "\u001B[2;35m";
				break;
			case CYAN:
				to_print = "\u001B[2;36m";
				break;
			case LIGHT_GRAY:
				to_print = "\u001B[2;37m";
				break;
			case DARK_GRAY:
				to_print = "\u001B[1;30m";
				break;
			case LIGHT_RED:
				to_print = "\u001B[1;31m";
				break;
			case LIGHT_GREEN:
				to_print = "\u001B[1;32m";
				break;
			case YELLOW:
				to_print = "\u001B[1;33m";
				break;
			case LIGHT_BLUE:
				to_print = "\u001B[1;34m";
				break;
			case LIGHT_PURPLE:
				to_print = "\u001B[1;35m";
				break;
			case LIGHT_CYAN:
				to_print = "\u001B[1;36m";
				break;
			case WHITE:
				to_print = "\u001B[1;37m";
				break;
			default:
				// Do nothing
				break;
			}
			printBytes(to_print);
			return this;
		}

		/**
		 * Shortcut to {@link #setForegroundColor(Color)}.
		 * @param c The colour
		 * @return This printer
		 */
		public AnsiPrinter fg(Color c)
		{
			return setForegroundColor(c);
		}

		/**
		 * Sets the foreground color based on its RGB components.
		 * @param r The amount of red (R), between 0 and 255
		 * @param g The amount of green (G), between 0 and 255
		 * @param b The amount of blue (B), between 0 and 255
		 * @return This printer
		 */
		public AnsiPrinter setForegroundColor(int r, int g, int b)
		{
			printBytes("\u001B[38;2;" + r + ";" + g + ";" + b + "m");
			return this;	  
		}

		/**
		 * Sets the background color for printed text.
		 * Until this color is changed, the text will be printed using
		 * that color.
		 * @param c The color
		 * @return This printer
		 */
		public AnsiPrinter setBackgroundColor(AnsiPrinter.Color c)
		{
			if (!m_enabled)
			{
				return this;
			}
			String to_print = "";
			switch (c)
			{
			case BLACK:
				to_print = "\u001B[2;40m";
				break;
			case RED:
				to_print = "\u001B[2;41m";
				break;
			case GREEN:
				to_print = "\u001B[2;42m";
				break;
			case BROWN:
				to_print = "\u001B[2;43m";
				break;
			case BLUE:
				to_print = "\u001B[2;44m";
				break;
			case PURPLE:
				to_print = "\u001B[2;45m";
				break;
			case CYAN:
				to_print = "\u001B[2;46m";
				break;
			case LIGHT_GRAY:
				to_print = "\u001B[2;47m";
				break;
			case DARK_GRAY:
				to_print = "\u001B[1;40m";
				break;
			case LIGHT_RED:
				to_print = "\u001B[1;41m";
				break;
			case LIGHT_GREEN:
				to_print = "\u001B[1;42m";
				break;
			case YELLOW:
				to_print = "\u001B[1;43m";
				break;
			case LIGHT_BLUE:
				to_print = "\u001B[1;44m";
				break;
			case LIGHT_PURPLE:
				to_print = "\u001B[1;45m";
				break;
			case LIGHT_CYAN:
				to_print = "\u001B[1;46m";
				break;
			case WHITE:
				to_print = "\u001B[1;47m";
				break;
			default:
				// Do nothing
				break;
			}
			printBytes(to_print);
			return this;
		}

		/**
		 * Shortcut to {@link #setBackgroundColor(Color)}.
		 * @param c The colour
		 * @return This printer
		 */
		public AnsiPrinter bg(Color c)
		{
			return setBackgroundColor(c);
		}

		/**
		 * Sets the background color based on its RGB components.
		 * @param r The amount of red (R), between 0 and 255
		 * @param g The amount of green (G), between 0 and 255
		 * @param b The amount of blue (B), between 0 and 255
		 * @return This printer
		 */
		public AnsiPrinter setBackgroundColor(int r, int g, int b)
		{
			printBytes("\u001B[48;2;" + r + ";" + g + ";" + b + "m");
			return this;	  
		}

		/**
		 * Resets the colors to their default values
		 * @return This printer
		 */
		public AnsiPrinter resetColors()
		{
			printBytes("\u001B[0m");
			printBytes("\u001B[39m");
			printBytes("\u001B[49m");
			return this;	  
		}

		public AnsiPrinter setUnderscore()
		{
			printBytes("\u001B[4m");
			return this;
		}

		/**
		 * Prints a string to bytes
		 * @param s The string to print
		 */
		private void printBytes(String s)
		{
			try
			{
				out.write(s.getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}	  
		}

		public static String padToLength(String s, int length)
		{
			if (s == null)
			{
				s = "";
			}
			int str_len = s.length();
			if (str_len == length)
			{
				return s;
			}
			if (str_len > length)
			{
				return s.substring(0, length);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(s);
			for (int i = str_len; i < length; i++)
			{
				sb.append(" ");
			}
			return sb.toString();
		}

		/**
		 * Reads a line from the standard input
		 * @return The line
		 */
		public String readLine()
		{
			try
			{
				return m_console.readLine();
			} 
			catch (IOException e)
			{
				Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
			}
			return "";
		}

		/**
		 * Determines if the display of colours is enabled
		 * @return true if colors enabled, false otherwise
		 */
		public boolean colorsEnabled()
		{
			return m_enabled;
		}

		public AnsiPrinter setWhiteOnBlack()
		{
			resetColors();
			setBackgroundColor(Color.BLACK);
			setForegroundColor(Color.LIGHT_GRAY);
			return this;
		}

}
