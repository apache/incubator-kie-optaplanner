package org.optaplanner.core.api.score.constraint;

import java.util.Comparator;

public interface ConstraintJustification {

    /**
     * Used by the GUI to sort the {@link ConstraintMatch} list by {@link ConstraintMatch#getJustificationList()}.
     */
    Comparator getConstraintJustificationComparator();

}
