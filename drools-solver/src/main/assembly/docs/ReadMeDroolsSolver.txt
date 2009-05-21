Welcome to drools-solver
========================

Introduction
------------

Drools Solver is in a beta state: backward incompatible changes may occur in the next version,
but the upgrade recipe makes it easy to upgrade your code to every new version.

Run the examples
----------------

To run the examples, run either of these:
Linux, Cygwin: ./runExamples.sh
Windows: runExamples.bat

Using drools-solver with maven 2
--------------------------------

If you're using maven 2 to handle your dependencies,
the jars and poms should be available in the jboss maven repository:
  http://repository.jboss.org/maven2/
You 'll want to add a dependency to drools-solver-core in your project:
<dependency>
  <groupId>org.drools.solver</groupId>
  <artifactId>drools-solver-core</artifactId>
  <version>${project.version}</version>
</dependency>

Using drools-solver otherwise
-----------------------------

The drools-solver jars are located in the lib directory.
To use drools-solver, you need all the jars except the drools-solver-examples jar.

Reference manual
----------------

Take a look at the online reference manual!
