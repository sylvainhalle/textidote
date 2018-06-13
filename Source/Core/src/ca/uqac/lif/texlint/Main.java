/*
    TexLint, a linter for LaTeX documents
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
package ca.uqac.lif.texlint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.languagetool.Language;

import ca.uqac.lif.texlint.as.AnnotatedString;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;

/**
 * Command-line interface for TexLint.
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
	protected static final String VERSION_STRING = "0.1";

	/**
	 * Main method
	 * @param args Command-line arguments
	 */
	public static void main(String[] args)
	{
		// Setup command line parser and arguents
		CliParser cli_parser = new CliParser();
		cli_parser.addArgument(new Argument().withLongName("html").withDescription("Formats the report as HTML"));
		cli_parser.addArgument(new Argument().withLongName("no-color").withDescription("Disables colors in ANSI printing"));
		cli_parser.addArgument(new Argument().withLongName("check").withArgument("lang").withDescription("Checks grammar in language lang"));
		ArgumentMap map = cli_parser.parse(args);
		boolean enable_colors = true;
		if (map.hasOption("no-color"))
		{
			enable_colors = false;
		}
		AnsiPrinter stdout = new AnsiPrinter(System.out);
		AnsiPrinter stderr = new AnsiPrinter(System.err);
		printGreeting(stderr, enable_colors);

		// Create a linter
		Linter linter = new Linter();
		populateRules(linter);
		
		// Do we check the language?
		if (map.hasOption("check"))
		{
			Language lang = LanguageFactory.getLanguageFromString(map.getOptionValue("check"));
			if (lang == null)
			{
				stderr.println("Unknown language: " + map.getOptionValue("check"));
				System.exit(-1);
			}
			linter.addDetexed(new CheckLanguage(lang));
		}

		// Process files
		List<Advice> all_advice = new ArrayList<Advice>();
		List<String> filenames = map.getOthers();
		if (filenames.isEmpty())
		{
			System.err.println("No filename is specified");
			System.err.println("");
			cli_parser.printHelp("Usage: java -jar texlint.jar [options] file1 [file2 ...]", System.err);
			System.exit(1);
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
				all_advice.addAll(linter.evaluateAll(s));
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

		// Render advice
		AdviceRenderer renderer = null;
		if (map.hasOption("html"))
		{
			renderAdviceHtml(System.out, all_advice);
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
		System.exit(all_advice.size());
	}

	protected static void printGreeting(AnsiPrinter out, boolean enable_colors)
	{
		out.println("TexLint v" + VERSION_STRING + " - A linter for LaTeX documents");
		out.println("(C) 2018 Sylvain Hallé - All rights reserved");
		out.println();
	}

	/**
	 * Renders the list of advice in HTML format
	 * @param out A print stream where to send the text
	 * @param list The list of advice to render
	 */
	protected static void renderAdviceHtml(PrintStream out, List<Advice> list)
	{
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Result of lint</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<table>");
		for (Advice ad : list)
		{
			out.println("<tr>");
			out.print("<td>");
			out.print(ad);
			out.println("</td>");
			out.println("</tr>");
		}
		out.println("</body>");
		out.println("</html>");
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
			RegexRule rr = new RegexRule(parts[0], parts[1], parts[2]);
			list.add(rr);
		}
		scanner.close();
		return list;
	}
}
