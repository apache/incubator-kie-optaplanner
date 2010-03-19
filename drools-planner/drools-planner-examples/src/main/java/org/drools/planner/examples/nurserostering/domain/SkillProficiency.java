package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("SkillProficiency")
public class SkillProficiency extends AbstractPersistable implements Comparable<SkillProficiency> {

    private Employee employee;
    private Skill skill;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public int compareTo(SkillProficiency other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(skill, other.skill)
                .toComparison();
    }

    @Override
    public String toString() {
        return employee + "-" + skill;
    }

}
