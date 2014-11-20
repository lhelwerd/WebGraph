#!/bin/bash

mvn=$(which mvn)

if [[ "x$mvn" = "x" ]]; then
	if [[ ! -L "maven" ]]; then
		echo "Specify the root maven directory (e.g. /scratch/.../apache-maven-3.2.3):"
		read -r dir
		ln -s $dir maven
	fi
	CWD=$(pwd)
	export JAVA_HOME="/usr/lib/jvm/java-7-openjdk-amd64"
	export M2_HOME="$CWD/maven"
	export M2="$M2_HOME/bin"
	PATH="$M2:$PATH"

	echo "You can now use mvn itself (at least until the end of the session)."
	echo "You can also add the following lines to your .bashrc to keep this:"
	echo "export JAVA_HOME=\"/usr/lib/jvm/java-7-openjdk-amd64\""
	echo "export M2_HOME=\"$CWD/maven\""
	echo "export M2=\"\$M2_HOME/bin\""
	echo "PATH=\"\$M2:\$PATH\""
	mvn="$CWD/maven/bin/mvn"
fi

if [[ "x$@" = "x" ]]; then
	$mvn --version
else
	$mvn $@
fi
