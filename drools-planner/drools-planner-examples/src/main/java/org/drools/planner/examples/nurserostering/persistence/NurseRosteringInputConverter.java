package org.drools.planner.examples.nurserostering.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.AbstractInputConverter;
import org.drools.planner.examples.common.persistence.AbstractTxtInputConverter;
import org.drools.planner.examples.common.persistence.AbstractXmlInputConverter;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.ShiftType;
import org.drools.planner.examples.nurserostering.domain.ShiftTypeSkillRequirement;
import org.drools.planner.examples.nurserostering.domain.Skill;
import org.jdom.Element;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringInputConverter extends AbstractXmlInputConverter {

    public static void main(String[] args) {
        new NurseRosteringInputConverter().convertAll();
    }

    public NurseRosteringInputConverter() {
        super(new NurseRosteringDaoImpl());
    }

    public XmlInputBuilder createXmlInputBuilder() {
        return new NurseRosteringInputBuilder();
    }

    public class NurseRosteringInputBuilder extends XmlInputBuilder {

        public Solution readSolution() throws IOException {
            // Note: javax.xml is terrible. JDom is much much easier.

            Element schedulingPeriodElement = document.getRootElement();
            // TODO verify root node name
            NurseRoster nurseRoster = new NurseRoster();
            nurseRoster.setId(0L);
            nurseRoster.setCode(schedulingPeriodElement.getAttribute("ID").getValue());

            // TODO StartDate EndDate

            Map<String, Skill> skillMap = readSkillList(nurseRoster, schedulingPeriodElement.getChild("Skills"));
            readShiftTypeList(nurseRoster, skillMap, schedulingPeriodElement.getChild("ShiftTypes"));

            return nurseRoster;
        }

        private Map<String, Skill> readSkillList(NurseRoster nurseRoster, Element skillsElement) {
            List<Element> skillElementList = (List<Element>) skillsElement.getChildren();
            List<Skill> skillList = new ArrayList<Skill>(skillElementList.size());
            Map<String, Skill> skillMap = new HashMap<String, Skill>(skillElementList.size());
            long id = 0L;
            for (Element skillElement : skillElementList) {
                assertElementName(skillElement, "Skill");
                Skill skill = new Skill();
                skill.setId(id);
                skill.setCode(skillElement.getText());
                skillList.add(skill);
                skillMap.put(skill.getCode(), skill);
                id++;
            }
            nurseRoster.setSkillList(skillList);
            return skillMap;
        }

        private void readShiftTypeList(NurseRoster nurseRoster, Map<String, Skill> skillMap,
                Element shiftTypesElement) {
            List<Element> shiftElementList = (List<Element>) shiftTypesElement.getChildren();
            List<ShiftType> shiftTypeList = new ArrayList<ShiftType>(shiftElementList.size());
            long id = 0L;
            List<ShiftTypeSkillRequirement> shiftTypeSkillRequirementList
                    = new ArrayList<ShiftTypeSkillRequirement>(shiftElementList.size() * 2);
            long shiftTypeSkillRequirementId = 0L;
            for (Element shiftElement : shiftElementList) {
                assertElementName(shiftElement, "Shift");
                ShiftType shiftType = new ShiftType();
                shiftType.setId(id);
                shiftType.setCode(shiftElement.getAttribute("ID").getValue());
                shiftType.setStartTimeString(shiftElement.getChild("StartTime").getText());
                shiftType.setEndTimeString(shiftElement.getChild("EndTime").getText());
                shiftType.setDescription(shiftElement.getChild("Description").getText());

                List<Element> skillElementList = (List<Element>) shiftElement.getChild("Skills").getChildren();
                for (Element skillElement : skillElementList) {
                    assertElementName(skillElement, "Skill");
                    ShiftTypeSkillRequirement shiftTypeSkillRequirement = new ShiftTypeSkillRequirement();
                    shiftTypeSkillRequirement.setId(shiftTypeSkillRequirementId);
                    shiftTypeSkillRequirement.setShiftType(shiftType);
                    Skill skill = skillMap.get(skillElement.getText());
                    if (skill == null) {
                        throw new IllegalArgumentException("The skill (" + skillElement.getText()
                                + ") of shiftType (" + shiftType.getCode() + ") does not exist.");
                    }
                    shiftTypeSkillRequirement.setSkill(skill);
                    shiftTypeSkillRequirementList.add(shiftTypeSkillRequirement);
                    shiftTypeSkillRequirementId++;
                }
                shiftTypeList.add(shiftType);
                id++;
            }
            nurseRoster.setShiftTypeList(shiftTypeList);
            nurseRoster.setShiftTypeSkillRequirementList(shiftTypeSkillRequirementList);
        }

    }

}
