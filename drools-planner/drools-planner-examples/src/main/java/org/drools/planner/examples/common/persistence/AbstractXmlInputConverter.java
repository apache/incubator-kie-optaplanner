package org.drools.planner.examples.common.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.drools.planner.core.solution.Solution;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

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
            SAXBuilder builder = new SAXBuilder(false);
            Document document = builder.build(in);
            XmlInputBuilder txtInputBuilder = createXmlInputBuilder();
            txtInputBuilder.setDocument(document);
            return txtInputBuilder.readSolution();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the file (" + inputFile.getName() + ").", e);
        } catch (JDOMException e) {
            throw new IllegalArgumentException("Could not parse the XML file (" + inputFile.getName() + ").", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public abstract class XmlInputBuilder {

        protected Document document;

        public void setDocument(Document document) {
            this.document = document;
        }

        public abstract Solution readSolution() throws IOException, JDOMException;

        // ************************************************************************
        // Helper methods
        // ************************************************************************

        protected void assertElementName(Element element, String name) {
            if (!element.getName().equals(name)) {
                throw new IllegalStateException("Element name (" + element.getName()
                        + ") should be " + name +".");
            }
        }

    }

}
