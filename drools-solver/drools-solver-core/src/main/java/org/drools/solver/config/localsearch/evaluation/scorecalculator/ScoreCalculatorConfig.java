package org.drools.solver.config.localsearch.evaluation.scorecalculator;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.solver.core.score.calculator.DynamicHardAndSoftConstraintScoreCalculator;
import org.drools.solver.core.score.calculator.ScoreCalculator;
import org.drools.solver.core.score.calculator.SimpleScoreCalculator;
import org.drools.solver.core.score.calculator.StaticHardAndSoftConstraintScoreCalculator;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("scoreCalculator")
public class ScoreCalculatorConfig {

    private ScoreCalculator scoreCalculator = null;
    private Class<ScoreCalculator> scoreCalculatorClass = null;
    private ScoreCalculatorType scoreCalculatorType = null;

    public ScoreCalculator getScoreCalculator() {
        return scoreCalculator;
    }

    public void setScoreCalculator(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    public Class<ScoreCalculator> getScoreCalculatorClass() {
        return scoreCalculatorClass;
    }

    public void setScoreCalculatorClass(Class<ScoreCalculator> scoreCalculatorClass) {
        this.scoreCalculatorClass = scoreCalculatorClass;
    }

    public ScoreCalculatorType getScoreCalculatorType() {
        return scoreCalculatorType;
    }

    public void setScoreCalculatorType(ScoreCalculatorType scoreCalculatorType) {
        this.scoreCalculatorType = scoreCalculatorType;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public ScoreCalculator buildScoreCalculator() {
        if (scoreCalculator != null) {
            return scoreCalculator;
        } else if (scoreCalculatorClass != null) {
            try {
                return scoreCalculatorClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("scoreCalculatorClass (" + scoreCalculatorClass.getName()
                        + ") does not have a public no-arg constructor", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("scoreCalculatorClass (" + scoreCalculatorClass.getName()
                        + ") does not have a public no-arg constructor", e);
            }
        } else if (scoreCalculatorType != null) {
            switch (scoreCalculatorType) {
                case SIMPLE:
                    return new SimpleScoreCalculator();
                case HARD_AND_SOFT_CONSTRAINTS:
                    return new StaticHardAndSoftConstraintScoreCalculator();
                case DYNAMIC_HARD_AND_SOFT_CONSTRAINTS:
                    return new DynamicHardAndSoftConstraintScoreCalculator(10000.0, 100.0, 1000000.0, 1.2);
                default:
                    throw new IllegalStateException("scoreCalculatorType (" + scoreCalculatorType + ") not implemented");
            }
        } else {
            return new SimpleScoreCalculator();
        }
    }

    public void inherit(ScoreCalculatorConfig inheritedConfig) {
        if (scoreCalculator == null && scoreCalculatorClass == null && scoreCalculatorType == null) {
            scoreCalculator = inheritedConfig.getScoreCalculator();
            scoreCalculatorClass = inheritedConfig.getScoreCalculatorClass();
            scoreCalculatorType = inheritedConfig.getScoreCalculatorType();
        }
    }

    public static enum ScoreCalculatorType {
        SIMPLE,
        HARD_AND_SOFT_CONSTRAINTS,
        DYNAMIC_HARD_AND_SOFT_CONSTRAINTS,
    }

}
