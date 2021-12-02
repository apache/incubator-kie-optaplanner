package org.optaplanner.examples.machinereassignment.optional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.ProblemFactChange;
import org.optaplanner.examples.machinereassignment.domain.MachineReassignment;
import org.optaplanner.examples.machinereassignment.domain.MrMachine;
import org.optaplanner.examples.machinereassignment.domain.MrMachineCapacity;
import org.optaplanner.examples.machinereassignment.domain.MrProcessAssignment;

// TODO: Delete. This class is just for illustrating the PFC impl.
public class DeleteMachinePFC implements ProblemFactChange<MachineReassignment> {

    private final MrMachine machine;

    public DeleteMachinePFC(MrMachine machine) {
        this.machine = machine;
    }

    @Override
    public void doChange(ScoreDirector<MachineReassignment> scoreDirector) {
        MachineReassignment machineReassignment = scoreDirector.getWorkingSolution();
        MrMachine workingMachine = scoreDirector.lookUpWorkingObject(machine);
        if (workingMachine == null) {
            // The machine has already been deleted.
            return;
        }

        List<MrMachineCapacity> machineCapacityList = new ArrayList<>(machineReassignment.getMachineCapacityList());
        machineReassignment.setMachineCapacityList(machineCapacityList);
        List<MrMachineCapacity> capacitiesToRemove = machineCapacityList.stream()
                .filter(machineCapacity -> machineCapacity.getMachine() == workingMachine)
                .collect(Collectors.toList());

        capacitiesToRemove.forEach(machineCapacity -> {
            scoreDirector.beforeProblemFactRemoved(machineCapacity);
            machineCapacityList.remove(machineCapacity);
            scoreDirector.afterProblemFactRemoved(machineCapacity);
        });

        // First remove the problem fact from all planning entities that use it
        for (MrProcessAssignment processAssignment : machineReassignment.getProcessAssignmentList()) {
            if (processAssignment.getOriginalMachine() == workingMachine) {
                scoreDirector.beforeProblemPropertyChanged(processAssignment);
                processAssignment.setOriginalMachine(null);
                scoreDirector.afterProblemPropertyChanged(processAssignment);
            }
            if (processAssignment.getMachine() == workingMachine) {
                scoreDirector.beforeVariableChanged(processAssignment, "machine");
                processAssignment.setMachine(null);
                scoreDirector.afterVariableChanged(processAssignment, "machine");
            }
        }
        // A SolutionCloner does not clone problem fact lists (such as machineList)
        // Shallow clone the machineList so only workingSolution is affected, not bestSolution or guiSolution
        ArrayList<MrMachine> machineList = new ArrayList<>(machineReassignment.getMachineList());
        machineReassignment.setMachineList(machineList);
        // Remove it the problem fact itself
        scoreDirector.beforeProblemFactRemoved(workingMachine);
        machineList.remove(workingMachine);
        scoreDirector.afterProblemFactRemoved(workingMachine);
        scoreDirector.triggerVariableListeners();
    }
}
