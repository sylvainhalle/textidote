TeXLint: Verification Tools for LaTeX Documents
===============================================

Have you ever dreamed of using a grammar checker on LaTeX files?

If so, you probably know that the process is far from simple. Since LaTeX documents contain special commands and keywords (the so-called "markup") that are not part of the "real" text, you cannot run a grammar checker directly on these files: it cannot tell the difference between markup and text. The other option is to remove all this markup, leaving only the "clear" text; however, when a grammar tool points to a problem at a specific line in this clear text, it becomes hard to retrace that location in the original LaTeX file.

TeXLint is a tool that allows you to perform various checks on a source LaTeX file. Among other things:

- It can check spelling and grammar directly from the LaTeX file, using the
  powerful [Language Tool](https://www.languagetool.org) library in the
  background. The errors that are reported refer to the actual locations *in
  the source file*. No need to "de-tex" your file beforehand.
- It applies various other sanity checks that are not related to grammar or
  spelliing: checking that every figure is referenced in the text and has a
  caption, enforce correct capitalization of titles, etc.

## Getting TeXLint

Make sure you have the following installed:

- Java version 6 or later (version 8 or later if you want to use
  Language Tool)
- (Optional): the Language Tool library. There is a [precompiled
  bundle](https://github.com/sylvainhalle/languagetool/releases/latest) you
  can download. Just unzip all the JAR files in your classpath. This is
  necessary only if you want to use TeXLint with the `--check` option.

Then, download the [latest
release](https://github.com/sylvainhalle/texlint/releases/latest) of TeXLint; unzip and put in folder of
your choice.

## Using TeXLint

To run TeXLint and perform a basic verification of the file, run:

    java -jar texlint.jar somefile.tex

TeXLint will analyze the file and produce a report that looks like this:

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

    java -jar texlint.jar --check en somefile.txt

The `--check` parameter must be accompanied by a two-letter code indicating
the language to be used. The code `en` stands for English; TeXLint also
supports `fr` (French), `nl` (Dutch), `de` (German), `es` (Spanish).

### Removing markup

TODO (the option is `--detex`).

%% :maxLineLen=78:wrap=soft: