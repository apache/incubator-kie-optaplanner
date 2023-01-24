package org.acme.common.persistence;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.acme.common.domain.Lesson;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class LessonRepository implements PanacheRepository<Lesson> {

    public List<Lesson> listAllByProblemId(long problemId) {
        return find(Lesson.TENANT_FIELD, Sort.by("subject").and("teacher").and("studentGroup").and("id"), problemId)
                .list();
    }
}
