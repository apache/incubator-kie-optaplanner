package org.drools.planner.examples.examination.solver.selector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.planner.config.localsearch.decider.selector.SelectorConfig;
import org.drools.planner.core.localsearch.decider.selector.Selector;

/**
 * A custom selector configuration for the Examination example.
 * @see AllMovesOfOneExamSelector
 * @author Geoffrey De Smet
 */
public class AllMovesOfOneExamSelectorConfig extends SelectorConfig {

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    @Override
    public Selector buildSelector() {
        // Note that all properties of SelectorConfig are ignored.
        return new AllMovesOfOneExamSelector();
    }

    @Override
    public void inherit(SelectorConfig inheritedConfig) {
        // Note that all inherited properties are ignored because all properties of SelectorConfig are ignored.
        super.inherit(inheritedConfig);
        if (inheritedConfig instanceof AllMovesOfOneExamSelectorConfig) {
            AllMovesOfOneExamSelectorConfig allMovesOfOneExamSelectorConfig
                    = (AllMovesOfOneExamSelectorConfig) inheritedConfig;
            // Nothing specifically inheritable at the moment
        }
    }

}
