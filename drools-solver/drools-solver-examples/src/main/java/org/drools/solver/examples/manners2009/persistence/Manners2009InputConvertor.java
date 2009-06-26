package org.drools.solver.examples.manners2009.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.solver.examples.common.persistence.AbstractInputConvertor;
import org.drools.solver.examples.manners2009.domain.Gender;
import org.drools.solver.examples.manners2009.domain.Guest;
import org.drools.solver.examples.manners2009.domain.Hobby;
import org.drools.solver.examples.manners2009.domain.HobbyPractician;
import org.drools.solver.examples.manners2009.domain.Job;
import org.drools.solver.examples.manners2009.domain.JobType;
import org.drools.solver.examples.manners2009.domain.Manners2009;
import org.drools.solver.examples.manners2009.domain.Seat;
import org.drools.solver.examples.manners2009.domain.Table;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class Manners2009InputConvertor extends AbstractInputConvertor {

    public static void main(String[] args) {
        new Manners2009InputConvertor().convert();
    }

    protected String getExampleDirName() {
        return "manners2009";
    }

    public Solution readSolution(BufferedReader bufferedReader) throws IOException {
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
        int tableListSize = readIntegerValue(bufferedReader, "Tables:");
        int seatsPerTable = readIntegerValue(bufferedReader, "SeatsPerTable:");
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
        readConstantLine(bufferedReader, "Num,Profession,SubProf,Gender,Spt1,Spt2,Spt3");
        readConstantLine(bufferedReader, "-------------------------------------------");
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

}