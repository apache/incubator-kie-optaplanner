/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.taskassigning.domain;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.CollectionPlanningVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.common.swingui.components.Labeled;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@PlanningEntity
@XStreamAlias("TaEmployee")
public class Employee extends AbstractPersistable implements Labeled {

    private String fullName;

    private Set<Skill> skillSet;
    private Map<Customer, Affinity> affinityMap;

    // TODO maybe needs graphType=DISJOINT_LIST(_ORDERED)
    // - disjoint because otherwise the inverse relation shadow variable would be a collection
    // - ordered because otherwise index shadow variable is not possible
    @CollectionPlanningVariable(valueRangeProviderRefs = "taskRange")
    private List<Task> tasks;

    // TODO pinning

    public Employee() {
    }

    public Employee(long id, String fullName) {
        super(id);
        this.fullName = fullName;
        skillSet = new LinkedHashSet<>();
        affinityMap = new LinkedHashMap<>();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<Skill> getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(Set<Skill> skillSet) {
        this.skillSet = skillSet;
    }

    public Map<Customer, Affinity> getAffinityMap() {
        return affinityMap;
    }

    public void setAffinityMap(Map<Customer, Affinity> affinityMap) {
        this.affinityMap = affinityMap;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @param customer never null
     * @return never null
     */
    public Affinity getAffinity(Customer customer) {
        Affinity affinity = affinityMap.get(customer);
        if (affinity == null) {
            affinity = Affinity.NONE;
        }
        return affinity;
    }

    public Integer getEndTime() {
        return tasks.isEmpty() ? 0 : tasks.get(tasks.size() - 1).getEndTime();
    }

    @Override
    public String getLabel() {
        return fullName;
    }

    public String getToolText() {
        StringBuilder toolText = new StringBuilder();
        toolText.append("<html><center><b>").append(fullName).append("</b><br/><br/>");
        toolText.append("Skills:<br/>");
        for (Skill skill : skillSet) {
            toolText.append(skill.getLabel()).append("<br/>");
        }
        toolText.append("</center></html>");
        return toolText.toString();
    }

    // equals & hashCode added because the UI needs to find and match employees from solution's employeeList and Task's
    // employee shadow variable when building and using the employeeIndexMap in TaskOverviewPanel.
    //
    // This a quick workaround.
    //
    // Solution's employeeList and Task's employee contain different Employee clones, which I think is unexpected.
    // This issue might be fixed by correctly implementing variable listener for Task's employee or by updating
    // the solution cloner.

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return fullName;
    }

}
