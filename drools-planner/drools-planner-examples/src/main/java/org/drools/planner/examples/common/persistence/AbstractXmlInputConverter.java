package org.drools.planner.examples.common.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.drools.planner.core.solution.Solution;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractXmlInputConverter extends AbstractInputConverter {

    private static final String DEFAULT_INPUT_FILE_SUFFIX = ".xml";

    protected AbstractXmlInputConverter(SolutionDao solutionDao) {
        super(solutionDao);
    }

    protected String getInputFileSuffix() {
        return DEFAULT_INPUT_FILE_SUFFIX;
    }

    public abstract XmlInputBuilder createXmlInputBuilder();

    public Solution readSolution(File inputFile) {
        InputStream in = null;
        try {
            in = new FileInputStream(inputFile);
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(in);
            XmlInputBuilder txtInputBuilder = createXmlInputBuilder();
            txtInputBuilder.setDocument(document);
            return txtInputBuilder.readSolution();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException(e);
        } catch (SAXException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public abstract class XmlInputBuilder {

        protected Document document;

        public void setDocument(Document document) {
            this.document = document;
        }

        public abstract Solution readSolution() throws IOException;

        // ************************************************************************
        // Helper methods
        // ************************************************************************

        protected void assertNodeName(Node node, String nodeName) {
            if (!node.getNodeName().equals(nodeName)) {
                throw new IllegalArgumentException("The node name (" + node.getNodeName()
                        + ") is expected to be " + nodeName + ".");
            }
        }

    }

}
