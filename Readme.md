TeXtidote: a correction tool for LaTeX documents
================================================

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

## Getting TeXtidote

Make sure you have Java version 8 or later installed on your system.
Then, download the [latest
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

    $ java -jar textidote.jar --html example.tex > report.html

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

    $ java -jar textidote.jar example.tex

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

    $ java -jar textidote.jar --check en example.tex

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

If you have a list of words that you want TeXtidote to ignore when checking
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
where TeXtidote is started (this is the filename Aspell gives to local
dictionaries). If such a file exists, TeXtidote will load it and mention it at
the console:

```
Found local Aspell dictionary
```

### Removing markup

You can also use TeXtidote just to remove the markup from your original LaTeX
file. This is done with the option `--detex`:

    $ java -jar textidote.jar --detex example.tex

By default, the resulting "clean" file is printed directly at the console. To
save it to a file, use a redirection:

    $ java -jar textidote.jar --detex example.tex > clean.txt

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

    $ java -jar textidote.jar --detex --map map.txt example.tex > clean.txt

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
- Put headings like `\section` or `\paragraph` alone on their line

## Creating shortcuts

To make using TeXtidote easier, you can create shortcuts on your system. Here
are a few recommended tips.

### In Linux

We recommend you create a folder called `/opt/textidote` and put the big
`textidote.jar` file there (this requires root privileges).

#### Command line shortcut

In`/usr/local/bin`, create a file called `textidote` with the following
contents:

```
#! /bin/bash
java -jar /opt/textidote/textidote.jar "$@"
```

Make this file executable by typing at the command line:

    $ sudo chmod +x /usr/local/bin/textidote

(These two operations also require root previliges.) From then on, you can
invoke TeXtidote on the command line from any folder by simply typing
`textidote`, e.g.:

    $ textidote somefile.tex

#### Desktop shortcut

If you use a desktop environment such as Gnome or Xfce, you can automate
this even further by creating a TeXtidote icon on your desktop. First,
create a file called `/opt/textidote/textidote.sh` with the following
contents, and make this file executable:

```
#! /bin/bash
dir=$(dirname "$1")
pushd $dir
java -jar /opt/textidote/textidote.jar --check en --html "$@" > /tmp/textidote.html
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
Comment=Check file with TeXtidote
Exec=/opt/textidote/textidote-desktop.sh %F
Path=
Terminal=false
StartupNotify=false
```

This will create a new desktop shortcut; make this file executable. From then
on, you can drag LaTeX files from your file manager with your mouse and drop
them on the TeXtidote icon. After the analysis, the report will automatically
pop up in your web browser. Voilà!

## Rules checked by TeXtidote

Here is a list of the rules that are checked on your LaTeX file by TeXtidote.
Each rule has a unique identifier, written between square brackets.

### Language Tool

In addition to all the rules below, the `--check xx` option activates all the
[rules verified by Language Tool](https://community.languagetool.org/rule/list?sort=pattern&max=10&offset=0&lang=en)
(more than 2,000 grammar and spelling errors). Note that the verification time
is considerably longer when using that option.

### Style

- A section title should start with a capital letter. [sh:001]
- A section title should not end with a punctuation symbol. [sh:002]
- A section title should not be written in all caps. The LaTeX stylesheet
  takes care of rendering titles in caps if needed. [sh:003]
- Use a capital letter when referring to a specific section, chapter
  or table: 'Section X'. [sh:secmag, sh:chamag, sh:tabmag]

### Citations and references

- There should be one space before a \cite or \ref command [sh:c:001], and
  no space after [sh:c:002].
- Do not use 'in [X]' or 'from [X]': the syntax of a sentence should not be
  changed by the removal of a citation. [sh:c:noin]

### Figures

- Every figure should have a label, and every figure should be referenced at
  least once in the text. [sh:figref]
- A figure caption should end with a period. [sh:004]
- Use a capital letter when referring to a specific figure: 'Figure X'.
  [sh:figmag]

### Typesetting

- You should not break lines manually in a paragraph. Either start a new
  paragraph or stay in the current one. [sh:nobreak]

### Structure

- A section should not contain a single sub-section. More generally, a
  division of level n should not contain a single division of level n+1.
  [sh:nsubdiv]

### Hard-coding

- Figures should not refer to hard-coded local paths. [sh:relpath]
- Do not refer to sections, figures and tables using a hard-coded number.
  Use \ref instead. [sh:hcfig, sh:hctab, sh:hcsec, sh:hccha]

### Potentially suspicious

- There should be at least N words between two section headings (currently
  N=100). [sh:seclen]

## About the author

TeXtidote was written by [Sylvain Hallé](https://leduotang.ca/sylvain), Full
Professor in the Department of Computer Science and Mathematics at
[Université du Québec à Chicoutimi](http://www.uqac.ca), Canada.

%% :maxLineLen=78:wrap=soft: