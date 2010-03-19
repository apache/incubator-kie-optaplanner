package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("ShiftTypeSkillRequirement")
public class ShiftTypeSkillRequirement extends AbstractPersistable implements Comparable<ShiftTypeSkillRequirement> {

    private ShiftType shiftType;
    private Skill skill;

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public int compareTo(ShiftTypeSkillRequirement other) {
        return new CompareToBuilder()
                .append(shiftType, other.shiftType)
                .append(skill, other.skill)
                .toComparison();
    }

    @Override
    public String toString() {
        return shiftType + "-" + skill;
    }

}
