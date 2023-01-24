package org.acme.common.persistence;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.common.domain.Lesson;
import org.acme.common.domain.TimeTable;

@ApplicationScoped
public class TimeTableRepository {

    private final RoomRepository roomRepository;
    private final TimeslotRepository timeslotRepository;
    private final LessonRepository lessonRepository;

    @Inject
    public TimeTableRepository(RoomRepository roomRepository, TimeslotRepository timeslotRepository,
            LessonRepository lessonRepository) {
        this.roomRepository = roomRepository;
        this.timeslotRepository = timeslotRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    public void persist(TimeTable timeTable) {
        timeslotRepository.persist(timeTable.getTimeslotList());
        roomRepository.persist(timeTable.getRoomList());
        lessonRepository.persist(timeTable.getLessonList());
    }

    @Transactional
    public void save(Long problemId, TimeTable timeTable) {
        for (Lesson lesson : timeTable.getLessonList()) {
            Lesson attachedLesson = lessonRepository.findById(lesson.getId());
            attachedLesson.setTimeslot(lesson.getTimeslot());
            attachedLesson.setRoom(lesson.getRoom());
        }
    }

    @Transactional
    public TimeTable load(Long problemId) {
        return new TimeTable(
                timeslotRepository.listAllByProblemId(problemId),
                roomRepository.listAllByProblemId(problemId),
                lessonRepository.listAllByProblemId(problemId));
    }
}
