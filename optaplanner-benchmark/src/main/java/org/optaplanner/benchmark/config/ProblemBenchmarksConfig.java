/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.benchmark.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.io.FilenameUtils;
import org.optaplanner.benchmark.config.statistic.ProblemStatisticType;
import org.optaplanner.benchmark.config.statistic.SingleStatisticType;
import org.optaplanner.benchmark.impl.loader.FileProblemProvider;
import org.optaplanner.benchmark.impl.loader.ProblemProvider;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;
import org.optaplanner.benchmark.impl.result.ProblemBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SingleBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SubSingleBenchmarkResult;
import org.optaplanner.benchmark.impl.statistic.ProblemStatistic;
import org.optaplanner.core.config.AbstractConfig;
import org.optaplanner.core.config.SolverConfigContext;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

@XStreamAlias("problemBenchmarks")
public class ProblemBenchmarksConfig extends AbstractConfig<ProblemBenchmarksConfig> {

    private Class<SolutionFileIO> solutionFileIOClass = null;
    @XStreamImplicit(itemFieldName = "xStreamAnnotatedClass")
    private List<Class> xStreamAnnotatedClassList = null;
    private Boolean writeOutputSolutionEnabled = null;

    @XStreamImplicit(itemFieldName = "inputSolutionFile")
    private List<File> inputSolutionFileList = null;

    @XStreamImplicit(itemFieldName = "problemStatisticType")
    private List<ProblemStatisticType> problemStatisticTypeList = null;

    @XStreamImplicit(itemFieldName = "singleStatisticType")
    private List<SingleStatisticType> singleStatisticTypeList = null;

    // ************************************************************************
    // Constructors and simple getters/setters
    // ************************************************************************

    public Class<SolutionFileIO> getSolutionFileIOClass() {
        return solutionFileIOClass;
    }

    public void setSolutionFileIOClass(Class<SolutionFileIO> solutionFileIOClass) {
        this.solutionFileIOClass = solutionFileIOClass;
    }

    public List<Class> getXStreamAnnotatedClassList() {
        return xStreamAnnotatedClassList;
    }

    public void setXStreamAnnotatedClassList(List<Class> xStreamAnnotatedClassList) {
        this.xStreamAnnotatedClassList = xStreamAnnotatedClassList;
    }

    public Boolean getWriteOutputSolutionEnabled() {
        return writeOutputSolutionEnabled;
    }

    public void setWriteOutputSolutionEnabled(Boolean writeOutputSolutionEnabled) {
        this.writeOutputSolutionEnabled = writeOutputSolutionEnabled;
    }

    public List<File> getInputSolutionFileList() {
        return inputSolutionFileList;
    }

    public void setInputSolutionFileList(List<File> inputSolutionFileList) {
        this.inputSolutionFileList = inputSolutionFileList;
    }

    public List<ProblemStatisticType> getProblemStatisticTypeList() {
        return problemStatisticTypeList;
    }

    public void setProblemStatisticTypeList(List<ProblemStatisticType> problemStatisticTypeList) {
        this.problemStatisticTypeList = problemStatisticTypeList;
    }

    public List<SingleStatisticType> getSingleStatisticTypeList() {
        return singleStatisticTypeList;
    }

    public void setSingleStatisticTypeList(List<SingleStatisticType> singleStatisticTypeList) {
        this.singleStatisticTypeList = singleStatisticTypeList;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public void buildProblemBenchmarkList(SolverConfigContext solverConfigContext,
            SolverBenchmarkResult solverBenchmarkResult) {
        validate(solverBenchmarkResult);
        PlannerBenchmarkResult plannerBenchmarkResult = solverBenchmarkResult.getPlannerBenchmarkResult();
        SolutionFileIO solutionFileIO = buildSolutionFileIO();
        List<ProblemBenchmarkResult> problemBenchmarkResultList = new ArrayList<>(inputSolutionFileList.size());
        List<ProblemBenchmarkResult> unifiedProblemBenchmarkResultList
                = plannerBenchmarkResult.getUnifiedProblemBenchmarkResultList();
        for (File inputSolutionFile : inputSolutionFileList) {
            if (!inputSolutionFile.exists()) {
                throw new IllegalArgumentException("The inputSolutionFile (" + inputSolutionFile + ") does not exist.");
            }
            // 2 SolverBenchmarks containing equal ProblemBenchmarks should contain the same instance
            ProblemBenchmarkResult newProblemBenchmarkResult = buildProblemBenchmark(
                    solverConfigContext, plannerBenchmarkResult,
                    new FileProblemProvider(solutionFileIO, inputSolutionFile));
            ProblemBenchmarkResult problemBenchmarkResult;
            int index = unifiedProblemBenchmarkResultList.indexOf(newProblemBenchmarkResult);
            if (index < 0) {
                problemBenchmarkResult = newProblemBenchmarkResult;
                unifiedProblemBenchmarkResultList.add(problemBenchmarkResult);
            } else {
                problemBenchmarkResult = unifiedProblemBenchmarkResultList.get(index);
            }
            problemBenchmarkResultList.add(problemBenchmarkResult);
            buildSingleBenchmark(solverConfigContext, solverBenchmarkResult, problemBenchmarkResult);
        }
    }

    private void validate(SolverBenchmarkResult solverBenchmarkResult) {
        if (ConfigUtils.isEmptyCollection(inputSolutionFileList)) {
            throw new IllegalArgumentException(
                    "Configure at least 1 <inputSolutionFile> for the solverBenchmarkResult (" + solverBenchmarkResult.getName()
                            + ") directly or indirectly by inheriting it.");
        }
    }

    private SolutionFileIO buildSolutionFileIO() {
        if (solutionFileIOClass != null && xStreamAnnotatedClassList != null) {
            throw new IllegalArgumentException("Cannot use solutionFileIOClass (" + solutionFileIOClass
                    + ") and xStreamAnnotatedClassList (" + xStreamAnnotatedClassList + ") together.");
        }
        if (solutionFileIOClass != null) {
            return ConfigUtils.newInstance(this, "solutionFileIOClass", solutionFileIOClass);
        } else {
            Class[] xStreamAnnotatedClasses;
            if (xStreamAnnotatedClassList != null) {
                xStreamAnnotatedClasses = xStreamAnnotatedClassList.toArray(new Class[0]);
            } else {
                xStreamAnnotatedClasses = new Class[0];
            }
            return new XStreamSolutionFileIO(xStreamAnnotatedClasses);
        }
    }

    private ProblemBenchmarkResult buildProblemBenchmark(SolverConfigContext solverConfigContext,
            PlannerBenchmarkResult plannerBenchmarkResult,
            ProblemProvider problemProvider) {
        ProblemBenchmarkResult problemBenchmarkResult = new ProblemBenchmarkResult(plannerBenchmarkResult);
        problemBenchmarkResult.setName(problemProvider.getProblemName());
        problemBenchmarkResult.setProblemProvider(problemProvider);
        problemBenchmarkResult.setWriteOutputSolutionEnabled(
                writeOutputSolutionEnabled == null ? false : writeOutputSolutionEnabled);
        List<ProblemStatistic> problemStatisticList = new ArrayList<>(
                problemStatisticTypeList == null ? 0 : problemStatisticTypeList.size());
        if (problemStatisticTypeList != null) {
            for (ProblemStatisticType problemStatisticType : problemStatisticTypeList) {
                problemStatisticList.add(problemStatisticType.buildProblemStatistic(problemBenchmarkResult));
            }
        }
        problemBenchmarkResult.setProblemStatisticList(problemStatisticList);
        problemBenchmarkResult.setSingleBenchmarkResultList(new ArrayList<>());
        return problemBenchmarkResult;
    }

    private void buildSingleBenchmark(SolverConfigContext solverConfigContext,
            SolverBenchmarkResult solverBenchmarkResult, ProblemBenchmarkResult problemBenchmarkResult) {
        SingleBenchmarkResult singleBenchmarkResult = new SingleBenchmarkResult(solverBenchmarkResult, problemBenchmarkResult);
        buildSubSingleBenchmarks(singleBenchmarkResult, solverBenchmarkResult.getSubSingleCount());
        for (SubSingleBenchmarkResult subSingleBenchmarkResult : singleBenchmarkResult.getSubSingleBenchmarkResultList()) {
            subSingleBenchmarkResult.setPureSubSingleStatisticList(new ArrayList<>(
                    singleStatisticTypeList == null ? 0 : singleStatisticTypeList.size()));
        }
        if (singleStatisticTypeList != null) {
            for (SingleStatisticType singleStatisticType : singleStatisticTypeList) {
                for (SubSingleBenchmarkResult subSingleBenchmarkResult : singleBenchmarkResult.getSubSingleBenchmarkResultList()) {
                    subSingleBenchmarkResult.getPureSubSingleStatisticList().add(singleStatisticType.buildPureSubSingleStatistic(subSingleBenchmarkResult));
                }
            }
        }
        singleBenchmarkResult.initSubSingleStatisticMaps();
        solverBenchmarkResult.getSingleBenchmarkResultList().add(singleBenchmarkResult);
        problemBenchmarkResult.getSingleBenchmarkResultList().add(singleBenchmarkResult);
    }

    private void buildSubSingleBenchmarks(SingleBenchmarkResult parent, int subSingleCount) {
        List<SubSingleBenchmarkResult> subSingleBenchmarkResultList = new ArrayList<>(subSingleCount);
        for (int i = 0; i < subSingleCount; i++) {
            SubSingleBenchmarkResult subSingleBenchmarkResult = new SubSingleBenchmarkResult(parent, i);
            subSingleBenchmarkResultList.add(subSingleBenchmarkResult);
        }
        parent.setSubSingleBenchmarkResultList(subSingleBenchmarkResultList);
    }

    @Override
    public void inherit(ProblemBenchmarksConfig inheritedConfig) {
        solutionFileIOClass = ConfigUtils.inheritOverwritableProperty(solutionFileIOClass,
                inheritedConfig.getSolutionFileIOClass());
        xStreamAnnotatedClassList = ConfigUtils.inheritMergeableListProperty(xStreamAnnotatedClassList,
                inheritedConfig.getXStreamAnnotatedClassList());
        writeOutputSolutionEnabled = ConfigUtils.inheritOverwritableProperty(writeOutputSolutionEnabled,
                inheritedConfig.getWriteOutputSolutionEnabled());
        inputSolutionFileList = ConfigUtils.inheritMergeableListProperty(inputSolutionFileList,
                inheritedConfig.getInputSolutionFileList());
        problemStatisticTypeList = ConfigUtils.inheritMergeableListProperty(problemStatisticTypeList,
                inheritedConfig.getProblemStatisticTypeList());
        singleStatisticTypeList = ConfigUtils.inheritMergeableListProperty(singleStatisticTypeList,
                inheritedConfig.getSingleStatisticTypeList());
    }

}
