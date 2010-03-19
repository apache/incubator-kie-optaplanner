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
import org.drools.planner.examples.nurserostering.domain.ShiftPattern;
import org.drools.planner.examples.nurserostering.domain.ShiftType;
import org.drools.planner.examples.nurserostering.domain.ShiftTypeSkillRequirement;
import org.drools.planner.examples.nurserostering.domain.Skill;
import org.jdom.Element;
import org.jdom.JDOMException;

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

        public Solution readSolution() throws IOException, JDOMException {
            // Note: javax.xml is terrible. JDom is much much easier.

            Element schedulingPeriodElement = document.getRootElement();
            assertElementName(schedulingPeriodElement, "SchedulingPeriod");
            NurseRoster nurseRoster = new NurseRoster();
            nurseRoster.setId(0L);
            nurseRoster.setCode(schedulingPeriodElement.getAttribute("ID").getValue());

            // TODO StartDate EndDate

            Map<String, Skill> skillMap = readSkillList(nurseRoster,
                    schedulingPeriodElement.getChild("Skills"));
            Map<String, ShiftType> shiftTypeMap = readShiftTypeList(nurseRoster, skillMap,
                    schedulingPeriodElement.getChild("ShiftTypes"));
            Map<String, ShiftPattern> shiftPatternMap = readShiftPatternList(nurseRoster, shiftTypeMap,
                    schedulingPeriodElement.getChild("Patterns"));

            logger.info("NurseRoster {} with TODO.",
                    new Object[]{nurseRoster.getCode()});
            // TODO log other stats

            return nurseRoster;
        }

        private Map<String, Skill> readSkillList(NurseRoster nurseRoster, Element skillsElement) throws JDOMException {
            List<Element> skillElementList = (List<Element>) skillsElement.getChildren();
            List<Skill> skillList = new ArrayList<Skill>(skillElementList.size());
            Map<String, Skill> skillMap = new HashMap<String, Skill>(skillElementList.size());
            long id = 0L;
            for (Element element : skillElementList) {
                assertElementName(element, "Skill");
                Skill skill = new Skill();
                skill.setId(id);
                skill.setCode(element.getText());
                skillList.add(skill);
                skillMap.put(skill.getCode(), skill);
                id++;
            }
            nurseRoster.setSkillList(skillList);
            return skillMap;
        }

        private Map<String, ShiftType> readShiftTypeList(NurseRoster nurseRoster, Map<String, Skill> skillMap,
                Element shiftTypesElement) throws JDOMException {
            List<Element> shiftElementList = (List<Element>) shiftTypesElement.getChildren();
            List<ShiftType> shiftTypeList = new ArrayList<ShiftType>(shiftElementList.size());
            Map<String, ShiftType> shiftTypeMap = new HashMap<String, ShiftType>(shiftElementList.size());
            long id = 0L;
            List<ShiftTypeSkillRequirement> shiftTypeSkillRequirementList
                    = new ArrayList<ShiftTypeSkillRequirement>(shiftElementList.size() * 2);
            long shiftTypeSkillRequirementId = 0L;
            for (Element element : shiftElementList) {
                assertElementName(element, "Shift");
                ShiftType shiftType = new ShiftType();
                shiftType.setId(id);
                shiftType.setCode(element.getAttribute("ID").getValue());
                shiftType.setStartTimeString(element.getChild("StartTime").getText());
                shiftType.setEndTimeString(element.getChild("EndTime").getText());
                shiftType.setDescription(element.getChild("Description").getText());

                List<Element> skillElementList = (List<Element>) element.getChild("Skills").getChildren();
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
                shiftTypeMap.put(shiftType.getCode(), shiftType);
                id++;
            }
            nurseRoster.setShiftTypeList(shiftTypeList);
            nurseRoster.setShiftTypeSkillRequirementList(shiftTypeSkillRequirementList);
            return shiftTypeMap;
        }

        private Map<String, ShiftPattern> readShiftPatternList(NurseRoster nurseRoster,
                Map<String, ShiftType> shiftTypeMap, Element patternsElement) throws JDOMException {
            List<Element> patternElementList = (List<Element>) patternsElement.getChildren();
            List<ShiftPattern> shiftPatternList = new ArrayList<ShiftPattern>(patternElementList.size());
            Map<String, ShiftPattern> shiftPatternMap = new HashMap<String, ShiftPattern>(patternElementList.size());
            long id = 0L;
            for (Element element : patternElementList) {
                assertElementName(element, "Pattern");
                ShiftPattern shiftPattern = new ShiftPattern();
                shiftPattern.setId(id);
                shiftPattern.setCode(element.getAttribute("ID").getValue());
                shiftPattern.setWeight(element.getAttribute("weight").getIntValue());

                List<Element> patternEntryElementList = (List<Element>) element.getChild("PatternEntries")
                        .getChildren();
                for (Element patternEntryElement : patternEntryElementList) {
                    assertElementName(patternEntryElement, "PatternEntry");
                    Element shiftTypeElement = patternEntryElement.getChild("ShiftType");
                    ShiftType shiftType = shiftTypeMap.get(shiftTypeElement.getText());
                    if (shiftType == null) {
                        if (shiftTypeElement.getText().equals("Any")) {
                            // TODO


                        } else if (shiftTypeElement.getText().equals("None")) {
                            // TODO

                            
                        } else {
                            throw new IllegalArgumentException("The shiftType (" + shiftTypeElement.getText()
                                    + ") of shiftPattern (" + shiftPattern.getCode() + ") does not exist.");
                        }
                    }
                    // TODO shiftType & day etc


                }

                shiftPatternList.add(shiftPattern);
                shiftPatternMap.put(shiftPattern.getCode(), shiftPattern);
                id++;
            }
            nurseRoster.setShiftPatternList(shiftPatternList);
            return shiftPatternMap;
        }

    }

}
