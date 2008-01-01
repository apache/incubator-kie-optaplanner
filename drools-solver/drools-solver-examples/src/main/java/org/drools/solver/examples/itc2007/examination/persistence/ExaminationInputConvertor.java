package org.drools.solver.examples.itc2007.examination.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.InstitutionalWeighting;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraint;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraintType;
import org.drools.solver.examples.itc2007.examination.domain.Room;
import org.drools.solver.examples.itc2007.examination.domain.RoomHardConstraint;
import org.drools.solver.examples.itc2007.examination.domain.RoomHardConstraintType;
import org.drools.solver.examples.itc2007.examination.domain.Student;
import org.drools.solver.examples.itc2007.examination.domain.Topic;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationInputConvertor {

    private static final String INPUT_FILE_SUFFIX = ".exam";
    private static final String OUTPUT_FILE_SUFFIX = ".xml";
    private static final String SPLIT_REGEX = "\\,\\ ?";

    public static void main(String[] args) {
        new ExaminationInputConvertor().convert();
    }

    private final File inputDir = new File("data/itc2007/examination/input/");
    private final File outputDir = new File("data/itc2007/examination/unsolved/");

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
                Examination examination = readExamination(inputFile);
                String outputFileName = inputFileName.substring(0, inputFileName.length() - INPUT_FILE_SUFFIX.length())
                        + OUTPUT_FILE_SUFFIX;
                File outputFile = new File(outputDir, outputFileName);
                solutionDao.writeSolution(examination, outputFile);
            }
        }
    }

    private Examination readExamination(File inputFile) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            return readExamination(bufferedReader);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    private Examination readExamination(BufferedReader bufferedReader) throws IOException {
        Examination examination = new Examination();
        examination.setId(0L);

        readTopicListAndStudentList( bufferedReader, examination);
        readPeriodList(bufferedReader, examination);
        readRoomList(bufferedReader, examination);

        String line = bufferedReader.readLine();
        if (!line.equals("[PeriodHardConstraints]")) {
            throw new IllegalStateException("Read line (" + line
                    + " is not the expected header ([PeriodHardConstraints])");
        }
        readPeriodHardConstraintList(bufferedReader, examination);
        readRoomHardConstraintList(bufferedReader, examination);
        readInstitutionalWeighting(bufferedReader, examination);
        tagFrontLoadLargeTopics(examination);
        tagFrontLoadLastPeriods(examination);

        createExamList(examination);
        return examination;
    }

    private void readTopicListAndStudentList(BufferedReader bufferedReader, Examination examination) throws IOException {
        Map<Integer, Student> studentMap = new HashMap<Integer, Student>();
        int examSize = readHeaderWithNumber(bufferedReader, "Exams");
        List<Topic> topicList = new ArrayList<Topic>(examSize);
        for (int i = 0; i < examSize; i++) {
            Topic topic = new Topic();
            topic.setId((long) i);
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split(SPLIT_REGEX);
            topic.setDuration(Integer.parseInt(lineTokens[0]));
            List<Student> topicStudentList = new ArrayList<Student>(lineTokens.length - 1);
            for (int j = 1; j < lineTokens.length; j++) {
                topicStudentList.add(findOrCreateStudent(studentMap, Integer.parseInt(lineTokens[j])));
            }
            topic.setStudentList(topicStudentList);
            topic.setFrontLoadLarge(false);
            topicList.add(topic);
        }
        examination.setTopicList(topicList);
        List<Student> studentList = new ArrayList<Student>(studentMap.size());
        for (Student student : studentMap.values()) {
            studentList.add(student);
        }
        examination.setStudentList(studentList);
    }

    private Student findOrCreateStudent(Map<Integer, Student> studentMap, int id) {
        Student student = studentMap.get(id);
        if (student == null) {
            student = new Student();
            student.setId((long) id);
            studentMap.put(id, student);
        }
        return student;
    }

    private void readPeriodList(BufferedReader bufferedReader, Examination examination) throws IOException {
        int periodSize = readHeaderWithNumber(bufferedReader, "Periods");
        List<Period> periodList = new ArrayList<Period>(periodSize);
        // Everything is in the default timezone and the default locale.
        Calendar calendar = Calendar.getInstance();
        final DateFormat DATE_FORMAT = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
        int referenceDayOfYear = -1;
        int referenceYear = -1;
        for (int i = 0; i < periodSize; i++) {
            Period period = new Period();
            period.setId((long) i);
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split(SPLIT_REGEX);
            if (lineTokens.length != 4) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 4 tokens.");
            }
            String startDateTimeString = lineTokens[0] + " " + lineTokens[1];
            period.setStartDateTimeString(startDateTimeString);
            period.setPeriodIndex(i);
            int dayOfYear;
            int year;
            try {
                calendar.setTime(DATE_FORMAT.parse(startDateTimeString));
                calendar.get(Calendar.DAY_OF_YEAR);
                dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
                year = calendar.get(Calendar.YEAR);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Illegal startDateTimeString (" + startDateTimeString + ").", e);
            }
            if (referenceDayOfYear < 0) {
                referenceDayOfYear = dayOfYear;
                referenceYear = year;
            }
            if (year != referenceYear) {
                // Because the Calendar API in JSE sucks... (java 7 will fix that FINALLY)
                throw new IllegalStateException("Not yet implemented to handle periods spread over different years...");
            }
            int dayIndex = dayOfYear - referenceDayOfYear;
            if (dayIndex < 0) {
                throw new IllegalStateException("The periods should be in ascending order.");
            }
            period.setDayIndex(dayIndex);
            period.setDurationInMinutes(Integer.parseInt(lineTokens[2]));
            period.setPenalty(Integer.parseInt(lineTokens[3]));
            periodList.add(period);
        }
        examination.setPeriodList(periodList);
    }

    private void readRoomList(BufferedReader bufferedReader, Examination examination) throws IOException {
        int roomSize = readHeaderWithNumber(bufferedReader, "Rooms");
        List<Room> roomList = new ArrayList<Room>(roomSize);
        for (int i = 0; i < roomSize; i++) {
            Room room = new Room();
            room.setId((long) i);
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split(SPLIT_REGEX);
            if (lineTokens.length != 2) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 2 tokens.");
            }
            room.setCapacity(Integer.parseInt(lineTokens[0]));
            room.setPenalty(Integer.parseInt(lineTokens[1]));
            roomList.add(room);
        }
        examination.setRoomList(roomList);
    }

    private void readPeriodHardConstraintList(BufferedReader bufferedReader, Examination examination)
            throws IOException {
        List<Topic> topicList = examination.getTopicList();
        List<PeriodHardConstraint> periodHardConstraintList = new ArrayList<PeriodHardConstraint>();
        String line = bufferedReader.readLine();
        while (!line.equals("[RoomHardConstraints]")) {
            String[] lineTokens = line.split(SPLIT_REGEX);
            PeriodHardConstraint periodHardConstraint = new PeriodHardConstraint();
            if (lineTokens.length != 3) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 3 tokens.");
            }
            periodHardConstraint.setLeftSideTopic(topicList.get(Integer.parseInt(lineTokens[0])));
            periodHardConstraint.setPeriodHardConstraintType(PeriodHardConstraintType.valueOf(lineTokens[1]));
            periodHardConstraint.setRightSideTopic(topicList.get(Integer.parseInt(lineTokens[2])));
            periodHardConstraintList.add(periodHardConstraint);
            line = bufferedReader.readLine();
        }
        examination.setPeriodHardConstraintList(periodHardConstraintList);
    }

    private void readRoomHardConstraintList(BufferedReader bufferedReader, Examination examination)
            throws IOException {
        List<Topic> topicList = examination.getTopicList();
        List<RoomHardConstraint> roomHardConstraintList = new ArrayList<RoomHardConstraint>();
        String line = bufferedReader.readLine();
        while (!line.equals("[InstitutionalWeightings]")) {
            String[] lineTokens = line.split(SPLIT_REGEX);
            RoomHardConstraint roomHardConstraint = new RoomHardConstraint();
            if (lineTokens.length != 2) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 3 tokens.");
            }
            roomHardConstraint.setTopic(topicList.get(Integer.parseInt(lineTokens[0])));
            roomHardConstraint.setRoomHardConstraintType(RoomHardConstraintType.valueOf(lineTokens[1]));
            roomHardConstraintList.add(roomHardConstraint);
            line = bufferedReader.readLine();
        }
        examination.setRoomHardConstraintList(roomHardConstraintList);
    }

    private int readHeaderWithNumber(BufferedReader bufferedReader, String header) throws IOException {
        String line = bufferedReader.readLine();
        if (!line.startsWith("[" + header + ":") || !line.endsWith("]")) {
            throw new IllegalStateException("Read line (" + line + " is not the expected header (["
                    + header + ":number])");
        }
        return Integer.parseInt(line.substring(header.length() + 2, line.length() - 1));
    }

    private void readInstitutionalWeighting(BufferedReader bufferedReader, Examination examination) throws IOException {
        InstitutionalWeighting institutionalWeighting = new InstitutionalWeighting();
        String[] lineTokens;
        lineTokens = readInstitutionalWeightingProperty(bufferedReader, "TWOINAROW", 2);
        institutionalWeighting.setTwoInARowPenality(Integer.parseInt(lineTokens[1]));
        lineTokens = readInstitutionalWeightingProperty(bufferedReader, "TWOINADAY", 2);
        institutionalWeighting.setTwoInADayPenality(Integer.parseInt(lineTokens[1]));
        lineTokens = readInstitutionalWeightingProperty(bufferedReader, "PERIODSPREAD", 2);
        institutionalWeighting.setPeriodSpreadLength(Integer.parseInt(lineTokens[1]));
        institutionalWeighting.setPeriodSpreadPenality(1); // constant
        lineTokens = readInstitutionalWeightingProperty(bufferedReader, "NONMIXEDDURATIONS", 2);
        institutionalWeighting.setMixedDurationPenality(Integer.parseInt(lineTokens[1]));
        lineTokens = readInstitutionalWeightingProperty(bufferedReader, "FRONTLOAD", 4);
        institutionalWeighting.setFrontLoadLargeTopicSize(Integer.parseInt(lineTokens[1]));
        institutionalWeighting.setFrontLoadLastPeriodSize(Integer.parseInt(lineTokens[2]));
        institutionalWeighting.setFrontLoadPenality(Integer.parseInt(lineTokens[3]));
        examination.setInstitutionalWeighting(institutionalWeighting);
    }

    private String[] readInstitutionalWeightingProperty(BufferedReader bufferedReader, String property,
            int propertySize) throws IOException {
        String[] lineTokens;
        lineTokens = bufferedReader.readLine().split(SPLIT_REGEX);
        if (!lineTokens[0].equals(property) || lineTokens.length != propertySize) {
            throw new IllegalArgumentException("Read line (" + Arrays.toString(lineTokens)
                    + ") is expected to contain " + propertySize + " tokens and start with " + property + ".");
        }
        return lineTokens;
    }

    private void tagFrontLoadLargeTopics(Examination examination) {
        List<Topic> sortedTopicList = new ArrayList<Topic>(examination.getTopicList());
        Collections.sort(sortedTopicList, new Comparator<Topic>() {
            public int compare(Topic a, Topic b) {
                return (a.getStudentListSize() < b.getStudentListSize()) ? -1 :
                        ((a.getStudentListSize() > b.getStudentListSize()) ? 1 : 0);
            }
        });
        int frontLoadLargeTopicSize = examination.getInstitutionalWeighting().getFrontLoadLargeTopicSize();
        if (frontLoadLargeTopicSize == 0) {
            return;
        }
        int minimumStudentListSize = sortedTopicList.get(sortedTopicList.size() - frontLoadLargeTopicSize)
                .getStudentListSize();
        for (Topic topic : sortedTopicList) {
            if (topic.getStudentListSize() >= minimumStudentListSize) {
                topic.setFrontLoadLarge(true);
            }
        }
    }

    private void tagFrontLoadLastPeriods(Examination examination) {
        List<Period> periodList = examination.getPeriodList();
        int frontLoadLastPeriodSize = examination.getInstitutionalWeighting().getFrontLoadLastPeriodSize();
        if (frontLoadLastPeriodSize == 0) {
            return;
        }
        int minimumPeriodIndex = periodList.get(periodList.size() - frontLoadLastPeriodSize).getPeriodIndex();
        for (Period period : periodList) {
            if (period.getPeriodIndex() >= minimumPeriodIndex) {
                period.setFrontLoadLast(true);
            }
        }
    }

    private void createExamList(Examination examination) {
        List<Topic> topicList = examination.getTopicList();
        List<Period> periodList = examination.getPeriodList();
        List<Room> roomList = examination.getRoomList();
        List<Exam> examList = new ArrayList<Exam>(topicList.size());
        int periodIndex = 0;
        int roomIndex = 0;
        int roomAddition = 0;
        for (Topic topic : topicList) {
            Exam exam = new Exam();
            exam.setId(topic.getId());
            exam.setTopic(topic);
            exam.setPeriod(periodList.get(periodIndex));
            exam.setRoom(roomList.get(roomIndex));
            periodIndex = (periodIndex + 1) % periodList.size();
            roomIndex = (roomIndex + 1) % roomList.size();
            if (roomIndex == 0) {
                roomAddition = (roomAddition + 1) % roomList.size();
                roomIndex = roomAddition;
            }
            examList.add(exam);
        }
        examination.setExamList(examList);
    }

}
