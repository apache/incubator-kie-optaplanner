package org.drools.planner.config.localsearch.decider.selector;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.drools.planner.core.localsearch.decider.selector.CompositeSelector;
import org.drools.planner.core.localsearch.decider.selector.MoveFactorySelector;
import org.drools.planner.core.localsearch.decider.selector.Selector;
import org.drools.planner.core.localsearch.decider.selector.TopListSelector;
import org.drools.planner.core.move.factory.MoveFactory;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("selector")
public class SelectorConfig {

    @XStreamImplicit(itemFieldName = "selector")
    private List<SelectorConfig> selectorConfigList = null;
    
    private MoveFactory moveFactory = null;
    private Class<MoveFactory> moveFactoryClass = null;
    protected Boolean shuffle = null;
    protected Double relativeSelection = null;
    protected Integer absoluteSelection = null;

    private Integer topSize = null;

    public List<SelectorConfig> getSelectorConfigList() {
        return selectorConfigList;
    }

    public void setSelectorConfigList(List<SelectorConfig> selectorConfigList) {
        this.selectorConfigList = selectorConfigList;
    }

    public MoveFactory getMoveFactory() {
        return moveFactory;
    }

    public void setMoveFactory(MoveFactory moveFactory) {
        this.moveFactory = moveFactory;
    }

    public Class<MoveFactory> getMoveFactoryClass() {
        return moveFactoryClass;
    }

    public void setMoveFactoryClass(Class<MoveFactory> moveFactoryClass) {
        this.moveFactoryClass = moveFactoryClass;
    }

    public Boolean getShuffle() {
        return shuffle;
    }

    public void setShuffle(Boolean shuffle) {
        this.shuffle = shuffle;
    }

    public Double getRelativeSelection() {
        return relativeSelection;
    }

    public void setRelativeSelection(Double relativeSelection) {
        this.relativeSelection = relativeSelection;
    }

    public Integer getAbsoluteSelection() {
        return absoluteSelection;
    }

    public void setAbsoluteSelection(Integer absoluteSelection) {
        this.absoluteSelection = absoluteSelection;
    }

    public Integer getTopSize() {
        return topSize;
    }

    public void setTopSize(Integer topSize) {
        this.topSize = topSize;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public Selector buildSelector() {
        if (selectorConfigList != null) {
            List<Selector> selectorList = new ArrayList<Selector>(selectorConfigList.size());
            for (SelectorConfig selectorConfig : selectorConfigList) {
                selectorList.add(selectorConfig.buildSelector());
            }
            CompositeSelector selector = new CompositeSelector();
            selector.setSelectorList(selectorList);
            return selector;
        } else if (moveFactory != null || moveFactoryClass != null) {
            MoveFactory initializedMoveFactory;
            if (moveFactory != null) {
                initializedMoveFactory = moveFactory;
            } else {
                try {
                    initializedMoveFactory = moveFactoryClass.newInstance();
                } catch (InstantiationException e) {
                    throw new IllegalArgumentException("The moveFactoryClass (" + moveFactoryClass.getName()
                            + ") does not have a public no-arg constructor", e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("The moveFactoryClass (" + moveFactoryClass.getName()
                            + ") does not have a public no-arg constructor", e);
                }
            }
            MoveFactorySelector selector = new MoveFactorySelector();
            selector.setMoveFactory(initializedMoveFactory);
            if (shuffle != null) {
                selector.setShuffle(shuffle.booleanValue());
            } else {
                selector.setShuffle(relativeSelection != null || absoluteSelection != null);
            }
            if (relativeSelection != null) {
                selector.setRelativeSelection(relativeSelection);
            }
            if (absoluteSelection != null) {
                selector.setAbsoluteSelection(absoluteSelection);
            }
            return selector;
        } else if (topSize != null) {
            TopListSelector selector = new TopListSelector();
            selector.setTopSize(topSize);
            return selector;
        } else {
            throw new IllegalArgumentException("A selector with a moveFactory or moveFactory class is required.");
        }
    }

    public void inherit(SelectorConfig inheritedConfig) {
        if (moveFactory == null && moveFactoryClass == null) {
            moveFactory = inheritedConfig.getMoveFactory();
            moveFactoryClass = inheritedConfig.getMoveFactoryClass();
        }
        if (selectorConfigList == null) {
            selectorConfigList = inheritedConfig.getSelectorConfigList();
        } else {
            List<SelectorConfig> inheritedSelectorConfigList = inheritedConfig.getSelectorConfigList();
            if (inheritedSelectorConfigList != null) {
                for (SelectorConfig selectorConfig : inheritedSelectorConfigList) {
                    selectorConfigList.add(selectorConfig);
                }
            }
        }
        if (shuffle == null) {
            shuffle = inheritedConfig.getShuffle();
        }
        if (relativeSelection == null) {
            relativeSelection = inheritedConfig.getRelativeSelection();
        }
        if (absoluteSelection == null) {
            absoluteSelection = inheritedConfig.getAbsoluteSelection();
        }
        if (topSize == null) {
            topSize = inheritedConfig.getTopSize();
        }
    }
    
}
