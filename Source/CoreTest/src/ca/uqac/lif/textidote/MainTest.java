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

import java.io.ByteArrayInputStream;
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
	protected static final String NO_WARNINGS = "Everything is OK";
	@Test
	public void testNoArgs() throws IOException
	{
		InputStream in = new ByteArrayInputStream("".getBytes());
		int ret_code = Main.mainLoop(new String[] {}, in, new NullPrintStream(), new NullPrintStream());
		assertEquals(-7, ret_code);
	}

	@Test
	public void testName() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		String app_name = "unitTest";
		int ret_code = Main.mainLoop(new String[] {"--help", "--name", app_name}, null, new NullPrintStream(), out);
		String output = new String(baos.toByteArray());
		assertEquals(0, ret_code);
		assertContains("Usage", output);
		assertContains(app_name, output);
	}

	@Test
	public void testInvalidArgs() throws IOException
	{
		int ret_code = Main.mainLoop(new String[] {"-"}, null, new NullPrintStream(), new NullPrintStream());
		assertEquals(-1, ret_code);
	}

	@Test(timeout = 1000)
	public void testHelp() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--help"}, null, new NullPrintStream(), out);
		String output = new String(baos.toByteArray());
		assertEquals(0, ret_code);
		assertContains("Usage", output);
	}

	@Test(timeout = 1000)
	public void testHelpQuiet() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--help", "--quiet"}, null, new NullPrintStream(), out);
		String output = new String(baos.toByteArray());
		assertEquals(0, ret_code);
		assertTrue(output, output.trim().isEmpty());
	}

	@Test
	public void testVersion() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--version"}, null, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(0, ret_code);
		assertContains("TeXtidote v", output);
	}

	@Test
	public void test5() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--type", "tex", "--encoding", "utf-8", "--read-all"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains(NO_WARNINGS, output);
		assertEquals(0, ret_code);
	}

	protected static void assertContains(String subs, String s)
	{
		assertTrue("Couldn't locate \""+subs+"\" inside \""+s+"\"", s.indexOf(subs)!=-1);
	}

	protected static void assertNotContains(String subs, String s)
	{
		assertTrue("The string \""+subs+"\" shouldn't be in \""+s+"\"", s.indexOf(subs)==-1);
	}

	@Test
	public void testCleanMarkdown() throws IOException
	{
		String in_path = new File(MainTest.class.getResource("rules/data/test1.md").getFile()).getAbsolutePath();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--clean", in_path}, null, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("Hello  world", output);
		assertEquals(0, ret_code);
	}

	@Test
	public void testCleanMarkdownWithType() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.md");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--type", "md", "--clean"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("Hello  world", output);
		assertEquals(0, ret_code);
	}

	@Test
	public void testMarkdown() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.md");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--type", "md"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertTrue(output.length()>0);
		assertEquals(0, ret_code);
	}


	@Test
	public void testMarkdownWithType() throws IOException
	{
		String in_path = new File(MainTest.class.getResource("rules/data/test1.md").getFile()).getAbsolutePath();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", in_path}, null, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertTrue(output.length()>0);
		assertEquals(0, ret_code);
	}

	@Test
	public void testCleanWithType() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-stacked-0.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--type", "tex", "--clean"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("Foo", output);
		assertContains("Lorem ipsum", output);
		assertEquals(0, ret_code);
	}

	@Test
	public void testCleanWithFileAsArgument() throws IOException
	{
		String in_path = new File(MainTest.class.getResource("rules/data/test1.tex").getFile()).getAbsolutePath();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--clean", in_path}, null, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("Hello", output);
		assertNotContains("comment", output);
		assertEquals(0, ret_code);
	}

	@Test
	public void testCleanWithInvalidFileAsArgument() throws IOException
	{
		String invalid_path = "very-invalid/path";
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--clean", invalid_path}, null, new NullPrintStream(), new NullPrintStream());
		assertEquals(0, ret_code);
	}

	@Test
	public void testInvalidEncoding() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--type", "tex", "--encoding", "invalid584", "--read-all"}, in, new NullPrintStream(), new NullPrintStream());
		assertEquals(9, ret_code);
	}

	@Test
	public void testPlain() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-text.txt");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--type", "txt"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains(NO_WARNINGS, output);
		assertEquals(0, ret_code);
	}

	@Test
	public void testPlainClean() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-text.txt");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--type", "txt", "--clean"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("simple plain file", output);
		assertEquals(0, ret_code);
	}

	@Test
	public void testUnknownType() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		String type = "strange_type";
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--type", type}, in, new NullPrintStream(), out);
		String output = new String(baos.toByteArray());
		assertContains(type, output);
		assertEquals(-3, ret_code);
	}
	
	@Test//(timeout = 2000)
	public void testMissingBeginDocumentWithoutReadAll() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertTrue(output, output.trim().isEmpty());
		assertEquals(-7, ret_code);
	}

	@Test//(timeout = 2000)
	public void test5Html() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-stacked-1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--output", "html", "--read-all"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("Lorem ipsum", output);
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
		assertContains("sh:nsubdiv", output);
		assertTrue(ret_code > 0);
	}

	@Test//(timeout = 2000)
	public void test7() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-subsec-1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--ignore", "sh:seclen,sh:nsubdiv"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains(NO_WARNINGS, output);
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
		assertContains(NO_WARNINGS, output);
		assertEquals(0, ret_code);
	}

	@Test(timeout = 20000)
	public void testCheckWithInvalidDictionary() throws IOException
	{
		String dict_path = "invalid/dictionary-path";
		InputStream in = new ByteArrayInputStream("Hello FooBar".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--check", "en", "--dict", dict_path}, in, new NullPrintStream(), out);
		String output = new String(baos.toByteArray());
		assertContains(dict_path, output);
		assertEquals(-1, ret_code);
	}

	@Test(timeout = 20000)
	public void testCheckWithEmptyDictionary() throws IOException
	{
		String dict_path = new File(MainTest.class.getResource("rules/data/empty.txt").getFile()).getAbsolutePath();
		InputStream in = new ByteArrayInputStream("Hello FooBar".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--check", "en", "--dict", dict_path}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("FooBar", output);
		assertEquals(1, ret_code);
	}

	@Test(timeout = 20000)
	public void testCheckWithDictionary() throws IOException
	{
		String dict_path = new File(MainTest.class.getResource("rules/data/dictionary.txt").getFile()).getAbsolutePath();
		InputStream in = new ByteArrayInputStream("Hello FooBar".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--check", "en", "--dict", dict_path}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains(NO_WARNINGS, output);
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
		assertContains(NO_WARNINGS, output);
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
		assertContains("Hello", output);
		assertContains("World", output);
		assertEquals(0, ret_code);
	}

	@Test
	public void testCleanWithMap() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		String map_file = "test_mapping.txt";
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "--clean", "--map", map_file}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("Hello", output);
		assertContains("World", output);
		assertEquals(0, ret_code);
		File f = new File(map_file);
		assertTrue(f.exists());
		f.delete();
	}

	@Test(timeout = 5000)
	public void testClean2() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test1.md");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--clean", "--read-all", "--type", "md"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertContains("Hello  world", output);
		assertNotContains("comment", output);
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
		assertTrue(output, output.trim().isEmpty());
		assertEquals(0, ret_code);
	}

	@Test
	public void testRemoveMacros() throws IOException
	{
		InputStream in = new ByteArrayInputStream("Hello \\foo world.".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--clean", "--read-all", "--remove-macros", "foo"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(0, ret_code);
		assertEquals("Hello world.", output.trim().toString());	
	}

	@Test(timeout = 5000)
	public void testInput1() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--no-color", "--read-all", "rules/data/test-input1.tex"}, null, out, new NullPrintStream(), MainTest.class);
		String output = new String(baos.toByteArray());
		// TODO: add checks when expanding includes
		assertFalse(output, output.trim().isEmpty());
		assertTrue(ret_code >= 0);
	}
	
	@Test//(timeout = 10000)
	public void testCheck1() throws IOException
	{
		ByteArrayOutputStream baos_out = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos_out);
		ByteArrayOutputStream baos_err = new ByteArrayOutputStream();
		PrintStream err = new PrintStream(baos_err);
		InputStream is = MainTest.class.getResourceAsStream("rules/data/test-input1.tex");
		int ret_code = Main.mainLoop(new String[] {"--check", "fr", "--read-all"}, is, out, err, MainTest.class);
		String output = new String(baos_out.toByteArray());
		// TODO: add checks when expanding includes
		assertFalse(output, output.trim().isEmpty());
		assertTrue(ret_code >= 0);
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
		int ret_code = Main.mainLoop(new String[] {"--check", "fr", "--languagemodel", "/foo", "--read-all"}, is, out, new NullPrintStream(), MainTest.class);
		String output = new String(baos.toByteArray());
		// TODO: add checks when expanding includes
		assertFalse(output, output.trim().isEmpty());
		assertTrue(ret_code >= 0);
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
	public void testCleanWithInvalidReplacement() throws IOException
	{
		String replace_path = "super-invalid-path";
		InputStream in = new ByteArrayInputStream("This is a text.".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--replace", replace_path, "--clean"}, in, new NullPrintStream(), out);
		String output = new String(baos.toByteArray());
		assertContains(replace_path, output);
		assertEquals(-1, ret_code);
	}

	@Test
	public void testCheckWithInvalidReplacement() throws IOException
	{
		String replace_path = "super-invalid-path";
		InputStream in = new ByteArrayInputStream("This is a text.".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--replace", replace_path}, in, new NullPrintStream(), out);
		String output = new String(baos.toByteArray());
		assertContains(replace_path, output);
		assertEquals(-1, ret_code);
	}

	@Test
	public void testCleanWithDummyReplacement() throws IOException
	{
		String replace_path = new File(MainTest.class.getResource("rules/data/replace.txt").getFile()).getAbsolutePath();
		InputStream in = new ByteArrayInputStream("This is a text.".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--replace", replace_path, "--clean"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals("This is a replacement.", output.trim());
		assertEquals(0, ret_code);
	}

	@Test
	public void testNoBreakOnPlainOutput() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-nobreak.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--output", "plain"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(1, ret_code);
		// Check that the no break warning is present
		assertContains("should not break lines manually", output);
	}

	@Test
	public void testNoBreakOnJsonOutput() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-nobreak.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--output", "json"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(1, ret_code);
		// Check that the no break warning is present
		assertContains("should not break lines manually", output);
	}

	@Test
	public void testCheckOnJsonOutput() throws IOException
	{
		InputStream in = new ByteArrayInputStream("Hello woarld.".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--check", "en", "--output", "json"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(1, ret_code);
		// Check that the typo fix is present
		assertContains("world", output);
	}

	@Test
	public void testWithInvalidFileAsArgument() throws IOException
	{
		String invalid_path = "invalid/path/to-file";
		InputStream in = new ByteArrayInputStream("Hello woarld.".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", invalid_path}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(-6, ret_code);
		assertTrue(output, output.trim().isEmpty());
	}

	@Test
	public void testNoBreakWithFileAsArgument() throws IOException
	{
		String in_path = new File(MainTest.class.getResource("rules/data/test-nobreak.tex").getFile()).getAbsolutePath();
		InputStream in = new ByteArrayInputStream("Hello woarld.".getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", in_path}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(1, ret_code);
		assertContains("should not break lines manually", output);
	}

	@Test
	public void testOnUnknownOutput() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-nobreak.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--output", "invalid-ouput-format"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(-1, ret_code);
		assertTrue(output, output.trim().isEmpty());
	}

	@Test
	public void testCiFlag() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/test-nobreak.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--ci", "--output", "singleline"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		assertEquals(0, ret_code);
		// Check that the no break warning is present
		assertContains("should not break lines manually", output);
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
		assertEquals(1, ret_code);
		// Check that the no break warning is present
		assertTrue(output, output.indexOf("<span class=\"highlight")!=-1);
	}

	@Test
	public void testIncludeWithRoot() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--output", "html", "rules/data/childs/child-section.tex"}, null, out, new NullPrintStream(), MainTest.class);
		String output = new String(baos.toByteArray());
		assertEquals(0, ret_code);
		// Check that the desired sections are present
		assertContains("child section", output);
		assertContains("child sibling section", output);
	}

	@Test
	public void testIncludeSeveralTimes() throws IOException
	{
		String in_path = new File(MainTest.class.getResource("rules/data/include-twice.tex").getFile()).getAbsolutePath();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--output", "singleline", "--no-color", in_path}, null, out, new NullPrintStream(), null);
		String output = new String(baos.toByteArray());
		assertContains("section is very short", output);
		assertTrue(ret_code>=0);
	}

	@Test
	public void testTwoRootsAsArguments() throws IOException
	{
		String in_path = new File(MainTest.class.getResource("rules/data/include-twice.tex").getFile()).getAbsolutePath();
		String in_path2 = new File(MainTest.class.getResource("rules/data/root.tex").getFile()).getAbsolutePath();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--output", "singleline", "--no-color", in_path, in_path2}, null, out, new NullPrintStream(), null);
		String output = new String(baos.toByteArray());
		assertEquals(-5, ret_code);
		assertTrue(output, output.trim().isEmpty());
	}

	@Test
	public void testIncludeWithRootAsArgument() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--output", "html", "--root", "rules/data/root.tex", "rules/data/childs/child-section-no-root.tex"}, null, out, new NullPrintStream(), MainTest.class);
		String output = new String(baos.toByteArray());
		assertEquals(0, ret_code);
		// Check that the desired sections are present
		assertContains("child section", output);
		assertContains("child sibling section", output);
	}

	@Test
	public void testIncludeWithRootBothWays() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--read-all", "--root", "rules/data/root.tex", "--output", "html", "rules/data/childs/child-section.tex"}, null, out, new NullPrintStream(), MainTest.class);
		String output = new String(baos.toByteArray());
		assertNotNull(output);
		assertEquals(0, ret_code);
		assertFalse(output.trim().isEmpty());
		// Check that the desired sections are present
		assertTrue(output.indexOf("child section")!=-1);
		assertTrue(output.indexOf("child sibling section")!=-1);
	}

	@Test
	public void testBeamerFile() throws IOException
	{
		InputStream in = MainTest.class.getResourceAsStream("rules/data/beamer.tex");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int ret_code = Main.mainLoop(new String[] {"--check", "en", "--firstlang", "es", "--no-color", "--output", "singleline"}, in, out, new NullPrintStream());
		String output = new String(baos.toByteArray());
		// Check that the correct number of warnings are generated
		assertEquals(2, ret_code);
		// Check that the warnings suggest the correct words
		assertFalse(output.trim().isEmpty());
		String[] lines = output.split("\n");
		assertEquals(2, lines.length);
		assertContains("Beneficial", output);
		assertContains("travelling", output);
	}
}
