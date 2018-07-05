#! /bin/bash

# Performs the last post-processing steps before building the package
# (C) 2018 Sylvain Hallé

PKG_FOLDER=contents
BRANCH_NAME=dev

# Create folders
mkdir -p $PKG_FOLDER/opt/textidote
mkdir -p $PKG_FOLDER/usr/local/bin
mkdir -p $PKG_FOLDER/usr/share/man/man1
mkdir -p $PKG_FOLDER/etc/bash.completion.d

# Fetch JAR and other files from master branch
if [ ! -f $PKG_FOLDER/opt/textidote/textidote.jar ]; then
	echo "Downloading latest release of textidote.jar"
	wget https://github.com/sylvainhalle/textidote/releases/download/v0.4/textidote.jar -P $PKG_FOLDER/opt/textidote
fi
git show $BRANCH_NAME:Completions/textidote.zsh > $PKG_FOLDER/opt/textidote/textidote.zsh
git show $BRANCH_NAME:Completions/textidote.bash > $PKG_FOLDER/etc/bash.completion.d/textidote

# Create MD5 checksums
pushd $PKG_FOLDER
find . -type f ! -regex '.*~' ! -regex '.*.hg.*' ! -regex '.*?debian-binary.*' ! -regex '.*?debian.*' -printf '%P ' | xargs md5sum > DEBIAN/md5sums
popd

# Build package
dpkg -b contents .
