package org.optaplanner.examples.common.score;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Retention(RetentionPolicy.RUNTIME)
@Execution(ExecutionMode.CONCURRENT)
@ParameterizedTest(name = "constraintStreamImplType = {0}")
@MethodSource("provideArguments")
public @interface ConstraintProviderTest {
}
