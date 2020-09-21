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
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Range;
import ca.uqac.lif.textidote.cleaning.CompositeCleaner;
import ca.uqac.lif.textidote.cleaning.ReplacementCleaner;
import ca.uqac.lif.textidote.cleaning.TextCleanerException;
import ca.uqac.lif.textidote.cleaning.latex.LatexCleaner;
import ca.uqac.lif.textidote.cleaning.markdown.MarkdownCleaner;
import ca.uqac.lif.textidote.render.AnsiAdviceRenderer;
import ca.uqac.lif.textidote.render.HtmlAdviceRenderer;
import ca.uqac.lif.textidote.render.JsonAdviceRenderer;
import ca.uqac.lif.textidote.render.SinglelineAdviceRenderer;
import ca.uqac.lif.textidote.rules.CheckCaptions;
import ca.uqac.lif.textidote.rules.CheckCiteMix;
import ca.uqac.lif.textidote.rules.CheckFigurePaths;
import ca.uqac.lif.textidote.rules.CheckFigureReferences;
import ca.uqac.lif.textidote.rules.CheckLanguage;
import ca.uqac.lif.textidote.rules.CheckNoBreak;
import ca.uqac.lif.textidote.rules.CheckStackedHeadings;
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
	public static final String REGEX_FILENAME = "rules/regex.csv";

	/**
	 * Filename where the regex rules are stored
	 */
	public static final String REGEX_FILENAME_DETEX = "rules/regex-detex.csv";

	/**
	 * A version string
	 */
	public static final String VERSION_STRING = "0.8.1";

	/**
	 * The name of the Aspell dictionary file to look for in a folder
	 */
	protected static final String ASPELL_DICT_FILENAME = ".aspell.XX.pws";

	/**
	 * The name of the optional file containing command line parameters
	 */
	protected static final String PARAM_FILENAME = ".textidote";

	/**
	 * The OS-dependent path separator
	 */
	protected static final transient String PATH_SEP = File.separator;

	/**
	 * Main method. This method simply calls the static method
	 * {@link #main(String[]) mainLoop()},
	 * and exits with the return value of that method. The reason for this is
	 * is that we can test the main loop, and the test is not interrupted
	 * by calls to {@code System.exit}.
	 * @param args Command-line arguments
	 * @throws IOException Thrown if some file cannot be found or open
	 */
	@SuppressWarnings({"squid:S106"})
	public static void main(String[] args) throws IOException
	{
		System.exit(mainLoop(args, System.in, System.out, System.err));
	}

	/**
	 * Delegate method of {@link #mainLoop(String[], InputStream, PrintStream, PrintStream, Class)}.
	 * @param args Command-line arguments
	 * @param in A stream corresponding to the standard input
	 * @param out A stream corresponding to the standard output
	 * @param err A stream corresponding to the standard error
	 * @return An exit code
	 * @throws IOException Thrown if some file cannot be found or open
	 */
	public static int mainLoop(String[] args, InputStream in, PrintStream out, PrintStream err) throws IOException
	{
		return mainLoop(args, in, out, err, null);
	}

	/**
	 * Main method. The {@code base_class} argument can be set to a non-null
	 * value to indicate that the loop is being run inside a unit test.
	 * When given a filename, the loop will fetch it using
	 * {@link Class#getResourceAsStream(String)} instead of through a
	 * {@link File} object.
	 * @param args Command-line arguments
	 * @param in A stream corresponding to the standard input
	 * @param out A stream corresponding to the standard output
	 * @param err A stream corresponding to the standard error
	 * @param base_class Unit tests can pass a {@code Class} object to indicate
	 * that the loop is being run inside a unit test.
	 * @return An exit code
	 * @throws IOException Thrown if some file cannot be found or open
	 */
	public static int mainLoop(String[] args, InputStream in, PrintStream out, PrintStream err, Class<?> base_class) throws IOException
	{
		// Store input type
		Linter.Language input_type = Linter.Language.UNSPECIFIED;

		// Setup command line parser and arguents
		CliParser cli_parser = new CliParser();
		cli_parser.addArgument(new Argument().withLongName("check").withArgument("lang").withDescription("Checks grammar in language lang"));
		cli_parser.addArgument(new Argument().withLongName("clean").withDescription("Remove markup from input file"));
		cli_parser.addArgument(new Argument().withLongName("dict").withArgument("file").withDescription("Load dictionary from file"));
		cli_parser.addArgument(new Argument().withLongName("help").withDescription("\tShow command line usage"));
		cli_parser.addArgument(new Argument().withLongName("ignore").withArgument("rules").withDescription("Ignore rules"));
		cli_parser.addArgument(new Argument().withLongName("languagemodel").withArgument("dir").withDescription("Use n-grams data from dir"));
		cli_parser.addArgument(new Argument().withLongName("map").withArgument("file").withDescription("Output correspondence map to file"));
		cli_parser.addArgument(new Argument().withLongName("name").withArgument("n").withDescription("Use n as app name when printing usage"));
		cli_parser.addArgument(new Argument().withLongName("no-color").withDescription("Disables colors in ANSI printing"));
		cli_parser.addArgument(new Argument().withLongName("no-config").withDescription("Ignore config file if any"));
		cli_parser.addArgument(new Argument().withLongName("quiet").withDescription("Don't print any message"));
		cli_parser.addArgument(new Argument().withLongName("read-all").withDescription("Don't ignore lines before \\begin{document}"));
		cli_parser.addArgument(new Argument().withLongName("remove").withArgument("envs").withDescription("Remove LaTeX environments envs"));
		cli_parser.addArgument(new Argument().withLongName("remove-macros").withArgument("macs").withDescription("Remove LaTeX macros macs"));
		cli_parser.addArgument(new Argument().withLongName("replace").withArgument("file").withDescription("Apply replacement patterns from file"));
		cli_parser.addArgument(new Argument().withLongName("type").withArgument("x").withDescription("Input is of type x (tex or md)"));
		cli_parser.addArgument(new Argument().withLongName("version").withDescription("Show version number"));
		cli_parser.addArgument(new Argument().withLongName("output").withArgument("method").withDescription("Output as plain (default), json, html, or singleline"));
		cli_parser.addArgument(new Argument().withLongName("ci").withDescription("Ignores the return code for CI usage"));

		// Check if we are using textidote in a CI tool
		boolean usingCI = false;

		// Check if there is a parameter filename
		ArgumentMap map = null;
		File param_file = new File(PARAM_FILENAME);
		if (param_file.exists())
		{
			Scanner f_c_scan = new Scanner(param_file);
			String[] f_c_args = readArguments(f_c_scan);
			map = cli_parser.parse(f_c_args);
		}
		ArgumentMap map_cline = cli_parser.parse(args);
		if (map == null)
		{
			// If no arguments were read from a file, just use those from
			// the actual command line
			map = map_cline;
		}
		else
		{
			if (map_cline.hasOption("no-config"))
			{
				// Just use command line args
				map = map_cline;
			}
			else
			{
				// Merge CLI and args from file
				map.putAll(map_cline);
			}
		}

		// Process command line arguments
		String app_name = "java -jar textidote.jar";
		if (map == null)
		{
			cli_parser.printHelp("Usage: " + app_name + " [options] file1 [file2 ...]", err);
			return -1;
		}
		if (map.hasOption("name"))
		{
			app_name = map.getOptionValue("name");
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
		AnsiPrinter stdout = new AnsiPrinter(out);
		AnsiPrinter stderr = null;
		if (map.hasOption("version"))
		{
			printGreeting(stdout);
			return 0;
		}
		if (map.hasOption("quiet"))
		{
			stderr = new AnsiPrinter(new NullPrintStream());
		}
		else
		{
			stderr = new AnsiPrinter(err);
		}
		assert stderr != null;
		// User has specified rules to ignore
		List<String> rule_blacklist = new ArrayList<String>();
		if (map.hasOption("ignore"))
		{
			String[] ids = map.getOptionValue("ignore").split(",");
			for (String id : ids)
			{
				rule_blacklist.add(id);
			}
		}
		// User has specified environments to remove
		List<String> env_blacklist = new ArrayList<String>();
		if (map.hasOption("remove"))
		{
			String[] ids = map.getOptionValue("remove").split(",");
			for (String id : ids)
			{
				env_blacklist.add(id);
			}
		}
		// User has specified environments to remove
		List<String> mac_blacklist = new ArrayList<String>();
		if (map.hasOption("remove-macros"))
		{
			String[] ids = map.getOptionValue("remove-macros").split(",");
			for (String id : ids)
			{
				mac_blacklist.add(id);
			}
		}
		// User uses n-gram
		String ngram_dir = "";
		File f_ngram_dir = null;
		if (map.hasOption("languagemodel"))
		{
			ngram_dir = map.getOptionValue("languagemodel");
			f_ngram_dir = new File(ngram_dir);
			if (!f_ngram_dir.exists())
			{
				stderr.println("N-gram directory " + ngram_dir + " not found. N-gram rules will be ignored.");
				f_ngram_dir = null;
			}
			else if (!f_ngram_dir.isDirectory())
			{
				stderr.println("N-gram path " + ngram_dir + " is not a directory. N-gram rules will be ignored.");
				f_ngram_dir = null;
			}
			else
			{
				stderr.println("Using N-grams from " + ngram_dir);
			}
		}
		printGreeting(stderr);
		if (map.hasOption("help"))
		{
			cli_parser.printHelp("Usage: " + app_name + " [options] file1 [file2 ...]", stderr);
			stdout.close();
			return 0;
		}
		if (map.hasOption("type"))
		{
			String type = map.getOptionValue("type");
			if (type.compareToIgnoreCase("md") == 0)
			{
				input_type = Linter.Language.MARKDOWN;
			}
			else if (type.compareToIgnoreCase("tex") == 0)
			{
				input_type = Linter.Language.LATEX;
			}
			else if (type.compareToIgnoreCase("txt") == 0)
			{
				input_type = Linter.Language.TEXT;
			}
		}

		// Only detex input
		if (map.hasOption("clean"))
		{
			CompositeCleaner cleaner = new CompositeCleaner();
			if (map.hasOption("replace"))
			{
				String replacement_filename = map.getOptionValue("replace");
				File f = new File(replacement_filename);
				if (!f.exists())
				{
					stderr.println("Replacement file " + replacement_filename + " not found");
				}
				else
				{
					Scanner scanner = new Scanner(f);
					cleaner.add(ReplacementCleaner.create(scanner));
					stderr.println("Using replacement file " + replacement_filename);
				}
			}
			List<String> filenames = map.getOthers();
			if (filenames.isEmpty())
			{
				filenames.add("--"); // This indicates: read from stdin
			}
			for (String filename : filenames)
			{
				Scanner scanner = null;
				try
				{
					File f = new File(filename);
					if (filename.compareTo("--") == 0)
					{
						// Open scanner on stdin
						scanner = new Scanner(in);
					}
					else
					{
						if (!f.exists())
						{
							stderr.println("File " + filename + " not found (skipping)");
							continue;
						}
						else
						{
							scanner = new Scanner(f);
						}
					}
					// Create cleaner based on file extension
					CompositeCleaner c_file = new CompositeCleaner(cleaner);
					if (input_type == Linter.Language.LATEX || (filename.compareTo("--") == 0 && input_type == Linter.Language.UNSPECIFIED) || filename.endsWith(".tex"))
					{
						// LaTeX file
						LatexCleaner latex_cleaner = new LatexCleaner();
						latex_cleaner.setIgnoreBeforeDocument(!read_all);
						latex_cleaner.ignoreEnvironments(env_blacklist);
						latex_cleaner.ignoreMacros(mac_blacklist);
						c_file.add(latex_cleaner);
					}
					else if (input_type == Linter.Language.MARKDOWN || filename.endsWith(".md"))
					{
						MarkdownCleaner markdown_cleaner = new MarkdownCleaner();
						c_file.add(markdown_cleaner);
					}
					AnnotatedString s = AnnotatedString.read(scanner);
					s.setResourceName(filename);
					AnnotatedString ds = c_file.clean(s);
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
				catch (TextCleanerException e)
				{
					stderr.print(e.getMessage());
					return -1;
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
		CompositeCleaner cleaner = new CompositeCleaner();
		if (map.hasOption("replace"))
		{
			String replacement_filename = map.getOptionValue("replace");
			File f = new File(replacement_filename);
			if (!f.exists())
			{
				stderr.println("Replacement file " + replacement_filename + " not found");
			}
			else
			{
				Scanner scanner = new Scanner(f);
				cleaner.add(ReplacementCleaner.create(scanner));
				stderr.println("Using replacement file " + replacement_filename);
			}
		}

		// Do we check the language?
		List<String> dictionary = new ArrayList<String>();
		String lang_s = "";
		if (map.hasOption("check"))
		{
			lang_s = map.getOptionValue("check");
			// Try to read dictionary from an Aspell file
			try
			{
				String dict_filename = ASPELL_DICT_FILENAME.replace("XX", lang_s);
				Set<String> dict = readDictionary(dict_filename);
				if (dictionary.addAll(dict))
				{
					stderr.println("Found local Aspell dictionary in " + dict_filename);
				}
				if (dict.isEmpty())
				{
					stderr.println("Warning: nothing read from local dictionary. Is the file written with the proper encoding?");
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
					Set<String> dict = readDictionary(map.getOptionValue("dict"));
					dictionary.addAll(dict);
					if (dict.isEmpty())
					{
						stderr.println("Warning: nothing read from dictionary file. Is the file written with the proper encoding?");
					}
				}
				catch (FileNotFoundException e)
				{
					stderr.println("Dictionary not found: " + map.getOptionValue("dict"));
				}
			}
		}

		// Setup the advice renderer
		if (enable_colors)
		{
			stdout.enableColors();
		}
		else
		{
			stdout.disableColors();
		}
		AdviceRenderer renderer = null;
		if (map.hasOption("output"))
		{
			String output_method = map.getOptionValue("output");
			if (output_method.compareToIgnoreCase("plain") == 0)
			{
				renderer = new AnsiAdviceRenderer(stdout);
			}
			else if (output_method.compareToIgnoreCase("html") == 0)
			{
				stdout.disableColors();
				renderer = new HtmlAdviceRenderer(stdout);
			}
			else if (output_method.compareToIgnoreCase("singleline") == 0)
			{
				renderer = new SinglelineAdviceRenderer(stdout);
			}
			else if (output_method.compareToIgnoreCase("json") == 0)
			{
				stdout.disableColors();
				renderer = new JsonAdviceRenderer(stdout, lang_s);
			}
		}
		else
		{
			renderer = new AnsiAdviceRenderer(stdout);
		}
		if (map.hasOption("ci"))
		{
			usingCI = true;
		}

		// Process files
		int num_advice = 0;
		int num_files = 0;
		List<String> cmd_filenames = map.getOthers();
		List<String> filenames = map.getOthers();
		if (filenames.isEmpty())
		{
			filenames.add("--"); // This indicates: read from stdin
			cmd_filenames.add("--");
		}
		Queue<String> filename_queue = new ArrayDeque<String>();
		Set<String> processed_filenames = new HashSet<String>();
		filename_queue.addAll(filenames);
		String top_level_filename = null;
		while (!filename_queue.isEmpty())
		{
			String filename = filename_queue.remove();
			if (processed_filenames.contains(filename))
			{
				continue;
			}
			processed_filenames.add(filename);
			if (top_level_filename == null || cmd_filenames.contains(filename))
			{
				// This is a top level filename
				top_level_filename = filename;
			}
			Scanner scanner = null;
			try
			{
				if (filename.compareTo("--") == 0)
				{
					// Open scanner on stdin
					scanner = new Scanner(in);
				}
				else
				{
					if (base_class != null)
					{
						InputStream is = base_class.getResourceAsStream(filename);
						if (is == null)
						{
							stderr.println("File " + filename + " not found (skipping)");
							continue;
						}
						scanner = new Scanner(is);
					}
					else
					{
						File f = new File(filename);
						if (!f.exists())
						{
							stderr.println("File " + filename + " not found (skipping)");
							continue;
						}
						scanner = new Scanner(f);
					}
				}
				num_files++;
				CompositeCleaner c_cleaner = new CompositeCleaner(cleaner);
				Linter linter = null;
				if (input_type == Linter.Language.MARKDOWN || filename.endsWith(".md"))
				{
					MarkdownCleaner markdown_cleaner = new MarkdownCleaner();
					linter = new Linter(c_cleaner);
					c_cleaner.add(markdown_cleaner);
					linter = new Linter(c_cleaner);
					populateMarkdownRules(linter);
					linter.addToBlacklist(rule_blacklist);
				}
				else
				{
					LatexCleaner latex_cleaner = new LatexCleaner();
					if (cmd_filenames.contains(filename))
					{
						latex_cleaner.setIgnoreBeforeDocument(!read_all);
					}
					else
					{
						latex_cleaner.setIgnoreBeforeDocument(false);
					}
					latex_cleaner.ignoreEnvironments(env_blacklist);
					latex_cleaner.ignoreMacros(mac_blacklist);
					c_cleaner.add(latex_cleaner);
					linter = new Linter(c_cleaner);
					populateLatexRules(linter);
					linter.addToBlacklist(rule_blacklist);
				}
				if (!lang_s.isEmpty())
				{
					try
					{
						CheckLanguage cl = new CheckLanguage(LanguageFactory.getLanguageFromString(lang_s), dictionary);
						if (f_ngram_dir != null)
						{
							cl.activateLanguageModelRules(f_ngram_dir);
						}
						linter.addCleaned(cl);
					}
					catch (CheckLanguage.FolderNotFoundException e)
					{
						stderr.println("Cannot open N-gram directory " + ngram_dir + ". N-gram rules will be ignored.");
					}
					catch (CheckLanguage.IncorrectFolderStructureException e)
					{
						stderr.println(e.getMessage().replaceAll("[\n\r]+", " ") + " N-gram rules will be ignored.");
					}
					catch (CheckLanguage.UnsupportedLanguageException e)
					{
						stderr.println("Unknown language: " + map.getOptionValue("check"));
						stdout.close();
						return -1;
					}
				}
				AnnotatedString last_string = AnnotatedString.read(scanner);
				last_string.setResourceName(filename);
				List<Advice> all_advice = linter.evaluateAll(last_string);
				renderer.addAdvice(filename, last_string, all_advice);
				num_advice += all_advice.size();
				int added = addInnerFilesToQueue(c_cleaner.getInnerFiles(), processed_filenames, filename_queue, top_level_filename);
				if (added > 0 && cmd_filenames.size() > 1)
				{
					// Corner case where file checking does not work
					stderr.println("Warning: one of the input files refers to sub-files, and");
					stderr.println("more than one file is specified on the command line. When");
					stderr.println("using sub-files, you should provide a single root document.");
					return -5;
				}
			}
			catch (LinterException e)
			{
				stderr.print(e.getMessage());
				return -1;
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
		if (num_files == 0)
		{
			// No file was processed
			stdout.close();
			return -1;
		}
		long end_time = System.currentTimeMillis();
		stderr.println("Found " + num_advice + " warning(s)");
		stderr.println("Total analysis time: " + ((end_time - start_time) / 1000) + " second(s)");
		stderr.println();

		// Render all the advice
		renderer.render();

		// The exit code is the number of warnings raised
		return usingCI ? 0 : num_advice;
	}

	/**
	 * Prints a simple greeting on a command line
	 * @param out The print stream to print on
	 */
	protected static void printGreeting(AnsiPrinter out)
	{
		out.println("TeXtidote v" + VERSION_STRING + " - A linter for LaTeX documents and others");
		out.println("(C) 2018-2019 Sylvain Hallé - All rights reserved");
		out.println();
	}

	/**
	 * Adds the rules to the LaTeX linter
	 * @param linter The linter to configure
	 */
	protected static void populateLatexRules(Linter linter)
	{
		linter.add(readRules(REGEX_FILENAME).values());
		linter.addCleaned(readRules(REGEX_FILENAME_DETEX).values());
		linter.add(new CheckFigureReferences());
		linter.add(new CheckFigurePaths());
		linter.add(new CheckCaptions());
		linter.add(new CheckSubsections());
		linter.add(new CheckSubsectionSize());
		linter.add(new CheckStackedHeadings());
		linter.add(new CheckNoBreak());
		linter.add(new CheckCiteMix());
	}

	/**
	 * Adds the rules to the Markdown linter
	 * @param linter The linter to configure
	 */
	protected static void populateMarkdownRules(Linter linter)
	{
		// Do nothing
	}

	/**
	 * Applies the linter on a document.
	 * @param scanner A scanner open on the document to read
	 * @param filename The name of the file for this document
	 * @param linter The linter to apply on the document
	 * @return A list of advice. Any new advice produced by the
	 * execution of the linter on the document will be added to this list.
	 * @throws LinterException Thrown if reading the file produces
	 * an exception
	 */
	protected static List<Advice> processDocument(Scanner scanner, String filename, Linter linter) throws LinterException
	{
		AnnotatedString last_string = AnnotatedString.read(scanner);
		last_string.setResourceName(filename);
		return linter.evaluateAll(last_string);
	}

	/**
	 * Reads a list of regex rules from a file
	 * @param filename The filename to read from
	 * @return A map of rule names to regex rules
	 */
	/*@ non_null @*/ public static Map<String,RegexRule> readRules(/*@ non_null @*/ String filename)
	{
		Map<String,RegexRule> list = new HashMap<String,RegexRule>();
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
				list.put(parts[0], rr);
			}
			if (parts.length == 4)
			{
				RegexRule rr = new RegexRule(parts[0], parts[1], parts[2], parts[3]);
				list.put(parts[0], rr);
			}
		}
		scanner.close();
		return list;
	}

	/**
	 * Reads a list of word from an Aspell-generated file
	 * @param filename The name of the file to read from
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

	/**
	 * Reads command-line arguments from a file.
	 * @param scanner A scanner open on the file to read from
	 * @return An array of strings, similar to what would be in the
	 * <tt>args</tt> input of the {@link #main(String[])} method if the
	 * arguments were received from the command line.
	 */
	/* This method has protected visibility in order to
	 * be accessible from the unit tests */
	/*@ non_null @*/ static String[] readArguments(/*@ non_null @*/ Scanner scanner)
	{
		List<String> arguments = new ArrayList<String>();
		StringBuilder current_argument = new StringBuilder();
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.isEmpty() || line.startsWith("#"))
			{
				// Blank and "comment" lines are ignored
				continue;
			}
			String[] parts = line.split("\\s+");
			boolean quotes = false;
			for (String part : parts)
			{
				part = part.trim();
				if (part.startsWith(("\"")))
				{
					quotes = true;
					part = part.substring(1);
				}
				current_argument.append(part).append(" ");
				if (part.endsWith("\""))
				{
					quotes = false;
				}
				if (!quotes)
				{
					String arg = current_argument.toString().trim();
					if (arg.endsWith("\""))
					{
						arg = arg.substring(0, arg.length() - 1);
					}
					arguments.add(arg);
					current_argument = new StringBuilder();
				}
			}
		}
		scanner.close();
		String[] out = new String[arguments.size()];
		for (int i = 0; i < arguments.size(); i++)
		{
			out[i] = arguments.get(i);
		}
		return out;
	}

	/**
	 * Adds filenames found in the <tt>input</tt> statements of the current
	 * file to the queue of files to process. A filename is added to the
	 * queue only if it has not already been processed earlier in the current
	 * run of the program.
	 * @param inner_files The list of new filenames
	 * @param processed_filenames The set of filenames already processed
	 * @param file_queue The queue of filenames waiting to be processed.
	 * This object is modified by the current method (new filenames can be
	 * added to it).
	 * @param current_filename The name of the file currently being processed
	 * @return The number of new files added to the queue
	 */
	protected static int addInnerFilesToQueue(List<String> inner_files, Set<String> processed_filenames,
			Queue<String> file_queue, String current_filename)
	{
		int added = 0;
		File f = new File(current_filename);
		String parent_path = f.getParent();
		if (parent_path == null)
		{
			// This happens if the filename is "--" or the file is in
			// the current folder
			parent_path = "";
		}
		else
		{
			if (!parent_path.endsWith(PATH_SEP))
			{
				parent_path += PATH_SEP;
			}
		}
		for (String filename : inner_files)
		{
			if (!processed_filenames.contains(filename))
			{
				file_queue.add(parent_path + filename);
				added++;
			}
		}
		return added;
	}
}
