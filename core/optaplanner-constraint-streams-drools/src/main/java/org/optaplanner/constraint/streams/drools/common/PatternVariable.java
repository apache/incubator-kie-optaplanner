package org.optaplanner.constraint.streams.drools.common;

import java.util.List;

import org.drools.model.Variable;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Predicate1;
import org.drools.model.functions.Predicate2;
import org.drools.model.functions.Predicate3;
import org.drools.model.functions.Predicate4;
import org.drools.model.view.ViewItem;
import org.optaplanner.constraint.streams.common.bi.DefaultBiJoiner;
import org.optaplanner.constraint.streams.common.quad.DefaultQuadJoiner;
import org.optaplanner.constraint.streams.common.tri.DefaultTriJoiner;
import org.optaplanner.core.impl.score.stream.JoinerType;

public interface PatternVariable<A, PatternVar_, Child_ extends PatternVariable<A, PatternVar_, Child_>> {

    Variable<A> getPrimaryVariable();

    List<ViewItem<?>> getPrerequisiteExpressions();

    List<ViewItem<?>> getDependentExpressions();

    Child_ filter(Predicate1<A> predicate);

    <LeftJoinVar_> Child_ filter(Predicate2<LeftJoinVar_, A> predicate, Variable<LeftJoinVar_> leftJoinVariable);

    <LeftJoinVarA_, LeftJoinVarB_> Child_ filter(Predicate3<LeftJoinVarA_, LeftJoinVarB_, A> predicate,
            Variable<LeftJoinVarA_> leftJoinVariableA, Variable<LeftJoinVarB_> leftJoinVariableB);

    <LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_> Child_ filter(
            Predicate4<LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, A> predicate, Variable<LeftJoinVarA_> leftJoinVariableA,
            Variable<LeftJoinVarB_> leftJoinVariableB, Variable<LeftJoinVarC_> leftJoinVariableC);

    <LeftJoinVar_> PatternVariable<A, PatternVar_, Child_> filterForJoin(Variable<LeftJoinVar_> leftJoinVar,
            DefaultBiJoiner<LeftJoinVar_, A> joiner, JoinerType joinerType, int mappingIndex);

    <LeftJoinVarA_, LeftJoinVarB_> PatternVariable<A, PatternVar_, Child_> filterForJoin(Variable<LeftJoinVarA_> leftJoinVarA,
            Variable<LeftJoinVarB_> leftJoinVarB, DefaultTriJoiner<LeftJoinVarA_, LeftJoinVarB_, A> joiner,
            JoinerType joinerType, int mappingIndex);

    <LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_> PatternVariable<A, PatternVar_, Child_> filterForJoin(
            Variable<LeftJoinVarA_> leftJoinVarA, Variable<LeftJoinVarB_> leftJoinVarB, Variable<LeftJoinVarC_> leftJoinVarC,
            DefaultQuadJoiner<LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, A> joiner, JoinerType joinerType,
            int mappingIndex);

    /**
     * Bind a new variable.
     * This call is safe for use outside accumulate() and groupBy(),
     * unlike binding with multiple bound variables.
     *
     * @param boundVariable the new variable
     * @param bindingFunction the function to apply to create the bound variable
     * @param <BoundVar_> generic type of the bound variable
     * @return never null
     */
    <BoundVar_> Child_ bind(Variable<BoundVar_> boundVariable, Function1<A, BoundVar_> bindingFunction);

    Child_ addDependentExpression(ViewItem<?> expression);

    List<ViewItem<?>> build();
}
