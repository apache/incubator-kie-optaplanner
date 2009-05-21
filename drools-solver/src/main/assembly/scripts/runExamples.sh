#!/bin/sh

mainJar=lib/drools-solver-examples-${project.version}.jar
mainClass=org.drools.solver.examples.app.ExamplesApp

echo "Usage: ./runExamples.sh"
echo "For example: ./runExamples.sh"
echo "Some notes:"
echo "- Working dir should be the directory of this script."
echo "- Java is recommended to be JDK and java 6 for optimal performance"
echo "- The environment variable JAVA_HOME should be set to the JDK installation directory"
echo "  For example: export JAVA_HOME=/usr/lib/jvm/java-6-sun"
echo
echo "Starting examples app..."

# -Xmx128M or less works too, but it might be slower
$JAVA_HOME/bin/java -Xms256m -Xmx512m -server -cp ${mainJar} ${mainClass} $*
