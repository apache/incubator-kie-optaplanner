package org.opaplanner.operator.integration;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaplanner.operator.impl.solver.model.ConfigMapDependentResource;
import org.optaplanner.operator.impl.solver.model.OptaPlannerSolver;
import org.optaplanner.operator.impl.solver.model.OptaPlannerSolverStatus;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

public class OptaPlannerSolverReconcilerIT {

    public static final String ARTEMIS_BROKER_YAML = "artemis-broker.yaml";
    public static final String SCHOOL_TIMETABLING_SOLVER_YML = "school-timetabling-solver.yml";
    private final static KubernetesClient kubernetesClient = new DefaultKubernetesClient();
    private static String solverName;
    private static Namespace testNamespace;

    @AfterAll
    public static void tearDown() {
        kubernetesClient.close();
    }

    @AfterEach
    public void remove() {
        kubernetesClient.namespaces().delete(testNamespace);
    }

    @BeforeEach
    public void createNamespace() {
        solverName = "school-timetabling-" + RandomStringUtils.randomNumeric(4);
        testNamespace = kubernetesClient.namespaces()
                .create(new NamespaceBuilder().withNewMetadata().withName(solverName).endMetadata().build());
    }

    @Test
    public void optaPlannerSolverReconcilerIT() {

        createCrAmqBroker(solverName);

        OptaPlannerSolver solver = new Yaml(new Constructor(OptaPlannerSolver.class))
                .load(OptaPlannerSolverReconcilerIT.class.getClassLoader()
                        .getResourceAsStream(SCHOOL_TIMETABLING_SOLVER_YML));
        solver.getSpec().getAmqBroker().setHost("ex-aao-amqp-0-svc." + solverName + ".svc.cluster.local");
        solver.getSpec().getAmqBroker().setManagementHost("ex-aao-hdls-svc." + solverName + ".svc.cluster.local");
        solver.getMetadata().setName(solverName);
        solver.getMetadata().setNamespace(solverName);
        kubernetesClient.resources(OptaPlannerSolver.class).inNamespace(solverName).create(solver);

        final String expectedMessageAddressIn = solver.getInputMessageAddressName();
        final String expectedMessageAddressOut = solver.getOutputMessageAddressName();

        await().ignoreException(NullPointerException.class).atMost(1, MINUTES).untilAsserted(() -> {
            OptaPlannerSolver updatedSolver = kubernetesClient
                    .resources(OptaPlannerSolver.class)
                    .inNamespace(solver.getMetadata().getNamespace())
                    .withName(solver.getMetadata().getName())
                    .get();

            assertThat(updatedSolver.getStatus()).isNotNull();
            assertThat(updatedSolver.getStatus().getConditions()).isNotEmpty();
            Condition condition = updatedSolver.getStatus().getConditions().get(0);
            assertStatusCondition(condition, updatedSolver, OptaPlannerSolverStatus.ConditionStatus.TRUE);

            assertThat(updatedSolver.getStatus().getInputMessageAddress())
                    .isEqualTo(expectedMessageAddressIn);
            assertThat(updatedSolver.getStatus().getOutputMessageAddress())
                    .isEqualTo(expectedMessageAddressOut);
        });

        ConfigMap configMap = kubernetesClient
                .resources(ConfigMap.class)
                .inNamespace(solver.getMetadata().getNamespace())
                .withName(solver.getConfigMapName())
                .get();
        Map<String, String> configMapData = configMap.getData();
        assertThat(configMapData.get(ConfigMapDependentResource.SOLVER_MESSAGE_INPUT_KEY))
                .isEqualTo(expectedMessageAddressIn);
        assertThat(configMapData.get(ConfigMapDependentResource.SOLVER_MESSAGE_OUTPUT_KEY))
                .isEqualTo(expectedMessageAddressOut);

        List<Deployment> deployments = kubernetesClient
                .resources(Deployment.class)
                .inNamespace(solver.getMetadata().getNamespace())
                .list()
                .getItems();
        assertThat(deployments).hasSize(1);
        assertThat(deployments.get(0).getMetadata().getName()).isEqualTo(solverName);
    }

    private void createCrAmqBroker(String namespace) {

        CustomResourceDefinition brokerCrd = kubernetesClient.apiextensions().v1()
                .customResourceDefinitions()
                .withName("activemqartemises.broker.amq.io")
                .get();

        CustomResourceDefinitionContext brokerContextFromCrd = CustomResourceDefinitionContext.fromCrd(brokerCrd);

        GenericKubernetesResource genericKubernetesResource = kubernetesClient
                .genericKubernetesResources(brokerContextFromCrd)
                .load(OptaPlannerSolverReconcilerIT.class.getClassLoader().getResourceAsStream(ARTEMIS_BROKER_YAML))
                .get();
        kubernetesClient.genericKubernetesResources(brokerContextFromCrd).inNamespace(namespace)
                .resource(genericKubernetesResource).create();
    }

    private void assertStatusCondition(Condition condition, OptaPlannerSolver optaPlannerSolver,
            OptaPlannerSolverStatus.ConditionStatus expectedStatus) {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.getType()).isEqualTo(OptaPlannerSolverStatus.CONDITION_TYPE_READY);
            softly.assertThat(condition.getStatus()).isEqualTo(expectedStatus.getName());
            softly.assertThat(condition.getLastTransitionTime()).isNotEmpty();
            softly.assertThat(condition.getObservedGeneration())
                    .isEqualTo(optaPlannerSolver.getMetadata().getGeneration());
        });
    }
}
