/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.common;

interface ConstraintStreamHelper<Right, JoinedStream, Joiner, Predicate> {

    JoinedStream join(Class<Right> otherClass);

    JoinedStream join(Class<Right> otherClass, Joiner joiner);

    JoinedStream join(Class<Right> otherClass, Joiner... joiners);

    JoinedStream filter(JoinedStream stream, Predicate predicate);

    Joiner mergeJoiners(Joiner... joiners);

    boolean isFilteringJoiner(Joiner joiner);

    Predicate extractPredicate(Joiner joiner);

    Predicate mergePredicates(Predicate predicate1, Predicate predicate2);

}
