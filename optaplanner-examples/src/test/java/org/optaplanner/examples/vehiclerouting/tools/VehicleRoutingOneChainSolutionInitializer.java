/*
 * Copyright 2013 JBoss by Red Hat.
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
package org.optaplanner.examples.vehiclerouting.tools;

import java.util.List;
import org.optaplanner.core.impl.phase.custom.CustomSolverPhaseCommand;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.vehiclerouting.domain.VrpAppearance;
import org.optaplanner.examples.vehiclerouting.domain.VrpCustomer;
import org.optaplanner.examples.vehiclerouting.domain.VrpSchedule;
import org.optaplanner.examples.vehiclerouting.domain.VrpVehicle;

/*
 * Initializes the VRP example => creates only ONE chain in the original order. 
 */
public class VehicleRoutingOneChainSolutionInitializer implements CustomSolverPhaseCommand {

    @Override
    public void changeWorkingSolution(ScoreDirector scoreDirector) {
        VrpSchedule solution = (VrpSchedule) scoreDirector.getWorkingSolution();

        List<VrpCustomer> customers = solution.getCustomerList();
        List<VrpVehicle> vehicles = solution.getVehicleList();

        for (int i = 0; i < customers.size(); i++) {
            VrpCustomer customer = customers.get(i);
            VrpAppearance tmpAppearance = (i == 0) ? vehicles.get(0) : customers.get(i - 1);

            scoreDirector.beforeVariableChanged(customer, "previousAppearance");
            customer.setPreviousAppearance(tmpAppearance);
            scoreDirector.afterVariableChanged(customer, "previousAppearance");
        }

        scoreDirector.calculateScore();

        if (!scoreDirector.isWorkingSolutionInitialized()) {
            throw new IllegalStateException("The solution [VrpSchedule] was not fully initialized by CustomSolverPhase: "
                    + this.getClass().getCanonicalName());
        }
    }
}
