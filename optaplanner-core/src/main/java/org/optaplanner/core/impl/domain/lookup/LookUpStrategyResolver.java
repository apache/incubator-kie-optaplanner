/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.lookup;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.optaplanner.core.api.domain.lookup.LookUpStrategyType;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;

/**
 * This class is thread-safe.
 */
public class LookUpStrategyResolver {

    private final LookUpStrategyType lookUpStrategyType;

    private final ConcurrentMap<Class<?>, LookUpStrategy> decisionCache = new ConcurrentHashMap<>();

    public LookUpStrategyResolver(LookUpStrategyType lookUpStrategyType) {
        this.lookUpStrategyType = lookUpStrategyType;
        decisionCache.put(Boolean.class, new ImmutableLookUpStrategy());
        decisionCache.put(Byte.class, new ImmutableLookUpStrategy());
        decisionCache.put(Short.class, new ImmutableLookUpStrategy());
        decisionCache.put(Integer.class, new ImmutableLookUpStrategy());
        decisionCache.put(Long.class, new ImmutableLookUpStrategy());
        decisionCache.put(Float.class, new ImmutableLookUpStrategy());
        decisionCache.put(Double.class, new ImmutableLookUpStrategy());
        decisionCache.put(BigInteger.class, new ImmutableLookUpStrategy());
        decisionCache.put(BigDecimal.class, new ImmutableLookUpStrategy());
        decisionCache.put(Character.class, new ImmutableLookUpStrategy());
        decisionCache.put(String.class, new ImmutableLookUpStrategy());
        decisionCache.put(LocalDate.class, new ImmutableLookUpStrategy());
        decisionCache.put(LocalTime.class, new ImmutableLookUpStrategy());
        decisionCache.put(LocalDateTime.class, new ImmutableLookUpStrategy());
    }

    /**
     * This method is thread-safe.
     * @param object never null
     * @return never null
     */
    public LookUpStrategy determineLookUpStrategy(Object object) {
        Class<?> objectClass = object.getClass();
        return decisionCache.computeIfAbsent(objectClass, key -> {
            if (object.getClass().isEnum()) {
                return new ImmutableLookUpStrategy();
            }
            switch (lookUpStrategyType) {
                case PLANNING_ID_OR_NONE:
                    MemberAccessor memberAccessor1 = ConfigUtils.findPlanningIdMemberAccessor(objectClass);
                    if (memberAccessor1 == null) {
                        return new NoneLookUpStrategy();
                    }
                    return new PlanningIdLookUpStrategy(memberAccessor1);
                case PLANNING_ID_OR_FAIL_FAST:
                    MemberAccessor memberAccessor2 = ConfigUtils.findPlanningIdMemberAccessor(objectClass);
                    if (memberAccessor2 == null) {
                        throw new IllegalArgumentException("The class (" + objectClass
                                + ") does not have a " + PlanningId.class.getSimpleName() + " annotation,"
                                + " but the lookUpStrategyType (" + lookUpStrategyType + ") requires it.\n"
                                + "Maybe add the " + PlanningId.class.getSimpleName() + " annotation"
                                + " or change the " + PlanningSolution.class.getSimpleName() + " annotation's "
                                + LookUpStrategyType.class.getSimpleName() + ".");
                    }
                    return new PlanningIdLookUpStrategy(memberAccessor2);
                case EQUALITY:
                    Method equalsMethod;
                    Method hashCodeMethod;
                    try {
                        equalsMethod = object.getClass().getMethod("equals", Object.class);
                        hashCodeMethod = object.getClass().getMethod("hashCode");
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException(
                                "Impossible state because equals() and hashCode() always exist.", e);
                    }
                    if (equalsMethod.getDeclaringClass().equals(Object.class)) {
                        throw new IllegalArgumentException("The class (" + object.getClass().getSimpleName()
                                + ") doesn't override the equals() method, neither does any superclass.");
                    }
                    if (hashCodeMethod.getDeclaringClass().equals(Object.class)) {
                        throw new IllegalArgumentException("The class (" + object.getClass().getSimpleName()
                                + ") overrides equals() but neither it nor any superclass"
                                + " overrides the hashCode() method.");
                    }
                    return new EqualsLookUpStrategy();
                case NONE:
                    return new NoneLookUpStrategy();
                default:
                    throw new IllegalStateException("The lookUpStrategyType (" + lookUpStrategyType
                            + ") is not implemented.");
            }
        });
    }

}
