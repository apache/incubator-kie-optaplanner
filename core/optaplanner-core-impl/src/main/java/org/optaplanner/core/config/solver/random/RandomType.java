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

package org.optaplanner.core.config.solver.random;

import javax.xml.bind.annotation.XmlEnum;

/**
 * Defines the pseudo random number generator.
 * See the <a href="http://commons.apache.org/proper/commons-math/userguide/random.html#a2.7_PRNG_Pluggability">PRNG</a>
 * documentation in commons-math.
 */
@XmlEnum
public enum RandomType {
    /**
     * This is the default.
     */
    JDK,
    MERSENNE_TWISTER,
    WELL512A,
    WELL1024A,
    WELL19937A,
    WELL19937C,
    WELL44497A,
    WELL44497B;
}
