package org.acme.common.persistence;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.acme.common.domain.Timeslot;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class TimeslotRepository implements PanacheRepository<Timeslot> {

    public List<Timeslot> listAllByProblemId(long problemId) {
        return find(Timeslot.TENANT_FIELD, Sort.by("dayOfWeek").and("startTime").and("endTime").and("id"), problemId)
                .list();
    }
}
