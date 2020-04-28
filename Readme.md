TeXtidote: a correction tool for LaTeX documents and other formats
==================================================================

[![Travis](https://img.shields.io/travis/sylvainhalle/textidote.svg?style=flat-square)](https://travis-ci.org/sylvainhalle/textidote)
[![SonarQube Coverage](https://sonarcloud.io/api/project_badges/measure?project=sylvainhalle%3Atextidote&metric=coverage)](https://sonarcloud.io/dashboard?id=sylvainhalle%3Atextidote)
<img src="http://leduotang.ca/textidote.svg" height="16" alt="Downloads"/>

Have you ever thought of using a grammar checker on LaTeX files?

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

You can see the list of all the rules checked by TeXtidote at the end of this
file.

TeXtidote also supports spelling and grammar checking of files in the [Markdown](https://en.wikipedia.org/wiki/Markdown) format.

## Getting TeXtidote

You can either install TeXtidote by downloading it manually, or by installing
it using a package.

### Under Debian systems: install package

Under Debian systems (Ubuntu and derivatives), you can install TeXtidote using
`dpkg`. Download the latest `.deb` file in the
[Releases](https://github.com/sylvainhalle/textidote/releases) page; suppose
it is called `textidote_X.Y.Z_all.deb`. You can install TeXtidote by typing:

    $ sudo apt-get install ./textidote_X.Y.Z_all.deb

The `./` is mandatory; otherwise the command won't work.

### Manual download

You can also download the TeXtidote executable manually: this works on all
operating systems. Simply make sure you have Java version 8 or later installed
on your system. Then, download the [latest
release](https://github.com/sylvainhalle/textidote/releases/latest) of
TeXtidote; put the JAR in the folder of your choice.

## Using TeXtidote

TeXtidote is run from the command line. The TeXtidote repository contains a
sample LaTeX file called
[example.tex](https://raw.githubusercontent.com/sylvainhalle/textidote/master/example.tex).
Download this file and save it to the folder where TeXtidote resides. You then
have the choice of producing two types of "reports" on the contents of your
file: an "HTML" report (viewable in a web browser) and a "console" report.

### HTML report

To run TeXtidote and perform a basic verification of the file, run:

    java -jar textidote.jar --output html example.tex > report.html

In Linux, if you installed TeXtidote using `apt-get`, you can also call it
directly by typing:

    textidote --output html example.tex > report.html

Here, the `--output html` option tells TeXtidote to produce a report in HTML format;
the `>` symbol indicates that the output should be saved to a file, whose name
is `report.html`. TeXtidote will run for some time, and print:

```
TeXtidote v0.8 - A linter for LaTeX documents
(C) 2018-2019 Sylvain Hallé - All rights reserved

Found 23 warnings(s)
Total analysis time: 2 second(s)
```

Once the process is over, switch to your favorite web browser, and open the
file `report.html` (using the *File/Open* menu). You should see something like this:

![Screenshot](https://raw.githubusercontent.com/sylvainhalle/textidote/master/docs/assets/images/Screenshot.png)

As you can see, the page shows your original LaTeX source file, where some
portions have been highlighted in various colors. These correspond to regions
in the file where an issue was found. You can hover your mouse over these
colored regions; a tooltip will show a message that describes the problem.

If you don't write any filename (or write `--` as the filename), TeXtidote
will attempt to read one from the standard input.

### Plain report

To run TeXtidote and display the results directly in the console, simply omit
the `--output html` option (you can also use `--output plain`), and do not redirect the output to a file:

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

### Single line report

Another option to display the results directly in the console is the single line report:

    java -jar textidote.jar --output singleline example.tex

Textidote will analyze the file like before, but this time the report looks like this:

```
example.tex(L25C1-L25C25): A section title should start with a capital letter. "\section{a first section}"
example.tex(L38C1-L38C29): A section title should not end with a punctuation symbol. "\subsection{ My subsection. }"
example.tex(L15C94-L15C99): Add a space before citation or reference. "things, like a citation\cite{my:paper} .The text"
```

Each line corresponds to a warning, and is parseable by regular expressions easily, e.g., for further processing in another tool.
The file is given at the beginning of the line, followed by the position in parentheses.
Then, the warning message is given, and the excerpt causing the warning is printed in double quotes ("").
Note, that sometimes it may happen that a position cannot be determined. In this case, instead of LxxCyy, ? is printed.

### Spelling, grammar and style

You can perform further checks on spelling and grammar, by passing the
`--check` option at the command line. For example, to check text in English,
you run:

    java -jar textidote.jar --check en example.tex

The `--check` parameter must be accompanied by a two-letter code indicating
the language to be used. Language Tool is a powerful library that can verify
spelling, grammar, and even provide suggestions regarding style. TeXtidote
simply passes a cleaned-up version of the LaTeX file to Language Tool,
retrieves the messages it generates, and coverts the line and column numbers
associated to each message back into line/column numbers of the original
source file. For more information about the kind of verifications made by
Language Tool, please refer to [its website](https://languagetool.org).

The language codes you can use are:

- `de`: (Germany) German, and the variants `de_AT` (Austrian) and `de_CH`
  (Swiss)
- `en`: (US) English, and the variants `en_CA` (Canadian) and `en_UK`
  (British)
- `es`: Spanish
- `fr`: French
- `nl`: Dutch
- `pt`: Portuguese

### Using a dictionary

If you have a list of words that you want TeXtidote to ignore when checking
spelling, you can use the `--dict` parameter to specify the location of a
text file:

    java -jar textidote.jar --check en --dict dico.txt example.tex

The file `dico.txt` must be a plain text file contain a list of words to be
ignored, with each word on a separate line. (The list is case sensitive.)

If you already spell checked you file using [Aspell](https://aspell.net) and
saved a [local dictionary](http://aspell.net/0.50-doc/man-html/5_Working.html)
(as is done for example by the
[PaperShell](https://github.com/sylvainhalle/PaperShell) environment),
TeXtidote can automatically load this dictionary when invoked. More
specifically, it will look for a file called `.aspell.XX.pws` in the folder
where TeXtidote is started (this is the filename Aspell gives to local
dictionaries). The characters `XX` are to be replaced with the two-letter
language code. If such a file exists, TeXtidote will load it and mention it at
the console:

```
Found local Aspell dictionary
```

### Ignoring rules

You may want to ignore some of TeXtidote's advice. You can do so by specifying
rule IDs to ignore with the `--ignore` command line parameter. For example,
the ID of the rule "A section title should start with a capital letter" is
`sh:001` (rule IDs are shown between brackets in the reports given by
TeXtidote); to ignore warnings triggered by this rule, you call TeXtidote as
follows:

    java -jar textidote.jar --ignore sh:001 myfile.tex

If you want to ignore multiple rules, separate their IDs with a comma (but no
space).

### Ignoring environments

TeXtidote can be instructed to remove user-specified environments using the `--remove` command line parameter. For example:

    $ java -jar textidote.jar --remove itemize myfile.tex

This command will remove all text lines between `\begin{itemize}` and `\end{itemize}` before further processing the file.

### Ignoring macros

The same can be done with macros:

    $ java -jar textidote.jar --remove-macros foo myfile.tex

This command will remove all occurrences of use-defined command `\foo` in the text. Alternate syntaxes like `\foo{bar}` and `\foo[x=y]{bar}` are also recognized and deleted.

### Reading a sub-file

By default, TeXtidote ignores everything before the `\begin{document}`
command. If you have a large document that consists of multiple included LaTeX
"sub-files", and you want to check one such file that does not contain a
`\begin{document}`, you must tell TeXtidote to read all the file using the
`--read-all` command line option. Otherwise, TeXtidote will ignore the whole
file and give you no advice.

TeXtidote also automatically follows sub-files that are embedded from a main document using `\input{filename}` and `\include{filename}` (braces are mandatory). Any such *non-commented* instruction will add the corresponding filename to the running queue. If you want to *exclude* an `\input` from being processed, you must surround the line with `ignore begin`/`end` comments (see below, *Helping TeXtidote*).

### Removing markup

You can also use TeXtidote just to remove the markup from your original LaTeX
file. This is done with the option `--clean`:

    java -jar textidote.jar --clean example.tex

By default, the resulting "clean" file is printed directly at the console. To
save it to a file, use a redirection:

    java -jar textidote.jar --clean example.tex > clean.txt

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
in the "cleaned" file, and the same character ranges in the original LaTeX
document. You can get this mapping by using the `--map` option:

    java -jar textidote.jar --clean --map map.txt example.tex > clean.txt

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

### Using a configuration file

If you need to run TeXtidote with many command line arguments (for example:
you load a local dictionary, ignore a few rules, apply replacements, etc.), it
may become tedious to invoke the program with a long list of arguments every
time. TeXtidote can be "configured" by putting those arguments in a text
file called `.textidote` in the directory from which it is called. Here is an
example of what such a file could contain:

```
--output html --read-all
--replace replacements.txt
--dict mydict.txt
--ignore sh:001,sh:d:001
--check en mytext.tex
```

As you can see, arguments can be split across multiple lines. You can then
call TeXtidote without any arguments like this:

    textidote > report.html

If you call TeXtidote with command line arguments, they will be merged with
whatever was found in `.textidote`. You can also tell TeXtidote to explicitly
ignore that file and only take into account the command line arguments using
the `--no-config` argument.

### Markdown input

TeXtidote also supports files in the Markdown format. The only difference is that rules specific to LaTeX (references to figures, citations) are not evaluated.

Simply call TeXtidote with a Markdown input file instead of a LaTeX file. The format is auto-detected by looking at the file extension. However, if you pass a file through the standard input, you must tell TeXtidote that the input file is Markdown by using the command line parameter `--type md`. Otherwise, TeXtidote assumes by default that the input file is LaTeX.

## Helping TeXtidote

It order to get the best results when using TeXtidote, it is advisable that
you follow a few formatting conventions when writing your LaTeX file:

- Avoid putting multiple `\begin{environment}` and/or `\end{environment}` on
  the same line
- Keep the arguments of a command on a single line. Commands (such as
  `\title{}`) that have their opening and closing braces on different lines
  are not recognized by TeXtidote and will result in garbled output and
  nonsensical warnings.
- Do not hard-wrap your paragraphs. It is easier for TeXtidote to detect
  paragraphs if they have no hard carriage returns inside. (If you need word
  wrapping, it is preferable to enable it in your text editor.)
- Put headings like `\section` or `\paragraph` alone on their line and
  separate them from the text below by a blank line.

As a rule, it is advisable to first see what your text looks like using the
`--clean` option, to make sure that TeXtidote is performing checks on
something that makes sense.

If you realize that a portion of LaTeX markup is not handled properly and
messes up the rest of the file, you can tell TeXtidote to ignore a region
using a special LaTeX comment:

```
% textidote: ignore begin
Some weird LaTeX markup that TeXtidote does not
understand...
% textidote: ignore end
```

The lines between `textidote: ignore begin` and `textidote: ignore end` will
be handled by TeXtidote as if they were comment lines.

## Linux shortcuts

To make using TeXtidote easier, you can create shortcuts on your system. Here
are a few recommended tips.

First, we recommend you create a folder called `/opt/textidote` and put the
big `textidote.jar` file there (this requires root privileges). This step is
already taken care of if you installed the TeXtidote package using `apt-get`.

### Command line shortcut

(This step is not necessary if TeXtidote has been installed with `apt-get`.)
In`/usr/local/bin`, create a file called `textidote` with the following
contents:

```
#! /bin/bash
java -jar /opt/textidote/textidote.jar "$@"
```

Make this file executable by typing at the command line:

    sudo chmod +x /usr/local/bin/textidote

(These two operations also require root privileges.) From then on, you can
invoke TeXtidote on the command line from any folder by simply typing
`textidote`, e.g.:

    textidote somefile.tex

### Desktop shortcut

If you use a desktop environment such as Gnome or Xfce, you can automate
this even further by creating a TeXtidote icon on your desktop. First,
create a file called `/opt/textidote/textidote.sh` with the following
contents, and make this file executable:

```
#! /bin/bash
dir=$(dirname "$1")
pushd $dir
java -jar /opt/textidote/textidote.jar --check en --output html "$@" > /tmp/textidote.html
popd
sensible-browser /tmp/textidote.html &
```

This script enters into the directory of the file passed as an argument,
calls TeXtidote, sends the HTML report to a temporary file, and opens the
default web browser to show that report.

Then, on your desktop (typically in your `~/Desktop` folder), create another
file called `TeXtidote.desktop` with the following contents:

```
[Desktop Entry]
Version=1.0
Type=Application
Name=TeXtidote
Comment=Check text with TeXtidote
Exec=/opt/textidote/textidote-desktop.sh %F
Icon=/opt/textidote/textidote-icon.svg
Path=
Terminal=false
StartupNotify=false
```

This will create a new desktop shortcut; make this file executable. From then
on, you can drag LaTeX files from your file manager with your mouse and drop
them on the TeXtidote icon. After the analysis, the report will automatically
pop up in your web browser. Voilà!

### Tab completions

You can auto-complete the commands you type at the command-line using the TAB
key (as you are probably used to). If you installed TeXtidote using `apt-get`,
auto-completion for [Bash](https://www.gnu.org/software/bash/) comes built-in.
You can also enable auto-completion for other shells as follows.

#### Zsh

Users of [Zsh](https://zsh.org) can also enable auto-completion; in your
`~/.zshrc` file, add the line

    source /opt/textidote/textidote.zsh

(Create the file if it does not exist.) You must then restart your Zsh shell
for the changes to take effect.

## Rules checked by TeXtidote

Here is a list of the rules that are checked on your LaTeX file by TeXtidote.
Each rule has a unique identifier, written between square brackets.

### Language Tool

In addition to all the rules below, the `--check xx` option activates all the
[rules verified by Language Tool](https://community.languagetool.org/rule/list?sort=pattern&max=10&offset=0&lang=en)
(more than 2,000 grammar and spelling errors). Note that the verification time
is considerably longer when using that option.

If the `--check` option is used, you can add the `--languagemodel xx` option to [find errors using n-gram data](http://wiki.languagetool.org/finding-errors-using-n-gram-data). In order to do so, `xx` must be a path pointing to an n-gram-index directory. Please refer to the LanguageTool page (link above) on how to use n-grams and what this directory should contain.

### Style

- A section title should start with a capital letter. [sh:001]
- A section title should not end with a punctuation symbol. [sh:002]
- A section title should not be written in all caps. The LaTeX stylesheet
  takes care of rendering titles in caps if needed. [sh:003]
- Use a capital letter when referring to a specific section, chapter
  or table: 'Section X'. [sh:secmag, sh:chamag, sh:tabmag]
- A (figure, table) caption should end with a period. [sh:capperiod]

### Citations and references

- There should be one space before a \cite or \ref command [sh:c:001], and
  no space after [sh:c:002].
- Do not use 'in [X]' or 'from [X]': the syntax of a sentence should not be
  changed by the removal of a citation. [sh:c:noin]
- Do not mix `\cite` and `\citep` or `\citet` in the same document.
  [sh:c:mix]
- When citing more than one reference, do not use multiple `\cite` commands;
  put all references in the same `\cite`. [sh:c:mul, sh:c:mulp]

### Figures

- Every figure should have a label, and every figure should be referenced at
  least once in the text. [sh:figref]
- Use a capital letter when referring to a specific figure: 'Figure X'.
  [sh:figmag]

### Structure

- A section should not contain a single sub-section. More generally, a
  division of level n should not contain a single division of level n+1.
  [sh:nsubdiv]
- The first heading of a document should be the one with the highest level.
  For example, if a document contains sections, the first section cannot be
  preceded by a sub-section. [sh:secorder]
- There should not be a jump down between two non-successive section
  levels (e.g. a `\section` followed by a `\subsubsection` without a
  `\subsection` in between). [sh:secskip]
- You should avoid stacked headings, i.e. consecutive headings without
  text in between. [sh:stacked]

### Hard-coding

- Figures should not refer to hard-coded local paths. [sh:relpath]
- Do not refer to sections, figures and tables using a hard-coded number.
  Use `\ref` instead. [sh:hcfig, sh:hctab, sh:hcsec, sh:hccha]
- You should not break lines manually in a paragraph with `\\`. Either start a
  new paragraph or stay in the current one. [sh:nobreak]
- If you are writing a research paper, do not hard-code page breaks with
  `\newpage`. [sh:nonp]

### LaTeX subtleties

- Use a backslash or a comma after the last period in "i.e.", "e.g." and "et al.";
  otherwise LaTeX will think it is a full stop ending a sentence. [sh:010, sh:011]
- There should not be a space before a semicolon or a colon. If in your
  language, typographic rules require a space here, LaTeX takes care of
  inserting it without your intervention. [sh:d:005, sh:d:006]

### Potentially suspicious

- There should be at least N words between two section headings (currently
  N=100). [sh:seclen]

## Building TeXtidote

First make sure you have the following installed:

- The Java Development Kit (JDK) to compile. TeXtidote requires version 8
  of the JDK (and probably works with later versions).
- [Ant](http://ant.apache.org) to automate the compilation and build process

Download the sources for TeXtidote from
[GitHub](http://github.com/sylvainhalle/Bullwinkle) or clone the repository
using Git:

    git clone git@github.com:sylvainhalle/textidote.git

### Compiling

First, download the dependencies by typing:

    ant download-deps

Then, compile the sources by simply typing:

    ant

This will produce a file called `textidote.jar` in the folder. This
file is runnable and stand-alone, or can be used as a library, so it can be
moved around to the location of your choice.

In addition, the script generates in the `docs/doc` folder the Javadoc
documentation for using TeXtidote.

### Testing

TeXtidote can test itself by running:

    ant test

Unit tests are run with [jUnit](http://junit.org); a detailed report of
these tests in HTML format is available in the folder `tests/junit`, which
is automatically created. Code coverage is also computed with
[JaCoCo](http://www.eclemma.org/jacoco/); a detailed report is available
in the folder `tests/coverage`.

About the author
----------------

TeXtidote was written by [Sylvain Hallé](https://leduotang.ca/sylvain), Full
Professor in the Department of Computer Science and Mathematics at
[Université du Québec à Chicoutimi](http://www.uqac.ca), Canada.

Like TeXtidote?
---------------

TeXtidote is free software licensed under the GNU [General Public License
3](https://www.gnu.org/licenses/gpl-3.0.en.html). It is released as
[postcardware](https://en.wikipedia.org/wiki/Postcardware): if you use and
like the software, please tell the author by sending a postcard of your town
at the following address:

    Sylvain Hallé
    Department of Computer Science and Mathematics
    Univerité du Québec à Chicoutimi
    555, boulevard de l'Université
    Chicoutimi, QC
    G7H 2B1 Canada

If you like TeXtidote, you might also want to look at
[PaperShell](https://github.com/sylvainhalle/PaperShell), a template
environment for writing scientific papers in LaTeX.

<!-- :maxLineLen=78:wrap=soft: -->
