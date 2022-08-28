package org.optaplanner.test.impl.score.stream;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactory;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public final class DefaultConstraintVerifier<ConstraintProvider_ extends ConstraintProvider, Solution_, Score_ extends Score<Score_>>
        implements ConstraintVerifier<ConstraintProvider_, Solution_> {

    /**
     * Exists so that people can not, even by accident, pick the same constraint ID as the default cache key.
     */
    private final String defaultScoreDirectorFactoryMapKey = UUID.randomUUID().toString();

    private final ConstraintProvider_ constraintProvider;
    /**
     * {@link java.util.ServiceLoader} is sensitive to classloaders.
     * Therefore we ensure that all SPI operations are bound to the current thread's classloader.
     * This also allows us to avoid thread safety concerns in the {@link ScoreDirectorFactoryCache}.
     */
    private final ThreadLocal<ScoreDirectorFactoryCache<ConstraintProvider_, Solution_, Score_>> scoreDirectorFactoryContainerThreadLocal;

    // Need to be volatile as they will be accessed from the thread-local score director factory cache.
    private volatile ConstraintStreamImplType constraintStreamImplType;
    private volatile Boolean droolsAlphaNetworkCompilationEnabled;

    public DefaultConstraintVerifier(ConstraintProvider_ constraintProvider, SolutionDescriptor<Solution_> solutionDescriptor) {
        this.constraintProvider = constraintProvider;
        this.scoreDirectorFactoryContainerThreadLocal =
                ThreadLocal.withInitial(() -> new ScoreDirectorFactoryCache<>(this, solutionDescriptor));
    }

    public ConstraintStreamImplType getConstraintStreamImplType() {
        return constraintStreamImplType;
    }

    @Override
    public ConstraintVerifier<ConstraintProvider_, Solution_> withConstraintStreamImplType(
            ConstraintStreamImplType constraintStreamImplType) {
        requireNonNull(constraintStreamImplType);
        if (droolsAlphaNetworkCompilationEnabled != null &&
                droolsAlphaNetworkCompilationEnabled &&
                constraintStreamImplType != ConstraintStreamImplType.DROOLS) {
            throw new IllegalArgumentException("Can not switch to " + ConstraintStreamImplType.class.getSimpleName()
                    + "." + constraintStreamImplType + " while Drools Alpha Network Compilation enabled.");
        }
        this.constraintStreamImplType = constraintStreamImplType;
        return this;
    }

    public Boolean getDroolsAlphaNetworkCompilationEnabled() {
        return droolsAlphaNetworkCompilationEnabled;
    }

    public boolean isDroolsAlphaNetworkCompilationEnabled() {
        return Objects.requireNonNullElse(droolsAlphaNetworkCompilationEnabled, !ConfigUtils.isNativeImage());
    }

    @Override
    public ConstraintVerifier<ConstraintProvider_, Solution_> withDroolsAlphaNetworkCompilationEnabled(
            boolean droolsAlphaNetworkCompilationEnabled) {
        if (droolsAlphaNetworkCompilationEnabled && getConstraintStreamImplType() == ConstraintStreamImplType.BAVET) {
            throw new IllegalArgumentException("Can not enable Drools Alpha Network Compilation with "
                    + ConstraintStreamImplType.class.getSimpleName() + "." + ConstraintStreamImplType.BAVET + ".");
        }
        this.droolsAlphaNetworkCompilationEnabled = droolsAlphaNetworkCompilationEnabled;
        return this;
    }

    // ************************************************************************
    // Verify methods
    // ************************************************************************

    @Override
    public DefaultSingleConstraintVerification<Solution_, Score_> verifyThat(
            BiFunction<ConstraintProvider_, ConstraintFactory, Constraint> constraintFunction) {
        requireNonNull(constraintFunction);
        AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory =
                scoreDirectorFactoryContainerThreadLocal.get().getScoreDirectorFactory(constraintFunction, constraintProvider);
        return new DefaultSingleConstraintVerification<>(scoreDirectorFactory);
    }

    @Override
    public DefaultMultiConstraintVerification<Solution_, Score_> verifyThat() {
        AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory =
                scoreDirectorFactoryContainerThreadLocal.get()
                        .getScoreDirectorFactory(defaultScoreDirectorFactoryMapKey, constraintProvider);
        return new DefaultMultiConstraintVerification<>(scoreDirectorFactory, constraintProvider);
    }

}
