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



%% :maxLineLen=78:wrap=soft: