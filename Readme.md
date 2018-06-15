TeXtidote: a correction tool for LaTeX documents
================================================

Have you ever dreamed of using a grammar checker on LaTeX files?

If so, you probably know that the process is far from simple. Since LaTeX
documents contain special commands and keywords (the so-called "markup") that
are not part of the "real" text, you cannot run a grammar checker directly on
these files: it cannot tell the difference between markup and text. The other
option is to remove all this markup, leaving only the "clear" text; however,
when a grammar tool points to a problem at a specific line in this clear text,
it becomes hard to retrace that location in the original LaTeX file.

TeXtidote solves this problem; it can read your original LaTeX file and
perform various sanity checks on it: for example, making sure that every
figure is referenced in the text, enforcing the correct capitalization of
titles, etc. In addition, TeXtidote can remove markup from the file and send
it to the [Language Tool](https://www.languagetool.org) library, which
performs a verification of **both spelling and grammar** in a dozen languages.
What is unique to TeXtidote is that it keeps track of the relative position of
words between the original and the "clean" text. This means that it can
translate the messages from Language Tool back to their proper location
**directly in your source file**.

## Getting TeXtidote

Make sure you have the following installed:

- Java version 6 or later (version 8 or later if you want to use
  Language Tool)
- (Optional): the Language Tool library. There is a [precompiled
  bundle](https://github.com/sylvainhalle/languagetool/releases/latest) you
  can download. Just unzip all the JAR files in your classpath. This is
  necessary only if you want to use TeXtidote with the `--check` option.

Then, download the [latest
release](https://github.com/sylvainhalle/textidote/releases/latest) of
TeXtidote; unzip and put in the folder of your choice.

## Using TeXtidote

TeXtidote is run from the command line. The TeXtidote repository contains a
sample LaTeX file called
[example.tex](https://raw.githubusercontent.com/sylvainhalle/textidote/master/example.tex).
Download this file and save it to the folder where TeXtidote resides. You then
have the choice of producing two types of "reports" on the contents of your
file: an "HTML" report (viewable in a web browser) and a "console" report.

### HTML report

To run TeXtidote and perform a basic verification of the file, run:

    java -jar textidote.jar --html example.tex > report.html

Here, the `--html` option tells TeXtidote to produce a report in HTML format;
the `>` symbol indicates that the output should be saved to a file, whose name
is `report.html`. TeXtidote will run for some time, and print:

```
TeXtidote v0.2 - A linter for LaTeX documents
(C) 2018 Sylvain Hallé - All rights reserved

Found 23 warnings(s)
Total analysis time: 2 second(s)
```

Once the process is over, switch to your favorite web browser, and open the
file `report.html` (using the *File/Open* menu). You should see something like this:

![Screenshot](https://raw.githubusercontent.com/sylvainhalle/textidote/master/docs/assets/images/Screenshot.png)

As you can see, the page shows your original LaTeX source file, where some
portions have been highlighted in various colors. These correpond to regions
in the file where an issue was found. You can hover your mouse over these
colored regions; a tooltip will show a message that describes the problem.

### Plain report

To run TeXtidote and display the results directly in the console, simply omit
the `--html` option, and do not redirect the output to a file:

    java -jar textidote.jar example.tex

TeXtidote will analyze the file like before, but produce a report that looks
like this:

```
* L25C1-L25C25 A section title should start with a capital letter. [sh:001] 
  \section{a first section}
  ^^^^^^^^^^^^^^^^^^^^^^^^^
* L38C1-L38C29 A section title should not end with a punctuation symbol. 
  [sh:002] 
  \subsection{ My subsection. }
  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
* L15C94-L15C99 Add a space before citation or reference. [sh:c:001] 
   things, like a citation\cite{my:paper} .The text 
```

Each element of the list corresponds to a "warning", indicating that
something in the text requires your attention. For each warning, the position
in the original source file is given: LxxCyy indicates line xx, column yy. The
warning is followed by a short comment describing the issue, and an excerpt
from the line in question is displayed. The range of characters where the
problem occurs is marked by the "^^^^" symbols below the text. Each of these
warnings results from the evaluation of some "rule" on the text; an identifier
of the rule in question is also shown between brackets.

### Spelling, grammar and style

If the Language Tool library is installed on your system, you can perform
further checks on spelling and grammar, by passing the `--check` option at
the command line. For example, to check text in English, you run:

    java -jar textidote.jar --check en example.tex

The `--check` parameter must be accompanied by a two-letter code indicating
the language to be used. The code `en` stands for English; TeXtidote also
supports `fr` (French), `nl` (Dutch), `de` (German), `es` (Spanish) and
`pt` (Portuguese).

Language Tool is a powerful library that can verify spelling, grammar, and
even provide suggestions regarding style. TeXtidote simply passes a cleaned-up
version of the LaTeX file to Language Tool, retrieves the messages it
generates, and coverts the line and column numbers associated to each message
back into line/column numbers of the original source file. For more
information about the kind of verifications made by Language Tool, please
refer to [its website](https://languagetool.org).

### Using a dictionary

If you have a list of words that you want TeXlint to ignore when checking
spelling, you can use the `--dict` parameter to specify the location of a
text file:

    java -jar textidote.jar --check en --dict dico.txt example.tex

The file `dico.txt` must be a plain text file contain a list of words to be
ignored, with each word on a separate line.

If you already spell checked you file using [Aspell](https://aspell.net) and
saved a [local dictionary](http://aspell.net/0.50-doc/man-html/5_Working.html)
(as is done for example by the
[PaperShell](https://github.com/sylvainhalle/PaperShell) environment),
TeXtidote can automatically load this dictionary when invoked. More
specifically, it will look for a file called `.aspell.en.pws` in the folder
where TeXlint is started (this is the filename Aspell gives to local
dictionaries). If such a file exists, TeXtidote will load it and mention it at
the console:

```
Found local Aspell dictionary
```

### Removing markup

You can also use TeXtidote just to remove the markup from your original LaTeX
file. This is done with the option `--detex`:

    java -jar textidote.jar --detex example.tex

By default, the resulting "clean" file is printed directly at the console. To
save it to a file, use a redirection:

    java -jar textidote.jar --detex example.tex > clean.txt

You will see that TeXtidote performs a very aggressive deletion of LaTeX
markup:

- All `figure`, `table` and `tabular` environments are removed
- All equations are removed
- All inline math expressions (`$...$`) are replaced by "X"
- All `\cite` commands are replaced by "0"
- All `\ref` commands are replaced by "[0]"
- Commands that alter text (`\textbf`, `\emph`, `\uline`, `\footnote`)
  are removed (but the text is kept)
- Virtually all other commands are simply deleted

Surprisingly, the result of applying these modifications is a text that is
clean and legible enough for a spelling or grammar checker to provide
sensible advice.

As was mentioned earlier, TeXtidote keeps a mapping between character ranges
in the "detexed" file, and the same character ranges in the original LaTeX
document. You can get this mapping by using the `--map` option:

    java -jar textidote.jar --detex --map map.txt example.tex > clean.txt

The `--map` parameter is given the name of a file. TeXtidote will put in this
file the list correspondences between character ranges. This file is made of
lines that look like this:

```
L1C1-L1C24=L1C5-L128
L1C26-L1C28=L1C29-L1C31
L2C1-L2C10=L3C1-L3C10
...
```

The first entry indicates that characters 1 to 24 in the first line of the
clean file correspond to characters 5 to 28 in the first line of the original
LaTeX file --and so on. This mapping can have "holes": for example, character
25 line 1 does not correspond to anything in the original file (this happens
when the "cleaner" inserts new characters, or replaces characters from the
original file by something else). Conversely, it is also possible that
characters in the original file do not correspond to anything in the clean
file (this happens when the cleaner deletes characters from the original).

## Helping TeXtidote

It order to get the best results when using TeXtidote, it is advisable that
you follow a few formatting conventions when writing your LaTeX file:

- Avoid putting multiple `\begin{envionment}` and/or `\end{environment}` on
  the same line
- Do not hard-wrap your paragraphs

## About the author

TeXtidote was written by [Sylvain Hallé](https://leduotang.ca/sylvain), Full
Professor in the Department of Computer Science and Mathematics at
[Université du Québec à Chicoutimi](http://www.uqac.ca), Canada.

%% :maxLineLen=78:wrap=soft: