package org.drools.solver.core.localsearch.decider.forager;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class Acception {

    private Move move;
    private double score;
    private double acceptChance;

    public Acception(Move move, double score, double acceptChance) {
        this.move = move;
        this.score = score;
        this.acceptChance = acceptChance;
    }

    public Move getMove() {
        return move;
    }

    public double getScore() {
        return score;
    }

    public double getAcceptChance() {
        return acceptChance;
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Acception) {
            Acception other = (Acception) o;
            return new EqualsBuilder()
                    .append(score, other.score)
                    .append(acceptChance, other.acceptChance)
                    .append(move, other.move)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(score)
                .append(acceptChance)
                .append(move)
                .toHashCode();
    }

}
