/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.operator.impl.solver;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaplanner.operator.impl.solver.model.AmqBroker;
import org.optaplanner.operator.impl.solver.model.ConfigMapDependentResource;
import org.optaplanner.operator.impl.solver.model.OptaPlannerSolver;
import org.optaplanner.operator.impl.solver.model.OptaPlannerSolverSpec;
import org.optaplanner.operator.impl.solver.model.OptaPlannerSolverStatus;
import org.optaplanner.operator.impl.solver.model.Scaling;
import org.optaplanner.operator.impl.solver.model.keda.ScaledObject;
import org.optaplanner.operator.impl.solver.model.keda.ScaledObjectDependentResource;
import org.optaplanner.operator.impl.solver.model.keda.SecretTargetRef;
import org.optaplanner.operator.impl.solver.model.keda.Trigger;
import org.optaplanner.operator.impl.solver.model.keda.TriggerAuthentication;
import org.optaplanner.operator.impl.solver.model.keda.TriggerAuthenticationDependentResource;

import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.SecretKeySelector;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class OptaPlannerSolverReconcilerTest extends AbstractKubernetesTest {

    @Inject
    private Operator operator;

    // TODO: Replace with @BeforeEach after https://github.com/quarkiverse/quarkus-operator-sdk/issues/388 is resolved.
    public void onStart(@Observes StartupEvent startupEvent) {
        operator.start();
    }

    private String namespace;

    @BeforeEach
    public void createNamespace() {
        namespace = "test-" + UUID.randomUUID();
    }

    @Test
    void createMandatoryDependentResources() {
        final OptaPlannerSolver solver = new OptaPlannerSolver();
        final String solverName = "test-solver";

        AmqBroker amqBroker = createAmqBroker();

        solver.getMetadata().setName(solverName);
        solver.getMetadata().setNamespace(namespace);
        solver.setSpec(new OptaPlannerSolverSpec());
        solver.getSpec().setTemplate(createPodTemplateSpec("solver-project-image"));
        solver.getSpec().setAmqBroker(amqBroker);
        solver.getSpec().setScaling(new Scaling());
        getClient().resources(OptaPlannerSolver.class).create(solver);

        final String expectedMessageAddressIn = solver.getInputMessageAddressName();
        final String expectedMessageAddressOut = solver.getOutputMessageAddressName();

        await().ignoreException(NullPointerException.class).atMost(1, MINUTES).untilAsserted(() -> {
            OptaPlannerSolver updatedSolver = getClient()
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
        ConfigMap configMap = getClient()
                .resources(ConfigMap.class)
                .inNamespace(solver.getMetadata().getNamespace())
                .withName(solver.getConfigMapName())
                .get();
        Map<String, String> configMapData = configMap.getData();
        assertThat(configMapData.get(ConfigMapDependentResource.SOLVER_MESSAGE_INPUT_KEY))
                .isEqualTo(expectedMessageAddressIn);
        assertThat(configMapData.get(ConfigMapDependentResource.SOLVER_MESSAGE_OUTPUT_KEY))
                .isEqualTo(expectedMessageAddressOut);

        List<Deployment> deployments = getClient()
                .resources(Deployment.class)
                .inNamespace(solver.getMetadata().getNamespace())
                .list()
                .getItems();
        assertThat(deployments).hasSize(1);
        assertThat(deployments.get(0).getMetadata().getName()).isEqualTo("test-solver");
    }

    @Test
    void dynamicScaling_configuresKeda() {
        final String solverName = "test-solver";
        final AmqBroker amqBroker = createAmqBroker();
        final int maxReplicas = 5;

        final OptaPlannerSolver solver = new OptaPlannerSolver();
        solver.getMetadata().setName(solverName);
        solver.getMetadata().setNamespace(namespace);
        solver.setSpec(new OptaPlannerSolverSpec());
        solver.getSpec().setTemplate(createPodTemplateSpec("solver-project-image"));
        solver.getSpec().setAmqBroker(amqBroker);
        solver.getSpec().setScaling(new Scaling(true, maxReplicas));

        getClient().resources(OptaPlannerSolver.class).create(solver);
        await().ignoreException(NullPointerException.class).atMost(1, MINUTES).untilAsserted(() -> {
            OptaPlannerSolver updatedSolver = getClient()
                    .resources(OptaPlannerSolver.class)
                    .inNamespace(solver.getMetadata().getNamespace())
                    .withName(solver.getMetadata().getName())
                    .get();
            assertThat(updatedSolver.getStatus().getConditions().get(0).getStatus())
                    .isNotEqualTo(OptaPlannerSolverStatus.ConditionStatus.UNKNOWN.getName());
        });

        ScaledObject scaledObject = getClient()
                .resources(ScaledObject.class)
                .inNamespace(solver.getMetadata().getNamespace())
                .withName(solverName)
                .get();

        TriggerAuthentication triggerAuthentication = getClient()
                .resources(TriggerAuthentication.class)
                .inNamespace(solver.getMetadata().getNamespace())
                .withName(solverName)
                .get();

        assertThat(scaledObject.getSpec().getScaleTargetRef().getName()).isEqualTo(solverName);
        assertThat(scaledObject.getSpec().getTriggers()).hasSize(1);
        Trigger trigger = scaledObject.getSpec().getTriggers().get(0);
        assertThat(trigger.getType()).isEqualTo(ScaledObjectDependentResource.ARTEMIS_QUEUE_TRIGGER);
        assertThat(trigger.getMetadata().getQueueName()).isEqualTo(solver.getInputMessageAddressName());
        assertThat(trigger.getAuthenticationRef().getName()).isEqualTo(triggerAuthentication.getMetadata().getName());

        List<SecretTargetRef> secretTargetRefs = triggerAuthentication.getSpec().getSecretTargetRefs();
        assertThat(secretTargetRefs).hasSize(2);
        assertThat(secretTargetRefs).allSatisfy(secretTargetRef -> {
            if (TriggerAuthenticationDependentResource.PARAM_USERNAME.equals(secretTargetRef.getParameter())) {
                assertSecretTargetRefFromSecretKeySelector(secretTargetRef, amqBroker.getUsernameSecretRef());
            } else {
                assertSecretTargetRefFromSecretKeySelector(secretTargetRef, amqBroker.getPasswordSecretRef());
            }
        });
    }

    @Test
    void incorrectSolverResource_FailedCondition() {
        final OptaPlannerSolver solver = new OptaPlannerSolver();
        final String solverName = "incorrect-solver";
        solver.getMetadata().setName(solverName);
        solver.getMetadata().setNamespace(namespace);
        solver.setSpec(new OptaPlannerSolverSpec()); // Empty spec.
        getClient().resources(OptaPlannerSolver.class).create(solver);

        await().ignoreException(NullPointerException.class).atMost(1, MINUTES).untilAsserted(() -> {
            OptaPlannerSolver updatedSolver = getClient()
                    .resources(OptaPlannerSolver.class)
                    .inNamespace(solver.getMetadata().getNamespace())
                    .withName(solver.getMetadata().getName())
                    .get();

            assertThat(updatedSolver.getStatus().getConditions()).isNotEmpty();
            Condition condition = updatedSolver.getStatus().getConditions().get(0);
            assertStatusCondition(condition, updatedSolver, OptaPlannerSolverStatus.ConditionStatus.FALSE);
        });
    }

    @Test
    void mergePodTemplate() {
        final OptaPlannerSolver solver = new OptaPlannerSolver();
        final String solverName = "test-solver-merge-environment";

        solver.getMetadata().setName(solverName);
        solver.getMetadata().setNamespace(namespace);
        solver.setSpec(new OptaPlannerSolverSpec());
        solver.getSpec().setAmqBroker(createAmqBroker());

        EnvVar existingEnvVar = new EnvVarBuilder()
                .withName("test-var")
                .withValue("test-var-value")
                .build();

        PodTemplateSpec podTemplateSpec = new PodTemplateSpecBuilder()
                .withNewMetadata()
                .withName("test-pod-template")
                .addToLabels("test-label", "test-label-value")
                .endMetadata()
                .withNewSpec()
                .withContainers(new ContainerBuilder()
                        .withImage("solver-project-image")
                        .withEnv(existingEnvVar)
                        .withNewResources()
                        .addToRequests("cpu", Quantity.parse("1"))
                        .endResources()
                        .build())
                .endSpec()
                .build();

        solver.getSpec().setTemplate(podTemplateSpec);

        getClient().resources(OptaPlannerSolver.class).create(solver);

        await().ignoreException(NullPointerException.class).atMost(1, MINUTES).untilAsserted(() -> {
            OptaPlannerSolver updatedSolver = getClient()
                    .resources(OptaPlannerSolver.class)
                    .inNamespace(solver.getMetadata().getNamespace())
                    .withName(solver.getMetadata().getName())
                    .get();
            assertThat(updatedSolver.getStatus().getConditions().get(0).getStatus())
                    .isNotEqualTo(OptaPlannerSolverStatus.ConditionStatus.UNKNOWN.getName());
        });

        List<Deployment> deployments = getClient()
                .resources(Deployment.class)
                .inNamespace(solver.getMetadata().getNamespace())
                .list()
                .getItems();
        assertThat(deployments).hasSize(1);
        PodTemplateSpec resolvedPodTemplateSpec = deployments.get(0).getSpec().getTemplate();
        assertThat(resolvedPodTemplateSpec.getMetadata().getName()).isEqualTo(podTemplateSpec.getMetadata().getName());
        assertThat(resolvedPodTemplateSpec.getMetadata().getLabels()).containsOnlyKeys("app", "test-label");
        assertThat(resolvedPodTemplateSpec.getSpec().getContainers()).hasSize(1);
        assertThat(resolvedPodTemplateSpec.getSpec().getContainers().get(0).getEnv())
                .hasSizeGreaterThan(1)
                .contains(existingEnvVar);
        assertThat(resolvedPodTemplateSpec.getSpec().getContainers().get(0).getResources().getRequests())
                .containsOnlyKeys("cpu");
    }

    private KubernetesClient getClient() {
        return getMockServer().getClient().inNamespace(namespace);
    }

    private void assertSecretTargetRefFromSecretKeySelector(SecretTargetRef secretTargetRef,
            SecretKeySelector secretKeySelector) {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(secretTargetRef.getName()).isEqualTo(secretKeySelector.getName());
            softly.assertThat(secretTargetRef.getKey()).isEqualTo(secretKeySelector.getKey());
        });
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

    private AmqBroker createAmqBroker() {
        AmqBroker amqBroker = new AmqBroker();
        amqBroker.setHost("amq-host");
        amqBroker.setPort(5678);
        amqBroker.setUsernameSecretRef(new SecretKeySelector("amq-username", "my-secret", false));
        amqBroker.setPasswordSecretRef(new SecretKeySelector("amq-password", "my-secret", false));

        return amqBroker;
    }

    private PodTemplateSpec createPodTemplateSpec(String imageName) {
        return new PodTemplateSpecBuilder()
                .withNewSpec()
                .withContainers(new ContainerBuilder()
                        .withImage(imageName)
                        .build())
                .endSpec()
                .build();
    }
}
