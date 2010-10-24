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

package org.drools.planner.examples.cloudbalancing.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import org.drools.planner.examples.cloudbalancing.domain.CloudBalance;
import org.drools.planner.examples.cloudbalancing.domain.CloudComputer;
import org.drools.planner.examples.cloudbalancing.domain.CloudProcess;
import org.drools.planner.examples.common.app.LoggingMain;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.nqueens.domain.NQueens;
import org.drools.planner.examples.nqueens.domain.Queen;

/**
 * @author Geoffrey De Smet
 */
public class CloudBalancingGenerator extends LoggingMain {

    private static class Price {
        private int hardwareValue;
        private String description;
        private int cost;

        private Price(int hardwareValue, String description, int cost) {
            this.hardwareValue = hardwareValue;
            this.description = description;
            this.cost = cost;
        }

        public int getHardwareValue() {
            return hardwareValue;
        }

        public String getDescription() {
            return description;
        }

        public int getCost() {
            return cost;
        }
    }

    private static final Price[] cpuPowerPrices = { // in gigahertz
        new Price(2, "single core 2ghz", 100),
        new Price(3, "single core 3ghz", 110),
        new Price(4, "dual core 2ghz", 140),
        new Price(6, "dual core 3ghz", 180),
        new Price(8, "quad core 2ghz", 270),
        new Price(12, "quad core 3ghz", 400),
        new Price(16, "quad core 4ghz", 1000),
    };
    private static final Price[] memoryPrices = { // in gigabyte RAM
        new Price(1, "1 gigabyte", 100),
        new Price(2, "2 gigabyte", 140),
        new Price(4, "4 gigabyte", 180),
        new Price(8, "8 gigabyte", 220),
        new Price(16, "16 gigabyte", 300),
        new Price(32, "32 gigabyte", 400),
        new Price(64, "64 gigabyte", 500),
    };
    private static final Price[] networkBandwidthPrices = { // in gigabyte per hour
        new Price(1, "1 gigabyte", 50),
        new Price(2, "2 gigabyte", 100),
        new Price(4, "4 gigabyte", 200),
        new Price(6, "6 gigabyte", 300),
        new Price(8, "8 gigabyte", 400),
        new Price(12, "12 gigabyte", 600),
        new Price(16, "16 gigabyte", 800),
    };

    private static final File outputDir = new File("data/cloudbalancing/unsolved/");

    public static void main(String[] args) {
        new CloudBalancingGenerator().generate();
    }


    protected SolutionDao solutionDao;
    private Random random;

    public CloudBalancingGenerator() {
        checkConfiguration();
        solutionDao = new CloudBalancingDaoImpl();
        random = new Random(1981);
    }

    public void generate() {
        writeCloudBalance(10, 10);
        writeCloudBalance(10, 20);
        writeCloudBalance(100, 200);
    }

    private void checkConfiguration() {
        if (cpuPowerPrices.length != memoryPrices.length || cpuPowerPrices.length != networkBandwidthPrices.length) {
            throw new IllegalStateException("All price arrays must be equal in length.");
        }
    }

    private void writeCloudBalance(int cloudComputerListSize, int cloudProcessListSize) {
        String outputFileName = "unsolvedCloudBalance" + cloudComputerListSize + "-" + cloudProcessListSize + ".xml";
        File outputFile = new File(outputDir, outputFileName);
        CloudBalance cloudBalance = createCloudBalance(cloudComputerListSize, cloudProcessListSize);
        solutionDao.writeSolution(cloudBalance, outputFile);
    }

    private CloudBalance createCloudBalance(int cloudComputerListSize, int cloudProcessListSize) {
        CloudBalance cloudBalance = new CloudBalance();
        cloudBalance.setId(0L);
        cloudBalance.setCloudComputerList(createCloudComputerList(cloudComputerListSize));
        cloudBalance.setCloudProcessList(createCloudProcessList(cloudProcessListSize));
        return cloudBalance;
    }

    private List<CloudComputer> createCloudComputerList(int cloudComputerListSize) {
        List<CloudComputer> cloudComputerList = new ArrayList<CloudComputer>(cloudComputerListSize);
        for (int i = 0; i < cloudComputerListSize; i++) {
            CloudComputer cloudComputer = new CloudComputer();
            cloudComputer.setId((long) i);
            int cpuPowerPricesIndex = random.nextInt(cpuPowerPrices.length);
            cloudComputer.setCpuPower(cpuPowerPrices[cpuPowerPricesIndex].getHardwareValue());
            int memoryPricesIndex = distortIndex(cpuPowerPricesIndex, memoryPrices.length);
            cloudComputer.setMemory(memoryPrices[memoryPricesIndex].getHardwareValue());
            int networkBandwidthPricesIndex = distortIndex(cpuPowerPricesIndex, networkBandwidthPrices.length);
            cloudComputer.setNetworkBandwidth(networkBandwidthPrices[networkBandwidthPricesIndex].getHardwareValue());
            int cost = cpuPowerPrices[cpuPowerPricesIndex].getCost()
                    + memoryPrices[memoryPricesIndex].getCost()
                    + networkBandwidthPrices[networkBandwidthPricesIndex].getCost();
            logger.info("Created cloudComputer with cpuPowerPricesIndex ({}), memoryPricesIndex({}),"
                    + " networkBandwidthPricesIndex({}).",
                    new Object[]{cpuPowerPricesIndex, memoryPricesIndex, networkBandwidthPricesIndex});
            cloudComputer.setCost(cost);
            cloudComputerList.add(cloudComputer);
        }
        return cloudComputerList;
    }

    private int distortIndex(int referenceIndex, int length) {
        int index = referenceIndex;
        double randomDouble = random.nextDouble();
        double loweringThreshold = 0.25;
        while (randomDouble < loweringThreshold && index >= 1) {
            index--;
            loweringThreshold *= 0.10;
        }
        double heighteningThreshold = 0.75;
        while (randomDouble >= heighteningThreshold && index <= (length - 2)) {
            index++;
            heighteningThreshold = (1.0 - ((1.0 - heighteningThreshold) * 0.10));
        }
        return index;
    }

    private List<CloudProcess> createCloudProcessList(int cloudProcessListSize) {
        List<CloudProcess> cloudProcessList = new ArrayList<CloudProcess>(cloudProcessListSize);
        for (int i = 0; i < cloudProcessListSize; i++) {
            CloudProcess cloudProcess = new CloudProcess();
            cloudProcess.setId((long) i);
            // TODO
            cloudProcessList.add(cloudProcess);
        }
        return cloudProcessList;
    }

}
