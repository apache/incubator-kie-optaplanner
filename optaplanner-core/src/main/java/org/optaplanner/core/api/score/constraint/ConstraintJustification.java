package org.optaplanner.core.api.score.constraint;

import java.util.Comparator;

/**
 * Used by the GUI to sort the {@link ConstraintMatch} list by {@link ConstraintMatch#getJustificationList()}.
 */
public interface ConstraintJustification {

    /**
     * Sorts the {@link ConstraintJustification}s in the ascending order of their {@link #getId()}.
     */
    Comparator<ConstraintJustification> COMPARATOR =
            Comparator.comparing((ConstraintJustification j) -> j.getClass().getName())
                    .thenComparingLong(ConstraintJustification::getId);

    Long getId();
}
