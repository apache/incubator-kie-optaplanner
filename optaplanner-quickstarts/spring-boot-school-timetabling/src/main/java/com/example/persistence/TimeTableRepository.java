package com.example.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.Lesson;
import com.example.domain.TimeTable;

@Service
@Transactional
public class TimeTableRepository {

    // There is only one time table, so there is only timeTableId (= problemId).
    public static final Long SINGLETON_TIME_TABLE_ID = 1L;

    @Autowired
    private TimeslotRepository timeslotRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private LessonRepository lessonRepository;

    public TimeTable findById(Long id) {
        if (!SINGLETON_TIME_TABLE_ID.equals(id)) {
            throw new IllegalStateException("There is no timeTable with id (" + id + ").");
        }
        // Occurs in a single transaction, so each initialized lesson references the same timeslot/room instance
        // that is contained by the timeTable's timeslotList/roomList.
        return new TimeTable(
                timeslotRepository.findAll(),
                roomRepository.findAll(),
                lessonRepository.findAll());
    }

    public void save(TimeTable timeTable) {
        for (Lesson lesson : timeTable.getLessonList()) {
            // TODO this is awfully naive: optimistic locking causes issues if called by the SolverManager
            lessonRepository.save(lesson);
        }
    }

}
