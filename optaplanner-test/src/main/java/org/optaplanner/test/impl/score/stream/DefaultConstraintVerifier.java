package org.optaplanner.test.impl.score.stream;

import static java.util.Objects.requireNonNull;
import static org.optaplanner.core.api.score.stream.ConstraintStreamImplType.DROOLS;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.optaplanner.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactory;
import org.optaplanner.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactoryService;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactoryService;
import org.optaplanner.core.impl.score.director.ScoreDirectorType;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public final class DefaultConstraintVerifier<ConstraintProvider_ extends ConstraintProvider, Solution_, Score_ extends Score<Score_>>
        implements ConstraintVerifier<ConstraintProvider_, Solution_> {

    /**
     * Exists so that people can not, even by accident, pick the same constraint ID as the default map key.
     */
    private final String defaultScoreDirectorFactoryMapKey = UUID.randomUUID().toString();
    private final Map<String, AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_>> scoreDirectorFactoryMap =
            new HashMap<>();

    private final ServiceLoader<ScoreDirectorFactoryService<Solution_, Score_>> serviceLoader;
    private final ConstraintProvider_ constraintProvider;
    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final Lock scoreDirectorFactoryMapReadLock;
    private final Lock scoreDirectorFactoryMapWriteLock;

    private ConstraintStreamImplType constraintStreamImplType;
    private Boolean droolsAlphaNetworkCompilationEnabled;

    public DefaultConstraintVerifier(ConstraintProvider_ constraintProvider, SolutionDescriptor<Solution_> solutionDescriptor) {
        ServiceLoader uncastServiceLoader = ServiceLoader.load(ScoreDirectorFactoryService.class);
        this.serviceLoader = uncastServiceLoader;
        this.constraintProvider = constraintProvider;
        this.solutionDescriptor = solutionDescriptor;
        /*
         * Creating score director factory is expensive and possibly not thread-safe.
         * We want to make sure these operations are mutually exclusive.
         */
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.scoreDirectorFactoryMapReadLock = readWriteLock.readLock();
        this.scoreDirectorFactoryMapWriteLock = readWriteLock.writeLock();
    }

    public ConstraintStreamImplType getConstraintStreamImplType() {
        return constraintStreamImplType;
    }

    @Override
    public ConstraintVerifier<ConstraintProvider_, Solution_> withConstraintStreamImplType(
            ConstraintStreamImplType constraintStreamImplType) {
        requireNonNull(constraintStreamImplType);
        this.constraintStreamImplType = constraintStreamImplType;
        clearScoreDirectorFactoryMap();
        return this;
    }

    private void clearScoreDirectorFactoryMap() {
        scoreDirectorFactoryMapWriteLock.lock();
        try {
            this.scoreDirectorFactoryMap.clear(); // All score director factories are invalidated.
        } finally {
            scoreDirectorFactoryMapWriteLock.unlock();
        }
    }

    public boolean isDroolsAlphaNetworkCompilationEnabled() {
        return Objects.requireNonNullElse(droolsAlphaNetworkCompilationEnabled, !ConfigUtils.isNativeImage());
    }

    @Override
    public ConstraintVerifier<ConstraintProvider_, Solution_> withDroolsAlphaNetworkCompilationEnabled(
            boolean droolsAlphaNetworkCompilationEnabled) {
        this.droolsAlphaNetworkCompilationEnabled = droolsAlphaNetworkCompilationEnabled;
        clearScoreDirectorFactoryMap();
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
                getScoreDirectorFactory(constraintFunction);
        return new DefaultSingleConstraintVerification<>(scoreDirectorFactory);
    }

    private AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> getScoreDirectorFactory(
            BiFunction<ConstraintProvider_, ConstraintFactory, Constraint> constraintFunction) {
        AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_> scoreDirectorFactoryService =
                getScoreDirectorFactoryService(serviceLoader, constraintStreamImplType);
        // Creating score director factory is potentially expensive; cache it per constraint.
        Constraint constraint = constraintFunction.apply(constraintProvider,
                scoreDirectorFactoryService.buildConstraintFactory(solutionDescriptor));
        String constraintId = constraint.getConstraintId();
        return getOrCreateScoreDirectorFactory(scoreDirectorFactoryService, constraintId,
                constraintFactory -> new Constraint[] {
                        constraintFunction.apply(constraintProvider, constraintFactory)
                });
    }

    /**
     * Ensures that score director factories are created in a thread-safe manner,
     * and that for each key only one instance is ever created.
     *
     * @param scoreDirectorFactoryService never null, used to create the score director factory
     * @param key never null, unique identifier of the constraint provider
     * @param constraintProvider never null, the constraint provider to be used by the score director factory
     * @return never null
     */
    private AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> getOrCreateScoreDirectorFactory(
            AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_> scoreDirectorFactoryService,
            String key, ConstraintProvider constraintProvider) {
        scoreDirectorFactoryMapReadLock.lock();
        try { // If already calculated, don't require the write lock.
            AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory =
                    scoreDirectorFactoryMap.get(key);
            if (scoreDirectorFactory != null) {
                return scoreDirectorFactory;
            }
        } finally {
            scoreDirectorFactoryMapReadLock.unlock();
        }
        scoreDirectorFactoryMapWriteLock.lock();
        try {
            return scoreDirectorFactoryMap.computeIfAbsent(key, k -> {
                boolean isDroolsAlphaNetworkCompilationEnabled =
                        droolsAlphaNetworkCompilationEnabled == null
                                ? scoreDirectorFactoryService.supportsImplType(DROOLS)
                                        && isDroolsAlphaNetworkCompilationEnabled()
                                : droolsAlphaNetworkCompilationEnabled;
                return scoreDirectorFactoryService.buildScoreDirectorFactory(solutionDescriptor, constraintProvider,
                        isDroolsAlphaNetworkCompilationEnabled);
            });
        } finally {
            scoreDirectorFactoryMapWriteLock.unlock();
        }
    }

    private static <Solution_, Score_ extends Score<Score_>>
            AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_>
            getScoreDirectorFactoryService(ServiceLoader<ScoreDirectorFactoryService<Solution_, Score_>> serviceLoader,
                    ConstraintStreamImplType constraintStreamImplType) {
        List<AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_>> services = serviceLoader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(s -> s.getSupportedScoreDirectorType() == ScoreDirectorType.CONSTRAINT_STREAMS)
                .map(s -> (AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_>) s)
                .filter(s -> constraintStreamImplType == null || s.supportsImplType(constraintStreamImplType))
                .sorted(Comparator
                        .comparingInt(
                                (AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_> service) -> service
                                        .getPriority())
                        .reversed()) // CS-D will be picked if both are available.
                .collect(Collectors.toList());
        if (services.isEmpty()) {
            throw new IllegalStateException(
                    "Constraint Streams implementation was not found on the classpath.\n"
                            + "Maybe include org.optaplanner:optaplanner-constraint-streams-drools dependency "
                            + "or org.optaplanner:optaplanner-constraint-streams-bavet in your project?\n"
                            + "Maybe ensure your uberjar bundles META-INF/services from included JAR files?");
        }
        return services.get(0);
    }

    @Override
    public DefaultMultiConstraintVerification<Solution_, Score_> verifyThat() {
        AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_> scoreDirectorFactoryService =
                getScoreDirectorFactoryService(serviceLoader, constraintStreamImplType);
        AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory =
                getOrCreateScoreDirectorFactory(scoreDirectorFactoryService, defaultScoreDirectorFactoryMapKey,
                        constraintProvider);
        return new DefaultMultiConstraintVerification<>(scoreDirectorFactory, constraintProvider);
    }

}
