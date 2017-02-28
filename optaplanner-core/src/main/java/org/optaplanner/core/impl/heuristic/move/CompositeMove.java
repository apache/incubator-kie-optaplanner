/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.move;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * A CompositeMove is composed out of multiple other moves.
 * <p>
 * Warning: each of moves in the moveList must not rely on the effect of a previous move in the moveList
 * to create its undoMove correctly.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @see Move
 */
public class CompositeMove<Solution_> implements Move<Solution_> {

    /**
     * @param moves never null, sometimes empty. Do not modify this argument afterwards or the CompositeMove corrupts.
     * @return never null
     */
    @SafeVarargs
    public static <Solution_, Move_ extends Move<Solution_>> Move<Solution_> buildMove(Move_... moves) {
        int size = moves.length;
        if (size > 1) {
            return new CompositeMove<>(moves);
        } else if (size == 1) {
            return moves[0];
        } else {
            return new NoChangeMove<>();
        }
    }

    /**
     * @param moveList never null, sometimes empty
     * @return never null
     */
    public static <Solution_, Move_ extends Move<Solution_>> Move<Solution_> buildMove(List<Move_> moveList) {
        int size = moveList.size();
        if (size > 1) {
            return new CompositeMove<>(moveList.toArray(new Move[0]));
        } else if (size == 1) {
            return moveList.get(0);
        } else {
            return new NoChangeMove<>();
        }
    }

    // ************************************************************************
    // Non-static members
    // ************************************************************************

    protected final Move<Solution_>[] moves;

    /**
     * @param moves never null, never empty. Do not modify this argument afterwards or this CompositeMove corrupts.
     */
    @SafeVarargs
    public CompositeMove(Move<Solution_>... moves) {
        this.moves = moves;
    }

    public Move<Solution_>[] getMoves() {
        return moves;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        for (Move<Solution_> move : moves) {
            if (!move.isMoveDoable(scoreDirector)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CompositeMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        Move<Solution_>[] undoMoves = new Move[moves.length];
        for (int i = 0; i < moves.length; i++) {
            // Note: this undoMove creation doesn't have the effect yet of a previous move in the moveList
            Move<Solution_> undoMove = moves[i].createUndoMove(scoreDirector);
            undoMoves[moves.length - 1 - i] = undoMove;
        }
        return new CompositeMove<>(undoMoves);
    }

    @Override
    public void doMove(ScoreDirector<Solution_> scoreDirector) {
        for (Move<Solution_> move : moves) {
            // Calls scoreDirector.triggerVariableListeners() between moves
            // because a later move can depend on the shadow variables changed by an earlier move
            move.doMove(scoreDirector);
        }
        // No need to call scoreDirector.triggerVariableListeners() because Move.doMove() already does it for every move.
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        Set<String> childMoveTypeDescriptionSet = new TreeSet<>();
        for (Move<Solution_> move : moves) {
            childMoveTypeDescriptionSet.add(move.getSimpleMoveTypeDescription());
        }
        StringBuilder moveTypeDescription = new StringBuilder(20 * (moves.length + 1));
        moveTypeDescription.append(getClass().getSimpleName()).append("(");
        String delimiter = "";
        for (String childMoveTypeDescription : childMoveTypeDescriptionSet) {
            moveTypeDescription.append(delimiter).append("* ").append(childMoveTypeDescription);
            delimiter = ", ";
        }
        moveTypeDescription.append(")");
        return moveTypeDescription.toString();
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        Set<Object> entities = new LinkedHashSet<>(moves.length * 2);
        for (Move<Solution_> move : moves) {
            entities.addAll(move.getPlanningEntities());
        }
        return entities;
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        Set<Object> values = new LinkedHashSet<>(moves.length * 2);
        for (Move<Solution_> move : moves) {
            values.addAll(move.getPlanningValues());
        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof CompositeMove) {
            CompositeMove<?> other = (CompositeMove) o;
            return Arrays.equals(moves, other.moves);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(moves);
    }

    @Override
    public String toString() {
        return Arrays.toString(moves);
    }

}
