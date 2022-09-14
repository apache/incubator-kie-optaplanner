package org.optaplanner.constraint.streams.common.inliner;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;

import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.score.stream.DefaultConstraintJustification;

/**
 * Allows to create justifications and indictments lazily if and only if constraint matches are enabled.
 *
 * Justification and indictment creation is performance expensive and constraint matches are typically disabled.
 * So justifications and indictments are created lazily, outside of the typical hot path.
 */
public final class JustificationsSupplier {

    public static JustificationsSupplier empty() {
        return new JustificationsSupplier(DefaultConstraintJustification::empty, Collections::emptyList);
    }

    public static JustificationsSupplier of(Supplier<ConstraintJustification> constraintJustificationSupplier,
            Supplier<Collection<?>> indictedObjectsSupplier) {
        return new JustificationsSupplier(constraintJustificationSupplier, indictedObjectsSupplier);
    }

    private final Supplier<ConstraintJustification> constraintJustificationSupplier;
    private final Supplier<Collection<?>> indictedObjectsSupplier;

    private JustificationsSupplier(Supplier<ConstraintJustification> constraintJustificationSupplier,
            Supplier<Collection<?>> indictedObjectsSupplier) {
        this.constraintJustificationSupplier = Objects.requireNonNull(constraintJustificationSupplier);
        this.indictedObjectsSupplier = Objects.requireNonNull(indictedObjectsSupplier);
    }

    public ConstraintJustification createConstraintJustification() {
        return constraintJustificationSupplier.get();
    }

    public Collection<?> createIndictedObjects() {
        return indictedObjectsSupplier.get();
    }

}
