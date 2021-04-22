/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.quarkus.constraints;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.quarkus.deployment.config.OptaPlannerBuildTimeConfig;
import org.optaplanner.quarkus.testdata.normal.constraints.TestdataQuarkusConstraintProvider;
import org.optaplanner.quarkus.testdata.normal.domain.TestdataQuarkusEntity;
import org.optaplanner.quarkus.testdata.normal.domain.TestdataQuarkusSolution;

import io.quarkus.bootstrap.model.AppArtifactCoords;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContextConfig;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;
import io.quarkus.bootstrap.resolver.maven.workspace.LocalProject;
import io.quarkus.bootstrap.resolver.maven.workspace.LocalWorkspace;
import io.quarkus.bootstrap.resolver.maven.workspace.ModelUtils;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.PathTestHelper;

public class OptaPlannerProcessorConstraintProviderAndDrlTest {

    private static final String CONSTRAINTS_DRL = "customConstraints.drl";

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setBeforeAllCustomizer(new Runnable() {
                @Override
                public void run() {
                    System.out.println("BEFORE ALL CUSTOMIZER");
                    final Path testLocation =
                            PathTestHelper.getTestClassesLocation(OptaPlannerProcessorConstraintProviderAndDrlTest.class);
                    System.out.println("  test location: " + testLocation + " " + Files.isDirectory(testLocation));

                    final BootstrapMavenContextConfig<?> config = BootstrapMavenContext.config();

                    try {
                        final BootstrapMavenContext mvnCtx = new BootstrapMavenContext(config);
                        final LocalProject currentProject = mvnCtx.getCurrentProject();

                        System.out.println("  current project " + currentProject);

                        System.out.println("  project pom " + currentProject.getRawModel().getPomFile() + " "
                                + currentProject.getRawModel().getPomFile().exists());
                        System.out.println("  outputDir " + currentProject.getOutputDir() + " "
                                + Files.exists(currentProject.getOutputDir()));
                        System.out.println("  classesDir " + currentProject.getClassesDir() + " "
                                + Files.exists(currentProject.getClassesDir()));

                        final LocalWorkspace ws = mvnCtx.getWorkspace();

                        final DefaultRepositorySystemSession session =
                                new DefaultRepositorySystemSession(mvnCtx.getRepositorySystemSession());
                        session.setWorkspaceReader(new WorkspaceReader() {

                            @Override
                            public WorkspaceRepository getRepository() {
                                return ws.getRepository();
                            }

                            @Override
                            public File findArtifact(Artifact artifact) {
                                final boolean log = artifact.getArtifactId().equals(currentProject.getArtifactId());
                                if (log) {
                                    System.out.println("LocalWorkspace.findArtifact " + artifact);
                                }

                                final LocalProject lp = ws.getProject(artifact.getGroupId(), artifact.getArtifactId());
                                final String findVersion = artifact.getVersion();
                                if (lp == null
                                        || !findVersion.isEmpty()
                                                && !lp.getVersion().equals(findVersion)
                                                && !(ModelUtils.isUnresolvedVersion(findVersion)
                                                        && lp.getVersion().equals(ws.getResolvedVersion()))) {
                                    return null;
                                }
                                if (!Objects.equals(artifact.getClassifier(), lp.getAppArtifact().getClassifier())) {
                                    if ("tests".equals(artifact.getClassifier())) {
                                        //special classifier used for test jars
                                        final Path path = lp.getTestClassesDir();
                                        if (Files.exists(path)) {
                                            return path.toFile();
                                        }
                                    }
                                    return null;
                                }
                                final String type = artifact.getExtension();
                                if (type.equals(AppArtifactCoords.TYPE_JAR)) {
                                    Path path = lp.getClassesDir();
                                    if (Files.exists(path)) {
                                        return path.toFile();
                                    }

                                    // it could be a project with no sources/resources, in which case Maven will create an empty JAR
                                    // if it has previously been packaged we can return it
                                    path = lp.getOutputDir().resolve(LocalWorkspace.getFileName(artifact));
                                    if (Files.exists(path)) {
                                        return path.toFile();
                                    }

                                    path = emptyJarOutput(lp, artifact);
                                    if (path != null) {
                                        return path.toFile();
                                    }

                                    // otherwise, this project hasn't been built yet
                                } else if (type.equals(AppArtifactCoords.TYPE_POM)) {
                                    final File pom = lp.getRawModel().getPomFile();
                                    System.out.println("  current project " + lp);
                                    System.out.println("  trying " + pom + " " + pom.exists());
                                    System.out.println(
                                            "  outputDir: " + lp.getOutputDir() + " " + Files.exists(lp.getOutputDir()));
                                    System.out.println(
                                            "  classesDir: " + lp.getClassesDir() + " " + Files.exists(lp.getClassesDir()));
                                    // if the pom exists we should also check whether the main artifact can also be resolved from the workspace
                                    if (pom.exists() && ("pom".equals(lp.getRawModel().getPackaging())
                                            || Files.exists(lp.getOutputDir())
                                            || emptyJarOutput(lp, artifact) != null)) {
                                        return pom;
                                    }
                                } else {
                                    // check whether the artifact exists in the project's output dir
                                    final Path path = lp.getOutputDir().resolve(LocalWorkspace.getFileName(artifact));
                                    if (Files.exists(path)) {
                                        return path.toFile();
                                    }
                                }
                                return null;
                            }

                            private Path emptyJarOutput(LocalProject lp, Artifact artifact) {
                                // If the project has neither sources nor resources directories then it is an empty JAR.
                                // If this method returns null then the Maven resolver will attempt to resolve the artifact from a repository
                                // which may fail if the artifact hasn't been installed yet.
                                // Here we are checking whether the artifact exists in the local repo first (Quarkus CI creates a Maven repo cache
                                // first and then runs tests using '-pl' in the clean project). If the artifact exists in the local repo we return null,
                                // so the Maven resolver will succeed resolving it from the repo.
                                // If the artifact does not exist in the local repo, we are creating an empty classes directory in the target directory.
                                if (!Files.exists(lp.getSourcesSourcesDir())
                                        && !Files.exists(lp.getResourcesSourcesDir())
                                        && !isFoundInLocalRepo(artifact)) {
                                    try {
                                        final Path classesDir = lp.getClassesDir();
                                        Files.createDirectories(classesDir);
                                        return classesDir;
                                    } catch (IOException e) {
                                        // ignore and return null
                                    }
                                }
                                return null;
                            }

                            private boolean isFoundInLocalRepo(Artifact artifact) {
                                String localRepo;
                                try {
                                    localRepo = mvnCtx.getLocalRepo();
                                } catch (BootstrapMavenException e) {
                                    throw new IllegalStateException(e);
                                }
                                if (localRepo == null) {
                                    return false;
                                }
                                Path p = Paths.get(localRepo);
                                for (String s : artifact.getGroupId().split("\\.")) {
                                    p = p.resolve(s);
                                }
                                p = p.resolve(artifact.getArtifactId());
                                p = p.resolve(artifact.getVersion());
                                p = p.resolve(LocalWorkspace.getFileName(artifact));
                                return Files.exists(p);
                            }

                            @Override
                            public List<String> findVersions(Artifact artifact) {
                                return ws.findVersions(artifact);
                            }
                        });

                        final MavenArtifactResolver resolver = MavenArtifactResolver.builder()
                                .setRepositorySystem(mvnCtx.getRepositorySystem())
                                .setRepositorySystemSession(session)
                                .setRemoteRepositories(mvnCtx.getRemoteRepositories())
                                .setRemoteRepositoryManager(mvnCtx.getRemoteRepositoryManager())
                                .setCurrentProject(currentProject)
                                .build();
                        System.out
                                .println("  " + resolver
                                        .resolve(new DefaultArtifact(currentProject.getGroupId(),
                                                currentProject.getArtifactId(), "jar", currentProject.getVersion()))
                                        .getArtifact().getFile());
                    } catch (BootstrapMavenException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            })
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class,
                            TestdataQuarkusConstraintProvider.class)
                    .addAsResource("org/optaplanner/quarkus/constraints/defaultConstraints.drl", CONSTRAINTS_DRL))
            .overrideConfigKey(OptaPlannerBuildTimeConfig.CONSTRAINTS_DRL_PROPERTY, CONSTRAINTS_DRL);

    @Inject
    SolverConfig solverConfig;

    @Inject
    SolverFactory<TestdataQuarkusSolution> solverFactory;

    @Test
    public void constraintsDrlInConflictWithConstraintProvider() {
        assertEquals(Collections.singletonList(CONSTRAINTS_DRL),
                solverConfig.getScoreDirectorFactoryConfig().getScoreDrlList());
        String errorMessage = "The scoreDirectorFactory cannot have a constraintProviderClass ("
                + TestdataQuarkusConstraintProvider.class.getName() + ") and a droolsScoreDirectorFactory ("
                + CONSTRAINTS_DRL + ")";
        assertThrows(IllegalArgumentException.class, () -> solverFactory.buildSolver(), errorMessage);
    }
}
