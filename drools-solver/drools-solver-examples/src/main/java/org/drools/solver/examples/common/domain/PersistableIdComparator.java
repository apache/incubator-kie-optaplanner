package org.drools.solver.examples.common.domain;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Geoffrey De Smet
 */
public class PersistableIdComparator implements Comparator<AbstractPersistable> {

    public int compare(AbstractPersistable a, AbstractPersistable b) {
        return new CompareToBuilder().append(a.getId(), b.getId()).toComparison();
    }

}
