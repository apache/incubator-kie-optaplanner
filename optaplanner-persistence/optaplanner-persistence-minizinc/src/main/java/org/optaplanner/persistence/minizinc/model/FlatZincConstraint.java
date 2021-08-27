/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.persistence.minizinc.model;

import java.util.List;

public class FlatZincConstraint {
    private final String predicateName;
    private final List<FlatZincExpr> predicateArguments;
    private final List<FlatZincAnnotation> annotationList;

    public FlatZincConstraint(String predicateName, List<FlatZincExpr> predicateArguments,
            List<FlatZincAnnotation> annotationList) {
        this.predicateName = predicateName;
        this.predicateArguments = predicateArguments;
        this.annotationList = annotationList;
    }

    public String getPredicateName() {
        return predicateName;
    }

    public List<FlatZincExpr> getPredicateArguments() {
        return predicateArguments;
    }

    public List<FlatZincAnnotation> getAnnotationList() {
        return annotationList;
    }

    @Override
    public String toString() {
        return "FlatZincConstraint{" +
                "predicateName='" + predicateName + '\'' +
                ", predicateArguments=" + predicateArguments +
                ", annotationList=" + annotationList +
                '}';
    }
}
