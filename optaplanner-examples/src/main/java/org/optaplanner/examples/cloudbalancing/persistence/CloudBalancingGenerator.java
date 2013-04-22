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

package org.optaplanner.examples.cloudbalancing.persistence;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.domain.CloudComputer;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;
import org.optaplanner.examples.common.app.LoggingMain;
import org.optaplanner.examples.common.persistence.SolutionDao;

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

    private static final Price[] CPU_POWER_PRICES = { // in gigahertz
            new Price(3, "single core 3ghz", 110),
            new Price(4, "dual core 2ghz", 140),
            new Price(6, "dual core 3ghz", 180),
            new Price(8, "quad core 2ghz", 270),
            new Price(12, "quad core 3ghz", 400),
            new Price(16, "quad core 4ghz", 1000),
            new Price(24, "eight core 3ghz", 3000),
    };
    private static final Price[] MEMORY_PRICES = { // in gigabyte RAM
            new Price(2, "2 gigabyte", 140),
            new Price(4, "4 gigabyte", 180),
            new Price(8, "8 gigabyte", 220),
            new Price(16, "16 gigabyte", 300),
            new Price(32, "32 gigabyte", 400),
            new Price(64, "64 gigabyte", 600),
            new Price(96, "96 gigabyte", 1000),
    };
    private static final Price[] NETWORK_BANDWIDTH_PRICES = { // in gigabyte per hour
            new Price(2, "2 gigabyte", 100),
            new Price(4, "4 gigabyte", 200),
            new Price(6, "6 gigabyte", 300),
            new Price(8, "8 gigabyte", 400),
            new Price(12, "12 gigabyte", 600),
            new Price(16, "16 gigabyte", 800),
            new Price(20, "20 gigabyte", 1000),
    };

    private static final int MAXIMUM_REQUIRED_CPU_POWER = 12; // in gigahertz
    private static final int MAXIMUM_REQUIRED_MEMORY = 32; // in gigabyte RAM
    private static final int MAXIMUM_REQUIRED_NETWORK_BANDWIDTH = 12; // in gigabyte per hour

    private static final File outputDir = new File("data/cloudbalancing/unsolved/");

    public static void main(String[] args) {
        new CloudBalancingGenerator().generate();
    }

    protected SolutionDao solutionDao;
    private Random random;

    public CloudBalancingGenerator() {
        checkConfiguration();
        solutionDao = new CloudBalancingDao();
    }

    public void generate() {
        writeCloudBalance(2, 6);
        writeCloudBalance(3, 9);
        writeCloudBalance(4, 12);
        writeCloudBalance(100, 300);
        writeCloudBalance(200, 600);
        writeCloudBalance(400, 1200);
        writeCloudBalance(800, 2400);
        writeCloudBalance(1600, 4800);
    }

    private void checkConfiguration() {
        if (CPU_POWER_PRICES.length != MEMORY_PRICES.length || CPU_POWER_PRICES.length != NETWORK_BANDWIDTH_PRICES.length) {
            throw new IllegalStateException("All price arrays must be equal in length.");
        }
    }

    private void writeCloudBalance(int cloudComputerListSize, int cloudProcessListSize) {
        String inputId = determineInputId(cloudComputerListSize, cloudProcessListSize);
        File outputFile = new File(outputDir, inputId + ".xml");
        CloudBalance cloudBalance = createCloudBalance(inputId, cloudComputerListSize, cloudProcessListSize);
        solutionDao.writeSolution(cloudBalance, outputFile);
    }

    private String determineInputId(int cloudComputerListSize, int cloudProcessListSize) {
        String cloudComputerListSizeString = Integer.toString(cloudComputerListSize);
        if (cloudComputerListSizeString.length() < 4) {
            cloudComputerListSizeString = "0000".substring(0, 4 - cloudComputerListSizeString.length()) + cloudComputerListSizeString;
        }
        String cloudProcessListSizeString = Integer.toString(cloudProcessListSize);
        if (cloudProcessListSizeString.length() < 4) {
            cloudProcessListSizeString = "0000".substring(0, 4 - cloudProcessListSizeString.length()) + cloudProcessListSizeString;
        }
        return "cb-" + cloudComputerListSizeString + "comp-" + cloudProcessListSizeString + "proc";
    }

    public CloudBalance createCloudBalance(int cloudComputerListSize, int cloudProcessListSize) {
        return createCloudBalance(determineInputId(cloudComputerListSize, cloudProcessListSize),
                cloudComputerListSize, cloudProcessListSize);
    }

    public CloudBalance createCloudBalance(String inputId, int cloudComputerListSize, int cloudProcessListSize) {
        random = new Random(47);
        CloudBalance cloudBalance = new CloudBalance();
        cloudBalance.setId(0L);
        createCloudComputerList(cloudBalance, cloudComputerListSize);
        createCloudProcessList(cloudBalance, cloudProcessListSize);
        BigInteger possibleSolutionSize = BigInteger.valueOf(cloudBalance.getComputerList().size()).pow(
                cloudBalance.getProcessList().size());
        String flooredPossibleSolutionSize = "10^" + (possibleSolutionSize.toString().length() - 1);
        logger.info("CloudBalance {} has {} computers and {} processes with a search space of {}.",
                inputId, cloudComputerListSize, cloudProcessListSize,
                possibleSolutionSize.compareTo(BigInteger.valueOf(1000L)) < 0
                        ? possibleSolutionSize : flooredPossibleSolutionSize);
        return cloudBalance;
    }

    private void createCloudComputerList(CloudBalance cloudBalance, int cloudComputerListSize) {
        List<CloudComputer> cloudComputerList = new ArrayList<CloudComputer>(cloudComputerListSize);
        for (int i = 0; i < cloudComputerListSize; i++) {
            CloudComputer cloudComputer = new CloudComputer();
            cloudComputer.setId((long) i);
            int cpuPowerPricesIndex = random.nextInt(CPU_POWER_PRICES.length);
            cloudComputer.setCpuPower(CPU_POWER_PRICES[cpuPowerPricesIndex].getHardwareValue());
            int memoryPricesIndex = distortIndex(cpuPowerPricesIndex, MEMORY_PRICES.length);
            cloudComputer.setMemory(MEMORY_PRICES[memoryPricesIndex].getHardwareValue());
            int networkBandwidthPricesIndex = distortIndex(cpuPowerPricesIndex, NETWORK_BANDWIDTH_PRICES.length);
            cloudComputer.setNetworkBandwidth(NETWORK_BANDWIDTH_PRICES[networkBandwidthPricesIndex].getHardwareValue());
            int cost = CPU_POWER_PRICES[cpuPowerPricesIndex].getCost()
                    + MEMORY_PRICES[memoryPricesIndex].getCost()
                    + NETWORK_BANDWIDTH_PRICES[networkBandwidthPricesIndex].getCost();
            logger.trace("Created cloudComputer with cpuPowerPricesIndex ({}), memoryPricesIndex({}),"
                    + " networkBandwidthPricesIndex({}).",
                    cpuPowerPricesIndex, memoryPricesIndex, networkBandwidthPricesIndex);
            cloudComputer.setCost(cost);
            cloudComputerList.add(cloudComputer);
        }
        cloudBalance.setComputerList(cloudComputerList);
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

    private void createCloudProcessList(CloudBalance cloudBalance, int cloudProcessListSize) {
        List<CloudProcess> cloudProcessList = new ArrayList<CloudProcess>(cloudProcessListSize);
        for (int i = 0; i < cloudProcessListSize; i++) {
            CloudProcess cloudProcess = new CloudProcess();
            cloudProcess.setId((long) i);
            int requiredCpuPower = generateRandom(MAXIMUM_REQUIRED_CPU_POWER);
            cloudProcess.setRequiredCpuPower(requiredCpuPower);
            int requiredMemory = generateRandom(MAXIMUM_REQUIRED_MEMORY);
            cloudProcess.setRequiredMemory(requiredMemory);
            int requiredNetworkBandwidth = generateRandom(MAXIMUM_REQUIRED_NETWORK_BANDWIDTH);
            cloudProcess.setRequiredNetworkBandwidth(requiredNetworkBandwidth);
            logger.trace("Created CloudProcess with requiredCpuPower ({}), requiredMemory({}),"
                    + " requiredNetworkBandwidth({}).",
                    requiredCpuPower, requiredMemory, requiredNetworkBandwidth);
            // Notice that we leave the PlanningVariable properties on null
            cloudProcessList.add(cloudProcess);
        }
        cloudBalance.setProcessList(cloudProcessList);
    }

    private int generateRandom(int maximumValue) {
        double randomDouble = random.nextDouble();
        double parabolaBase = 2000.0;
        double parabolaRandomDouble = (Math.pow(parabolaBase, randomDouble) - 1.0) / (parabolaBase - 1.0);
        if (parabolaRandomDouble < 0.0 || parabolaRandomDouble >= 1.0) {
            throw new IllegalArgumentException("Invalid generated parabolaRandomDouble (" + parabolaRandomDouble + ")");
        }
        int value = ((int) Math.floor(parabolaRandomDouble * ((double) maximumValue))) + 1;
        if (value < 1 || value > maximumValue) {
            throw new IllegalArgumentException("Invalid generated value (" + value + ")");
        }
        return value;
    }

}
