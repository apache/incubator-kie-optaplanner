package org.optaplanner.core.impl.domain.common.accessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.impl.domain.common.accessor.gizmo.GizmoMemberAccessorFactory;

public final class CachedMemberAccessorFactory {

    private final Map<String, MemberAccessor> memberAccessorCache;

    public CachedMemberAccessorFactory() {
        this(null);
    }

    /**
     * Prefills the member accessor cache.
     *
     * @param memberAccessorMap key is the fully qualified member name
     */
    public CachedMemberAccessorFactory(Map<String, MemberAccessor> memberAccessorMap) {
        // The MemberAccessorFactory may be accessed, and this cache both read and updated, by multiple threads.
        this.memberAccessorCache =
                memberAccessorMap == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(memberAccessorMap);
    }

    /**
     * Creates a new member accessor based on the given parameters. Caches the result.
     *
     * @param member never null, method or field to access
     * @param memberAccessorType
     * @param annotationClass the annotation the member was annotated with (used for error reporting)
     * @param domainAccessType
     * @return never null, new {@link MemberAccessor} instance unless already found in memberAccessorMap
     */
    public MemberAccessor buildMemberAccessor(Member member, MemberAccessorFactory.MemberAccessorType memberAccessorType,
            Class<? extends Annotation> annotationClass, DomainAccessType domainAccessType) {
        String generatedClassName = GizmoMemberAccessorFactory.getGeneratedClassName(member);
        return memberAccessorCache.computeIfAbsent(generatedClassName,
                k -> MemberAccessorFactory.buildMemberAccessor(member, memberAccessorType, annotationClass, domainAccessType));
    }
}
