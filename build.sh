#! /bin/bash

# Performs the last post-processing steps before building the package
# (C) 2018 Sylvain Hallé

PKG_FOLDER=contents

# Fetch JAR and other files from master branch
git show master:textidote.jar > $PKG_FOLDER/opt/textidote/textidote.jar
#git show master:Completions/textidote.zsh > $PKG_FOLDER/opt/textidote/textidote.zsh
#git show master:Completions/textidote.bash > $PKG_FOLDER/etc/bash.completion.d/textidote

# Create MD5 checksums
pushd $PKG_FOLDER
find . -type f ! -regex '.*~' ! -regex '.*.hg.*' ! -regex '.*?debian-binary.*' ! -regex '.*?debian.*' -printf '%P ' | xargs md5sum > DEBIAN/md5sums
popd

# Build package
dpkg -b contents .
