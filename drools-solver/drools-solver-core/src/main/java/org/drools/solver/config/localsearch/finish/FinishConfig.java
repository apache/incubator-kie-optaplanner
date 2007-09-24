package org.drools.solver.config.localsearch.finish;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.solver.core.localsearch.finish.AbstractCompositeFinish;
import org.drools.solver.core.localsearch.finish.AndCompositeFinish;
import org.drools.solver.core.localsearch.finish.FeasableScoreFinish;
import org.drools.solver.core.localsearch.finish.Finish;
import org.drools.solver.core.localsearch.finish.OrCompositeFinish;
import org.drools.solver.core.localsearch.finish.StepCountFinish;
import org.drools.solver.core.localsearch.finish.TimeMillisSpendFinish;
import org.drools.solver.core.localsearch.finish.UnimprovedStepCountFinish;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("finish")
public class FinishConfig {

    private Finish finish = null; // TODO make into a list
    private Class<Finish> finishClass = null;

    private FinishCompositionStyle finishCompositionStyle = null;

    private Integer maximumStepCount = null;
    private Long maximumTimeMillisSpend = null;
    private Long maximumSecondsSpend = null;
    private Long maximumMinutesSpend = null;
    private Long maximumHouresSpend = null;
    private Double feasableScore = null;
    private Integer maximumUnimprovedStepCount = null;

    public Finish getFinish() {
        return finish;
    }

    public void setFinish(Finish finish) {
        this.finish = finish;
    }

    public Class<Finish> getFinishClass() {
        return finishClass;
    }

    public void setFinishClass(Class<Finish> finishClass) {
        this.finishClass = finishClass;
    }

    public FinishCompositionStyle getFinishCompositionStyle() {
        return finishCompositionStyle;
    }

    public void setFinishCompositionStyle(FinishCompositionStyle finishCompositionStyle) {
        this.finishCompositionStyle = finishCompositionStyle;
    }

    public Integer getMaximumStepCount() {
        return maximumStepCount;
    }

    public void setMaximumStepCount(Integer maximumStepCount) {
        this.maximumStepCount = maximumStepCount;
    }

    public Long getMaximumTimeMillisSpend() {
        return maximumTimeMillisSpend;
    }

    public void setMaximumTimeMillisSpend(Long maximumTimeMillisSpend) {
        this.maximumTimeMillisSpend = maximumTimeMillisSpend;
    }

    public Long getMaximumSecondsSpend() {
        return maximumSecondsSpend;
    }

    public void setMaximumSecondsSpend(Long maximumSecondsSpend) {
        this.maximumSecondsSpend = maximumSecondsSpend;
    }

    public Long getMaximumMinutesSpend() {
        return maximumMinutesSpend;
    }

    public void setMaximumMinutesSpend(Long maximumMinutesSpend) {
        this.maximumMinutesSpend = maximumMinutesSpend;
    }

    public Long getMaximumHouresSpend() {
        return maximumHouresSpend;
    }

    public void setMaximumHouresSpend(Long maximumHouresSpend) {
        this.maximumHouresSpend = maximumHouresSpend;
    }

    public Double getFeasableScore() {
        return feasableScore;
    }

    public void setFeasableScore(Double feasableScore) {
        this.feasableScore = feasableScore;
    }

    public Integer getMaximumUnimprovedStepCount() {
        return maximumUnimprovedStepCount;
    }

    public void setMaximumUnimprovedStepCount(Integer maximumUnimprovedStepCount) {
        this.maximumUnimprovedStepCount = maximumUnimprovedStepCount;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public Finish buildFinish() {
        List<Finish> finishList = new ArrayList<Finish>();
        if (finish != null) {
            finishList.add(finish);
        }
        if (finishClass != null) {
            try {
                finishList.add(finishClass.newInstance());
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("finishClass (" + finishClass.getName()
                        + ") does not have a public no-arg constructor", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("finishClass (" + finishClass.getName()
                        + ") does not have a public no-arg constructor", e);
            }
        }
        if (maximumStepCount != null) {
            StepCountFinish finish = new StepCountFinish();
            finish.setMaximumStepCount(maximumStepCount);
            finishList.add(finish);
        }
        if (maximumTimeMillisSpend != null) {
            TimeMillisSpendFinish finish = new TimeMillisSpendFinish();
            finish.setMaximumTimeMillisSpend(maximumTimeMillisSpend);
            finishList.add(finish);
        }
        if (maximumSecondsSpend != null) {
            TimeMillisSpendFinish finish = new TimeMillisSpendFinish();
            finish.setMaximumTimeMillisSpend(maximumSecondsSpend * 1000L);
            finishList.add(finish);
        }
        if (maximumMinutesSpend != null) {
            TimeMillisSpendFinish finish = new TimeMillisSpendFinish();
            finish.setMaximumTimeMillisSpend(maximumMinutesSpend * 60000L);
            finishList.add(finish);
        }
        if (maximumHouresSpend != null) {
            TimeMillisSpendFinish finish = new TimeMillisSpendFinish();
            finish.setMaximumTimeMillisSpend(maximumHouresSpend * 3600000L);
            finishList.add(finish);
        }
        if (feasableScore != null) {
            FeasableScoreFinish finish = new FeasableScoreFinish();
            finish.setFeasableScore(feasableScore);
            finishList.add(finish);
        }
        if (maximumUnimprovedStepCount != null) {
            UnimprovedStepCountFinish finish = new UnimprovedStepCountFinish();
            finish.setMaximumUnimprovedStepCount(maximumUnimprovedStepCount);
            finishList.add(finish);
        }
        if (finishList.size() == 1) {
            return finishList.get(0);
        } else if (finishList.size() > 1) {
            AbstractCompositeFinish compositeFinish;
            if (finishCompositionStyle == null || finishCompositionStyle == FinishCompositionStyle.OR) {
                compositeFinish = new OrCompositeFinish();
            } else if (finishCompositionStyle == FinishCompositionStyle.AND) {
                compositeFinish = new AndCompositeFinish();
            } else {
                throw new IllegalStateException("finishCompositionStyle (" + finishCompositionStyle
                        + ") not implemented");
            }
            compositeFinish.setFinishList(finishList);
            return compositeFinish;
        } else {
            TimeMillisSpendFinish finish = new TimeMillisSpendFinish();
            finish.setMaximumTimeMillisSpend(60000);
            return finish;
        }
    }

    public void inherit(FinishConfig inheritedConfig) {
        // inherited finishes get compositely added
        if (finish == null) {
            finish = inheritedConfig.getFinish();
        }
        if (finishClass == null) {
            finishClass = inheritedConfig.getFinishClass();
        }
        if (finishCompositionStyle == null) {
            finishCompositionStyle = inheritedConfig.getFinishCompositionStyle();
        }
        if (maximumStepCount == null) {
            maximumStepCount = inheritedConfig.getMaximumStepCount();
        }
        if (maximumTimeMillisSpend == null) {
            maximumTimeMillisSpend = inheritedConfig.getMaximumTimeMillisSpend();
        }
        if (maximumSecondsSpend == null) {
            maximumSecondsSpend = inheritedConfig.getMaximumSecondsSpend();
        }
        if (maximumMinutesSpend == null) {
            maximumMinutesSpend = inheritedConfig.getMaximumMinutesSpend();
        }
        if (maximumHouresSpend == null) {
            maximumHouresSpend = inheritedConfig.getMaximumHouresSpend();
        }
        if (feasableScore == null) {
            feasableScore = inheritedConfig.getFeasableScore();
        }
        if (maximumUnimprovedStepCount == null) {
            maximumUnimprovedStepCount = inheritedConfig.getMaximumUnimprovedStepCount();
        }
    }

    public enum FinishCompositionStyle {
        AND,
        OR,
    }
    
}
