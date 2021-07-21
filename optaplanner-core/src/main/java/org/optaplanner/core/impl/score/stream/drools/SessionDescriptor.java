package org.optaplanner.core.impl.score.stream.drools;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public final class SessionDescriptor<Score_ extends Score<Score_>> {

    private final KieSession session;
    private final ScoreInliner<Score_> scoreInliner;

    public SessionDescriptor(KieSession session, ScoreInliner<Score_> scoreInliner) {
        this.session = session;
        this.scoreInliner = scoreInliner;
    }

    public KieSession getSession() {
        return session;
    }

    public ScoreInliner<Score_> getScoreInliner() {
        return scoreInliner;
    }
}
