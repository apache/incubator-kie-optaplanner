/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.domain.common.accessor.gizmo;

import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

public class GizmoMemberAccessorFactory {
    // GizmoMemberAccessors are stateless, and thus can be safely reused across multiple instances
    private static Map<String, MemberAccessor> memberAccessorMap = new HashMap<>();

    /**
     * Returns the generated class name for a given member.
     * (Here as accessing any method of GizmoMemberAccessorImplementor
     * will try to load Gizmo code)
     *
     * @param member The member to get the generated class name for
     * @return The generated class name for member
     */
    public static String getGeneratedClassName(Member member) {
        return member.getDeclaringClass().getPackage().getName() + ".$optaplanner$__"
                + member.getDeclaringClass().getSimpleName() + "$__" + member.getName();
    }

    public static void usePregeneratedMemberAccessorMap(Map<String, MemberAccessor> memberAccessorMap) {
        GizmoMemberAccessorFactory.memberAccessorMap = memberAccessorMap;
    }

    public static MemberAccessor buildGizmoMemberAccessor(Member member, Class<? extends Annotation> annotationClass) {
        String gizmoMemberAccessorClassName = getGeneratedClassName(member);
        return memberAccessorMap.computeIfAbsent(gizmoMemberAccessorClassName, key -> {
            if (!ConfigUtils.isGizmoOnClasspath()) {
                throw new IllegalStateException("When using the domainAccessType (" +
                        DomainAccessType.GIZMO +
                        ") the classpath or modulepath must contain io.quarkus.gizmo:gizmo.\n" +
                        "Maybe add a dependency to io.quarkus.gizmo:gizmo.");
            }
            MemberAccessor accessor = GizmoMemberAccessorImplementor.createAccessorFor(member, annotationClass);
            return accessor;
        });
    }

    private GizmoMemberAccessorFactory() {
    }
}
