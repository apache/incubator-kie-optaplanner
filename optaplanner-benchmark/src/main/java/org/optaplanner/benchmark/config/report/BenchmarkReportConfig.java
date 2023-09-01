/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.benchmark.config.report;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.optaplanner.benchmark.config.ranking.SolverRankingType;
import org.optaplanner.benchmark.impl.ranking.SolverRankingWeightFactory;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.core.config.AbstractConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.io.jaxb.adapter.JaxbLocaleAdapter;

@XmlType(propOrder = {
        "locale",
        "solverRankingType",
        "solverRankingComparatorClass",
        "solverRankingWeightFactoryClass"
})
public class BenchmarkReportConfig extends AbstractConfig<BenchmarkReportConfig> {

    @XmlJavaTypeAdapter(JaxbLocaleAdapter.class)
    private Locale locale = null;
    private SolverRankingType solverRankingType = null;
    private Class<? extends Comparator<SolverBenchmarkResult>> solverRankingComparatorClass = null;
    private Class<? extends SolverRankingWeightFactory> solverRankingWeightFactoryClass = null;

    public BenchmarkReportConfig() {
    }

    public BenchmarkReportConfig(BenchmarkReportConfig inheritedConfig) {
        inherit(inheritedConfig);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public SolverRankingType getSolverRankingType() {
        return solverRankingType;
    }

    public void setSolverRankingType(SolverRankingType solverRankingType) {
        this.solverRankingType = solverRankingType;
    }

    public Class<? extends Comparator<SolverBenchmarkResult>> getSolverRankingComparatorClass() {
        return solverRankingComparatorClass;
    }

    public void setSolverRankingComparatorClass(
            Class<? extends Comparator<SolverBenchmarkResult>> solverRankingComparatorClass) {
        this.solverRankingComparatorClass = solverRankingComparatorClass;
    }

    public Class<? extends SolverRankingWeightFactory> getSolverRankingWeightFactoryClass() {
        return solverRankingWeightFactoryClass;
    }

    public void setSolverRankingWeightFactoryClass(
            Class<? extends SolverRankingWeightFactory> solverRankingWeightFactoryClass) {
        this.solverRankingWeightFactoryClass = solverRankingWeightFactoryClass;
    }

    public Locale determineLocale() {
        return getLocale() == null ? Locale.getDefault() : getLocale();
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public BenchmarkReportConfig withLocale(Locale locale) {
        this.setLocale(locale);
        return this;
    }

    public BenchmarkReportConfig withSolverRankingType(SolverRankingType solverRankingType) {
        this.setSolverRankingType(solverRankingType);
        return this;
    }

    public BenchmarkReportConfig withSolverRankingComparatorClass(
            Class<? extends Comparator<SolverBenchmarkResult>> solverRankingComparatorClass) {
        this.setSolverRankingComparatorClass(solverRankingComparatorClass);
        return this;
    }

    public BenchmarkReportConfig withSolverRankingWeightFactoryClass(
            Class<? extends SolverRankingWeightFactory> solverRankingWeightFactoryClass) {
        this.setSolverRankingWeightFactoryClass(solverRankingWeightFactoryClass);
        return this;
    }

    @Override
    public BenchmarkReportConfig inherit(BenchmarkReportConfig inheritedConfig) {
        locale = ConfigUtils.inheritOverwritableProperty(locale, inheritedConfig.getLocale());
        solverRankingType = ConfigUtils.inheritOverwritableProperty(solverRankingType,
                inheritedConfig.getSolverRankingType());
        solverRankingComparatorClass = ConfigUtils.inheritOverwritableProperty(solverRankingComparatorClass,
                inheritedConfig.getSolverRankingComparatorClass());
        solverRankingWeightFactoryClass = ConfigUtils.inheritOverwritableProperty(solverRankingWeightFactoryClass,
                inheritedConfig.getSolverRankingWeightFactoryClass());
        return this;
    }

    @Override
    public BenchmarkReportConfig copyConfig() {
        return new BenchmarkReportConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        classVisitor.accept(solverRankingComparatorClass);
        classVisitor.accept(solverRankingWeightFactoryClass);
    }

}
