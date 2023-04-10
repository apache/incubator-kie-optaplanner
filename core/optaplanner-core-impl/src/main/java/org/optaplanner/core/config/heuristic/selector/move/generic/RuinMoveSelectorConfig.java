package org.optaplanner.core.config.heuristic.selector.move.generic;

import java.util.List;
import java.util.function.Consumer;

import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;

public class RuinMoveSelectorConfig extends MoveSelectorConfig<RuinMoveSelectorConfig> {

    public RuinMoveSelectorConfig() {
    }

    public RuinMoveSelectorConfig(EntitySelectorConfig entitySelectorConfig, EntitySelectorConfig secondaryEntitySelectorConfig,
            List<String> variableNameIncludeList, Integer percentageToRuin) {
        this.entitySelectorConfig = entitySelectorConfig;
        this.secondaryEntitySelectorConfig = secondaryEntitySelectorConfig;
        this.variableNameIncludeList = variableNameIncludeList;
        this.percentageToRuin = percentageToRuin;
    }

    private EntitySelectorConfig entitySelectorConfig = null;
    private EntitySelectorConfig secondaryEntitySelectorConfig = null;
    private List<String> variableNameIncludeList = null;
    private Integer percentageToRuin = null;

    public EntitySelectorConfig getEntitySelectorConfig() {
        return entitySelectorConfig;
    }

    public EntitySelectorConfig getSecondaryEntitySelectorConfig() {
        return secondaryEntitySelectorConfig;
    }

    public List<String> getVariableNameIncludeList() {
        return variableNameIncludeList;
    }

    public Integer getPercentageToRuin() {
        return percentageToRuin;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    @Override
    public RuinMoveSelectorConfig inherit(RuinMoveSelectorConfig inheritedConfig) {
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
    public RuinMoveSelectorConfig copyConfig() {
        return new RuinMoveSelectorConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        visitCommonReferencedClasses(classVisitor);
        if (entitySelectorConfig != null) {
            entitySelectorConfig.visitReferencedClasses(classVisitor);
        }
        if (secondaryEntitySelectorConfig != null) {
            secondaryEntitySelectorConfig.visitReferencedClasses(classVisitor);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + entitySelectorConfig
                + (secondaryEntitySelectorConfig == null ? "" : ", " + secondaryEntitySelectorConfig) + ") " + percentageToRuin
                + "% to be ruined";
    }
}
