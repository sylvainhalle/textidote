/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2023  Sylvain Hallé

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
package ca.uqac.lif.textidote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.io.File;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.util.NullPrintStream;

public class MainTest
{
	@Test(timeout = 1000)
	public void test1() throws IOException
	{
		Main.mainLoop(new String[] {"--help"}, null, new NullPrintStream(), new NullPrintStream());
	}

	@Test(timeout = 1000)
	public void test2() throws IOException
	{
		Main.mainLoop(new String[] {"--help", "--quiet"}, null, new NullPrintStream(), new NullPrintStream());
	}

	@Test(timeout = 1000)
	public void test3() throws IOException
	{
		Main.mainLoop(new String[] {"--help", "--quiet"}, null, new NullPrintStream(), new NullPrintStream());
	}

	@Test(timeout = 1000)
	public void test4() throws IOException
	{
		Main.mainLoop(new String[] {"--version"}, null, new NullPrintStream(), new NullPrintStream());
	}

	@Test//(timeout = 2000)
	public void test5() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(0, ret_code);
	}
	
	@Test//(timeout = 2000)
	public void test10() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(-4, ret_code);
	}

	@Test//(timeout = 2000)
	public void test5Html() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-stacked-1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--output", "html", "--read-all"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertTrue(ret_code > 0);
	}

	@Test(timeout = 2000)
	public void test6() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-subsec-1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertTrue(ret_code > 0);
	}

	@Test//(timeout = 2000)
	public void test7() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-subsec-1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--ignore", "sh:seclen,sh:nsubdiv"}, in, out, new NullPrintStream());
		assertEquals(0, ret_code);
	}

	@Test(timeout = 20000)
	public void test8() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--check", "en"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(0, ret_code);
	}

	@Test(timeout = 20000)
	public void test9() throws IOException
	{
		// We don't test n-gram use, but least check that pointing to a
		// nonexistent/invalid folder is gracefully ignored
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--check", "en", "--languagemodel", "/tmp/foobarbaz"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(0, ret_code);
	}

	@Test(timeout = 5000)
	public void testClean1() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--clean"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(0, ret_code);
		assertFalse(output.trim().isEmpty());
	}

	@Test(timeout = 5000)
	public void testClean2() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.md");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--clean", "--read-all", "--type", "md"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(0, ret_code);
	}

	@Test(timeout = 5000)
	public void testClean3() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--clean", "--read-all", "--remove", "itemize"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(0, ret_code);
		assertTrue(output.trim().isEmpty());
	}

	@Test(timeout = 5000)
	public void testInput1() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "rules/data/test-input1.tex"}, null, out, new NullPrintStream(), MainTest.class);
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertTrue(ret_code > 0);
		assertFalse(output.trim().isEmpty());
	}
	
	@Test//(timeout = 10000)
	public void testCheck1() throws IOException
	{
		ByteArrayOutputStream baos_out = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos_out);
		ByteArrayOutputStream baos_err = new ByteArrayOutputStream();
		PrintStream err = new PrintStream(baos_err);
		InputStream is = MainTest.class.getResourceAsStream("rules/data/test-input1.tex");
		Main.mainLoop(new String[] {"--check", "fr", "--read-all"}, is, out, err, MainTest.class);
		String output = new String(baos_out.toByteArray());
		assertNotNull(output);
		assertFalse(output.trim().isEmpty());
	}

	@Test(timeout = 5000)
	public void testNgrams1() throws IOException
	{
		// We instruct Textidote to read from a non-existent n-gram directory (.)
		// This will still trigger the initialization of the n-gram
		// functionality and check that it works
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		InputStream is = MainTest.class.getResourceAsStream("rules/data/test-input1.tex");
		Main.mainLoop(new String[] {"--check", "fr", "--languagemodel", "/foo", "--read-all"}, is, out, new NullPrintStream(), MainTest.class);
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertFalse(output.trim().isEmpty());
	}

	@Test
	public void testReadArguments1()
	{
		String arg_string = "--foo --bar";
		Scanner scanner = new Scanner(arg_string);
		String[] args = Main.readArguments(scanner);
		assertEquals(2, args.length);
		assertEquals("--foo", args[0]);
		assertEquals("--bar", args[1]);
	}

	@Test
	public void testReadArguments2()
	{
		String arg_string = "--foo\n--bar";
		Scanner scanner = new Scanner(arg_string);
		String[] args = Main.readArguments(scanner);
		assertEquals(2, args.length);
		assertEquals("--foo", args[0]);
		assertEquals("--bar", args[1]);
	}

	@Test
	public void testReadArguments3()
	{
		String arg_string = "--foo \"Some\" --bar";
		Scanner scanner = new Scanner(arg_string);
		String[] args = Main.readArguments(scanner);
		assertEquals(3, args.length);
		assertEquals("--foo", args[0]);
		assertEquals("Some", args[1]);
		assertEquals("--bar", args[2]);
	}

	@Test
	public void testReadArguments4()
	{
		String arg_string = "--foo \"Some Thing\" --bar";
		Scanner scanner = new Scanner(arg_string);
		String[] args = Main.readArguments(scanner);
		assertEquals(3, args.length);
		assertEquals("--foo", args[0]);
		assertEquals("Some Thing", args[1]);
		assertEquals("--bar", args[2]);
	}

	@Test
	public void testReadArguments5()
	{
		String arg_string = "#--foo\n\n--bar";
		Scanner scanner = new Scanner(arg_string);
		String[] args = Main.readArguments(scanner);
		assertEquals(1, args.length);
		assertEquals("--bar", args[0]);
	}

	@Test
	public void testReadArguments6()
	{
		String arg_string = "--foo\n\"Some\n#\nThing\"";
		Scanner scanner = new Scanner(arg_string);
		String[] args = Main.readArguments(scanner);
		assertEquals(2, args.length);
		assertEquals("--foo", args[0]);
		assertEquals("Some Thing", args[1]);
	}

	@Test
	public void testNoBreakOnHTMLWithDummyReplacement() throws IOException
	{
		File replace_file = new File(MainTest.class.getResource("rules/data/replace.txt").getFile());
		String replace_path = replace_file.getAbsolutePath();
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-nobreak.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--replace", replace_path, "--output", "html"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(1, ret_code);
		assertFalse(output.trim().isEmpty());
		// Check that the no break warning is present
		assertTrue(output.indexOf("<span class=\"highlight")!=-1);
	}
}
