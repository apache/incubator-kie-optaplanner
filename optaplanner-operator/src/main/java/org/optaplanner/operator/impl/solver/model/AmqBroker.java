package org.optaplanner.operator.impl.solver.model;

import io.fabric8.kubernetes.api.model.SecretKeySelector;

public class AmqBroker {
    private String host;
    private int port;

    private SecretKeySelector usernameSecretRef;

    private SecretKeySelector passwordSecretRef;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SecretKeySelector getUsernameSecretRef() {
        return usernameSecretRef;
    }

    public void setUsernameSecretRef(SecretKeySelector usernameSecretRef) {
        this.usernameSecretRef = usernameSecretRef;
    }

    public SecretKeySelector getPasswordSecretRef() {
        return passwordSecretRef;
    }

    public void setPasswordSecretRef(SecretKeySelector passwordSecretRef) {
        this.passwordSecretRef = passwordSecretRef;
    }
}
