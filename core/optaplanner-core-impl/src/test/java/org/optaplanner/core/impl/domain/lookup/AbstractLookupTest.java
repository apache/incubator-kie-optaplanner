package org.optaplanner.core.impl.domain.lookup;

import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.api.domain.lookup.LookUpStrategyType;
import org.optaplanner.core.impl.domain.common.accessor.CachedMemberAccessorFactory;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;

abstract class AbstractLookupTest {

    protected LookUpStrategyResolver createLookupStrategyResolver(DomainAccessType domainAccessType,
            LookUpStrategyType lookUpStrategyType) {
        DescriptorPolicy descriptorPolicy = new DescriptorPolicy();
        descriptorPolicy.setCachedMemberAccessorFactory(new CachedMemberAccessorFactory());
        descriptorPolicy.setDomainAccessType(domainAccessType);
        return new LookUpStrategyResolver(descriptorPolicy, lookUpStrategyType);
    }
}
