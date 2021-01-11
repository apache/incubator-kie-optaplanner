package org.optaplanner.quarkus.deployment.rest.gizmo;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreManager;

import io.quarkus.gizmo.MethodDescriptor;

/**
 * The class provides {@link MethodDescriptor}s of the {@link ScoreManager} interface's methods.
 */
public class ScoreManagerDescriptor {

    public static MethodDescriptor updateScore() {
        return MethodDescriptor
                .ofMethod(ScoreManager.class, "updateScore", Score.class, Object.class);
    }
}
