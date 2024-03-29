/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.persistence.jaxb.api.score;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.optaplanner.core.api.score.Score;

/**
 * JAXB binding support for a {@link Score} type.
 * <p>
 * For example: use {@code @XmlJavaTypeAdapter(HardSoftScoreJaxbAdapter.class)}
 * on a {@code HardSoftScore score} field and it will marshalled to XML as {@code <score>-999hard/-999soft</score>}.
 *
 * @see Score
 * @param <Score_> the actual score type
 */
public abstract class AbstractScoreJaxbAdapter<Score_ extends Score<Score_>> extends XmlAdapter<String, Score_> {

    @Override
    public String marshal(Score_ score) {
        return score.toString();
    }

}
