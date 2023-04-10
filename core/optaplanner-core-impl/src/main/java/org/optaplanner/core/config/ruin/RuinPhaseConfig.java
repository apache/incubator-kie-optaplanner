package org.optaplanner.core.config.ruin;

import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.*;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.config.util.ConfigUtils;

@XmlType(propOrder = {
        "percentageToRuin",
        "entitySelectorConfig",
        "secondaryEntitySelectorConfig",
        "variableNameIncludeList"
})
public class RuinPhaseConfig extends PhaseConfig<RuinPhaseConfig> {

    public static final String XML_ELEMENT_NAME = "ruin";

    @XmlTransient
    private RuinMoveSelectorConfig moveSelectorConfig = null;

    @XmlElement(name = "entitySelector")
    private EntitySelectorConfig entitySelectorConfig = null;
    @XmlElement(name = "secondaryEntitySelector")
    private EntitySelectorConfig secondaryEntitySelectorConfig = null;

    @XmlElementWrapper(name = "variableNameIncludes")
    @XmlElement(name = "variableNameInclude")
    private List<String> variableNameIncludeList = null;

    @XmlElement(name = "percentageToRuin")
    private Integer percentageToRuin = 20;

    public void setEntitySelectorConfig(EntitySelectorConfig entitySelectorConfig) {
        this.entitySelectorConfig = entitySelectorConfig;
    }

    public void setSecondaryEntitySelectorConfig(EntitySelectorConfig secondaryEntitySelectorConfig) {
        this.secondaryEntitySelectorConfig = secondaryEntitySelectorConfig;
    }

    public void setVariableNameIncludeList(List<String> variableNameIncludeList) {
        this.variableNameIncludeList = variableNameIncludeList;
    }

    public void setPercentageToRuin(Integer percentageToRuin) {
        this.percentageToRuin = percentageToRuin;
    }

    // ************************************************************************
    // Constructors and simple getters/setters
    // ************************************************************************

    public RuinMoveSelectorConfig getMoveSelectorConfig() {
        if (moveSelectorConfig == null) {
            moveSelectorConfig = new RuinMoveSelectorConfig(entitySelectorConfig, secondaryEntitySelectorConfig,
                    variableNameIncludeList, percentageToRuin);
        }
        return moveSelectorConfig;
    }

    @Override
    public TerminationConfig getTerminationConfig() {
        return null;
    }

    @Override
    public void setTerminationConfig(TerminationConfig terminationConfig) {
        throw new UnsupportedOperationException("Setting termination config on ruin phase is not allowed.");
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    @Override
    public RuinPhaseConfig inherit(RuinPhaseConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        entitySelectorConfig = ConfigUtils.inheritConfig(entitySelectorConfig, inheritedConfig.entitySelectorConfig);
        secondaryEntitySelectorConfig = ConfigUtils.inheritConfig(secondaryEntitySelectorConfig,
                inheritedConfig.secondaryEntitySelectorConfig);
        variableNameIncludeList = ConfigUtils.inheritMergeableListProperty(
                variableNameIncludeList, inheritedConfig.variableNameIncludeList);
        percentageToRuin = ConfigUtils.inheritOverwritableProperty(percentageToRuin, inheritedConfig.percentageToRuin);
        moveSelectorConfig = ConfigUtils.inheritOverwritableProperty(
                getMoveSelectorConfig(), inheritedConfig.moveSelectorConfig);
        return this;
    }

    @Override
    public RuinPhaseConfig copyConfig() {
        return new RuinPhaseConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        if (moveSelectorConfig != null) {
            moveSelectorConfig.visitReferencedClasses(classVisitor);
        }
    }
}
