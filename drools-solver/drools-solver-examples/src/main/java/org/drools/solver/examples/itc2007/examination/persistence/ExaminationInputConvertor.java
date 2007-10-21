package org.drools.solver.examples.itc2007.examination.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.InstitutionalWeighting;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraint;
import org.drools.solver.examples.itc2007.examination.domain.Room;
import org.drools.solver.examples.itc2007.examination.domain.RoomHardConstraint;
import org.drools.solver.examples.itc2007.examination.domain.Student;
import org.drools.solver.examples.itc2007.examination.domain.Topic;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationInputConvertor {

    private static final String INPUT_FILE_SUFFIX = ".exam";

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
                Examination examination = createExamination(inputFile);
                String outputFileName = inputFileName.substring(0, inputFileName.length() - INPUT_FILE_SUFFIX.length()) + ".xml";
                File outputFile = new File(outputDir, outputFileName);
                solutionDao.writeSolution(examination, outputFile);
            }
        }
    }

    private Examination createExamination(File inputFile) {
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

        Map<Integer, Student> studentMap = new HashMap<Integer, Student>();
        List<Topic> topicList = readExamListAndFillStudentMap(studentMap, bufferedReader);
        examination.setTopicList(topicList);
        List<Student> studentList = new ArrayList<Student>(studentMap.size());
        for (Student student : studentMap.values()) {
            studentList.add(student);
        }
        examination.setStudentList(studentList);
        List<Period> periodList = readPeriodList(bufferedReader);
        examination.setPeriodList(periodList);
        List<Room> roomList = readRoomList(bufferedReader);
        examination.setRoomList(roomList);

        List<PeriodHardConstraint> periodHardConstraintList = readPeriodHardConstraintList(bufferedReader);
        examination.setPeriodHardConstraintList(periodHardConstraintList);
        List<RoomHardConstraint> roomHardConstraintList = readRoomHardConstraintList(bufferedReader);
        examination.setRoomHardConstraintList(roomHardConstraintList);
        InstitutionalWeighting institutionalWeighting = readInstitutionalWeighting(bufferedReader);
        examination.setInstitutionalWeighting(institutionalWeighting);
        
        initializeExamPeriodsAndRooms(examination);
        return examination;
    }

    private List<Topic> readExamListAndFillStudentMap(Map<Integer, Student> studentMap, BufferedReader bufferedReader) throws IOException {
        int examSize = readHeaderWithNumber(bufferedReader, "Exams");
        List<Topic> topicList = new ArrayList<Topic>(examSize);
        for (int i = 0; i < examSize; i++) {
            Topic topic = new Topic();
            topic.setId((long) i);
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split("\\,\\ ");
            topic.setDuration(Integer.parseInt(lineTokens[0]));
            for (int j = 1; j < lineTokens.length; j++) {
                findOrCreateStudent(studentMap, Integer.parseInt(lineTokens[j]));
            }
            topicList.add(topic);
        }
        return topicList;
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

    private List<Period> readPeriodList(BufferedReader bufferedReader) throws IOException {
        int periodSize = readHeaderWithNumber(bufferedReader, "Periods");
        List<Period> periodList = new ArrayList<Period>(periodSize);
        // the timezone needs to be specified or the timeDifference will change with a different -Duser.timezone=...
        final DateFormat DATE_FORMAT = new SimpleDateFormat("dd:MM:yyyy HH:mm:ssZ");
        final Date referenceDate; // in the same locale, timezone, DST as the other parsed dates
        try {
            referenceDate = DATE_FORMAT.parse("01:01:2000 12:00:00+0000");
        } catch (ParseException e) {
            throw new IllegalStateException("Illegal referenceDateString.", e);
        }
        final long MILLISECONDS_PER_DAY = 1000L * 3600L * 24L;
        for (int i = 0; i < periodSize; i++) {
            Period period = new Period();
            period.setId((long) i);
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split("\\,\\ ");
            if (lineTokens.length != 4) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 4 tokens.");
            }
            String startDateTimeString = lineTokens[0] + " " + lineTokens[1];
            period.setStartDateTimeString(startDateTimeString);
            Date startDateTime;
            try {
                startDateTime = DATE_FORMAT.parse(startDateTimeString + "+0000");
            } catch (ParseException e) {
                throw new IllegalArgumentException("Illegal startDateTimeString (" + startDateTimeString + ").", e);
            }
            // startDateTime.getTime() alone does NOT suffice
            // as it is locale and timezone dependend (we need GMT without DST)
            long timeDifference = startDateTime.getTime() - referenceDate.getTime();
            period.setDateInDays((int) (timeDifference / MILLISECONDS_PER_DAY));
            period.setStartTimeInMinutes((int) (startDateTime.getTime() % MILLISECONDS_PER_DAY / 60000L));
            if ((timeDifference % 60000L) != 0L) {
                throw new IllegalArgumentException("The startDateTimeString (" + startDateTimeString
                        + ") should not be specified below minutes.");
            }
            period.setDurationInMinutes(Integer.parseInt(lineTokens[2]));
            period.setPenalty(Integer.parseInt(lineTokens[3]));
            periodList.add(period);
        }
        return periodList;
    }

    private List<Room> readRoomList(BufferedReader bufferedReader) throws IOException {
        int roomSize = readHeaderWithNumber(bufferedReader, "Rooms");
        List<Room> roomList = new ArrayList<Room>(roomSize);
        for (int i = 0; i < roomSize; i++) {
            Room room = new Room();
            room.setId((long) i);
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split("\\,\\ ");
            if (lineTokens.length != 2) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 2 tokens.");
            }
            room.setCapacity(Integer.parseInt(lineTokens[0]));
            room.setPenalty(Integer.parseInt(lineTokens[1]));
            roomList.add(room);
        }
        return roomList;
    }

    private List<PeriodHardConstraint> readPeriodHardConstraintList(BufferedReader bufferedReader) {
        // TODO generated
        return null;
    }

    private List<RoomHardConstraint> readRoomHardConstraintList(BufferedReader bufferedReader) {
        // TODO generated
        return null;
    }

    private InstitutionalWeighting readInstitutionalWeighting(BufferedReader bufferedReader) {
        // TODO generated
        return null;
    }

    private void initializeExamPeriodsAndRooms(Examination examination) {
        // TODO generated
    }

    private void readHeader(BufferedReader bufferedReader, String header) throws IOException {
        String line = bufferedReader.readLine();
        if (!line.equals("[" + header + "]")) {
            throw new IllegalStateException("Read line (" + line + " is not the expected header ([" + header + "])");
        }
    }

    private int readHeaderWithNumber(BufferedReader bufferedReader, String header) throws IOException {
        String line = bufferedReader.readLine();
        if (!line.startsWith("[" + header + ":") || !line.endsWith("]")) {
            throw new IllegalStateException("Read line (" + line + " is not the expected header ([" + header + ":number])");
        }
        return Integer.parseInt(line.substring(header.length() + 2, line.length() - 1));
    }

}
