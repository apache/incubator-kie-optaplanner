package org.drools.solver.examples.itc2007.curriculumcourse.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Course;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Curriculum;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Day;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Period;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Room;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Teacher;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Timeslot;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.UnavailablePeriodConstraint;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseInputConvertor extends LoggingMain {

    private static final String INPUT_FILE_SUFFIX = ".ctt";
    private static final String OUTPUT_FILE_SUFFIX = ".xml";
    private static final String SPLIT_REGEX = "[\\ \\t]+";

    public static void main(String[] args) {
        new CurriculumCourseInputConvertor().convert();
    }

    private final File inputDir = new File("data/itc2007/curriculumcourse/input/");
    private final File outputDir = new File("data/itc2007/curriculumcourse/unsolved/");

    public void convert() {
        XstreamSolutionDaoImpl solutionDao = new XstreamSolutionDaoImpl();
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            throw new IllegalArgumentException(
                    "Your working dir should be drools-solver-examples and contain: " + inputDir);
        }
        for (File inputFile : inputFiles) {
            String inputFileName = inputFile.getName();
            if (inputFileName.endsWith(INPUT_FILE_SUFFIX)) {
                CurriculumCourseSchedule schedule = readCurriculumCourseSchedule(inputFile);
                String outputFileName = inputFileName.substring(0, inputFileName.length() - INPUT_FILE_SUFFIX.length())
                        + OUTPUT_FILE_SUFFIX;
                File outputFile = new File(outputDir, outputFileName);
                solutionDao.writeSolution(schedule, outputFile);
            }
        }
    }

    public CurriculumCourseSchedule readCurriculumCourseSchedule(File inputFile) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            return readCurriculumCourseSchedule(bufferedReader);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public CurriculumCourseSchedule readCurriculumCourseSchedule(BufferedReader bufferedReader) throws IOException {
        CurriculumCourseSchedule schedule = new CurriculumCourseSchedule();
        schedule.setId(0L);
        // Name: ToyExample
        schedule.setName(readParam(bufferedReader, "Name:"));
        // Courses: 4
        int courseListSize = Integer.parseInt(readParam(bufferedReader, "Courses:"));
        // Rooms: 2
        int roomListSize = Integer.parseInt(readParam(bufferedReader, "Rooms:"));
        // Days: 5
        int dayListSize = Integer.parseInt(readParam(bufferedReader, "Days:"));
        // Periods_per_day: 4
        int timeslotListSize = Integer.parseInt(readParam(bufferedReader, "Periods_per_day:"));
        // Curricula: 2
        int curriculumListSize = Integer.parseInt(readParam(bufferedReader, "Curricula:"));
        // Constraints: 8
        int unavailablePeriodConstraintListSize = Integer.parseInt(readParam(bufferedReader, "Constraints:"));

        Map<String, Course> courseMap = readCourseListAndTeacherList(bufferedReader,
                schedule, courseListSize);
        readRoomList(bufferedReader,
                schedule, roomListSize);
        Map<Integer[], Period> periodMap = createPeriodListAndDayListAndTimeslotList(
                schedule, dayListSize, timeslotListSize);
        readCurriculumList(bufferedReader,
                schedule, courseMap, curriculumListSize);
        readUnavailablePeriodConstraintList(bufferedReader,
                schedule, courseMap, periodMap, unavailablePeriodConstraintListSize);
        readHeader(bufferedReader, "END.");

        logger.info("CurriculumCourseSchedule with {} teachers, {} curricula, {} courses, {} periods, {} rooms" +
                " and {} unavailable period constraints.",
                new Object[]{schedule.getTeacherList().size(),
                        schedule.getCurriculumList().size(),
                        schedule.getCourseList().size(),
                        schedule.getPeriodList().size(),
                        schedule.getRoomList().size(),
                        schedule.getUnavailablePeriodConstraintList().size()});

        // Note: lectureList stays null, that's work for the StartingSolutionInitializer
        return schedule;
    }

    private Map<String, Course> readCourseListAndTeacherList(BufferedReader bufferedReader,
            CurriculumCourseSchedule schedule, int courseListSize) throws IOException {
        Map<String, Course> courseMap = new HashMap<String, Course>(courseListSize);
        Map<String, Teacher> teacherMap = new HashMap<String, Teacher>();
        List<Course> courseList = new ArrayList<Course>(courseListSize);
        readHeader(bufferedReader, "COURSES:");
        for (int i = 0; i < courseListSize; i++) {
            Course course = new Course();
            course.setId((long) i);
            // Courses: <CourseID> <Teacher> <# Lectures> <MinWorkingDays> <# Students>
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split(SPLIT_REGEX);
            if (lineTokens.length != 5) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 4 tokens.");
            }
            course.setCode(lineTokens[0]);
            course.setTeacher(findOrCreateTeacher(teacherMap, lineTokens[1]));
            course.setLectureSize(Integer.parseInt(lineTokens[2]));
            course.setMinWorkingDaySize(Integer.parseInt(lineTokens[3]));
            course.setStudentSize(Integer.parseInt(lineTokens[4]));
            courseList.add(course);
            courseMap.put(course.getCode(), course);
        }
        schedule.setCourseList(courseList);
        List<Teacher> teacherList = new ArrayList<Teacher>(teacherMap.values());
        schedule.setTeacherList(teacherList);
        return courseMap;
    }

    private Teacher findOrCreateTeacher(Map<String, Teacher> teacherMap, String code) {
        Teacher teacher = teacherMap.get(code);
        if (teacher == null) {
            teacher = new Teacher();
            int id = teacherMap.size();
            teacher.setId((long) id);
            teacher.setCode(code);
            teacherMap.put(code, teacher);
        }
        return teacher;
    }

    private void readRoomList(BufferedReader bufferedReader, CurriculumCourseSchedule schedule, int roomListSize)
            throws IOException {
        readHeader(bufferedReader, "ROOMS:");
        List<Room> roomList = new ArrayList<Room>(roomListSize);
        for (int i = 0; i < roomListSize; i++) {
            Room room = new Room();
            room.setId((long) i);
            // Rooms: <RoomID> <Capacity>
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split(SPLIT_REGEX);
            if (lineTokens.length != 2) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 2 tokens.");
            }
            room.setCode(lineTokens[0]);
            room.setCapacity(Integer.parseInt(lineTokens[1]));
            roomList.add(room);
        }
        schedule.setRoomList(roomList);
    }

    private Map<Integer[], Period> createPeriodListAndDayListAndTimeslotList(
            CurriculumCourseSchedule schedule, int dayListSize, int timeslotListSize) throws IOException {
        int periodListSize = dayListSize * timeslotListSize;
        Map<Integer[], Period> periodMap = new HashMap<Integer[], Period>(periodListSize);
        List<Day> dayList = new ArrayList<Day>(dayListSize);
        for (int i = 0; i < dayListSize; i++) {
            Day day = new Day();
            day.setId((long) i);
            day.setDayIndex(i);
            dayList.add(day);
        }
        schedule.setDayList(dayList);
        List<Timeslot> timeslotList = new ArrayList<Timeslot>(timeslotListSize);
        for (int i = 0; i < timeslotListSize; i++) {
            Timeslot timeslot = new Timeslot();
            timeslot.setId((long) i);
            timeslot.setTimeslotIndex(i);
            timeslotList.add(timeslot);
        }
        schedule.setTimeslotList(timeslotList);
        List<Period> periodList = new ArrayList<Period>(periodListSize);
        for (int i = 0; i < dayListSize; i++) {
            for (int j = 0; j < timeslotListSize; j++) {
                Period period = new Period();
                period.setId((long) (i * timeslotListSize + j));
                period.setDay(dayList.get(i));
                period.setTimeslot(timeslotList.get(j));
                periodList.add(period);
                periodMap.put(new Integer[]{i, j}, period);
            }
        }
        schedule.setPeriodList(periodList);
        return periodMap;
    }

    private void readCurriculumList(BufferedReader bufferedReader, CurriculumCourseSchedule schedule,
            Map<String, Course> courseMap, int curriculumListSize) throws IOException {
        readHeader(bufferedReader, "CURRICULA:");
        List<Curriculum> curriculumList = new ArrayList<Curriculum>(curriculumListSize);
        for (int i = 0; i < curriculumListSize; i++) {
            Curriculum curriculum = new Curriculum();
            curriculum.setId((long) i);
            // Curricula: <CurriculumID> <# Courses> <MemberID> ... <MemberID>
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split(SPLIT_REGEX);
            if (lineTokens.length < 2) {
                throw new IllegalArgumentException("Read line (" + line
                        + ") is expected to contain at least 2 tokens.");
            }
            curriculum.setCode(lineTokens[0]);
            int coursesInCurriculum = Integer.parseInt(lineTokens[1]);
            if (lineTokens.length != (coursesInCurriculum + 2)) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain "
                        + (coursesInCurriculum + 2) + " tokens.");
            }
            for (int j = 2; j < lineTokens.length; j++) {
                Course course = courseMap.get(lineTokens[j]);
                course.setCurriculum(curriculum);
            }
            curriculumList.add(curriculum);
        }
        schedule.setCurriculumList(curriculumList);
    }

    private void readUnavailablePeriodConstraintList(BufferedReader bufferedReader, CurriculumCourseSchedule schedule,
            Map<String, Course> courseMap, Map<Integer[], Period> periodMap, int unavailablePeriodConstraintListSize)
            throws IOException {
        readHeader(bufferedReader, "UNAVAILABILITY_CONSTRAINTS:");
        List<UnavailablePeriodConstraint> constraintList = new ArrayList<UnavailablePeriodConstraint>(
                unavailablePeriodConstraintListSize);
        for (int i = 0; i < unavailablePeriodConstraintListSize; i++) {
            UnavailablePeriodConstraint constraint = new UnavailablePeriodConstraint();
            constraint.setId((long) i);
            // Unavailability_Constraints: <CourseID> <Day> <Day_Period>
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split(SPLIT_REGEX);
            if (lineTokens.length != 3) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 3 tokens.");
            }
            constraint.setCourse(courseMap.get(lineTokens[0]));
            constraint.setPeriod(periodMap.get(new Integer[]{
                    Integer.parseInt(lineTokens[1]), Integer.parseInt(lineTokens[2])}));
            constraintList.add(constraint);
        }
        schedule.setUnavailablePeriodConstraintList(constraintList);
    }

    private String readParam(BufferedReader bufferedReader, String key) throws IOException {
        String line = bufferedReader.readLine();
        String[] lineTokens = line.split(SPLIT_REGEX);
        if (lineTokens.length != 2 || !lineTokens[0].equals(key)) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 2 tokens"
                    + " and start with \"" + key + "\".");
        }
        return lineTokens[1];
    }

    private void readHeader(BufferedReader bufferedReader, String header) throws IOException {
        String line = bufferedReader.readLine();
        if (line.length() != 0) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to be empty"
                    + " and be followed with a line \"" + header + "\".");
        }
        line = bufferedReader.readLine();
        if (!line.equals(header)) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to be \"" + header + "\".");
        }
    }

}