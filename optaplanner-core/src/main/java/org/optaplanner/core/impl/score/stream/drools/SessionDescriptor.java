package org.optaplanner.core.impl.score.stream.drools;

import java.util.Objects;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.AgendaFilter;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public final class SessionDescriptor<Score_ extends Score<Score_>> {

    private final KieSession session;
    private final AgendaFilter agendaFilter;
    private final ScoreInliner<Score_> scoreInliner;

    public SessionDescriptor(KieSession session, AgendaFilter agendaFilter, ScoreInliner<Score_> scoreInliner) {
        this.session = Objects.requireNonNull(session);
        this.agendaFilter = Objects.requireNonNull(agendaFilter);
        this.scoreInliner = Objects.requireNonNull(scoreInliner);
    }

    public KieSession getSession() {
        return session;
    }

    public AgendaFilter getAgendaFilter() {
        return agendaFilter;
    }

    public ScoreInliner<Score_> getScoreInliner() {
        return scoreInliner;
    }
}
