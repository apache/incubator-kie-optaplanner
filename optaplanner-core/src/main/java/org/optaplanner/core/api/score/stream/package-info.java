/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

/**
 * The {@link org.optaplanner.core.api.score.stream.ConstraintStream} API:
 * a way to define constraints for {@link org.optaplanner.core.api.score.Score} calculation. Throughout this
 * documentation, we will be using the following terminology:
 *
 * <dl>
 *     <dt>Stream</dt>
 *          <dd>Short for "constraint stream". A chain of different operations, originated by
 *          {@link org.optaplanner.core.api.score.stream.ConstraintFactory#from(java.lang.Class)} (or similar methods)
 *          and terminated by a penalization or reward operation.</dd>
 *     <dt>Operation</dt>
 *          <dd>Operations (implementations of
 *          {@link org.optaplanner.core.api.score.stream.ConstraintStream}) are parts of Stream which mutate it. They
 *          may remove tuples from further evaluation, expand or contract streams. Every Stream has a terminal
 *          Operation, which is either a penalization or a reward.</dd>
 *     <dt>Fact</dt>
 *          <dd>Object instance entering the Stream.</dd>
 *     <dt>Genuine Fact</dt>
 *          <dd>Fact that enters the Stream either through a from(...) call or through a join(...) call. Genuine Facts
 *          are either planning entities (see {@link org.optaplanner.core.api.domain.entity.PlanningEntity}) or problem
 *          facts (see @{@link org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty} or
 *          {@link org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty}).</dd>
 *     <dt>Inferred Fact</dt>
 *          <dd>Fact that enters Stream through a computation. This would typically happen through an Operation such as
 *          groupBy(...).</dd>
 *     <dt>Tuple</dt>
 *          <dd>A collection of Facts that Stream operates on. For example,
 *          {@link org.optaplanner.core.api.score.stream.uni.UniConstraintStream} operates on single-Fact Tuples {A} and
 *          {@link org.optaplanner.core.api.score.stream.bi.BiConstraintStream} operates on two-Fact Tuples {A, B}.
 *          Putting Facts into a Tuple implies a relationship exists between these Facts.</dd>
 *     <dt>Match</dt>
 *          <dd>Match is a Tuple that reached the terminal Operation of a Stream and is therefore either penalized or
 *          rewarded.</dd>
 *     <dt>Cardinality</dt>
 *          <dd>The number of Facts in a Tuple. Uni Streams have a cardinality of 1, Bi Streams have a cardinality of 2,
 *          etc.</dd>
 *     <dt>Expansion</dt>
 *          <dd>An operation that increases the cardinality of Stream. This typically happens through join(...) or
 *          a groupBy(...) Operations. Uni Streams are expanded into Bi Streams, Bi Streams into Tri Streams etc.
 *          Expanding Stream infers relationships between Facts where none previously existed.</dd>
 *     <dt>Contraction</dt>
 *          <dd>An Operation that decreases the cardinality of Stream. This typically happens through a groupBy(...)
 *          Operation. Bi Streams are contracted into Uni Streams, Tri Streams into Bi or even Uni Streams. Contracting
 *          Stream breaks relationships between Facts, often discarding the facts themselves, but is capable of
 *          inferring additional Facts in the process.</dd>
 * </dl>
 *
 */
package org.optaplanner.core.api.score.stream;
