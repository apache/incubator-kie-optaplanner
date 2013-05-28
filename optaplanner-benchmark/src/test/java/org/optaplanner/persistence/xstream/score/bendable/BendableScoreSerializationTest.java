/*
 * Copyright 2013 JBoss by Red Hat.
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
package org.optaplanner.persistence.xstream.score.bendable;

import java.io.File;
import java.net.URISyntaxException;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.optaplanner.persistence.xstream.XStreamProblemIO;
import org.optaplanner.persistence.xstream.score.bendable.domain.CloudBalance;

/**
 * In case solution class contains bendable score and org.optaplanner.persistence.xstream.XStreamScoreConverter is used 
 * for its (de)serialization, exception is thrown because of missing public no-arg constructor in BendableScoreDefinition class.
 * 
 * @author rsynek
 */
public class BendableScoreSerializationTest {
    
    private XStreamProblemIO xStreamProblemIO;
    
    @Test
    public void testBendableScore() {
        xStreamProblemIO = new XStreamProblemIO(CloudBalance.class);
        
        File dataFile;
        try {
            dataFile = new File(getClass().getResource("cb-0002comp-0006proc.xml").toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        
        CloudBalance cb = (CloudBalance) xStreamProblemIO.read(dataFile);
        assertNotNull(cb);
    }
}
