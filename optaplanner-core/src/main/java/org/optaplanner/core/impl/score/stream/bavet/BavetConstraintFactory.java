/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.score.stream.bavet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.constraintweight.descriptor.ConstraintConfigurationDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.stream.ConstraintSessionFactory;
import org.optaplanner.core.impl.score.stream.InnerConstraintFactory;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetAbstractUniConstraintStream;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetFromUniConstraintStream;

import static org.optaplanner.core.api.score.stream.Joiners.lessThan;

public final class BavetConstraintFactory<Solution_> implements InnerConstraintFactory<Solution_> {

    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final String defaultConstraintPackage;

    public BavetConstraintFactory(SolutionDescriptor<Solution_> solutionDescriptor) {
        this.solutionDescriptor = solutionDescriptor;
        ConstraintConfigurationDescriptor<Solution_> configurationDescriptor
                = solutionDescriptor.getConstraintConfigurationDescriptor();
        if (configurationDescriptor == null) {
            defaultConstraintPackage = solutionDescriptor.getSolutionClass().getPackage().getName();
        } else {
            defaultConstraintPackage = configurationDescriptor.getConstraintPackage();
        }
    }

    // ************************************************************************
    // from
    // ************************************************************************

    @Override
    public <A> BavetAbstractUniConstraintStream<Solution_, A> fromUnfiltered(Class<A> fromClass) {
        return new BavetFromUniConstraintStream<>(this, fromClass);
    }

    // ************************************************************************
    // fromUniquePair
    // ************************************************************************

    @Override
    public <A> BiConstraintStream<A, A> fromUniquePair(Class<A> fromClass, BiJoiner<A, A> joiner) {
        MemberAccessor planningIdMemberAccessor = ConfigUtils.findPlanningIdMemberAccessor(fromClass);
        if (planningIdMemberAccessor == null) {
            throw new IllegalArgumentException("The fromClass (" + fromClass + ") has no member with a @"
                    + PlanningId.class.getSimpleName() + " annotation,"
                    + " so the pairs can not be made unique ([A,B] vs [B,A]).");
        }
        // TODO Breaks node sharing + involves unneeded indirection
        Function<A, Comparable> planningIdGetter = (fact) -> (Comparable<?>) planningIdMemberAccessor.executeGetter(fact);
        return from(fromClass).join(fromClass, lessThan(planningIdGetter), joiner);
    }

    // ************************************************************************
    // SessionFactory creation
    // ************************************************************************

    @Override
    public ConstraintSessionFactory<Solution_> buildSessionFactory(Constraint[] constraints) {
        List<BavetConstraint<Solution_>> bavetConstraintList = new ArrayList<>(constraints.length);
        Set<String> constraintIdSet = new HashSet<>(constraints.length);
        for (Constraint constraint : constraints) {
            if (constraint.getConstraintFactory() != this) {
                throw new IllegalStateException("The constraint (" + constraint.getConstraintId()
                        + ") must be created from the same constraintFactory.");
            }
            boolean added = constraintIdSet.add(constraint.getConstraintId());
            if (!added) {
                throw new IllegalStateException(
                        "There are 2 constraints with the same constraintName (" + constraint.getConstraintName()
                        + ") in the same constraintPackage (" + constraint.getConstraintPackage() + ").");
            }
            BavetConstraint<Solution_> bavetConstraint = (BavetConstraint) constraint;
            bavetConstraintList.add(bavetConstraint);
        }
        return new BavetConstraintSessionFactory<>(solutionDescriptor, bavetConstraintList);
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public SolutionDescriptor<Solution_> getSolutionDescriptor() {
        return solutionDescriptor;
    }

    @Override
    public String getDefaultConstraintPackage() {
        return defaultConstraintPackage;
    }

}
