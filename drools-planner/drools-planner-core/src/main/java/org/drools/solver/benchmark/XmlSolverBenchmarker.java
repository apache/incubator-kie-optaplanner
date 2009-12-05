package org.drools.solver.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.NativeFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.apache.commons.io.IOUtils;

/**
 * @author Geoffrey De Smet
 */
public class XmlSolverBenchmarker {

    private XStream xStream;
    private SolverBenchmarkSuite suite = null;

    public XmlSolverBenchmarker() {
        // TODO From Xstream 1.3.3 that KeySorter will be the default. See http://jira.codehaus.org/browse/XSTR-363
        xStream = new XStream(new PureJavaReflectionProvider(new FieldDictionary(new NativeFieldKeySorter())));
        xStream.setMode(XStream.ID_REFERENCES);
        xStream.processAnnotations(SolverBenchmarkSuite.class);
        // It doesn't pick up the annotations of the @XStreamImplicit in xstream 1.2.2
        xStream.processAnnotations(SolverBenchmark.class);
    }

    public void addXstreamAnnotations(Class annotationsClass) {
        xStream.processAnnotations(annotationsClass);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public XmlSolverBenchmarker configure(String resource) {
        InputStream in = getClass().getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found: " + resource);
        }
        return configure(in);
    }

    public XmlSolverBenchmarker configure(InputStream in) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(in, "utf-8");
            return configure(reader);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("This vm does not support utf-8 encoding.", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public XmlSolverBenchmarker configure(Reader reader) {
        suite = (SolverBenchmarkSuite) xStream.fromXML(reader);
        return this;
    }

    public void benchmark() {
        if (suite == null) {
            throw new IllegalStateException("No configuration found, call SolverBenchmarker.configure(...) first.");
        }
        suite.benchmark(xStream);
    }

    public void writeResults(File resultFile) {
        resultFile.getParentFile().mkdirs();
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(resultFile), "utf-8");
            xStream.toXML(suite, writer);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("This vm does not support utf-8 encoding.", e);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not create file: " + resultFile, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

}
