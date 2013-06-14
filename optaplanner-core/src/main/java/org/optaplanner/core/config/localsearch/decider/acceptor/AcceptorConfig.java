/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.config.localsearch.decider.acceptor;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import org.apache.commons.lang.ObjectUtils;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.localsearch.decider.acceptor.Acceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.CompositeAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.greatdeluge.GreatDelugeAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.lateacceptance.LateAcceptanceAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.latesimulatedannealing.LateSimulatedAnnealingAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.simulatedannealing.SimulatedAnnealingAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.tabu.EntityTabuAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.tabu.MoveTabuAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.tabu.ValueTabuAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.tabu.SolutionTabuAcceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.tabu.size.EntityRatioTabuSizeStrategy;
import org.optaplanner.core.impl.localsearch.decider.acceptor.tabu.size.FixedTabuSizeStrategy;
import org.optaplanner.core.impl.localsearch.decider.acceptor.tabu.size.ValueRatioTabuSizeStrategy;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;

@XStreamAlias("acceptor")
public class AcceptorConfig {

    @XStreamImplicit(itemFieldName = "acceptorClass")
    private List<Class<? extends Acceptor>> acceptorClassList = null; // TODO make into a list

    @XStreamImplicit(itemFieldName = "acceptorType")
    private List<AcceptorType> acceptorTypeList = null;

    protected Integer entityTabuSize = null;
    protected Double entityTabuRatio = null;
    protected Integer fadingEntityTabuSize = null;
    protected Double fadingEntityTabuRatio = null;
    protected Integer valueTabuSize = null;
    protected Integer valueTabuRatio = null;
    protected Integer fadingValueTabuSize = null;
    protected Integer fadingValueTabuRatio = null;
    protected Integer moveTabuSize = null;
    protected Integer fadingMoveTabuSize = null;
    protected Integer undoMoveTabuSize = null;
    protected Integer fadingUndoMoveTabuSize = null;
    protected Integer solutionTabuSize = null;
    protected Integer fadingSolutionTabuSize = null;

    protected String simulatedAnnealingStartingTemperature = null;

    protected Double greatDelugeWaterLevelUpperBoundRate = null;
    protected Double greatDelugeWaterRisingRate = null;

    protected Integer lateAcceptanceSize = null;

    protected Integer lateSimulatedAnnealingSize = null;

    public List<Class<? extends Acceptor>> getAcceptorClassList() {
        return acceptorClassList;
    }

    public void setAcceptorClassList(List<Class<? extends Acceptor>> acceptorClassList) {
        this.acceptorClassList = acceptorClassList;
    }

    public List<AcceptorType> getAcceptorTypeList() {
        return acceptorTypeList;
    }

    public void setAcceptorTypeList(List<AcceptorType> acceptorTypeList) {
        this.acceptorTypeList = acceptorTypeList;
    }

    public Integer getEntityTabuSize() {
        return entityTabuSize;
    }

    public void setEntityTabuSize(Integer entityTabuSize) {
        this.entityTabuSize = entityTabuSize;
    }

    public Double getEntityTabuRatio() {
        return entityTabuRatio;
    }

    public void setEntityTabuRatio(Double entityTabuRatio) {
        this.entityTabuRatio = entityTabuRatio;
    }

    public Integer getFadingEntityTabuSize() {
        return fadingEntityTabuSize;
    }

    public void setFadingEntityTabuSize(Integer fadingEntityTabuSize) {
        this.fadingEntityTabuSize = fadingEntityTabuSize;
    }

    public Double getFadingEntityTabuRatio() {
        return fadingEntityTabuRatio;
    }

    public void setFadingEntityTabuRatio(Double fadingEntityTabuRatio) {
        this.fadingEntityTabuRatio = fadingEntityTabuRatio;
    }

    public Integer getValueTabuSize() {
        return valueTabuSize;
    }

    public void setValueTabuSize(Integer valueTabuSize) {
        this.valueTabuSize = valueTabuSize;
    }

    public Integer getValueTabuRatio() {
        return valueTabuRatio;
    }

    public void setValueTabuRatio(Integer valueTabuRatio) {
        this.valueTabuRatio = valueTabuRatio;
    }

    public Integer getFadingValueTabuSize() {
        return fadingValueTabuSize;
    }

    public void setFadingValueTabuSize(Integer fadingValueTabuSize) {
        this.fadingValueTabuSize = fadingValueTabuSize;
    }

    public Integer getFadingValueTabuRatio() {
        return fadingValueTabuRatio;
    }

    public void setFadingValueTabuRatio(Integer fadingValueTabuRatio) {
        this.fadingValueTabuRatio = fadingValueTabuRatio;
    }

    public Integer getMoveTabuSize() {
        return moveTabuSize;
    }

    public void setMoveTabuSize(Integer moveTabuSize) {
        this.moveTabuSize = moveTabuSize;
    }

    public Integer getFadingMoveTabuSize() {
        return fadingMoveTabuSize;
    }

    public void setFadingMoveTabuSize(Integer fadingMoveTabuSize) {
        this.fadingMoveTabuSize = fadingMoveTabuSize;
    }

    public Integer getUndoMoveTabuSize() {
        return undoMoveTabuSize;
    }

    public void setUndoMoveTabuSize(Integer undoMoveTabuSize) {
        this.undoMoveTabuSize = undoMoveTabuSize;
    }

    public Integer getFadingUndoMoveTabuSize() {
        return fadingUndoMoveTabuSize;
    }

    public void setFadingUndoMoveTabuSize(Integer fadingUndoMoveTabuSize) {
        this.fadingUndoMoveTabuSize = fadingUndoMoveTabuSize;
    }

    public Integer getSolutionTabuSize() {
        return solutionTabuSize;
    }

    public void setSolutionTabuSize(Integer solutionTabuSize) {
        this.solutionTabuSize = solutionTabuSize;
    }

    public Integer getFadingSolutionTabuSize() {
        return fadingSolutionTabuSize;
    }

    public void setFadingSolutionTabuSize(Integer fadingSolutionTabuSize) {
        this.fadingSolutionTabuSize = fadingSolutionTabuSize;
    }

    public String getSimulatedAnnealingStartingTemperature() {
        return simulatedAnnealingStartingTemperature;
    }

    public void setSimulatedAnnealingStartingTemperature(String simulatedAnnealingStartingTemperature) {
        this.simulatedAnnealingStartingTemperature = simulatedAnnealingStartingTemperature;
    }

    public Double getGreatDelugeWaterLevelUpperBoundRate() {
        return greatDelugeWaterLevelUpperBoundRate;
    }

    public void setGreatDelugeWaterLevelUpperBoundRate(Double greatDelugeWaterLevelUpperBoundRate) {
        this.greatDelugeWaterLevelUpperBoundRate = greatDelugeWaterLevelUpperBoundRate;
    }

    public Double getGreatDelugeWaterRisingRate() {
        return greatDelugeWaterRisingRate;
    }

    public void setGreatDelugeWaterRisingRate(Double greatDelugeWaterRisingRate) {
        this.greatDelugeWaterRisingRate = greatDelugeWaterRisingRate;
    }

    public Integer getLateAcceptanceSize() {
        return lateAcceptanceSize;
    }

    public void setLateAcceptanceSize(Integer lateAcceptanceSize) {
        this.lateAcceptanceSize = lateAcceptanceSize;
    }

    public Integer getLateSimulatedAnnealingSize() {
        return lateSimulatedAnnealingSize;
    }

    public void setLateSimulatedAnnealingSize(Integer lateSimulatedAnnealingSize) {
        this.lateSimulatedAnnealingSize = lateSimulatedAnnealingSize;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public Acceptor buildAcceptor(EnvironmentMode environmentMode, ScoreDefinition scoreDefinition) {
        List<Acceptor> acceptorList = new ArrayList<Acceptor>();
        if (acceptorClassList != null) {
            for (Class<? extends Acceptor> acceptorClass : acceptorClassList) {
                Acceptor acceptor = ConfigUtils.newInstance(this, "acceptorClass", acceptorClass);
                acceptorList.add(acceptor);
            }
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.PLANNING_ENTITY_TABU))
                || entityTabuSize != null || entityTabuRatio != null
                || fadingEntityTabuSize != null || fadingEntityTabuRatio != null) {
            EntityTabuAcceptor acceptor = new EntityTabuAcceptor();
            if (entityTabuSize != null) {
                if (entityTabuRatio != null) {
                    throw new IllegalArgumentException("The acceptor cannot have both entityTabuSize ("
                            + entityTabuSize + ") and entityTabuRatio (" + entityTabuRatio + ").");
                }
                acceptor.setTabuSizeStrategy(new FixedTabuSizeStrategy(entityTabuSize));
            } else if (entityTabuRatio != null) {
                acceptor.setTabuSizeStrategy(new EntityRatioTabuSizeStrategy(entityTabuRatio));
            }
            if (fadingEntityTabuSize != null) {
                if (fadingEntityTabuRatio != null) {
                    throw new IllegalArgumentException("The acceptor cannot have both fadingEntityTabuSize ("
                            + fadingEntityTabuSize + ") and fadingEntityTabuRatio ("
                            + fadingEntityTabuRatio + ").");
                }
                acceptor.setFadingTabuSizeStrategy(new FixedTabuSizeStrategy(fadingEntityTabuSize));
            } else if (fadingEntityTabuRatio != null) {
                acceptor.setFadingTabuSizeStrategy(new EntityRatioTabuSizeStrategy(fadingEntityTabuRatio));
            }
            if (environmentMode.isNonIntrusiveFullAsserted()) {
                acceptor.setAssertTabuHashCodeCorrectness(true);
            }
            acceptorList.add(acceptor);
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.PLANNING_VALUE_TABU))
                || valueTabuSize != null || valueTabuRatio != null
                || fadingValueTabuSize != null  || fadingValueTabuRatio != null) {
            ValueTabuAcceptor acceptor = new ValueTabuAcceptor();
            if (valueTabuSize != null) {
                if (valueTabuRatio != null) {
                    throw new IllegalArgumentException("The acceptor cannot have both valueTabuSize ("
                            + valueTabuSize + ") and valueTabuRatio (" + valueTabuRatio + ").");
                }
                acceptor.setTabuSizeStrategy(new FixedTabuSizeStrategy(valueTabuSize));
            } else if (valueTabuRatio != null) {
                acceptor.setTabuSizeStrategy(new ValueRatioTabuSizeStrategy(valueTabuRatio));
            }
            if (fadingValueTabuSize != null) {
                if (fadingValueTabuRatio != null) {
                    throw new IllegalArgumentException("The acceptor cannot have both fadingValueTabuSize ("
                            + fadingValueTabuSize + ") and fadingValueTabuRatio ("
                            + fadingValueTabuRatio + ").");
                }
                acceptor.setFadingTabuSizeStrategy(new FixedTabuSizeStrategy(fadingValueTabuSize));
            } else if (fadingValueTabuRatio != null) {
                acceptor.setFadingTabuSizeStrategy(new ValueRatioTabuSizeStrategy(fadingValueTabuRatio));
            }

            if (valueTabuSize != null) {
                acceptor.setTabuSizeStrategy(new FixedTabuSizeStrategy(valueTabuSize));
            }
            if (fadingValueTabuSize != null) {
                acceptor.setFadingTabuSizeStrategy(new FixedTabuSizeStrategy(fadingValueTabuSize));
            }
            if (environmentMode.isNonIntrusiveFullAsserted()) {
                acceptor.setAssertTabuHashCodeCorrectness(true);
            }
            acceptorList.add(acceptor);
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.MOVE_TABU))
                || moveTabuSize != null || fadingMoveTabuSize != null) {
            MoveTabuAcceptor acceptor = new MoveTabuAcceptor();
            acceptor.setUseUndoMoveAsTabuMove(false);
            if (moveTabuSize != null) {
                acceptor.setTabuSizeStrategy(new FixedTabuSizeStrategy(moveTabuSize));
            }
            if (fadingMoveTabuSize != null) {
                acceptor.setFadingTabuSizeStrategy(new FixedTabuSizeStrategy(fadingMoveTabuSize));
            }
            if (environmentMode.isNonIntrusiveFullAsserted()) {
                acceptor.setAssertTabuHashCodeCorrectness(true);
            }
            acceptorList.add(acceptor);
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.UNDO_MOVE_TABU))
                || undoMoveTabuSize != null || fadingUndoMoveTabuSize != null) {
            MoveTabuAcceptor acceptor = new MoveTabuAcceptor();
            acceptor.setUseUndoMoveAsTabuMove(true);
            if (undoMoveTabuSize != null) {
                acceptor.setTabuSizeStrategy(new FixedTabuSizeStrategy(undoMoveTabuSize));
            }
            if (fadingUndoMoveTabuSize != null) {
                acceptor.setFadingTabuSizeStrategy(new FixedTabuSizeStrategy(fadingUndoMoveTabuSize));
            }
            if (environmentMode.isNonIntrusiveFullAsserted()) {
                acceptor.setAssertTabuHashCodeCorrectness(true);
            }
            acceptorList.add(acceptor);
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.SOLUTION_TABU))
                || solutionTabuSize != null || fadingSolutionTabuSize != null) {
            SolutionTabuAcceptor acceptor = new SolutionTabuAcceptor();
            if (solutionTabuSize != null) {
                acceptor.setTabuSizeStrategy(new FixedTabuSizeStrategy(solutionTabuSize));
            }
            if (fadingSolutionTabuSize != null) {
                acceptor.setFadingTabuSizeStrategy(new FixedTabuSizeStrategy(fadingSolutionTabuSize));
            }
            if (environmentMode.isNonIntrusiveFullAsserted()) {
                acceptor.setAssertTabuHashCodeCorrectness(true);
            }
            acceptorList.add(acceptor);
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.SIMULATED_ANNEALING))
                || simulatedAnnealingStartingTemperature != null) {
            SimulatedAnnealingAcceptor acceptor = new SimulatedAnnealingAcceptor();
            acceptor.setStartingTemperature(scoreDefinition.parseScore(simulatedAnnealingStartingTemperature));
            acceptorList.add(acceptor);
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.GREAT_DELUGE))
                || greatDelugeWaterLevelUpperBoundRate != null || greatDelugeWaterRisingRate != null) {
            double waterLevelUpperBoundRate = (Double) ObjectUtils.defaultIfNull(
                    greatDelugeWaterLevelUpperBoundRate, 1.20);
            double waterRisingRate = (Double) ObjectUtils.defaultIfNull(
                    greatDelugeWaterRisingRate, 0.0000001);
            acceptorList.add(new GreatDelugeAcceptor(waterLevelUpperBoundRate, waterRisingRate));
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.LATE_ACCEPTANCE))
                || lateAcceptanceSize != null ) {
            LateAcceptanceAcceptor acceptor = new LateAcceptanceAcceptor();
            acceptor.setLateAcceptanceSize((lateAcceptanceSize == null) ? 1000 : lateAcceptanceSize);
            acceptorList.add(acceptor);
        }
        if ((acceptorTypeList != null && acceptorTypeList.contains(AcceptorType.LATE_SIMULATED_ANNEALING))
                || lateSimulatedAnnealingSize != null ) {
            LateSimulatedAnnealingAcceptor acceptor = new LateSimulatedAnnealingAcceptor();
            acceptor.setLateSimulatedAnnealingSize((lateSimulatedAnnealingSize == null) ? 1000 : lateSimulatedAnnealingSize);
            acceptorList.add(acceptor);
        }
        if (acceptorList.size() == 1) {
            return acceptorList.get(0);
        } else if (acceptorList.size() > 1) {
            CompositeAcceptor compositeAcceptor = new CompositeAcceptor();
            compositeAcceptor.setAcceptorList(acceptorList);
            return compositeAcceptor;
        } else {
            EntityTabuAcceptor entityTabuAcceptor = new EntityTabuAcceptor();
            entityTabuAcceptor.setTabuSizeStrategy(new FixedTabuSizeStrategy(5)); // TODO number pulled out of thin air
            if (environmentMode.isNonIntrusiveFullAsserted()) {
                entityTabuAcceptor.setAssertTabuHashCodeCorrectness(true);
            }
            return entityTabuAcceptor;
        }
    }

    public void inherit(AcceptorConfig inheritedConfig) {
        acceptorClassList = ConfigUtils.inheritMergeableListProperty(acceptorClassList,
                inheritedConfig.getAcceptorClassList());
        if (acceptorTypeList == null) {
            acceptorTypeList = inheritedConfig.getAcceptorTypeList();
        } else {
            List<AcceptorType> inheritedAcceptorTypeList = inheritedConfig.getAcceptorTypeList();
            if (inheritedAcceptorTypeList != null) {
                for (AcceptorType acceptorType : inheritedAcceptorTypeList) {
                    if (!acceptorTypeList.contains(acceptorType)) {
                        acceptorTypeList.add(acceptorType);
                    }
                }
            }
        }
        entityTabuSize = ConfigUtils.inheritOverwritableProperty(entityTabuSize, inheritedConfig.getEntityTabuSize());
        entityTabuRatio = ConfigUtils.inheritOverwritableProperty(entityTabuRatio, inheritedConfig.getEntityTabuRatio());
        fadingEntityTabuSize = ConfigUtils.inheritOverwritableProperty(fadingEntityTabuSize,
                inheritedConfig.getFadingEntityTabuSize());
        fadingEntityTabuRatio = ConfigUtils.inheritOverwritableProperty(fadingEntityTabuRatio,
                inheritedConfig.getFadingEntityTabuRatio());
        valueTabuSize = ConfigUtils.inheritOverwritableProperty(valueTabuSize, inheritedConfig.getValueTabuSize());
        valueTabuRatio = ConfigUtils.inheritOverwritableProperty(valueTabuRatio, inheritedConfig.getValueTabuRatio());
        fadingValueTabuSize = ConfigUtils.inheritOverwritableProperty(fadingValueTabuSize,
                inheritedConfig.getFadingValueTabuSize());
        fadingValueTabuRatio = ConfigUtils.inheritOverwritableProperty(fadingValueTabuRatio,
                inheritedConfig.getFadingValueTabuRatio());
        moveTabuSize = ConfigUtils.inheritOverwritableProperty(moveTabuSize, inheritedConfig.getMoveTabuSize());
        fadingMoveTabuSize = ConfigUtils.inheritOverwritableProperty(fadingMoveTabuSize,
                inheritedConfig.getFadingMoveTabuSize());
        undoMoveTabuSize = ConfigUtils.inheritOverwritableProperty(undoMoveTabuSize,
                inheritedConfig.getUndoMoveTabuSize());
        fadingUndoMoveTabuSize = ConfigUtils.inheritOverwritableProperty(fadingUndoMoveTabuSize,
                inheritedConfig.getFadingUndoMoveTabuSize());
        solutionTabuSize = ConfigUtils.inheritOverwritableProperty(solutionTabuSize,
                inheritedConfig.getSolutionTabuSize());
        fadingSolutionTabuSize = ConfigUtils.inheritOverwritableProperty(fadingSolutionTabuSize,
                inheritedConfig.getFadingSolutionTabuSize());
        simulatedAnnealingStartingTemperature = ConfigUtils.inheritOverwritableProperty(
                simulatedAnnealingStartingTemperature, inheritedConfig.getSimulatedAnnealingStartingTemperature());
        greatDelugeWaterLevelUpperBoundRate = ConfigUtils.inheritOverwritableProperty(
                greatDelugeWaterLevelUpperBoundRate, inheritedConfig.getGreatDelugeWaterLevelUpperBoundRate());
        greatDelugeWaterRisingRate = ConfigUtils.inheritOverwritableProperty(greatDelugeWaterRisingRate,
                inheritedConfig.getGreatDelugeWaterRisingRate());
        lateAcceptanceSize = ConfigUtils.inheritOverwritableProperty(lateAcceptanceSize,
                inheritedConfig.getLateAcceptanceSize());
        lateSimulatedAnnealingSize = ConfigUtils.inheritOverwritableProperty(lateSimulatedAnnealingSize,
                inheritedConfig.getLateSimulatedAnnealingSize());
    }

    public static enum AcceptorType {
        PLANNING_ENTITY_TABU,
        PLANNING_VALUE_TABU,
        MOVE_TABU,
        UNDO_MOVE_TABU,
        SOLUTION_TABU,
        SIMULATED_ANNEALING,
        GREAT_DELUGE,
        LATE_ACCEPTANCE,
        LATE_SIMULATED_ANNEALING,
    }

}
