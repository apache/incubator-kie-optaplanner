package org.optaplanner.core.impl.score.stream.drools.bi;

import java.util.function.BiPredicate;

import org.drools.model.Declaration;
import org.drools.model.PatternDSL;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;

public class DroolsFilterBiConstraintStream<Solution_, A, B> extends DroolsAbstractBiConstraintStream<Solution_, A, B> {

    private final BiPredicate<A, B> biPredicate;
    private final PatternDSL.PatternDef<B> rightPattern;

    public DroolsFilterBiConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiPredicate<A, B> biPredicate) {
        super(constraintFactory, parent);
        this.biPredicate = biPredicate;
        this.rightPattern = parent.getRightPattern().expr(getLeftVariableDeclaration(), (b, a) -> biPredicate.test(a, b));
    }

    @Override
    public Declaration<A> getLeftVariableDeclaration() {
        return parent.getLeftVariableDeclaration();
    }

    @Override
    public PatternDSL.PatternDef<A> getLeftPattern() {
        return parent.getLeftPattern();
    }

    @Override
    public Declaration<B> getRightVariableDeclaration() {
        return parent.getRightVariableDeclaration();
    }

    @Override
    public PatternDSL.PatternDef<B> getRightPattern() {
        return rightPattern;
    }

    @Override
    public String toString() {
        return "BiFilter() with " + childStreamList.size()  + " children";
    }

}
