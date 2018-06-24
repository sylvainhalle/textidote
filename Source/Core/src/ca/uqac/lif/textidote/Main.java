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
package ca.uqac.lif.textidote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Range;
import ca.uqac.lif.textidote.render.AnsiAdviceRenderer;
import ca.uqac.lif.textidote.render.HtmlAdviceRenderer;
import ca.uqac.lif.textidote.rules.CheckCaptions;
import ca.uqac.lif.textidote.rules.CheckFigurePaths;
import ca.uqac.lif.textidote.rules.CheckFigureReferences;
import ca.uqac.lif.textidote.rules.CheckLanguage;
import ca.uqac.lif.textidote.rules.CheckNoBreak;
import ca.uqac.lif.textidote.rules.CheckSubsectionSize;
import ca.uqac.lif.textidote.rules.CheckSubsections;
import ca.uqac.lif.textidote.rules.LanguageFactory;
import ca.uqac.lif.textidote.rules.RegexRule;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;
import ca.uqac.lif.util.NullPrintStream;

/**
 * Command-line interface for TeXtidote.
 * @author Sylvain Hallé
 */
public class Main 
{
	/**
	 * Filename where the regex rules are stored
	 */
	protected static final String REGEX_FILENAME = "rules/regex.csv";

	/**
	 * Filename where the regex rules are stored
	 */
	protected static final String REGEX_FILENAME_DETEX = "rules/regex-detex.csv";

	/**
	 * A version string
	 */
	protected static final String VERSION_STRING = "0.3";
	
	/**
	 * The name of the Aspell dictionary file to look for in a folder
	 */
	protected static final String ASPELL_DICT_FILENAME = ".aspell.XX.pws";
	
	/**
	 * Main method
	 * @param args Command-line arguments
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		System.exit(mainLoop(args));
	}

	/**
	 * Main method
	 * @param args Command-line arguments
	 * @throws IOException 
	 */
	public static int mainLoop(String[] args) throws IOException
	{
		// Setup command line parser and arguents
		CliParser cli_parser = new CliParser();
		cli_parser.addArgument(new Argument().withLongName("html").withDescription("Formats the report as HTML"));
		cli_parser.addArgument(new Argument().withLongName("no-color").withDescription("Disables colors in ANSI printing"));
		cli_parser.addArgument(new Argument().withLongName("check").withArgument("lang").withDescription("Checks grammar in language lang"));
		cli_parser.addArgument(new Argument().withLongName("dict").withArgument("file").withDescription("Load dictionary from file"));
		cli_parser.addArgument(new Argument().withLongName("detex").withDescription("Detex input file"));
		cli_parser.addArgument(new Argument().withLongName("map").withArgument("file").withDescription("Output correspondence map to file"));
		cli_parser.addArgument(new Argument().withLongName("read-all").withDescription("Don't ignore lines before \\begin{document}"));
		cli_parser.addArgument(new Argument().withLongName("quiet").withDescription("Don't print any message"));
		cli_parser.addArgument(new Argument().withLongName("help").withDescription("Show command line usage"));
		ArgumentMap map = cli_parser.parse(args);
		if (map == null)
		{
			cli_parser.printHelp("Usage: java -jar textidote.jar [options] file1 [file2 ...]", System.err);
			return -1;
		}
		boolean read_all = false;
		if (map.hasOption("read-all"))
		{
			read_all = true;
		}
		boolean enable_colors = true;
		if (map.hasOption("no-color"))
		{
			enable_colors = false;
		}
		AnsiPrinter stdout = new AnsiPrinter(System.out);
		AnsiPrinter stderr = null;
		if (map.hasOption("quiet"))
		{
			stderr = new AnsiPrinter(new NullPrintStream());
		}
		else
		{
			stderr = new AnsiPrinter(System.err);
		}
		assert stderr != null;
		printGreeting(stderr, enable_colors);
		if (map.hasOption("help"))
		{
			cli_parser.printHelp("Usage: java -jar textidote.jar [options] file1 [file2 ...]", stderr);
			stdout.close();
			return 0;
		}
		
		// Only detex input
		if (map.hasOption("detex"))
		{
			Detexer detexer = new Detexer();
			detexer.setIgnoreBeforeDocument(!read_all);
			List<String> filenames = map.getOthers();
			if (filenames.isEmpty())
			{
				System.err.println("No filename is specified");
				System.err.println("");
				cli_parser.printHelp("Usage: java -jar textidote.jar [options] file1 [file2 ...]", System.err);
				stdout.close();
				return 1;
			}
			for (String filename : filenames)
			{
				File f = new File(filename);
				if (!f.exists())
				{
					stderr.println("File " + filename + " not found (skipping)");
					continue;
				}
				Scanner scanner = null;
				try 
				{
					scanner = new Scanner(f);
					AnnotatedString s = AnnotatedString.read(scanner);
					s.setResourceName(filename);
					AnnotatedString ds = detexer.detex(s);
					stdout.println(ds);
					if (map.hasOption("map"))
					{
						File map_file = new File(map.getOptionValue("map"));
						FileOutputStream fos = new FileOutputStream(map_file);
						PrintStream ps_fos = new PrintStream(fos);
						printMap(ps_fos, ds.getMap());
						ps_fos.close();
					}
				}
				catch (FileNotFoundException e) 
				{
					// Nothing to do; we already trapped this
				}
				finally
				{
					if (scanner != null)
					{
						scanner.close();
					}
				}
			}
			stdout.close();
			return 0;
		}

		// Create a linter
		long start_time = System.currentTimeMillis();
		Linter linter = new Linter();
		populateRules(linter);
		linter.getDetexer().setIgnoreBeforeDocument(!read_all);

		// Do we check the language?
		if (map.hasOption("check"))
		{
			String lang_s = map.getOptionValue("check");
			// Try to read dictionary from an Aspell file
			Set<String> dictionary = new HashSet<String>();
			try
			{
				String dict_filename = ASPELL_DICT_FILENAME.replace("XX", lang_s);
				if (dictionary.addAll(readDictionary(dict_filename)))
				{
					stderr.println("Found local Aspell dictionary");
				}
			}
			catch (FileNotFoundException e)
			{
				// Do nothing
			}
			if (map.hasOption("dict"))
			{
				try
				{
					dictionary.addAll(readDictionary(map.getOptionValue("dict")));
				}
				catch (FileNotFoundException e)
				{
					stderr.println("Dictionary not found: " + map.getOptionValue("dict"));
				}
			}
			try
			{
				linter.addDetexed(new CheckLanguage(LanguageFactory.getLanguageFromString(lang_s), dictionary));
			}
			catch (CheckLanguage.UnsupportedLanguageException e)
			{
				stderr.println("Unknown language: " + map.getOptionValue("check"));
				stdout.close();
				return -1;
			}
		}

		// Process files
		List<Advice> all_advice = new ArrayList<Advice>();
		List<String> filenames = map.getOthers();
		if (filenames.isEmpty())
		{
			System.err.println("No filename is specified");
			System.err.println("");
			cli_parser.printHelp("Usage: java -jar textidote.jar [options] file1 [file2 ...]", System.err);
			stdout.close();
			return 1;
		}
		AnnotatedString last_string = null;
		for (String filename : filenames)
		{
			File f = new File(filename);
			if (!f.exists())
			{
				stderr.println("File " + filename + " not found (skipping)");
				continue;
			}
			Scanner scanner = null;
			try 
			{
				scanner = new Scanner(f);
				last_string = AnnotatedString.read(scanner);
				last_string.setResourceName(filename);
				all_advice.addAll(linter.evaluateAll(last_string));
			}
			catch (FileNotFoundException e) 
			{
				// Nothing to do; we already trapped this
			}
			finally
			{
				if (scanner != null)
				{
					scanner.close();
				}
			}
		}
		if (last_string == null)
		{
			// No file was processed
			stdout.close();
			return -1;
		}
		long end_time = System.currentTimeMillis();
		stderr.println("Found " + all_advice.size() + " warning(s)");
		stderr.println("Total analysis time: " + ((end_time - start_time) / 1000) + " second(s)");
		stderr.println();

		// Render advice
		AdviceRenderer renderer = null;
		if (map.hasOption("html"))
		{
			stdout.disableColors();
			renderer = new HtmlAdviceRenderer(stdout, last_string);
		}
		else
		{
			if (enable_colors)
			{
				stdout.enableColors();
			}
			else
			{
				stdout.disableColors();
			}
			renderer = new AnsiAdviceRenderer(stdout);
		}
		renderer.render(all_advice);

		// The exit code is the number of warnings raised
		return all_advice.size();
	}

	protected static void printGreeting(AnsiPrinter out, boolean enable_colors)
	{
		out.println("TeXtidote v" + VERSION_STRING + " - A linter for LaTeX documents");
		out.println("(C) 2018 Sylvain Hallé - All rights reserved");
		out.println();
	}
	
	/**
	 * Adds the rules to the linter
	 * @param linter The linter to configure
	 */
	protected static void populateRules(Linter linter)
	{
		linter.add(readRules(REGEX_FILENAME));
		linter.addDetexed(readRules(REGEX_FILENAME_DETEX));
		linter.add(new CheckFigureReferences());
		linter.add(new CheckFigurePaths());
		linter.add(new CheckCaptions());
		linter.add(new CheckSubsections());
		linter.add(new CheckSubsectionSize());
		linter.add(new CheckNoBreak());
	}

	protected static List<Rule> readRules(String filename)
	{
		List<Rule> list = new ArrayList<Rule>();
		Scanner scanner = new Scanner(Main.class.getResourceAsStream(filename));
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			if (line.trim().isEmpty() || line.startsWith("#"))
			{
				continue;
			}
			String[] parts = line.split("\\t+");
			if (parts.length < 3)
			{
				// Just ignore
				continue;
			}
			if (parts.length == 3)
			{
				RegexRule rr = new RegexRule(parts[0], parts[1], parts[2]);
				list.add(rr);
			}
			if (parts.length == 4)
			{
				RegexRule rr = new RegexRule(parts[0], parts[1], parts[2], parts[3]);
				list.add(rr);
			}
		}
		scanner.close();
		return list;
	}

	/**
	 * Reads a list of word from an Aspell-generated file
	 * @return A set of words read from the file
	 * @throws FileNotFoundException Thrown if file not found
	 */
	/*@ non_null @*/ protected static Set<String> readDictionary(String filename) throws FileNotFoundException
	{
		Set<String> dict = new HashSet<String>();
		File f = new File(filename);
		Scanner sc = null;
		sc = new Scanner(f);
		while (sc.hasNextLine())
		{
			String line = sc.nextLine();
			dict.add(line.trim());
		} 
		sc.close();
		return dict;
	}
	
	/**
	 * Prints the contents of an associative map
	 * @param ps The print stream to print to
	 * @param map The associative map to print
	 */
	protected static void printMap(PrintStream ps, Map<Range,Range> map)
	{
		List<Range> keys = new ArrayList<Range>(map.size());
		keys.addAll(map.keySet());
		Collections.sort(keys);
		for (Range key : keys)
		{
			ps.print(key);
			ps.print("=");
			ps.print(map.get(key));
			ps.println();
		}
	}
}
