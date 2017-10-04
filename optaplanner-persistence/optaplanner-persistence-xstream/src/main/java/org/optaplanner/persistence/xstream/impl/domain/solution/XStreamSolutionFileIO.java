/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.persistence.xstream.impl.domain.solution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

/**
 * Security warning: only use this class with XML files from a trusted source,
 * because {@link XStream} is configured to allow all permissions,
 * which can be exploited if the XML comes from an untrusted source.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class XStreamSolutionFileIO<Solution_> implements SolutionFileIO<Solution_> {

    protected XStream xStream;

    public XStreamSolutionFileIO(Class... xStreamAnnotatedClasses) {
        xStream = new XStream();
        String[] voidDeny = {"void.class", "Void.class"};
        xStream.denyTypes(voidDeny);
        xStream.setMode(XStream.ID_REFERENCES);
        xStream.processAnnotations(xStreamAnnotatedClasses);
        XStream.setupDefaultSecurity(xStream);
        // Presume the XML file comes from a trusted source so it works out of the box. See class javadoc.
        xStream.addPermission(new AnyTypePermission());
    }

    public XStream getXStream() {
        return xStream;
    }

    @Override
    public String getInputFileExtension() {
        return "xml";
    }

    @Override
    public Solution_ read(File inputSolutionFile) {
        // xStream.fromXml(InputStream) does not use UTF-8
        try (Reader reader = new InputStreamReader(new FileInputStream(inputSolutionFile), "UTF-8")) {
            return (Solution_) xStream.fromXML(reader);
        } catch (XStreamException | IOException e) {
            throw new IllegalArgumentException("Failed reading inputSolutionFile (" + inputSolutionFile + ").", e);
        }
    }

    @Override
    public void write(Solution_ solution, File outputSolutionFile) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputSolutionFile), "UTF-8")) {
            xStream.toXML(solution, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed writing outputSolutionFile (" + outputSolutionFile + ").", e);
        }
    }

}
