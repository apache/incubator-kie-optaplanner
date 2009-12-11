#!/bin/sh

mainClass=org.drools.planner.examples.pas.app.PatientAdmissionScheduleBenchmarkApp
if [ $# -ge 1 ];
  then
    mvn exec:exec -Dexec.mainClass="${mainClass}" -Dexec.programArgs="$*"
  else
    mvn exec:exec -Dexec.mainClass="${mainClass}"
fi
