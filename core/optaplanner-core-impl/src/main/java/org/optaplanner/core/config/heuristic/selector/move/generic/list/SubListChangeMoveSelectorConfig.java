package org.optaplanner.core.config.heuristic.selector.move.generic.list;

import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;

@XmlType(propOrder = {
        "minimumSubListSize",
        "maximumSubListSize"
})
public class SubListChangeMoveSelectorConfig extends MoveSelectorConfig<SubListChangeMoveSelectorConfig> {

    public static final String XML_ELEMENT_NAME = "subListChangeMoveSelector";

    protected Integer minimumSubListSize = null;
    protected Integer maximumSubListSize = null;

    public Integer getMinimumSubListSize() {
        return minimumSubListSize;
    }

    public void setMinimumSubListSize(Integer minimumSubListSize) {
        this.minimumSubListSize = minimumSubListSize;
    }

    public Integer getMaximumSubListSize() {
        return maximumSubListSize;
    }

    public void setMaximumSubListSize(Integer maximumSubListSize) {
        this.maximumSubListSize = maximumSubListSize;
    }

    @Override
    public SubListChangeMoveSelectorConfig inherit(SubListChangeMoveSelectorConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        this.minimumSubListSize =
                ConfigUtils.inheritOverwritableProperty(minimumSubListSize, inheritedConfig.minimumSubListSize);
        this.maximumSubListSize =
                ConfigUtils.inheritOverwritableProperty(maximumSubListSize, inheritedConfig.maximumSubListSize);
        return this;
    }

    @Override
    public SubListChangeMoveSelectorConfig copyConfig() {
        return new SubListChangeMoveSelectorConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "()";
    }
}
