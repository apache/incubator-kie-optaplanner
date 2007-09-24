package org.drools.solver.config.localsearch.decider.selector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.solver.core.localsearch.decider.selector.MoveFactory;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("selector")
public class SelectorConfig {

    private MoveFactory moveFactory = null;
    private Class<MoveFactory> moveFactoryClass = null;

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

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public MoveFactory buildMoveFactory() {
        if (moveFactory != null) {
            return moveFactory;
        } else if (moveFactoryClass != null) {
            try {
                return moveFactoryClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("moveFactoryClass (" + moveFactoryClass.getName()
                        + ") does not have a public no-arg constructor", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("moveFactoryClass (" + moveFactoryClass.getName()
                        + ") does not have a public no-arg constructor", e);
            }
        } else {
            throw new IllegalArgumentException("A selector with a moveFactory or moveFactory class is required.");
        }
    }

    public void inherit(SelectorConfig inheritedConfig) {
        if (moveFactory == null && moveFactoryClass == null) {
            moveFactory = inheritedConfig.getMoveFactory();
            moveFactoryClass = inheritedConfig.getMoveFactoryClass();
        }
    }
    
}
