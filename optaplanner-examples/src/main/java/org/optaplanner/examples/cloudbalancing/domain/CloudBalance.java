package org.optaplanner.examples.cloudbalancing.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.examples.common.domain.AbstractPersistableJaxb;
import org.optaplanner.persistence.jaxb.api.score.buildin.hardsoft.HardSoftScoreJaxbAdapter;

@PlanningSolution
@XmlRootElement
public class CloudBalance extends AbstractPersistableJaxb {

    @XmlElementWrapper
    @XmlElement(name = "cloudComputer")
    private List<CloudComputer> computerList;

    @XmlElementWrapper
    @XmlElement(name = "cloudProcess")
    private List<CloudProcess> processList;

    @XmlJavaTypeAdapter(HardSoftScoreJaxbAdapter.class)
    private HardSoftScore score;

    CloudBalance() { // For JAXB
    }

    public CloudBalance(long id, List<CloudComputer> computerList, List<CloudProcess> processList) {
        super(id);
        this.computerList = computerList;
        this.processList = processList;
    }

    @ValueRangeProvider(id = "computerRange")
    @ProblemFactCollectionProperty
    public List<CloudComputer> getComputerList() {
        return computerList;
    }

    public void setComputerList(List<CloudComputer> computerList) {
        this.computerList = computerList;
    }

    @PlanningEntityCollectionProperty
    public List<CloudProcess> getProcessList() {
        return processList;
    }

    public void setProcessList(List<CloudProcess> processList) {
        this.processList = processList;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
