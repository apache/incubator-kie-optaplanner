package org.drools.solver.examples.manners2009.persistence;

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
import org.drools.solver.examples.manners2009.domain.Gender;
import org.drools.solver.examples.manners2009.domain.Guest;
import org.drools.solver.examples.manners2009.domain.Hobby;
import org.drools.solver.examples.manners2009.domain.HobbyPractician;
import org.drools.solver.examples.manners2009.domain.Job;
import org.drools.solver.examples.manners2009.domain.JobType;
import org.drools.solver.examples.manners2009.domain.Manners2009;
import org.drools.solver.examples.manners2009.domain.Seat;
import org.drools.solver.examples.manners2009.domain.Table;

/**
 * @author Geoffrey De Smet
 */
public class Manners2009InputConvertor extends LoggingMain {

    private static final String INPUT_FILE_SUFFIX = ".txt";
    private static final String OUTPUT_FILE_SUFFIX = ".xml";
    private static final String SPLIT_REGEX = "[\\ \\t]+";

    public static void main(String[] args) {
        new Manners2009InputConvertor().convert();
    }

    private final File inputDir = new File("data/manners2009/input/");
    private final File outputDir = new File("data/manners2009/unsolved/");

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
                Manners2009 manners2009 = readManners2009(inputFile);
                String outputFileName = inputFileName.substring(0, inputFileName.length() - INPUT_FILE_SUFFIX.length())
                        + OUTPUT_FILE_SUFFIX;
                File outputFile = new File(outputDir, outputFileName);
                solutionDao.writeSolution(manners2009, outputFile);
            }
        }
    }

    public Manners2009 readManners2009(File inputFile) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            return readManners2009(bufferedReader);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public Manners2009 readManners2009(BufferedReader bufferedReader) throws IOException {
        Manners2009 manners2009 = new Manners2009();
        manners2009.setId(0L);

        readTableListAndSeatList(bufferedReader, manners2009);
        readJobListGuestListAndHobbyPracticianList(bufferedReader, manners2009);

        logger.info("Manners2009 with {} jobs, {} guests, {} hobby practicians, {} tables and {} seats.",
                new Object[]{manners2009.getJobList().size(),
                        manners2009.getGuestList().size(),
                        manners2009.getHobbyPracticianList().size(),
                        manners2009.getTableList().size(),
                        manners2009.getSeatList().size()});
        
        return manners2009;
    }

    private void readTableListAndSeatList(BufferedReader bufferedReader, Manners2009 manners2009)
            throws IOException {
        int tableListSize = Integer.parseInt(readParam(bufferedReader, "Tables:"));
        int seatsPerTable = Integer.parseInt(readParam(bufferedReader, "SeatsPerTable:"));
        List<Table> tableList = new ArrayList<Table>(tableListSize);
        List<Seat> seatList = new ArrayList<Seat>(tableListSize * seatsPerTable);
        for (int i = 0; i < tableListSize; i++) {
            Table table = new Table();
            table.setId((long) i);
            table.setTableIndex(i);
            List<Seat> tableSeatList = new ArrayList<Seat>(seatsPerTable);
            Seat firstSeat = null;
            Seat previousSeat = null;
            for (int j = 0; j < seatsPerTable; j++) {
                Seat seat = new Seat();
                seat.setId((long) ((i * seatsPerTable) + j));
                seat.setTable(table);
                seat.setSeatIndexInTable(j);
                if (previousSeat != null) {
                    seat.setLeftSeat(previousSeat);
                    previousSeat.setRightSeat(seat);
                } else {
                    firstSeat = seat; 
                }
                tableSeatList.add(seat);
                seatList.add(seat);
                previousSeat = seat;
            }
            firstSeat.setLeftSeat(previousSeat);
            previousSeat.setRightSeat(firstSeat);
            table.setSeatList(tableSeatList);
            tableList.add(table);
        }
        manners2009.setTableList(tableList);
        manners2009.setSeatList(seatList);
    }


    private void readJobListGuestListAndHobbyPracticianList(BufferedReader bufferedReader, Manners2009 manners2009)
            throws IOException {
        readHeader(bufferedReader, "Num,Profession,SubProf,Gender,Spt1,Spt2,Spt3");
        readHeader(bufferedReader, "-------------------------------------------");
        int guestSize = manners2009.getSeatList().size();

        List<Guest> guestList = new ArrayList<Guest>(guestSize);
        List<HobbyPractician> hobbyPracticianList = new ArrayList<HobbyPractician>(guestSize * 3);
        Map<String, Job> jobMap = new HashMap<String, Job>(JobType.values().length * 5);
        int jobNextId = 0;
        int hobbyPracticianJobId = 0;
        for (int i = 0; i < guestSize; i++) {
            Guest guest = new Guest();
            guest.setId((long) i);
            String line = bufferedReader.readLine();
            String[] lineTokens = line.split("\\,");
            if (lineTokens.length < 5) {
                throw new IllegalArgumentException("Read line (" + line
                        + ") is expected to contain at least 5 tokens.");
            }
            guest.setCode(lineTokens[0].trim());
            JobType jobType = JobType.valueOfCode(lineTokens[1].trim());
            String jobName = lineTokens[2].trim();
            String jobMapKey = jobType + "/" + jobName;
            Job job = jobMap.get(jobMapKey);
            if (job == null) {
                job = new Job();
                job.setId((long) jobNextId);
                jobNextId++;
                job.setJobType(jobType);
                job.setName(jobName);
                jobMap.put(jobMapKey, job);
            }
            guest.setJob(job);
            guest.setGender(Gender.valueOfCode(lineTokens[3].trim()));
            List<HobbyPractician> hobbyPracticianOfGuestList = new ArrayList<HobbyPractician>(lineTokens.length - 4);
            for (int j = 4; j < lineTokens.length; j++) {
                HobbyPractician hobbyPractician = new HobbyPractician();
                hobbyPractician.setId((long) hobbyPracticianJobId);
                hobbyPracticianJobId++;
                hobbyPractician.setGuest(guest);
                hobbyPractician.setHobby(Hobby.valueOfCode(lineTokens[j].trim()));
                hobbyPracticianOfGuestList.add(hobbyPractician);
                hobbyPracticianList.add(hobbyPractician);
            }
            guest.setHobbyPracticianList(hobbyPracticianOfGuestList);
            guestList.add(guest);
        }
        manners2009.setJobList(new ArrayList<Job>(jobMap.values()));
        manners2009.setGuestList(guestList);
        manners2009.setHobbyPracticianList(hobbyPracticianList);
    }

    private String readParam(BufferedReader bufferedReader, String key) throws IOException {
        String line = bufferedReader.readLine();
        String[] lineTokens = line.split("[\\ \\t]+");
        if (lineTokens.length != 2 || !lineTokens[0].equals(key)) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 2 tokens"
                    + " and start with \"" + key + "\".");
        }
        return lineTokens[1];
    }

    private void readHeader(BufferedReader bufferedReader, String header) throws IOException {
        String line = bufferedReader.readLine();
        if (!line.equals(header)) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to be \"" + header + "\".");
        }
    }

}