package org.acme.common.persistence;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.acme.common.domain.Room;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class RoomRepository implements PanacheRepository<Room> {

    public List<Room> listAllByProblemId(long problemId) {
        return find(Room.TENANT_FIELD, Sort.by("name").and("id"), problemId).list();
    }
}
