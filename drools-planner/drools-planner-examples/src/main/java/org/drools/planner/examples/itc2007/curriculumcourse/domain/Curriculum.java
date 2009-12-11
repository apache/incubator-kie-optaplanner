package org.drools.planner.examples.itc2007.curriculumcourse.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Curriculum extends AbstractPersistable implements Comparable<Curriculum> {

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int compareTo(Curriculum other) {
        return new CompareToBuilder()
                .append(code, other.code)
                .toComparison();
    }

    @Override
    public String toString() {
        return code;
    }

}