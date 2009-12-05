package org.drools.solver.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.NativeFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.apache.commons.io.IOUtils;
import org.drools.solver.config.localsearch.LocalSearchSolverConfig;
import org.drools.solver.core.localsearch.LocalSearchSolver;

/**
 * XML based configuration that builds a Solver.
 *
 * @author Geoffrey De Smet
 */
public class XmlSolverConfigurer {

    private XStream xStream;
    private LocalSearchSolverConfig config = null;

    public XmlSolverConfigurer() {
        // TODO From Xstream 1.3.3 that KeySorter will be the default. See http://jira.codehaus.org/browse/XSTR-363
        xStream = new XStream(new PureJavaReflectionProvider(new FieldDictionary(new NativeFieldKeySorter())));
        xStream.setMode(XStream.ID_REFERENCES);
        Annotations.configureAliases(xStream, LocalSearchSolverConfig.class);
    }

    public XmlSolverConfigurer(String resource) {
        this();
        configure(resource);
    }

    public void addXstreamAlias(Class aliasClass) {
		Annotations.configureAliases(xStream, aliasClass);
    }

    public LocalSearchSolverConfig getConfig() {
        return config;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public XmlSolverConfigurer configure(String resource) {
        InputStream in = getClass().getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalArgumentException("The solver configuration (" + resource + ") does not exist.");
        }
        return configure(in);
    }

    public XmlSolverConfigurer configure(InputStream in) {
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

    public XmlSolverConfigurer configure(Reader reader) {
        config = (LocalSearchSolverConfig) xStream.fromXML(reader);
        return this;
    }

    public LocalSearchSolver buildSolver() {
        return config.buildSolver();
    }

}
