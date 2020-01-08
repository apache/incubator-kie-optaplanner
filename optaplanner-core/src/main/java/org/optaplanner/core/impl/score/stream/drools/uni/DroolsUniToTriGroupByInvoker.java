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

package org.optaplanner.core.impl.score.stream.drools.uni;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.function.Function;

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.SubnetworkTuple;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Accumulator;
import org.drools.core.spi.CompiledInvoker;
import org.drools.core.spi.Tuple;
import org.drools.model.Variable;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

public class DroolsUniToTriGroupByInvoker<A, ResultContainer, NewA, NewB, NewC> implements Accumulator, CompiledInvoker {

    private final UniConstraintCollector<A, ResultContainer, NewC> collector;
    private final Function<A, NewA> groupKeyAMapping;
    private final Function<A, NewB> groupKeyBMapping;
    private final Variable<A> aVariable;

    public DroolsUniToTriGroupByInvoker(Function<A, NewA> groupKeyAMapping, Function<A, NewB> groupKeyBMapping,
            UniConstraintCollector<A, ResultContainer, NewC> collector, Variable<A> aVariable) {
        this.collector = collector;
        this.groupKeyAMapping = groupKeyAMapping;
        this.groupKeyBMapping = groupKeyBMapping;
        this.aVariable = aVariable;
    }

    @Override
    public Serializable createContext() {
        return new DroolsUniToTriGroupBy<>(groupKeyAMapping, groupKeyBMapping, collector);
    }

    @Override
    public void init(Object workingMemoryContext, Object context, Tuple tuple, Declaration[] declarations,
            WorkingMemory workingMemory) {
        castContext(context).init();
    }

    @Override
    public void accumulate(Object workingMemoryContext, Object context, Tuple tuple, InternalFactHandle handle,
            Declaration[] declarations, Declaration[] innerDeclarations, final WorkingMemory workingMemory) {
        InternalWorkingMemory internalWorkingMemory = (InternalWorkingMemory) workingMemory;
        Object handleObject = handle.getObject();
        final A groupKey = getValue(aVariable, internalWorkingMemory, handleObject, innerDeclarations);
        castContext(context).accumulate(handle, groupKey);
    }

    private static <X> X getValue(Variable<X> var, InternalWorkingMemory internalWorkingMemory, Object handleObject,
            Declaration... innerDeclarations) {
        Declaration declaration = getDeclarationForVariable(var, innerDeclarations);
        Object actualHandleObject = handleObject instanceof SubnetworkTuple ?
                ((SubnetworkTuple)handleObject).getObject(declaration) :
                handleObject;
        return (X) declaration.getValue(internalWorkingMemory, actualHandleObject);
    }

    private DroolsUniToTriGroupBy<A, ResultContainer, NewA, NewB, NewC> castContext(Object context) {
        return (DroolsUniToTriGroupBy<A, ResultContainer, NewA, NewB, NewC>) context;
    }

    /**
     * Declarations in Drools appear to show up in random order. Therefore, we need to match the proper declaration
     * not by directly addressing within the array, but by looking it up based on the associated variable.
     * @param variable
     * @param declarations
     * @return
     */
    private static Declaration getDeclarationForVariable(Variable<?> variable, Declaration... declarations) {
        for (Declaration declaration: declarations) {
            if (declaration.getIdentifier().equals(variable.getName())) {
                return declaration;
            }
        }
        throw new IllegalStateException("Could not find declaration for variable (" + variable + ").");
    }

    @Override
    public void reverse(Object workingMemoryContext, Object context, Tuple tuple, InternalFactHandle handle,
            Declaration[] declarations, Declaration[] innerDeclarations, WorkingMemory workingMemory) {
        castContext(context).reverse(handle);
    }

    @Override
    public Object getResult(Object workingMemoryContext, Object context, Tuple tuple, Declaration[] declarations,
            WorkingMemory workingMemory) {
        return castContext(context).getResult();
    }

    @Override
    public boolean supportsReverse() {
        return true;
    }

    @Override
    public Object createWorkingMemoryContext() {
        return null;
    }

    @Override
    public String getMethodBytecode() {
        Class<?> accumulateClass = DroolsUniToTriGroupBy.class;
        String classFileName = accumulateClass.getCanonicalName().replace('.', '/') + ".class";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(classFileName)) {
            final byte[] data = new byte[1024];
            int byteCount;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((byteCount = is.read(data, 0, 1024)) > -1) {
                bos.write(data, 0, byteCount);
            }
            return bos.toString();
        } catch (final IOException e) {
            throw new RuntimeException("Unable to getResourceAsStream for " + accumulateClass);
        }
    }

}
