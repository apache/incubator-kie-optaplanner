package org.optaplanner.core.api.score.constraint;

import java.util.Comparator;

public interface ConstraintJustification {

    /**
     * Used by the GUI to sort the {@link ConstraintMatch} list by {@link ConstraintMatch#getJustificationList()}. Must
     * be able to meaningfully compare classes of different types, if only by comparing their {@link Class#getName()}.
     */
    Comparator getConstraintJustificationComparator();

}
