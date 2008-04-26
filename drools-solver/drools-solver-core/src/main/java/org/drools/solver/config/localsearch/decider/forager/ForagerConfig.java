package org.drools.solver.config.localsearch.decider.forager;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.solver.core.localsearch.decider.forager.AcceptedForager;
import org.drools.solver.core.localsearch.decider.forager.Forager;
import org.drools.solver.core.localsearch.decider.forager.PickEarlyByScore;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("forager")
public class ForagerConfig {

    private Forager forager = null;
    private Class<Forager> foragerClass = null;
    private ForagerType foragerType = null;

    public Forager getForager() {
        return forager;
    }

    public void setForager(Forager forager) {
        this.forager = forager;
    }

    public Class<Forager> getForagerClass() {
        return foragerClass;
    }

    public void setForagerClass(Class<Forager> foragerClass) {
        this.foragerClass = foragerClass;
    }

    public ForagerType getForagerType() {
        return foragerType;
    }

    public void setForagerType(ForagerType foragerType) {
        this.foragerType = foragerType;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public Forager buildForager() {
        if (forager != null) {
            return forager;
        } else if (foragerClass != null) {
            try {
                return foragerClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("foragerClass (" + foragerClass.getName()
                        + ") does not have a public no-arg constructor", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("foragerClass (" + foragerClass.getName()
                        + ") does not have a public no-arg constructor", e);
            }
        } else if (foragerType != null) {
            switch (foragerType) {
                case MAX_SCORE_OF_ALL:
                    return new AcceptedForager(PickEarlyByScore.NONE, false);
                case FIRST_BEST_SCORE_IMPROVING:
                    return new AcceptedForager(PickEarlyByScore.FIRST_BEST_SCORE_IMPROVING, false);
                case FIRST_LAST_STEP_SCORE_IMPROVING:
                    return new AcceptedForager(PickEarlyByScore.FIRST_LAST_STEP_SCORE_IMPROVING, false);
                case FIRST_RANDOMLY_ACCEPTED:
                    return new AcceptedForager(PickEarlyByScore.NONE, true);
                default:
                    throw new IllegalStateException("foragerType (" + foragerType + ") not implemented");
            }
        } else {
            return new AcceptedForager(PickEarlyByScore.NONE, false);
        }
    }

    public void inherit(ForagerConfig inheritedConfig) {
        if (forager == null && foragerClass == null && foragerType == null) {
            forager = inheritedConfig.getForager();
            foragerClass = inheritedConfig.getForagerClass();
            foragerType = inheritedConfig.getForagerType();
        }
    }

    public static enum ForagerType {
        MAX_SCORE_OF_ALL,
        FIRST_BEST_SCORE_IMPROVING,
        FIRST_LAST_STEP_SCORE_IMPROVING,
        FIRST_RANDOMLY_ACCEPTED
    }

}
