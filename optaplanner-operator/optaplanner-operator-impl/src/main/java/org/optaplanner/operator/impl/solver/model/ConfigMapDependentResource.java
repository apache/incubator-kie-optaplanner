package org.optaplanner.operator.impl.solver.model;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent
public final class ConfigMapDependentResource extends CRUDKubernetesDependentResource<ConfigMap, OptaPlannerSolver> {

    public static final String SOLVER_MESSAGE_INPUT_KEY = "solver.message.input";
    public static final String SOLVER_MESSAGE_OUTPUT_KEY = "solver.message.output";
    public static final String SOLVER_MESSAGE_AMQ_HOST_KEY = "solver.amq.host";
    public static final String SOLVER_MESSAGE_AMQ_PORT_KEY = "solver.amq.port";

    public ConfigMapDependentResource(KubernetesClient kubernetesClient) {
        super(ConfigMap.class);
        setKubernetesClient(kubernetesClient);
    }

    @Override
    protected ConfigMap desired(OptaPlannerSolver solver, Context<OptaPlannerSolver> context) {
        Map<String, String> data = new HashMap<>();
        data.put(SOLVER_MESSAGE_INPUT_KEY, solver.getInputMessageAddressName());
        data.put(SOLVER_MESSAGE_OUTPUT_KEY, solver.getOutputMessageAddressName());
        data.put(SOLVER_MESSAGE_AMQ_HOST_KEY, solver.getSpec().getAmqBroker().getHost());
        data.put(SOLVER_MESSAGE_AMQ_PORT_KEY, String.valueOf(solver.getSpec().getAmqBroker().getPort()));

        return new ConfigMapBuilder()
                .withNewMetadata()
                .withName(solver.getConfigMapName())
                .withNamespace(solver.getNamespace())
                .endMetadata()
                .withData(data)
                .build();
    }

    @Override
    public ConfigMap update(ConfigMap actual, ConfigMap target, OptaPlannerSolver solver, Context<OptaPlannerSolver> context) {
        ConfigMap resultingConfigMap = super.update(actual, target, solver, context);
        String namespace = actual.getMetadata().getNamespace();
        getKubernetesClient()
                .pods()
                .inNamespace(namespace)
                .withLabel("app", solver.getMetadata().getName())
                .delete();
        return resultingConfigMap;
    }
}
