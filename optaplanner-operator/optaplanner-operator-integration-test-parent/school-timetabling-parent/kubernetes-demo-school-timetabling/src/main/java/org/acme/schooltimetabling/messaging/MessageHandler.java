package org.acme.schooltimetabling.messaging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Queue;

import org.acme.common.domain.TimeTable;
import org.acme.common.message.SolverRequest;
import org.acme.common.message.SolverResponse;
import org.acme.common.persistence.TimeTableRepository;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class MessageHandler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);
    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();
    @Inject
    TimeTableRepository repository;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    ConnectionFactory connectionFactory;
    private Solver<TimeTable> solver;

    @Inject
    public MessageHandler(SolverFactory<TimeTable> solverFactory) {
        solver = solverFactory.buildSolver();
    }

    void onStart(@Observes StartupEvent event) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent event) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        while (true) {
            solve();
        }
    }

    public void solve() {
        Long problemId = null;
        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED);
                JMSConsumer jmsConsumer = context.createConsumer(context.createQueue("school-timetabling-problem"))) {
            Queue solutionQueue = context.createQueue("school-timetabling-solution");
            JMSProducer jmsProducer = context.createProducer();
            Message solverRequestMessage = jmsConsumer.receive(1_000L);
            if (solverRequestMessage == null) {
                return;
            }
            try {
                SolverRequest solverRequest =
                        objectMapper.readValue(solverRequestMessage.getBody(String.class), SolverRequest.class);
                problemId = solverRequest.getProblemId();
            } catch (JsonProcessingException jsonProcessingException) { // Bad request.
                LOGGER.error("Unable to deserialize a solver request (" + solverRequestMessage + ").",
                        jsonProcessingException);
                context.rollback();
                return;
            }

            TimeTable problem = repository.load(problemId);
            TimeTable solution;

            LOGGER.debug("Started solving an input problem (" + problemId + ").");
            try {
                solution = solver.solve(problem);
            } catch (Exception solverException) { // Exception during solving will most likely require code change.
                LOGGER.error("Solving an input problem (" + problemId + ") has failed.", solverException);
                String errorMessage = createReplyFailure(problemId, solverException);
                jmsProducer.send(solutionQueue, errorMessage);
                context.commit();
                return;
            }

            repository.save(problemId, solution);
            LOGGER.debug("Solution saved for an input problem (" + problemId + ")");
            String solvingFinishedMessage = createReplySuccess(problemId);
            jmsProducer.send(solutionQueue, solvingFinishedMessage);
            context.commit(); // The output message is sent out and the input message removed from a queue.
        } catch (Exception ex) {
            throw new IllegalStateException("Exception during processing an input problem (" + problemId + ").", ex);
        }
    }

    private String createReplySuccess(Long problemId) {
        try {
            return objectMapper.writeValueAsString(new SolverResponse(problemId));
        } catch (JsonProcessingException serializationException) {
            throw new IllegalStateException("Unable to serialize a success response to the input problem ("
                    + problemId + ") to JSON.", serializationException);
        }
    }

    private String createReplyFailure(Long problemId, Throwable throwable) {
        SolverResponse solverResponse =
                new SolverResponse(problemId,
                        new SolverResponse.ErrorInfo(throwable.getClass().getName(), throwable.getMessage()));
        try {
            return objectMapper.writeValueAsString(solverResponse);
        } catch (JsonProcessingException serializationException) {
            throw new IllegalStateException("Unable to serialize a failure response to the input problem ("
                    + problemId + ") to JSON.", serializationException);
        }
    }
}