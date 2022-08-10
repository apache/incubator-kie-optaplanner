package org.optaplanner.core.impl.domain.common.accessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.api.domain.solution.ProblemFactProperty;
import org.optaplanner.core.impl.domain.common.accessor.gizmo.GizmoMemberAccessorFactory;
import org.optaplanner.core.impl.testdata.domain.reflect.accessmodifier.TestdataVisibilityModifierSolution;

class CachedMemberAccessorFactoryTest {

    @Test
    void shouldUseGeneratedMemberAccessorIfExists() throws NoSuchMethodException {
        Member member = TestdataVisibilityModifierSolution.class.getDeclaredMethod("getPublicProperty");
        MemberAccessor mockMemberAccessor = Mockito.mock(MemberAccessor.class);

        Map<String, MemberAccessor> preexistingMemberAccessors = new HashMap<>();
        preexistingMemberAccessors.put(GizmoMemberAccessorFactory.getGeneratedClassName(member), mockMemberAccessor);
        CachedMemberAccessorFactory cachedMemberAccessorFactory = new CachedMemberAccessorFactory(preexistingMemberAccessors);
        MemberAccessor memberAccessor = cachedMemberAccessorFactory.buildMemberAccessor(member,
                MemberAccessorFactory.MemberAccessorType.FIELD_OR_GETTER_METHOD_WITH_SETTER, ProblemFactProperty.class,
                DomainAccessType.REFLECTION);
        assertThat(memberAccessor)
                .isSameAs(mockMemberAccessor);
    }

}
