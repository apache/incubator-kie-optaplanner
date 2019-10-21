<!--
  ~ Copyright 2019 Red Hat, Inc. and/or its affiliates.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<plannerBenchmark>
  <benchmarkDirectory>local/data/scoreDirectorFactory</benchmarkDirectory>
  <parallelBenchmarkCount>AUTO</parallelBenchmarkCount>

  <inheritedSolverBenchmark>
    <solver>
      <termination>
        <minutesSpentLimit>1</minutesSpentLimit>
      </termination>
    </solver>
  </inheritedSolverBenchmark>

<#macro scoreDirectorDetails exampleId>
  <#if exampleId == "cloudBalancing">
    <initializingScoreTrend>ONLY_DOWN/ONLY_DOWN</initializingScoreTrend>
  <#elseif exampleId == "nQueens">
    <initializingScoreTrend>ONLY_DOWN</initializingScoreTrend>
  <#elseif exampleId == "vehicleRouting">
    <initializingScoreTrend>ONLY_DOWN</initializingScoreTrend>
  </#if>
</#macro>

<#macro solverDetails benchmarkDescriptor>
  <solutionClass>${benchmarkDescriptor.getSolutionClass()}</solutionClass>
  <#list benchmarkDescriptor.getEntityClasses() as entityClass>
    <entityClass>${entityClass}</entityClass>
  </#list>
  <constructionHeuristic/>
  <localSearch />
</#macro>

<#macro problemDetails benchmarkDescriptor>
  <problemBenchmarks>
    <#if benchmarkDescriptor.getSolutionFileIoClass()??>
      <solutionFileIOClass>${benchmarkDescriptor.getSolutionFileIoClass()}</solutionFileIOClass>
    <#elseif benchmarkDescriptor.getXStreamAnnotatedClass()??>
      <xStreamAnnotatedClass>${benchmarkDescriptor.getXStreamAnnotatedClass()}</xStreamAnnotatedClass>
    </#if>
    <inputSolutionFile>${benchmarkDescriptor.getInputSolutionFile()}</inputSolutionFile>
    <problemStatisticType>BEST_SCORE</problemStatisticType>
    <problemStatisticType>SCORE_CALCULATION_SPEED</problemStatisticType>
  </problemBenchmarks>
</#macro>

<#list benchmarkDescriptors as benchmarkDescriptor>
  <#if benchmarkDescriptor.getEasyScoreCalculator()??>
    <solverBenchmark>
      <@problemDetails benchmarkDescriptor/>
      <name>${benchmarkDescriptor.getExampleId()} Easy (Java)</name>
      <solver>
        <scoreDirectorFactory>
          <easyScoreCalculatorClass>${benchmarkDescriptor.getEasyScoreCalculator()}</easyScoreCalculatorClass>
          <@scoreDirectorDetails benchmarkDescriptor.getExampleId()/>
        </scoreDirectorFactory>
        <@solverDetails benchmarkDescriptor/>
      </solver>
    </solverBenchmark>
  </#if>
  <#if benchmarkDescriptor.getIncrementalScoreCalculator()??>
    <solverBenchmark>
      <@problemDetails benchmarkDescriptor/>
      <name>${benchmarkDescriptor.getExampleId()} Incremental (Java)</name>
      <solver>
        <scoreDirectorFactory>
          <incrementalScoreCalculatorClass>${benchmarkDescriptor.getIncrementalScoreCalculator()}</incrementalScoreCalculatorClass>
          <@scoreDirectorDetails benchmarkDescriptor.getExampleId()/>
        </scoreDirectorFactory>
        <@solverDetails benchmarkDescriptor/>
      </solver>
    </solverBenchmark>
  </#if>
  <#if benchmarkDescriptor.getConstraintProvider()??>
    <solverBenchmark>
      <@problemDetails benchmarkDescriptor/>
      <name>${benchmarkDescriptor.getExampleId()} Constraint Streams (Bavet)</name>
      <solver>
        <scoreDirectorFactory>
          <constraintStreamImplType>BAVET</constraintStreamImplType>
          <constraintProviderClass>${benchmarkDescriptor.getConstraintProvider()}</constraintProviderClass>
          <@scoreDirectorDetails benchmarkDescriptor.getExampleId()/>
        </scoreDirectorFactory>
        <@solverDetails benchmarkDescriptor/>
      </solver>
    </solverBenchmark>
    <solverBenchmark>
      <@problemDetails benchmarkDescriptor/>
      <name>${benchmarkDescriptor.getExampleId()} Constraint Streams (Drools)</name>
      <solver>
        <scoreDirectorFactory>
          <constraintStreamImplType>DROOLS</constraintStreamImplType>
          <constraintProviderClass>${benchmarkDescriptor.getConstraintProvider()}</constraintProviderClass>
          <@scoreDirectorDetails benchmarkDescriptor.getExampleId()/>
        </scoreDirectorFactory>
        <@solverDetails benchmarkDescriptor/>
      </solver>
    </solverBenchmark>
  </#if>
  <#if benchmarkDescriptor.getDrlFile()??>
    <solverBenchmark>
      <@problemDetails benchmarkDescriptor/>
      <name>${benchmarkDescriptor.getExampleId()} DRL (Drools)</name>
      <solver>
        <scoreDirectorFactory>
          <scoreDrl>${benchmarkDescriptor.getDrlFile()}</scoreDrl>
          <@scoreDirectorDetails benchmarkDescriptor.getExampleId()/>
        </scoreDirectorFactory>
        <@solverDetails benchmarkDescriptor/>
      </solver>
    </solverBenchmark>
  </#if>
</#list>
</plannerBenchmark>
