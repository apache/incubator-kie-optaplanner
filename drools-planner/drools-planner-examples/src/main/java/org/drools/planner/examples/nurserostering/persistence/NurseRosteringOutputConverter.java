package org.drools.planner.examples.nurserostering.persistence;

import java.io.IOException;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.AbstractTxtOutputConverter;
import org.drools.planner.examples.common.persistence.AbstractXmlOutputConverter;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Shift;
import org.jdom.Element;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringOutputConverter extends AbstractXmlOutputConverter {

    public static void main(String[] args) {
        new NurseRosteringOutputConverter().convertAll();
    }

    public NurseRosteringOutputConverter() {
        super(new NurseRosteringDaoImpl());
    }

    public XmlOutputBuilder createXmlOutputBuilder() {
        return new NurseRosteringOutputBuilder();
    }

    public class NurseRosteringOutputBuilder extends XmlOutputBuilder {

        private NurseRoster nurseRoster;

        public void setSolution(Solution solution) {
            nurseRoster = (NurseRoster) solution;
        }

        public void writeSolution() throws IOException {
            Element solutionElement = new Element("Solution");
            document.setRootElement(solutionElement);

            Element schedulingPeriodIDElement = new Element("SchedulingPeriodID");
            schedulingPeriodIDElement.setText(nurseRoster.getCode());
            solutionElement.addContent(schedulingPeriodIDElement);

            Element competitorElement = new Element("Competitor");
            competitorElement.setText("Geoffrey De Smet with Drools Planner");
            solutionElement.addContent(competitorElement);

            Element softConstraintsPenaltyElement = new Element("SoftConstraintsPenalty");
            softConstraintsPenaltyElement.setText(Integer.toString(nurseRoster.getScore().getSoftScore()));
            solutionElement.addContent(softConstraintsPenaltyElement);

            for (EmployeeAssignment employeeAssignment : nurseRoster.getEmployeeAssignmentList()) {
                Shift shift = employeeAssignment.getShift();
                if (shift != null) {
                    Element assignmentElement = new Element("Assignment");
                    solutionElement.addContent(assignmentElement);

                    Element dateElement = new Element("Date");
                    dateElement.setText(shift.getShiftDate().getDateString());
                    assignmentElement.addContent(dateElement);

                    Element employeeElement = new Element("Employee");
                    employeeElement.setText(employeeAssignment.getEmployee().getCode());
                    assignmentElement.addContent(employeeElement);

                    Element shiftTypeElement = new Element("ShiftType");
                    shiftTypeElement.setText(shift.getShiftType().getCode());
                    assignmentElement.addContent(shiftTypeElement);
                }
            }
        }
    }

}
