Shell auto-complete scripts for TeXtidote
=========================================

This folder contains shell scripts to allow auto-completion (with the TAB key)
when calling TeXtidote from the command line.

Bash
----

The Bash script is called `textidote.bash`. When installed using the Debian
package, it is placed in the folder `/etc/bash.completion.d` and renamed
`textidote`. Otherwise, you can download it and put it in the location of your
choice. To enable Bash auto-completion, locate your `.bashrc` file and add the
line:

    source /path/to/textidote.bash

Replace `/path/to` by the actual path to the location of the file.

Zsh
---

The Zsh script is called `textidote.zsh`. When installed using the Debian
package, it is placed in the folder `/opt/textidote`. Otherwise, you can
download it and put it in the location of your choice. To enable Zsh
auto-completion, locate your `.zshrc` file and add the line:

    source /path/to/textidote.zsh

Replace `/path/to` by the actual path to the location of the file.

<!-- :wrap=none:maxLineLen=78: -->