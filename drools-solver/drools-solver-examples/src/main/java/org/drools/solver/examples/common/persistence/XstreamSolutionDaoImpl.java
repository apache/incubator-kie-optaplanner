package org.drools.solver.examples.common.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.NativeFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class XstreamSolutionDaoImpl implements SolutionDao {

    protected final transient Log log = LogFactory.getLog(getClass());

    private XStream xStream;

    public XstreamSolutionDaoImpl() {
        // TODO From Xstream 1.3.3 that KeySorter will be the default. See http://jira.codehaus.org/browse/XSTR-363
        xStream = new XStream(new PureJavaReflectionProvider(new FieldDictionary(new NativeFieldKeySorter())));
        xStream.setMode(XStream.ID_REFERENCES);
    }

    public Solution readSolution(File file) {
        Reader reader = null;
        try {
            // xStream.fromXml(InputStream) does not use UTF-8
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            Solution solution = (Solution) xStream.fromXML(reader);
            log.info("Loaded: " + file);
            return solution;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read: " + file, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Problem closing file (" + file + ")", e);
                }
            }
        }
    }

    public void writeSolution(Solution solution, File file) {
        Writer writer = null;
        try {
            // xStream.toXml(OutputStream) does not use UTF-8
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            xStream.toXML(solution, writer);
            log.info("Saved: " + file);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write file (" + file + ")", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.warn("Problem closing file (" + file + ")", e);
                }
            }
        }
    }

}
