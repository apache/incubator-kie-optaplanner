package org.optaplanner.core.config.solver.termination;

import jakarta.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum TerminationCompositionStyle {
    AND,
    OR;
}
