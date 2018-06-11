AntRun: a general-purpose Ant build script
==========================================

[![Build Status](https://semaphoreci.com/api/v1/projects/5eab613c-29af-43c0-8961-0170588f6368/466366/badge.svg)](https://semaphoreci.com/sylvainhalle/antrun)

AntRun is a template structure for Java projects. Through its comprehensive
Ant build script, it supports automated execution of unit tests, generation
of [Javadoc](http://www.oracle.com/technetwork/articles/java/index-jsp-135444.html)
documentation and code coverage reports (with
[JaCoCo](http://www.eclemma.org/jacoco/)), and download and installation
of JAR dependencies as specified in an external, user-definable XML file.
It also includes a boilerplate `.gitignore` file suitable for an Eclipse
project.

All this is done in a platform-independent way, so your build scripts
should work on both MacOS, Linux and Windows.

Table of Contents                                                    {#toc}
-----------------

- [Quick start guide](#quickstart)
- [Available tasks](#tasks)
- [Continuous integration](#ci)
- [Cross-compiling](#xcompile)
- [About the author](#about)

Quick start guide                                             {#quickstart}
-----------------

1. First make sure you have the following installed:

  - The Java Development Kit (JDK) to compile. AntRun was developed and
    tested on version 6 and 7 of the JDK, but it is probably safe to use
    any later version.
  - [Ant](http://ant.apache.org) to automate the compilation and build
    process

2. Download the AntRun template from
   [GitHub](https://github.com/sylvainhalle/AntRun) or clone the repository
   using Git:
   
   git@github.com:sylvainhalle/AntRun.git

3. Override any defaults, and specify any dependencies your project
   requires by editing `config.xml`. In particular, you may want
   to change the name of the Main class.

4. Start writing your code in the `Source/Core` folder, and your unit
   tests in `Source/CoreTest`. Optionally, you can create an Eclipse
   workspace out of the `Source` folder, with `Core` and `CoreTest` as
   two projects within this workspace.

5. Use Ant to build your project. To compile the code, generate the
   Javadoc, run the unit tests, generate a test and code coverage report
   and bundle everything in a runnable JAR file, simply type `ant` (without
   any arguments) on the command line.
   
6. If dependencies were specified in step 4 and are not present in the
   system, type `ant download-deps`, followed by `ant install-deps` to
   automatically download and install them before compiling. The latter
   command might require to be run as administrator --the way to do this
   varies according to your operating system (see below).

Otherwise, use one of the many [tasks](#tasks) that are predefined.

Available tasks                                                    {#tasks}
---------------

This document is incomplete. Execute

    $ ant -p

from the project's top folder to get the list of all available targets.

### dist

The default task. Currently applies `jar`.

### compile

Compiles the project.

### compile-tests

Compiles the unit tests.

### jar

Compiles the project, generates the Javadoc and creates a runnable JAR,
including the sources and the documentation (and possibly the project's
dependencies, see `download-deps` below).

### test

Performs tests with jUnit and generates code coverage report with JaCoCo.
The unit test report (in HTML format) is available in the `test/junit`
folder (which will be created if it does not exist). The code coverage
report is available in the `test/coverage` folder.

### download-deps

Downloads all the JAR dependencies declared in `config.xml`, and required
to correctly build the project. The JAR files are extracted and placed in
the `dep` folder. When compiling (with the `compile` task), the compiler
is instructed to include these JARs in its classpath. Depending on the
setting specified in `config.xml`, these JARs are also bundled in the
output JAR file of the `jar` task.

### download-rt6

Downloads the bootstrap classpath (`rt.jar`) for Java 6, and places it in
the project's root folder. See [cross-compiling]{#xcompile}.

Continuous integration                                               {#ci}
----------------------

AntRun makes it easy to use [continuous
integration](https://en.wikipedia.org/wiki/Continuous_integration) services
like [Travis CI](https://travis-ci.org) or
[Semaphore](http://semaphoreapp.com). The sequence of commands to
automatically setup the environment, build and test it is (for Linux):

    $ ant download-deps
    $ sudo ant install-deps
    $ ant dist test

The second command must be run as administrator, as it copies the required
dependencies into a system folder that generally requires that access. For
Windows systems, running as administrator is done with the
[`runas` command](https://technet.microsoft.com/en-us/library/cc771525.aspx#BKMK_examples).

Notice how, apart from the call to `sudo`, all the process is
platform-independent.

Cross-compiling                                                 {#xcompile}
---------------

The `.class` files are marked with the major version number of the compiler
that created them; hence a file compiled with JDK 1.7 will contain this
version number in its metadata. A JRE 1.6 will refuse to run them,
regardless of whether they were built from 1.6-compliant code.
*Cross-compiling* is necessary if one wants to make a project compatible
with a version of Java earlier than the one used to compile it. 

By default, AntRun compiles your project using the default JDK installed on
your computer. However, you can compile files that are compatible with
a specific version of Java by putting the *bootstrap* JAR file `rt.jar`
that corresponds to that version in the project's root folder (i.e. in the
same folder as `build.xml`). When started, AntRun checks for the presence
bootstrap JAR; if present, it uses it instead of the system's bootstrap
classpath.

For example, if one downloads the `rt.jar` file from JDK 1.6 (using
the `download-rt6` task), the compiled files will be able to be run by
a Java 6 virtual machine. (Assuming the code itself is Java 6-compliant.)

Projects that use AntRun                                        {#projects}
------------------------

Virtually every Java project developed at [LIF](http://liflab.ca) uses
an AntRun template project. This includes:

- [Azrael](https://github.com/sylvainhalle/Azrael), a generic serialization
  library
- [BeepBeep 3](https://liflab.github.io/beepbeep-3), an event stream
  processing engine, and most of its
  [palettes](https://github.com/liflab/beepbeep-3-palettes)
- [Bullwinkle](https://github.com/sylvainhalle/Bullwinkle), a runtime BNF
  parser
- [Jerrydog](https://github.com/sylvainhalle/Jerrydog), a lightweight web
  server
- [LabPal](https://liflab.github.io/labpal), a framework for running
  computer experiments

...and more.

About the author                                                   {#about}
----------------

AntRun was written by [Sylvain Hallé](http://leduotang.ca/sylvain),
associate professor at [Université du Québec à
Chicoutimi](http://www.uqac.ca/), Canada.
