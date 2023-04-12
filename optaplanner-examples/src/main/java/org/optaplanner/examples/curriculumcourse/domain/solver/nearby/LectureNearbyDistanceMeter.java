package org.optaplanner.examples.curriculumcourse.domain.solver.nearby;

import java.util.Objects;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.curriculumcourse.domain.Lecture;

public class LectureNearbyDistanceMeter implements NearbyDistanceMeter<Lecture, Lecture> {

    @Override
    public double getNearbyDistance(Lecture origin, Lecture destination) {
        return Objects.equals(origin.getCourse().getTeacher().getCode(), destination.getCourse().getTeacher().getCode()) ? 0
                : 1;
    }

}
