package org.drools.planner.config.localsearch.decider.accepter;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.lang.ObjectUtils;
import org.drools.planner.core.localsearch.decider.accepter.Accepter;
import org.drools.planner.core.localsearch.decider.accepter.CompositeAccepter;
import org.drools.planner.core.localsearch.decider.accepter.greatdeluge.GreatDelugeAccepter;
import org.drools.planner.core.localsearch.decider.accepter.simulatedannealing.SimulatedAnnealingAccepter;
import org.drools.planner.core.localsearch.decider.accepter.tabu.MoveTabuAccepter;
import org.drools.planner.core.localsearch.decider.accepter.tabu.PropertyTabuAccepter;
import org.drools.planner.core.localsearch.decider.accepter.tabu.SolutionTabuAccepter;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("accepter")
public class AccepterConfig {

    private Accepter accepter = null; // TODO make into a list
    private Class<Accepter> accepterClass = null;

    @XStreamImplicit(itemFieldName = "accepterType")
    private List<AccepterType> accepterTypeList = null;

    protected Integer completeMoveTabuSize = null;
    protected Integer partialMoveTabuSize = null;
    protected Integer completeUndoMoveTabuSize = null;
    protected Integer partialUndoMoveTabuSize = null;
    protected Integer completePropertyTabuSize = null;
    protected Integer partialPropertyTabuSize = null;
    protected Integer completeSolutionTabuSize = null;
    protected Integer partialSolutionTabuSize = null;

    protected Double greatDelugeWaterLevelUpperBoundRate = null;
    protected Double greatDelugeWaterRisingRate = null;

    public Accepter getAccepter() {
        return accepter;
    }

    public void setAccepter(Accepter accepter) {
        this.accepter = accepter;
    }

    public Class<Accepter> getAccepterClass() {
        return accepterClass;
    }

    public void setAccepterClass(Class<Accepter> accepterClass) {
        this.accepterClass = accepterClass;
    }

    public List<AccepterType> getAccepterTypeList() {
        return accepterTypeList;
    }

    public void setAccepterTypeList(List<AccepterType> accepterTypeList) {
        this.accepterTypeList = accepterTypeList;
    }

    public Integer getCompleteMoveTabuSize() {
        return completeMoveTabuSize;
    }

    public void setCompleteMoveTabuSize(Integer completeMoveTabuSize) {
        this.completeMoveTabuSize = completeMoveTabuSize;
    }

    public Integer getPartialMoveTabuSize() {
        return partialMoveTabuSize;
    }

    public void setPartialMoveTabuSize(Integer partialMoveTabuSize) {
        this.partialMoveTabuSize = partialMoveTabuSize;
    }

    public Integer getCompleteUndoMoveTabuSize() {
        return completeUndoMoveTabuSize;
    }

    public void setCompleteUndoMoveTabuSize(Integer completeUndoMoveTabuSize) {
        this.completeUndoMoveTabuSize = completeUndoMoveTabuSize;
    }

    public Integer getPartialUndoMoveTabuSize() {
        return partialUndoMoveTabuSize;
    }

    public void setPartialUndoMoveTabuSize(Integer partialUndoMoveTabuSize) {
        this.partialUndoMoveTabuSize = partialUndoMoveTabuSize;
    }

    public Integer getCompletePropertyTabuSize() {
        return completePropertyTabuSize;
    }

    public void setCompletePropertyTabuSize(Integer completePropertyTabuSize) {
        this.completePropertyTabuSize = completePropertyTabuSize;
    }

    public Integer getPartialPropertyTabuSize() {
        return partialPropertyTabuSize;
    }

    public void setPartialPropertyTabuSize(Integer partialPropertyTabuSize) {
        this.partialPropertyTabuSize = partialPropertyTabuSize;
    }

    public Integer getCompleteSolutionTabuSize() {
        return completeSolutionTabuSize;
    }

    public void setCompleteSolutionTabuSize(Integer completeSolutionTabuSize) {
        this.completeSolutionTabuSize = completeSolutionTabuSize;
    }

    public Integer getPartialSolutionTabuSize() {
        return partialSolutionTabuSize;
    }

    public void setPartialSolutionTabuSize(Integer partialSolutionTabuSize) {
        this.partialSolutionTabuSize = partialSolutionTabuSize;
    }

    public void setGreatDelugeWaterLevelUpperBoundRate(Double greatDelugeWaterLevelUpperBoundRate) {
        this.greatDelugeWaterLevelUpperBoundRate = greatDelugeWaterLevelUpperBoundRate;
    }

    public void setGreatDelugeWaterRisingRate(Double greatDelugeWaterRisingRate) {
        this.greatDelugeWaterRisingRate = greatDelugeWaterRisingRate;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public Accepter buildAccepter() {
        List<Accepter> accepterList = new ArrayList<Accepter>();
        if (accepter != null) {
            accepterList.add(accepter);
        }
        if (accepterClass != null) {
            try {
                accepterList.add(accepterClass.newInstance());
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("accepterClass (" + accepterClass.getName()
                        + ") does not have a public no-arg constructor", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("accepterClass (" + accepterClass.getName()
                        + ") does not have a public no-arg constructor", e);
            }
        }

        if ((accepterTypeList != null && accepterTypeList.contains(AccepterType.MOVE_TABU))
                || completeMoveTabuSize != null || partialMoveTabuSize != null) {
            MoveTabuAccepter moveTabuAccepter = new MoveTabuAccepter();
            moveTabuAccepter.setUseUndoMoveAsTabuMove(false);
            if (completeMoveTabuSize != null) {
                moveTabuAccepter.setCompleteTabuSize(completeMoveTabuSize);
            }
            if (partialMoveTabuSize != null) {
                moveTabuAccepter.setPartialTabuSize(partialMoveTabuSize);
            }
            accepterList.add(moveTabuAccepter);
        }
        if ((accepterTypeList != null && accepterTypeList.contains(AccepterType.UNDO_MOVE_TABU))
                || completeUndoMoveTabuSize != null || partialUndoMoveTabuSize != null) {
            MoveTabuAccepter undoMoveTabuAccepter = new MoveTabuAccepter();
            undoMoveTabuAccepter.setUseUndoMoveAsTabuMove(true);
            if (completeUndoMoveTabuSize != null) {
                undoMoveTabuAccepter.setCompleteTabuSize(completeUndoMoveTabuSize);
            }
            if (partialUndoMoveTabuSize != null) {
                undoMoveTabuAccepter.setPartialTabuSize(partialUndoMoveTabuSize);
            }
            accepterList.add(undoMoveTabuAccepter);
        }
        if ((accepterTypeList != null && accepterTypeList.contains(AccepterType.PROPERTY_TABU))
                || completePropertyTabuSize != null || partialPropertyTabuSize != null) {
            PropertyTabuAccepter propertyTabuAccepter = new PropertyTabuAccepter();
            if (completePropertyTabuSize != null) {
                propertyTabuAccepter.setCompleteTabuSize(completePropertyTabuSize);
            }
            if (partialPropertyTabuSize != null) {
                propertyTabuAccepter.setPartialTabuSize(partialPropertyTabuSize);
            }
            accepterList.add(propertyTabuAccepter);
        }
        if ((accepterTypeList != null && accepterTypeList.contains(AccepterType.SOLUTION_TABU))
                || completeSolutionTabuSize != null || partialSolutionTabuSize != null) {
            SolutionTabuAccepter solutionTabuAccepter = new SolutionTabuAccepter();
            if (completeSolutionTabuSize != null) {
                solutionTabuAccepter.setCompleteTabuSize(completeSolutionTabuSize);
            }
            if (partialSolutionTabuSize != null) {
                solutionTabuAccepter.setPartialTabuSize(partialSolutionTabuSize);
            }
            accepterList.add(solutionTabuAccepter);
        }
        if ((accepterTypeList != null && accepterTypeList.contains(AccepterType.SIMULATED_ANNEALING))) {
            SimulatedAnnealingAccepter simulatedAnnealingAccepter = new SimulatedAnnealingAccepter();
            accepterList.add(simulatedAnnealingAccepter);
        }
        if ((accepterTypeList != null && accepterTypeList.contains(AccepterType.GREAT_DELUGE))
                || greatDelugeWaterLevelUpperBoundRate != null || greatDelugeWaterRisingRate != null) {
            double waterLevelUpperBoundRate = (Double) ObjectUtils.defaultIfNull(
                    greatDelugeWaterLevelUpperBoundRate, 1.20);
            double waterRisingRate = (Double) ObjectUtils.defaultIfNull(
                    greatDelugeWaterRisingRate, 0.0000001);
            accepterList.add(new GreatDelugeAccepter(waterLevelUpperBoundRate, waterRisingRate));
        }
        if (accepterList.size() == 1) {
            return accepterList.get(0);
        } else if (accepterList.size() > 1) {
            CompositeAccepter compositeAccepter = new CompositeAccepter();
            compositeAccepter.setAccepterList(accepterList);
            return compositeAccepter;
        } else {
            SolutionTabuAccepter solutionTabuAccepter = new SolutionTabuAccepter();
            solutionTabuAccepter.setCompleteTabuSize(1500); // TODO number pulled out of thin air
            return solutionTabuAccepter;
        }
    }

    public void inherit(AccepterConfig inheritedConfig) {
        // inherited accepters get compositely added
        if (accepter == null) {
            accepter = inheritedConfig.getAccepter();
        }
        if (accepterClass == null) {
            accepterClass = inheritedConfig.getAccepterClass();
        }
        if (accepterTypeList == null) {
            accepterTypeList = inheritedConfig.getAccepterTypeList();
        } else {
            List<AccepterType> inheritedAccepterTypeList = inheritedConfig.getAccepterTypeList();
            if (inheritedAccepterTypeList != null) {
                for (AccepterType accepterType : inheritedAccepterTypeList) {
                    if (!accepterTypeList.contains(accepterType)) {
                        accepterTypeList.add(accepterType);
                    }
                }
            }
        }
        if (completeMoveTabuSize == null) {
            completeMoveTabuSize = inheritedConfig.getCompleteMoveTabuSize();
        }
        if (partialMoveTabuSize == null) {
            partialMoveTabuSize = inheritedConfig.getPartialMoveTabuSize();
        }
        if (completeUndoMoveTabuSize == null) {
            completeUndoMoveTabuSize = inheritedConfig.getCompleteUndoMoveTabuSize();
        }
        if (partialUndoMoveTabuSize == null) {
            partialUndoMoveTabuSize = inheritedConfig.getPartialUndoMoveTabuSize();
        }
        if (completePropertyTabuSize == null) {
            completePropertyTabuSize = inheritedConfig.getCompletePropertyTabuSize();
        }
        if (partialPropertyTabuSize == null) {
            partialPropertyTabuSize = inheritedConfig.getPartialPropertyTabuSize();
        }
        if (completeSolutionTabuSize == null) {
            completeSolutionTabuSize = inheritedConfig.getCompleteSolutionTabuSize();
        }
        if (partialSolutionTabuSize == null) {
            partialSolutionTabuSize = inheritedConfig.getPartialSolutionTabuSize();
        }
    }

    public static enum AccepterType {
        MOVE_TABU,
        UNDO_MOVE_TABU,
        PROPERTY_TABU,
        SOLUTION_TABU,
        SIMULATED_ANNEALING,
        GREAT_DELUGE,
    }

}
