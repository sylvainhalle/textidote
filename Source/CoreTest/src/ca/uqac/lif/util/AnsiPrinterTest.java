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
package ca.uqac.lif.util;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

import ca.uqac.lif.util.AnsiPrinter;
import static ca.uqac.lif.textidote.as.AnnotatedString.CRLF;

public class AnsiPrinterTest {

    @Test
    public void testColoredHelloWorld() {
        String in = "Colored World";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnsiPrinter printer = new AnsiPrinter(baos);
        for (AnsiPrinter.Color color : AnsiPrinter.Color.values()) { 
            printer.fg(color);
            printer.bg(color);
        }
        printer.setBackgroundColor(255, 255, 255);
        printer.setForegroundColor(222, 0, 0);
        printer.disableColors();
        printer.fg(AnsiPrinter.Color.LIGHT_RED);
        printer.bg(AnsiPrinter.Color.WHITE);
        printer.println(in);
        String output = new String(baos.toByteArray());
        assertContains(in, output);
    }
    
    @Test
    public void testWhiteOnBlackHelloWorld() {
        String in = "White World";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnsiPrinter printer = new AnsiPrinter(baos);
        printer.setWhiteOnBlack();
        assertTrue(printer.colorsEnabled());
        printer.disableColors();
        assertFalse(printer.colorsEnabled());
        printer.println(in);
        String output = new String(baos.toByteArray());
        assertContains(in, output);
    }
    
    @Test
    public void testPadToLength() {
        String in = "Padded World";
        int length = 15;
        String output;
        output = AnsiPrinter.padToLength(in, length);
        assertEquals(in, output.trim());
        assertEquals(length, output.length());
        assertEquals("   ", AnsiPrinter.padToLength(null, 3));
        assertEquals(in, AnsiPrinter.padToLength(in, in.length()));
        assertEquals("Pad", AnsiPrinter.padToLength(in, 3));
    }

    protected static void assertContains(String subs, String s)
    {
        assertTrue("Couldn't locate \""+subs+"\" inside \""+s+"\"", s.indexOf(subs)!=-1);
    }
}
