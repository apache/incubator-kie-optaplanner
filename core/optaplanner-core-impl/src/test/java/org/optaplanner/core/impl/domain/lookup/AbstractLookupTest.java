package org.optaplanner.core.impl.domain.lookup;

import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.api.domain.lookup.LookUpStrategyType;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessorFactory;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;

abstract class AbstractLookupTest {

    protected LookUpStrategyResolver createLookupStrategyResolver(DomainAccessType domainAccessType, LookUpStrategyType lookUpStrategyType) {
        DescriptorPolicy descriptorPolicy = new DescriptorPolicy();
        descriptorPolicy.setMemberAccessorFactory(new MemberAccessorFactory());
        descriptorPolicy.setDomainAccessType(domainAccessType);
        return new LookUpStrategyResolver(descriptorPolicy, lookUpStrategyType);
    }
}
