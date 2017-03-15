/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;

public class ClassAndPlanningIdComparator implements Comparator<Object> {

    private boolean failFastIfNoPlanningId;

    public ClassAndPlanningIdComparator() {
        this(true);
    }

    public ClassAndPlanningIdComparator(boolean failFastIfNoPlanningId) {
        this.failFastIfNoPlanningId = failFastIfNoPlanningId;
    }

    private Map<Class, MemberAccessor> decisionCache = new HashMap<>();

    @Override
    public int compare(Object a, Object b) {
        if (a == null) {
            return b == null ? 0 : -1;
        } else if (b == null) {
            return 1;
        }
        Class<?> aClass = a.getClass();
        Class<?> bClass = b.getClass();
        if (aClass != bClass) {
            return aClass.getName().compareTo(bClass.getName());
        }
        MemberAccessor aMemberAccessor = decisionCache.computeIfAbsent(aClass,
                ConfigUtils::findPlanningIdMemberAccessor);
        MemberAccessor bMemberAccessor = decisionCache.computeIfAbsent(bClass,
                ConfigUtils::findPlanningIdMemberAccessor);
        if (failFastIfNoPlanningId) {
            if (aMemberAccessor == null) {
                throw new IllegalArgumentException("The class (" + aClass
                        + ") does not have a " + PlanningId.class.getSimpleName() + " annotation.\n"
                        + "Maybe add the " + PlanningId.class.getSimpleName() + " annotation.");
            }
            if (bMemberAccessor == null) {
                throw new IllegalArgumentException("The class (" + bClass
                        + ") does not have a " + PlanningId.class.getSimpleName() + " annotation.\n"
                        + "Maybe add the " + PlanningId.class.getSimpleName() + " annotation.");
            }
        } else {
            if (aMemberAccessor == null) {
                // Return 0 to keep original order
                return bMemberAccessor == null ? 0 : -1;
            } else if (bMemberAccessor == null) {
                return 1;
            }
        }
        Comparable aPlanningId = (Comparable) aMemberAccessor.executeGetter(a);
        Comparable bPlanningId = (Comparable) bMemberAccessor.executeGetter(b);
        if (aPlanningId == null) {
            throw new IllegalArgumentException("The planningId (" + aPlanningId
                    + ") of the member (" + aMemberAccessor + ") of the class (" + aClass
                    + ") on object (" + a + ") must not be null.\n"
                    + "Maybe initialize the planningId of the original object before solving..");
        }
        if (bPlanningId == null) {
            throw new IllegalArgumentException("The planningId (" + bPlanningId
                    + ") of the member (" + bMemberAccessor + ") of the class (" + bClass
                    + ") on object (" + a + ") must not be null.\n"
                    + "Maybe initialize the planningId of the original object before solving..");
        }
        // If a and b are different classes, this method would have already returned.
        return aPlanningId.compareTo(bPlanningId);
    }

}
