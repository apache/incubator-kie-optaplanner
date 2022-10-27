package org.optaplanner.examples.cloudbalancing.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

import org.optaplanner.examples.common.domain.AbstractPersistableJaxb;
import org.optaplanner.examples.common.swingui.components.Labeled;

public class CloudComputer extends AbstractPersistableJaxb implements Labeled {

    private int cpuPower; // in gigahertz
    private int memory; // in gigabyte RAM
    private int networkBandwidth; // in gigabyte per hour
    private int cost; // in euro per month

    CloudComputer() { // For JAXB.
    }

    public CloudComputer(long id, int cpuPower, int memory, int networkBandwidth, int cost) {
        super(id);
        this.cpuPower = cpuPower;
        this.memory = memory;
        this.networkBandwidth = networkBandwidth;
        this.cost = cost;
    }

    public int getCpuPower() {
        return cpuPower;
    }

    public void setCpuPower(int cpuPower) {
        this.cpuPower = cpuPower;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getNetworkBandwidth() {
        return networkBandwidth;
    }

    public void setNetworkBandwidth(int networkBandwidth) {
        this.networkBandwidth = networkBandwidth;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    // ************************************************************************
    // JAXB-related methods; JAXB doesn't like these on AbstractPersistable.
    // ************************************************************************

    @XmlID
    @XmlAttribute(required = true, name = "id")
    final String getXmlId() { // Works around the fact that XML ID has to be a String.
        return Long.toString(id);
    }

    final void setXmlId(String id) {
        this.id = Long.parseLong(id);
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int getMultiplicand() {
        return cpuPower * memory * networkBandwidth;
    }

    @Override
    public String getLabel() {
        return "Computer " + id;
    }

}
