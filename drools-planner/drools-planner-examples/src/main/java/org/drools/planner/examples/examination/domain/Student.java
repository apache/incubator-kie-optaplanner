package org.drools.planner.examples.examination.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * Not used during score calculation, so not inserted into the working memory.
 * @author Geoffrey De Smet
 */
@XStreamAlias("Student")
public class Student extends AbstractPersistable implements Comparable<Student> {
    
    public int compareTo(Student other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

}
