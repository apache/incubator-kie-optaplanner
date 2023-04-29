package org.optaplanner.core.config.ruin;

import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
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

    static Integer defaultPercentageToRuin = 20;

    @XmlElement(name = "entitySelector")
    private EntitySelectorConfig entitySelectorConfig = null;
    @XmlElement(name = "secondaryEntitySelector")
    private EntitySelectorConfig secondaryEntitySelectorConfig = null;

    @XmlElementWrapper(name = "variableNameIncludes")
    @XmlElement(name = "variableNameInclude")
    private List<String> variableNameIncludeList = null;

    @XmlElement(name = "percentageToRuin")
    private Integer percentageToRuin = null;

    public void setEntitySelectorConfig(EntitySelectorConfig entitySelectorConfig) {
        this.entitySelectorConfig = entitySelectorConfig;
    }

    public void setSecondaryEntitySelectorConfig(EntitySelectorConfig secondaryEntitySelectorConfig) {
        this.secondaryEntitySelectorConfig = secondaryEntitySelectorConfig;
    }

    public void setVariableNameIncludeList(List<String> variableNameIncludeList) {
        this.variableNameIncludeList = variableNameIncludeList;
    }

    public Integer getPercentageToRuin() {
        return percentageToRuin == null ? defaultPercentageToRuin : percentageToRuin;
    }

    public EntitySelectorConfig getEntitySelectorConfig() {
        return entitySelectorConfig;
    }

    public EntitySelectorConfig getSecondaryEntitySelectorConfig() {
        return secondaryEntitySelectorConfig;
    }

    public List<String> getVariableNameIncludeList() {
        return variableNameIncludeList;
    }

    public void setPercentageToRuin(Integer percentageToRuin) {
        this.percentageToRuin = percentageToRuin;
    }

    // ************************************************************************
    // Constructors and simple getters/setters
    // ************************************************************************

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

    public RuinPhaseConfig withEntitySelectorConfig(EntitySelectorConfig entitySelectorConfig) {
        this.entitySelectorConfig = entitySelectorConfig;
        return this;
    }

    public RuinPhaseConfig withSecondaryEntitySelectorConfig(EntitySelectorConfig secondaryEntitySelectorConfig) {
        this.secondaryEntitySelectorConfig = secondaryEntitySelectorConfig;
        return this;
    }

    public RuinPhaseConfig withVariableNameIncludeList(List<String> variableNameIncludeList) {
        this.variableNameIncludeList = variableNameIncludeList;
        return this;
    }

    public RuinPhaseConfig withPercentageToRuin(Integer percentageToRuin) {
        this.percentageToRuin = percentageToRuin;
        return this;
    }

    @Override
    public RuinPhaseConfig inherit(RuinPhaseConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        entitySelectorConfig = ConfigUtils.inheritConfig(entitySelectorConfig, inheritedConfig.entitySelectorConfig);
        secondaryEntitySelectorConfig = ConfigUtils.inheritConfig(secondaryEntitySelectorConfig,
                inheritedConfig.secondaryEntitySelectorConfig);
        variableNameIncludeList = ConfigUtils.inheritMergeableListProperty(
                variableNameIncludeList, inheritedConfig.variableNameIncludeList);
        percentageToRuin = ConfigUtils.inheritOverwritableProperty(percentageToRuin, inheritedConfig.percentageToRuin);
        return this;
    }

    @Override
    public RuinPhaseConfig copyConfig() {
        return new RuinPhaseConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        if (entitySelectorConfig != null) {
            entitySelectorConfig.visitReferencedClasses(classVisitor);
        }
        if (secondaryEntitySelectorConfig != null) {
            secondaryEntitySelectorConfig.visitReferencedClasses(classVisitor);
        }
    }
}
