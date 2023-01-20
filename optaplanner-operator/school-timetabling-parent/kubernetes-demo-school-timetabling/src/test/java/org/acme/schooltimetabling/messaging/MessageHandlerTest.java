package org.acme.schooltimetabling.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Session;

import org.acme.common.domain.Lesson;
import org.acme.common.domain.Room;
import org.acme.common.domain.TimeTable;
import org.acme.common.domain.Timeslot;
import org.acme.common.message.SolverRequest;
import org.acme.common.message.SolverResponse;
import org.acme.common.persistence.TimeTableRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ActiveMQEmbeddedBrokerLifecycleManager.class)
public class MessageHandlerTest {

    private static final String SOLVER_REQUEST_QUEUE = "school-timetabling-problem";
    private static final String SOLVER_RESPONSE_QUEUE = "school-timetabling-solution";

    private static final long TEST_TIMEOUT_SECONDS = 60L;

    private static final int MESSAGE_RECEIVE_TIMEOUT_SECONDS = 10;

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    TimeTableRepository repository;

    @Inject
    ObjectMapper objectMapper;

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void solve() {
        long problemId = 1L;
        TimeTable unsolvedTimeTable = createTimetable(problemId);
        repository.persist(unsolvedTimeTable);

        sendSolverRequest(new SolverRequest(problemId));
        SolverResponse solverResponse = receiveSolverResponse(MESSAGE_RECEIVE_TIMEOUT_SECONDS);

        assertThat(solverResponse.getResponseStatus()).isEqualTo(SolverResponse.ResponseStatus.SUCCESS);
        assertThat(solverResponse.getProblemId()).isEqualTo(problemId);

        TimeTable timeTable = repository.load(solverResponse.getProblemId());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(timeTable.getLessonList()).hasSameSizeAs(unsolvedTimeTable.getLessonList());
            timeTable.getLessonList().forEach(lesson -> {
                softly.assertThat(lesson.getRoom()).isNotNull();
                softly.assertThat(lesson.getTimeslot()).isNotNull();
            });
        });
    }

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void incorrectSolutionClass_respondsWithError() {
        long problemId = 10L;
        TimeTableRepository timeTableRepositoryMock = Mockito.mock(TimeTableRepository.class);
        // OptaPlanner detects the mock is not the class nor subclass of the PlanningSolution.
        when(timeTableRepositoryMock.load(eq(problemId))).thenReturn(mock(TimeTable.class));
        QuarkusMock.installMockForInstance(timeTableRepositoryMock, repository);

        sendSolverRequest(new SolverRequest(problemId));

        SolverResponse solverResponse = receiveSolverResponse(MESSAGE_RECEIVE_TIMEOUT_SECONDS);
        assertThat(solverResponse.getResponseStatus()).isEqualTo(SolverResponse.ResponseStatus.FAILURE);
        assertThat(solverResponse.getProblemId()).isEqualTo(problemId);
        assertThat(solverResponse.getErrorInfo().getExceptionClassName()).isEqualTo(IllegalArgumentException.class.getName());
        assertThat(solverResponse.getErrorInfo().getExceptionMessage()).contains("not a known subclass of the solution class");
    }

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void badRequest_DLQ() {
        final String wrongMessage = "Bad request!";
        sendMessage(SOLVER_REQUEST_QUEUE, wrongMessage);
        String messageFromDLQ = receiveMessage("DLQ", 10);
        assertThat(messageFromDLQ).isEqualTo(wrongMessage);
    }

    private SolverResponse receiveSolverResponse(int timeoutSeconds) {
        String solverResponsePayload = receiveMessage(SOLVER_RESPONSE_QUEUE, timeoutSeconds);
        try {
            return objectMapper.readValue(solverResponsePayload, SolverResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String receiveMessage(String queueName, int timeoutSeconds) {
        try (JMSContext context = connectionFactory.createContext("admin", "admin", Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue(queueName));
            Message message = consumer.receive(timeoutSeconds * 1_000);
            return message.getBody(String.class);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendSolverRequest(SolverRequest solverRequest) {
        try {
            String message = objectMapper.writeValueAsString(solverRequest);
            sendMessage(SOLVER_REQUEST_QUEUE, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(String queueName, String message) {
        try (JMSContext context = connectionFactory.createContext("quarkus", "quarkus", Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            Destination solverRequestQueue = context.createQueue(queueName);
            producer.send(solverRequestQueue, message);
        }
    }

    private TimeTable createTimetable(long problemId) {
        Lesson english = new Lesson(problemId, "English", "I. Jones", "9th grade");
        Lesson math = new Lesson(problemId, "Math", "A. Turing", "10th grade");

        Timeslot mondayMorning = new Timeslot(problemId, DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30));
        Timeslot mondayNoon = new Timeslot(problemId, DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.NOON.plusHours(1L));

        Room roomA = new Room("Room A");
        roomA.setProblemId(problemId);
        Room roomB = new Room("Room B");
        roomB.setProblemId(problemId);

        return new TimeTable(List.of(mondayMorning, mondayNoon), List.of(roomA, roomB), List.of(english, math));
    }
}
