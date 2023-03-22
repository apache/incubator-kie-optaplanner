package org.optaplanner.core.config.ruin;

import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.move.generic.*;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.config.util.ConfigUtils;

@XmlType(propOrder = {
        "moveSelectorConfig"
})
public class RuinPhaseConfig extends PhaseConfig<RuinPhaseConfig> {

    public static final String XML_ELEMENT_NAME = "ruin";

    @XmlElement(name = RuinMoveSelectorConfig.XML_ELEMENT_NAME, type = RuinMoveSelectorConfig.class,
            namespace = "https://www.optaplanner.org/xsd/solver")
    private RuinMoveSelectorConfig moveSelectorConfig = null;

    // ************************************************************************
    // Constructors and simple getters/setters
    // ************************************************************************

    public RuinMoveSelectorConfig getMoveSelectorConfig() {
        return moveSelectorConfig;
    }

    public void setMoveSelectorConfig(RuinMoveSelectorConfig moveSelectorConfig) {
        this.moveSelectorConfig = moveSelectorConfig;
    }

    public TerminationConfig getTerminationConfig() {
        TerminationConfig terminationConfig = super.getTerminationConfig();
        if (terminationConfig != null) {
            return terminationConfig;
        }
        TerminationConfig defaultTerminationConfig = new TerminationConfig();
        defaultTerminationConfig.setStepCountLimit(1);
        return defaultTerminationConfig;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public RuinPhaseConfig withMoveSelectorConfig(RuinMoveSelectorConfig moveSelectorConfig) {
        this.moveSelectorConfig = moveSelectorConfig;
        return this;
    }

    @Override
    public RuinPhaseConfig inherit(RuinPhaseConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        setMoveSelectorConfig(ConfigUtils.inheritOverwritableProperty(
                getMoveSelectorConfig(), inheritedConfig.getMoveSelectorConfig()));
        return this;
    }

    @Override
    public RuinPhaseConfig copyConfig() {
        return new RuinPhaseConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        if (getTerminationConfig() != null) {
            getTerminationConfig().visitReferencedClasses(classVisitor);
        }
        if (moveSelectorConfig != null) {
            moveSelectorConfig.visitReferencedClasses(classVisitor);
        }
    }
}
