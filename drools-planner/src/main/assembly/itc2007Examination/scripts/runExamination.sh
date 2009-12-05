#!/bin/sh

mainJar=lib/drools-solver-examples-${project.version}.jar
mainClass=org.drools.solver.examples.itc2007.examination.app.ExaminationShellApp

echo "Usage: ./runExamination.sh [maximumSecondsSpendPerSolution]"
echo "For example: ./runExamination.sh 429"
echo "Some notes:"
echo "- Working dir should be the directory of this script."
echo "- Java should be at least JDK 6 (not just JRE 6)"
echo "- Environment variable JAVA_HOME should be set to the JDK installation directory"
echo "  For example: export JAVA_HOME=/usr/lib/sun-jdk-...-6"
echo
echo "Starting..."

# -Xmx128M or less works too, but might be slower
$JAVA_HOME/bin/java -server -Xmx256M -cp ${mainJar} ${mainClass} $*
