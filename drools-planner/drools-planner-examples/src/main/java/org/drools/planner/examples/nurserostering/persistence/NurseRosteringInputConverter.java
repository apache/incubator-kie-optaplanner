package org.drools.planner.examples.nurserostering.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.AbstractInputConverter;
import org.drools.planner.examples.common.persistence.AbstractTxtInputConverter;
import org.drools.planner.examples.common.persistence.AbstractXmlInputConverter;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Skill;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
            NurseRoster nurseRoster = new NurseRoster();
            nurseRoster.setId(0L);

            Node schedulingPeriodNode = document.getChildNodes().item(0);
            nurseRoster.setCode(schedulingPeriodNode.getAttributes().getNamedItem("ID").getNodeValue());
            Node skillsNode = schedulingPeriodNode.getChildNodes().item(5);
            assertNodeName(skillsNode, "Skills");
            NodeList skillNodeList = skillsNode.getChildNodes();
            List<Skill> skillList = new ArrayList<Skill>(skillNodeList.getLength());
            for (int i = 1; i < skillNodeList.getLength(); i += 2) {
                Node skillNode = skillNodeList.item(i);
                assertNodeName(skillNode, "Skill");
                Skill skill = new Skill();
                skill.setId((long) i);
                skill.setCode(skillNode.getTextContent());
                skillList.add(skill);
            }
            nurseRoster.setSkillList(skillList);

            return nurseRoster;
        }

    }

}
