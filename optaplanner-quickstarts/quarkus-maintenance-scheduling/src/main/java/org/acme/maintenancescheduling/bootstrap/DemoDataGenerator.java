/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.acme.maintenancescheduling.bootstrap;

import io.quarkus.runtime.StartupEvent;
import org.acme.maintenancescheduling.domain.MaintainableUnit;
import org.acme.maintenancescheduling.domain.MaintenanceCrew;
import org.acme.maintenancescheduling.domain.MaintenanceJob;
import org.acme.maintenancescheduling.domain.MutuallyExclusiveJobs;
import org.acme.maintenancescheduling.domain.TimeGrain;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class DemoDataGenerator {

    @ConfigProperty(name = "schedule.demoData", defaultValue = "SMALL")
    public DemoData demoData;

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        if (demoData == DemoData.NONE) {
            return;
        }

        List<MaintainableUnit> maintainableUnitList = new ArrayList<>();
        maintainableUnitList.add(new MaintainableUnit("Track 1"));
        maintainableUnitList.add(new MaintainableUnit("Track 2"));
        maintainableUnitList.add(new MaintainableUnit("Track 3"));
        maintainableUnitList.add(new MaintainableUnit("Train 1"));
        maintainableUnitList.add(new MaintainableUnit("Train 2"));
        maintainableUnitList.add(new MaintainableUnit("Train 3"));
        if (demoData == DemoData.LARGE) {
            maintainableUnitList.add(new MaintainableUnit("Track 4"));
            maintainableUnitList.add(new MaintainableUnit("Track 5"));
            maintainableUnitList.add(new MaintainableUnit("Track 6"));
            maintainableUnitList.add(new MaintainableUnit("Train 4"));
            maintainableUnitList.add(new MaintainableUnit("Train 5"));
            maintainableUnitList.add(new MaintainableUnit("Train 6"));
        }
        MaintainableUnit.persist(maintainableUnitList);

        List<MaintenanceCrew> maintenanceCrewList = new ArrayList<>();
        maintenanceCrewList.add(new MaintenanceCrew("Crew 1"));
        maintenanceCrewList.add(new MaintenanceCrew("Crew 2"));
        maintenanceCrewList.add(new MaintenanceCrew("Crew 3"));
        if (demoData == DemoData.LARGE) {
            maintenanceCrewList.add(new MaintenanceCrew("Crew 4"));
            maintenanceCrewList.add(new MaintenanceCrew("Crew 5"));
            maintenanceCrewList.add(new MaintenanceCrew("Crew 6"));
        }
        MaintenanceCrew.persist(maintenanceCrewList);

        List<TimeGrain> timeGrainList = new ArrayList<>();
        timeGrainList.add(new TimeGrain(0));
        timeGrainList.add(new TimeGrain(1));
        timeGrainList.add(new TimeGrain(2));
        timeGrainList.add(new TimeGrain(3));
        timeGrainList.add(new TimeGrain(4));
        timeGrainList.add(new TimeGrain(5));
        timeGrainList.add(new TimeGrain(6));
        timeGrainList.add(new TimeGrain(7));
        timeGrainList.add(new TimeGrain(8));
        timeGrainList.add(new TimeGrain(9));
        timeGrainList.add(new TimeGrain(10));
        timeGrainList.add(new TimeGrain(11));
        timeGrainList.add(new TimeGrain(12));
        timeGrainList.add(new TimeGrain(13));
        timeGrainList.add(new TimeGrain(14));
        timeGrainList.add(new TimeGrain(15));
        timeGrainList.add(new TimeGrain(16));
        timeGrainList.add(new TimeGrain(17));
        timeGrainList.add(new TimeGrain(18));
        timeGrainList.add(new TimeGrain(19));
        timeGrainList.add(new TimeGrain(20));
        timeGrainList.add(new TimeGrain(21));
        timeGrainList.add(new TimeGrain(22));
        timeGrainList.add(new TimeGrain(23));
        timeGrainList.add(new TimeGrain(24));
        if (demoData == DemoData.LARGE) {
            timeGrainList.add(new TimeGrain(25));
            timeGrainList.add(new TimeGrain(26));
            timeGrainList.add(new TimeGrain(27));
            timeGrainList.add(new TimeGrain(28));
            timeGrainList.add(new TimeGrain(29));
            timeGrainList.add(new TimeGrain(30));
            timeGrainList.add(new TimeGrain(31));
            timeGrainList.add(new TimeGrain(32));
            timeGrainList.add(new TimeGrain(33));
            timeGrainList.add(new TimeGrain(34));
            timeGrainList.add(new TimeGrain(35));
            timeGrainList.add(new TimeGrain(36));
            timeGrainList.add(new TimeGrain(37));
            timeGrainList.add(new TimeGrain(38));
            timeGrainList.add(new TimeGrain(39));
            timeGrainList.add(new TimeGrain(40));
            timeGrainList.add(new TimeGrain(41));
            timeGrainList.add(new TimeGrain(42));
            timeGrainList.add(new TimeGrain(43));
            timeGrainList.add(new TimeGrain(44));
            timeGrainList.add(new TimeGrain(45));
            timeGrainList.add(new TimeGrain(46));
            timeGrainList.add(new TimeGrain(47));
            timeGrainList.add(new TimeGrain(48));
        }
        TimeGrain.persist(timeGrainList);

        List<MaintenanceJob> maintenanceJobList = new ArrayList<>();
        maintenanceJobList.add(new MaintenanceJob("Bolt tightening 1", maintainableUnitList.get(0), 0, 24, 1, true));
        maintenanceJobList.add(new MaintenanceJob("Bolt tightening 2", maintainableUnitList.get(1), 0, 24, 1, true));
        maintenanceJobList.add(new MaintenanceJob("Bolt tightening 3", maintainableUnitList.get(2), 0, 24, 1, true));
        maintenanceJobList.add(new MaintenanceJob("Track cleaning 1", maintainableUnitList.get(0), 8, 24, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Track cleaning 2", maintainableUnitList.get(1), 8, 24, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Track cleaning 3", maintainableUnitList.get(2), 8, 24, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Train inspection 1", maintainableUnitList.get(3), 0, 24, 4, true));
        maintenanceJobList.add(new MaintenanceJob("Train inspection 2", maintainableUnitList.get(4), 0, 24, 4, true));
        maintenanceJobList.add(new MaintenanceJob("Train inspection 3", maintainableUnitList.get(5), 0, 24, 4, true));
        maintenanceJobList.add(new MaintenanceJob("Track replacement 1", maintainableUnitList.get(0), 0, 24, 8, true));
        if (demoData == DemoData.LARGE) {
            maintenanceJobList.add(new MaintenanceJob("Bolt tightening 4", maintainableUnitList.get(6), 24, 48, 1, true));
            maintenanceJobList.add(new MaintenanceJob("Bolt tightening 5", maintainableUnitList.get(7), 24, 48, 1, true));
            maintenanceJobList.add(new MaintenanceJob("Bolt tightening 6", maintainableUnitList.get(8), 24, 48, 1, true));
            maintenanceJobList.add(new MaintenanceJob("Track cleaning 4", maintainableUnitList.get(6), 32, 48, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Track cleaning 5", maintainableUnitList.get(7), 32, 48, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Track cleaning 6", maintainableUnitList.get(8), 32, 48, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Train inspection 4", maintainableUnitList.get(9), 24, 48, 4, true));
            maintenanceJobList.add(new MaintenanceJob("Train inspection 5", maintainableUnitList.get(10), 24, 48, 4, true));
            maintenanceJobList.add(new MaintenanceJob("Train inspection 6", maintainableUnitList.get(11), 24, 48, 4, true));
            maintenanceJobList.add(new MaintenanceJob("Track replacement 2", maintainableUnitList.get(6), 24, 48, 8, true));

            maintenanceJobList.add(new MaintenanceJob("Bolt tightening 7", maintainableUnitList.get(0), 0, 48, 1, false));
            maintenanceJobList.add(new MaintenanceJob("Bolt tightening 8", maintainableUnitList.get(1), 0, 48, 1, false));
            maintenanceJobList.add(new MaintenanceJob("Bolt tightening 9", maintainableUnitList.get(2), 0, 48, 1, false));
            maintenanceJobList.add(new MaintenanceJob("Track cleaning 7", maintainableUnitList.get(0), 8, 48, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Track cleaning 8", maintainableUnitList.get(1), 8, 48, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Track cleaning 9", maintainableUnitList.get(2), 8, 48, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Train inspection 7", maintainableUnitList.get(3), 0, 48, 4, false));
            maintenanceJobList.add(new MaintenanceJob("Train inspection 8", maintainableUnitList.get(4), 0, 48, 4, false));
            maintenanceJobList.add(new MaintenanceJob("Train inspection 9", maintainableUnitList.get(5), 0, 48, 4, false));
            maintenanceJobList.add(new MaintenanceJob("Track replacement 3", maintainableUnitList.get(0), 0, 48, 8, false));
        }
        MaintenanceJob.persist(maintenanceJobList);

        List<MutuallyExclusiveJobs> mutuallyExclusiveJobsList = new ArrayList<>();
        mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(
                Arrays.asList(maintenanceJobList.get(0), maintenanceJobList.get(1))));
        mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(Arrays.asList(maintenanceJobList.get(3),
                maintenanceJobList.get(4), maintenanceJobList.get(5))));
        mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(
                Arrays.asList(maintenanceJobList.get(6), maintenanceJobList.get(7), maintenanceJobList.get(8))));
        if (demoData == DemoData.LARGE) {
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(
                    Arrays.asList(maintenanceJobList.get(10), maintenanceJobList.get(11))));
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(Arrays.asList(maintenanceJobList.get(13),
                    maintenanceJobList.get(14), maintenanceJobList.get(15))));
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(
                    Arrays.asList(maintenanceJobList.get(16), maintenanceJobList.get(17), maintenanceJobList.get(18))));
        }
        MutuallyExclusiveJobs.persist(mutuallyExclusiveJobsList);
    }
}
