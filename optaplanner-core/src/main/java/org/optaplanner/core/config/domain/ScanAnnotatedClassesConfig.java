/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.config.domain;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.drools.core.common.ProjectClassLoader;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.config.AbstractConfig;
import org.optaplanner.core.config.SolverConfigContext;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XStreamAlias("scanAnnotatedClasses")
public class ScanAnnotatedClassesConfig extends AbstractConfig<ScanAnnotatedClassesConfig> {

    @XStreamImplicit(itemFieldName = "packageInclude")
    private List<String> packageIncludeList = null;

    public List<String> getPackageIncludeList() {
        return packageIncludeList;
    }

    public void setPackageIncludeList(List<String> packageIncludeList) {
        this.packageIncludeList = packageIncludeList;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public SolutionDescriptor buildSolutionDescriptor(SolverConfigContext configContext) {
        ClassLoader[] classLoaders;
        if (configContext.getClassLoader() != null) {
            classLoaders = new ClassLoader[] {configContext.getClassLoader()};
        } else if (configContext.getKieContainer() != null) {
            ClassLoader kieContainerClassLoader = configContext.getKieContainer().getClassLoader();
            if (kieContainerClassLoader instanceof ProjectClassLoader) {
                // TODO this does not work if the kjar contains java source files which are not compiled in advance
                // see ignored tests in KieContainerSolverFactoryTest
                ClassLoader parent = kieContainerClassLoader.getParent();
                classLoaders = new ClassLoader[] {parent};
            } else {
                throw new IllegalStateException("The kieContainer (" + configContext.getKieContainer()
                        + ")'s class loader (" + kieContainerClassLoader
                        + ") is not a " + ProjectClassLoader.class.getSimpleName() + ".");
            }
        } else {
            classLoaders = new ClassLoader[0];
        }
        ConfigurationBuilder builder = new ConfigurationBuilder();
        if (!ConfigUtils.isEmptyCollection(packageIncludeList)) {
            FilterBuilder filterBuilder = new FilterBuilder();
            for (String packageInclude : packageIncludeList) {
                builder.addUrls(ClasspathHelper.forPackage(packageInclude, classLoaders));
                filterBuilder.includePackage(packageInclude);
            }
            builder.filterInputsBy(filterBuilder);
        } else {
            builder.addUrls(ClasspathHelper.forPackage("", classLoaders));
        }
        Reflections reflections = new Reflections(builder);
        Class<? extends Solution> solutionClass = loadSolutionClass(reflections);
        List<Class<?>> entityClassList = loadEntityClassList(reflections);
        return SolutionDescriptor.buildSolutionDescriptor(solutionClass, entityClassList);
    }

    protected Class<? extends Solution> loadSolutionClass(Reflections reflections) {
        Set<Class<?>> solutionClassSet = reflections.getTypesAnnotatedWith(PlanningSolution.class);
        retainOnlyClassesWithDeclaredAnnotation(solutionClassSet, PlanningSolution.class);
        if (ConfigUtils.isEmptyCollection(solutionClassSet)) {
            throw new IllegalStateException("The scanAnnotatedClasses (" + this
                    + ") did not find any classes with a " + PlanningSolution.class.getSimpleName()
                    + " annotation.");
        } else if (solutionClassSet.size() > 1) {
            throw new IllegalStateException("The scanAnnotatedClasses (" + this
                    + ") found multiple classes (" + solutionClassSet
                    + ") with a " + PlanningSolution.class.getSimpleName() + " annotation.");
        }
        Class<? extends Solution> solutionClass = (Class<? extends Solution>) solutionClassSet.iterator().next();
        return solutionClass;
    }

    protected List<Class<?>> loadEntityClassList(Reflections reflections) {
        Set<Class<?>> entityClassSet = reflections.getTypesAnnotatedWith(PlanningEntity.class);
        retainOnlyClassesWithDeclaredAnnotation(entityClassSet, PlanningEntity.class);
        if (ConfigUtils.isEmptyCollection(entityClassSet)) {
            throw new IllegalStateException("The scanAnnotatedClasses (" + this
                    + ") did not find any classes with a " + PlanningEntity.class.getSimpleName()
                    + " annotation.");
        }
        return new ArrayList<Class<?>>(entityClassSet);
    }

    private void retainOnlyClassesWithDeclaredAnnotation(Set<Class<?>> classSet, Class<? extends Annotation> annotation) {
        for (Iterator<Class<?>> it = classSet.iterator(); it.hasNext(); ) {
            Class<?> clazz = it.next();
            if (!clazz.isAnnotationPresent(annotation)) {
                it.remove();
            }
        }
    }

    public void inherit(ScanAnnotatedClassesConfig inheritedConfig) {
        packageIncludeList = ConfigUtils.inheritMergeableListProperty(
                packageIncludeList, inheritedConfig.getPackageIncludeList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + (packageIncludeList == null ? "" : packageIncludeList) + ")";
    }

}
