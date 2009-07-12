package org.drools.solver.examples.pas.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.score.DefaultHardAndSoftScore;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.pas.domain.AdmissionPart;
import org.drools.solver.examples.pas.domain.Bed;
import org.drools.solver.examples.pas.domain.BedDesignation;
import org.drools.solver.examples.pas.domain.PatientAdmissionSchedule;
import org.drools.solver.examples.pas.domain.Room;
import org.drools.solver.examples.pas.domain.Night;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleStartingSolutionInitializer extends AbstractStartingSolutionInitializer {

    private boolean checkSameBedInSameNight = true;

    @Override
    public boolean isSolutionInitialized(LocalSearchSolverScope localSearchSolverScope) {
        PatientAdmissionSchedule patientAdmissionSchedule = (PatientAdmissionSchedule) localSearchSolverScope.getWorkingSolution();
        return patientAdmissionSchedule.isInitialized();
    }

    public void initializeSolution(LocalSearchSolverScope localSearchSolverScope) {
        PatientAdmissionSchedule patientAdmissionSchedule = (PatientAdmissionSchedule)
                localSearchSolverScope.getWorkingSolution();
        initializeBedDesignationList(localSearchSolverScope, patientAdmissionSchedule);
    }

    private void initializeBedDesignationList(LocalSearchSolverScope localSearchSolverScope,
            PatientAdmissionSchedule patientAdmissionSchedule) {
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();
        List<BedDesignation> bedDesignationList = createBedDesignationList(patientAdmissionSchedule);
        Map<Bed, Set<Integer>> bedToTakenNightIndexSetMap = null;
        if (checkSameBedInSameNight) {
            bedToTakenNightIndexSetMap = new HashMap<Bed, Set<Integer>>(
                    patientAdmissionSchedule.getBedList().size());
        }
        // Assign one admissionPart at a time
        List<Bed> bedListInPriority = new ArrayList(patientAdmissionSchedule.getBedList());
int stillRunningCounter = 0; // TODO https://jira.jboss.org/jira/browse/JBRULES-2145
        for (BedDesignation bedDesignation : bedDesignationList) {
System.out.println("Trunk is bugged " + ++stillRunningCounter +"/" + bedDesignationList.size() + " but we do not use trunk. See JBRULES-2145.");
            Score unscheduledScore = localSearchSolverScope.calculateScoreFromWorkingMemory();
            int firstNightIndex = bedDesignation.getAdmissionPart().getFirstNight().getIndex();
            int lastNightIndex = bedDesignation.getAdmissionPart().getLastNight().getIndex();
            boolean perfectMatch = false;
            Score bestScore = DefaultHardAndSoftScore.valueOf(Integer.MIN_VALUE);
            Bed bestBed = null;

            FactHandle bedDesignationHandle = null;
            // Try every bed for that admissionPart
            // TODO by reordening the beds so index 0 has a different table then index 1 and so on,
            // this will probably be faster because perfectMatch will be true sooner
            for (Bed bed : bedListInPriority) {
                if (!bed.allowsAdmissionPart(bedDesignation.getAdmissionPart())) {
                    continue;
                }
                if (checkSameBedInSameNight) {
                    boolean taken = false;
                    Set<Integer> takenNightIndexSet = bedToTakenNightIndexSetMap.get(bed);
                    if (takenNightIndexSet != null) {
                        for (int i = firstNightIndex; i <= lastNightIndex; i++) {
                            if (takenNightIndexSet.contains(i)) {
                                taken = true;
                                break;
                            }
                        }
                    }
                    if (taken) {
                        continue;
                    }
                }
                if (bedDesignationHandle == null) {
                    bedDesignation.setBed(bed);
                    bedDesignationHandle = workingMemory.insert(bedDesignation);
                } else {
                    workingMemory.modifyRetract(bedDesignationHandle);
                    bedDesignation.setBed(bed);
                    workingMemory.modifyInsert(bedDesignationHandle, bedDesignation);
                }
                Score score = localSearchSolverScope.calculateScoreFromWorkingMemory();
                if (score.compareTo(unscheduledScore) < 0) {
                    if (score.compareTo(bestScore) > 0) {
                        bestScore = score;
                        bestBed = bed;
                    }
                } else if (score.equals(unscheduledScore)) {
                    perfectMatch = true;
                    bestScore = score;
                    bestBed = bed;
                    break;
                } else {
                    throw new IllegalStateException("The score (" + score
                            + ") cannot be higher than unscheduledScore (" + unscheduledScore + ").");
                }
                if (perfectMatch) {
                    break;
                }
            }
            if (bestBed == null) {
                if (checkSameBedInSameNight) {
                    throw new IllegalArgumentException(
                            "The initializer could not locate an allowed and empty bed for admissionPart ("
                                    + bedDesignation.getAdmissionPart() + ").");
                } else {
                    throw new IllegalArgumentException(
                            "The initializer could not locate an allowed bed for admissionPart ("
                                    + bedDesignation.getAdmissionPart() + ").");
                }
            }
            if (checkSameBedInSameNight) {
                Set<Integer> takenNightIndexSet = bedToTakenNightIndexSetMap.get(bestBed);
                if (takenNightIndexSet == null) {
                    takenNightIndexSet = new HashSet<Integer>(patientAdmissionSchedule.getNightList().size());
                    bedToTakenNightIndexSetMap.put(bestBed, takenNightIndexSet);
                }
                if (takenNightIndexSet != null) {
                    for (int i = firstNightIndex; i <= lastNightIndex; i++) {
                        boolean unique = takenNightIndexSet.add(i);
                        if (!unique) {
                            throw new IllegalStateException(
                                    "The takenNightIndexSet cannot possibly already have nightIndex (" + i + ").");
                        }
                    }
                }
            }
            if (!perfectMatch) {
                workingMemory.modifyRetract(bedDesignationHandle);
                bedDesignation.setBed(bestBed);
                workingMemory.modifyInsert(bedDesignationHandle, bedDesignation);
            }
            // put the occupied bed at the end of the list
            bedListInPriority.remove(bestBed);
            bedListInPriority.add(bestBed);
        }
        // For the GUI's combobox list mainly, not really needed
        Collections.sort(bedDesignationList, new PersistableIdComparator());
        patientAdmissionSchedule.setBedDesignationList(bedDesignationList);
    }

    private List<BedDesignation> createBedDesignationList(PatientAdmissionSchedule patientAdmissionSchedule) {
        List<BedDesignationInitializationWeight> initializationWeightList
                = new ArrayList<BedDesignationInitializationWeight>(
                        patientAdmissionSchedule.getAdmissionPartList().size());
        for (AdmissionPart admissionPart : patientAdmissionSchedule.getAdmissionPartList()) {
            BedDesignation bedDesignation = new BedDesignation();
            bedDesignation.setId(admissionPart.getId());
            bedDesignation.setAdmissionPart(admissionPart);
            int disallowedCount = 0;
            for (Room room : patientAdmissionSchedule.getRoomList()) {
                disallowedCount += (room.getCapacity() * room.countDisallowedAdmissionPart(admissionPart));
            }
            initializationWeightList.add(new BedDesignationInitializationWeight(bedDesignation,
                    disallowedCount, bedDesignation.getAdmissionPart().getNightCount()));
        }
        Collections.sort(initializationWeightList);
        List<BedDesignation> bedDesignationList = new ArrayList<BedDesignation>(
                patientAdmissionSchedule.getAdmissionPartList().size());
        for (BedDesignationInitializationWeight bedDesignationInitializationWeight : initializationWeightList) {
            bedDesignationList.add(bedDesignationInitializationWeight.getBedDesignation());
        }
        return bedDesignationList;
    }

    private class BedDesignationInitializationWeight implements Comparable<BedDesignationInitializationWeight> {

        private BedDesignation bedDesignation;
        private int disallowedCount;
        private int nightCount;

        private BedDesignationInitializationWeight(BedDesignation bedDesignation, int disallowedCount, int nightCount) {
            this.bedDesignation = bedDesignation;
            this.disallowedCount = disallowedCount;
            this.nightCount = nightCount;
        }

        public BedDesignation getBedDesignation() {
            return bedDesignation;
        }

        public int compareTo(BedDesignationInitializationWeight other) {
            if (disallowedCount < other.disallowedCount) {
                return 1;
            } else if (disallowedCount > other.disallowedCount) {
                return -1;
            } else if (nightCount < other.nightCount) {
                return 1;
            } else if (nightCount > other.nightCount) {
                return -1;
            } else {
                return 0;
            }
        }

    }

}