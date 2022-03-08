/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.common.business;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.change.ProblemChange;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMoveSelector;
import org.optaplanner.core.impl.score.constraint.DefaultConstraintMatchTotal;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.DefaultSolverFactory;
import org.optaplanner.core.impl.solver.change.DefaultProblemChangeDirector;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.persistence.AbstractSolutionExporter;
import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.common.swingui.SolverAndPersistenceFrame;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class SolutionBusiness<Solution_, Score_ extends Score<Score_>> {

    public static String getBaseFileName(File file) {
        return getBaseFileName(file.getName());
    }

    public static String getBaseFileName(String name) {
        int indexOfLastDot = name.lastIndexOf('.');
        if (indexOfLastDot > 0) {
            return name.substring(0, indexOfLastDot);
        } else {
            return name;
        }
    }

    private static final ProblemFileComparator FILE_COMPARATOR = new ProblemFileComparator();

    private static final Logger LOGGER = LoggerFactory.getLogger(SolutionBusiness.class);

    private final CommonApp<Solution_> app;
    private File dataDir;
    private SolutionFileIO<Solution_> solutionFileIO;

    private Set<AbstractSolutionImporter<Solution_>> importers;
    private Set<AbstractSolutionExporter<Solution_>> exporters;

    private File importDataDir;
    private File unsolvedDataDir;
    private File solvedDataDir;
    private File exportDataDir;

    private final DefaultSolverFactory<Solution_> solverFactory;
    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final Solver<Solution_> solver;
    private final ScoreManager<Solution_, Score_> scoreManager;
    private String solutionFileName = null;

    private final AtomicReference<Solution_> workingSolutionRef = new AtomicReference<>();
    private final AtomicReference<Solution_> skipToBestSolutionRef = new AtomicReference<>();

    public SolutionBusiness(CommonApp<Solution_> app, SolverFactory<Solution_> solverFactory) {
        this.app = app;
        this.solverFactory = ((DefaultSolverFactory<Solution_>) solverFactory);
        this.solutionDescriptor = this.solverFactory.getSolutionDescriptor();
        this.solver = solverFactory.buildSolver();
        this.scoreManager = ScoreManager.create(solverFactory);
    }

    public String getAppName() {
        return app.getName();
    }

    public String getAppDescription() {
        return app.getDescription();
    }

    public String getAppIconResource() {
        return app.getIconResource();
    }

    public File getDataDir() {
        return dataDir;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public SolutionFileIO<Solution_> getSolutionFileIO() {
        return solutionFileIO;
    }

    public void setSolutionFileIO(SolutionFileIO<Solution_> solutionFileIO) {
        this.solutionFileIO = solutionFileIO;
    }

    public Set<AbstractSolutionImporter<Solution_>> getImporters() {
        return importers;
    }

    public void setImporters(Set<AbstractSolutionImporter<Solution_>> importers) {
        this.importers = importers;
    }

    public void setExporters(Set<AbstractSolutionExporter<Solution_>> exporters) {
        if (exporters == null) {
            throw new IllegalArgumentException("Passed exporters must not be null");
        }
        this.exporters = exporters;
    }

    public void addExporter(AbstractSolutionExporter<Solution_> exporter) {
        this.exporters.add(exporter);
    }

    public Set<AbstractSolutionExporter<Solution_>> getExporters() {
        return this.exporters;
    }

    public boolean hasImporter() {
        return !importers.isEmpty();
    }

    public boolean hasExporter() {
        return exporters != null && exporters.size() > 0;
    }

    public void updateDataDirs() {
        if (hasImporter()) {
            importDataDir = new File(dataDir, "import");
            if (!importDataDir.exists()) {
                throw new IllegalStateException("The directory importDataDir (" + importDataDir.getAbsolutePath()
                        + ") does not exist.");
            }
        }
        unsolvedDataDir = new File(dataDir, "unsolved");
        if (!unsolvedDataDir.exists()) {
            throw new IllegalStateException("The directory unsolvedDataDir (" + unsolvedDataDir.getAbsolutePath()
                    + ") does not exist.");
        }
        solvedDataDir = new File(dataDir, "solved");
        if (!solvedDataDir.exists() && !solvedDataDir.mkdir()) {
            throw new IllegalStateException("The directory solvedDataDir (" + solvedDataDir.getAbsolutePath()
                    + ") does not exist and could not be created.");
        }
        if (hasExporter()) {
            exportDataDir = new File(dataDir, "export");
            if (!exportDataDir.exists() && !exportDataDir.mkdir()) {
                throw new IllegalStateException("The directory exportDataDir (" + exportDataDir.getAbsolutePath()
                        + ") does not exist and could not be created.");
            }
        }
    }

    public File getImportDataDir() {
        return importDataDir;
    }

    public File getUnsolvedDataDir() {
        return unsolvedDataDir;
    }

    public File getSolvedDataDir() {
        return solvedDataDir;
    }

    public File getExportDataDir() {
        return exportDataDir;
    }

    public List<File> getUnsolvedFileList() {
        return getFileList(unsolvedDataDir, solutionFileIO.getInputFileExtension());
    }

    private static List<File> getFileList(File dataDir, String extension) {
        try (Stream<Path> paths = Files.walk(dataDir.toPath(), FileVisitOption.FOLLOW_LINKS)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("." + extension))
                    .map(Path::toFile)
                    .sorted(FILE_COMPARATOR)
                    .collect(toList());
        } catch (IOException e) {
            throw new IllegalStateException("Error while crawling data directory (" + dataDir + ").", e);
        }
    }

    public List<File> getSolvedFileList() {
        return getFileList(solvedDataDir, solutionFileIO.getOutputFileExtension());
    }

    public Solution_ getSolution() {
        return workingSolutionRef.get();
    }

    public void setSolution(Solution_ solution) {
        workingSolutionRef.set(solution);
    }

    public String getSolutionFileName() {
        return solutionFileName;
    }

    public void setSolutionFileName(String solutionFileName) {
        this.solutionFileName = solutionFileName;
    }

    public Score_ getScore() {
        return scoreManager.updateScore(getSolution());
    }

    public boolean isSolving() {
        return solver.isSolving();
    }

    public void registerForBestSolutionChanges(final SolverAndPersistenceFrame<Solution_> solverAndPersistenceFrame) {
        solver.addEventListener(event -> {
            // Called on the Solver thread, so not on the Swing Event thread
            /*
             * Avoid ConcurrentModificationException when there is an unprocessed ProblemFactChange
             * because the paint method uses the same problem facts instances as the Solver's workingSolution
             * unlike the planning entities of the bestSolution which are cloned from the Solver's workingSolution
             */
            if (solver.isEveryProblemChangeProcessed()) {
                // The final is also needed for thread visibility
                final Solution_ newBestSolution = event.getNewBestSolution();
                skipToBestSolutionRef.set(newBestSolution);
                SwingUtilities.invokeLater(() -> {
                    // Called on the Swing Event thread
                    Solution_ skipToBestSolution = skipToBestSolutionRef.get();
                    // Skip this event if a newer one arrived meanwhile to avoid flooding the Swing Event thread
                    if (newBestSolution != skipToBestSolution) {
                        return;
                    }
                    setSolution(newBestSolution);
                    solverAndPersistenceFrame.bestSolutionChanged();
                });
            }
        });
    }

    public boolean isConstraintMatchEnabled() {
        return withScoreDirector(scoreDirector -> {
            return scoreDirector.isConstraintMatchEnabled();
        });
    }

    private <Result_> Result_ withScoreDirector(Function<InnerScoreDirector<Solution_, Score_>, Result_> function) {
        try (InnerScoreDirector<Solution_, Score_> scoreDirector =
                (InnerScoreDirector<Solution_, Score_>) solverFactory.getScoreDirectorFactory().buildScoreDirector(false, true)) {
            scoreDirector.setWorkingSolution(getSolution());
            Result_ result = function.apply(scoreDirector);
            setSolution(scoreDirector.getWorkingSolution());
            return result;
        }
    }

    public List<ConstraintMatchTotal<Score_>> getConstraintMatchTotalList() {
        return scoreManager.explainScore(getSolution())
                .getConstraintMatchTotalMap()
                .values()
                .stream()
                .map(constraintMatchTotal -> (DefaultConstraintMatchTotal<Score_>) constraintMatchTotal)
                .sorted()
                .collect(toList());
    }

    public Map<Object, Indictment<Score_>> getIndictmentMap() {
        return scoreManager.explainScore(getSolution()).getIndictmentMap();
    }

    public void importSolution(File file) {
        AbstractSolutionImporter<Solution_> importer = determineImporter(file);
        Solution_ solution = importer.readSolution(file);
        solutionFileName = file.getName();
        setSolution(solution);
    }

    private AbstractSolutionImporter<Solution_> determineImporter(File file) {
        for (AbstractSolutionImporter<Solution_> importer : importers) {
            if (importer.acceptInputFile(file)) {
                return importer;
            }
        }
        return importers.stream()
                .findFirst()
                .orElseThrow();
    }

    public void openSolution(File file) {
        Solution_ solution = solutionFileIO.read(file);
        LOGGER.info("Opened: {}", file);
        solutionFileName = file.getName();
        workingSolutionRef.set(solution);
    }

    public void saveSolution(File file) {
        solutionFileIO.write(getSolution(), file);
        LOGGER.info("Saved: {}", file);
    }

    public void exportSolution(AbstractSolutionExporter<Solution_> exporter, File file) {
        exporter.writeSolution(getSolution(), file);
    }

    public void doMove(Move<Solution_> move) {
        withScoreDirector(scoreDirector -> {
            doMove(move, scoreDirector);
        });
    }

    private void doMove(Move<Solution_> move, InnerScoreDirector<Solution_, Score_> scoreDirector) {
        if (solver.isSolving()) {
            LOGGER.error("Not doing user move ({}) because the solver is solving.", move);
            return;
        }
        if (!move.isMoveDoable(scoreDirector)) {
            LOGGER.warn("Not doing user move ({}) because it is not doable.", move);
            return;
        }
        LOGGER.info("Doing user move ({}).", move);
        move.doMoveOnly(scoreDirector);
        scoreDirector.calculateScore();
    }

    private void withScoreDirector(Consumer<InnerScoreDirector<Solution_, Score_>> consumer) {
        withScoreDirector(s -> {
            consumer.accept(s);
            return null;
        });
    }

    public void doProblemChange(ProblemChange<Solution_> problemChange) {
        if (solver.isSolving()) {
            solver.addProblemChange(problemChange);
        } else {
            withScoreDirector(scoreDirector -> {
                DefaultProblemChangeDirector<Solution_> problemChangeDirector =
                        new DefaultProblemChangeDirector<>(scoreDirector);
                problemChangeDirector.doProblemChange(problemChange);
            });
        }
    }

    /**
     * Can be called on any thread.
     * <p>
     * Note: This method does not change the guiScoreDirector because that can only be changed on the event thread.
     *
     * @param problem never null
     * @return never null
     */
    public Solution_ solve(Solution_ problem) {
        return solver.solve(problem);
    }

    public void terminateSolvingEarly() {
        solver.terminateEarly();
    }

    public GenuineVariableDescriptor<Solution_> findVariableDescriptor(Object entity, String variableName) {
        return solutionDescriptor.findGenuineVariableDescriptorOrFail(entity, variableName);
    }

    private ChangeMove<Solution_> createChangeMove(Object entity, String variableName, Object toPlanningValue) {
        // TODO Solver should support building a ChangeMove
        EntityDescriptor<Solution_> entityDescriptor = solutionDescriptor.findEntityDescriptorOrFail(entity.getClass());
        GenuineVariableDescriptor<Solution_> variableDescriptor = findVariableDescriptor(entity, variableName);
        ChangeMoveSelector<Solution_> changeMoveSelector = new ChangeMoveSelector<>(
                new SingleEntitySelector<>(entityDescriptor, entity),
                new SingleValueSelector<>(variableDescriptor, toPlanningValue), false);
        return (ChangeMove<Solution_>) changeMoveSelector.iterator().next();
    }

    public void doChangeMove(Object entity, String variableName, Object toPlanningValue) {
        ChangeMove<Solution_> move = createChangeMove(entity, variableName, toPlanningValue);
        doMove(move);
    }

}
