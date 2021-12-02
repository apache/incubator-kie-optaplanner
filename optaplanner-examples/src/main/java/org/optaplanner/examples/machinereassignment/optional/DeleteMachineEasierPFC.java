package org.optaplanner.examples.machinereassignment.optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.optaplanner.core.api.solver.change.ProblemChange;
import org.optaplanner.core.api.solver.change.ProblemChangeDirector;
import org.optaplanner.examples.machinereassignment.domain.MachineReassignment;
import org.optaplanner.examples.machinereassignment.domain.MrMachine;
import org.optaplanner.examples.machinereassignment.domain.MrMachineCapacity;
import org.optaplanner.examples.machinereassignment.domain.MrProcessAssignment;

// TODO: Delete. This class is just for illustrating the PFC impl.
public class DeleteMachineEasierPFC implements ProblemChange<MachineReassignment> {

    private final MrMachine machine;

    public DeleteMachineEasierPFC(MrMachine machine) {
        this.machine = machine;
    }

    @Override
    public void doChange(MachineReassignment workingSolution, ProblemChangeDirector problemChangeDirector) {
        MrMachine workingMachine = problemChangeDirector.lookUpWorkingObject(Objects.requireNonNull(machine));

        List<MrMachineCapacity> machineCapacityList = new ArrayList<>(workingSolution.getMachineCapacityList());
        workingSolution.setMachineCapacityList(machineCapacityList);
        for (MrMachineCapacity machineCapacity : machineCapacityList) {
            if (machineCapacity.getMachine() == workingMachine) {
                problemChangeDirector.removeProblemFact(machineCapacity, machineCapacityList::remove);
            }
        }

        // First remove the problem fact from all planning entities that use it
        for (MrProcessAssignment processAssignment : workingSolution.getProcessAssignmentList()) {
            if (processAssignment.getOriginalMachine() == workingMachine) {
                // new style
                problemChangeDirector.changeProblemProperty(processAssignment,
                        lookedUpProcessAssignment -> lookedUpProcessAssignment.setOriginalMachine(null));
            }
            if (processAssignment.getMachine() == workingMachine) {
                problemChangeDirector.changeVariable(processAssignment,
                        lookedUpProcessAssignment -> lookedUpProcessAssignment.setMachine(null), "machine");
            }
        }
        // A SolutionCloner does not clone problem fact lists (such as machineList)
        // Shallow clone the machineList so only workingSolution is affected, not bestSolution or guiSolution
        ArrayList<MrMachine> machineList = new ArrayList<>(workingSolution.getMachineList());
        workingSolution.setMachineList(machineList);
        // Remove it the problem fact itself
        problemChangeDirector.removeProblemFact(workingMachine, machine -> machineList.remove(machine)); // Hides the field.
    }
}
