package org.acme.schooltimetabling.messaging;

import static org.awaitility.Awaitility.await;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ActiveMQEmbeddedBrokerLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private final EmbeddedActiveMQ server = new EmbeddedActiveMQ();

    public Map<String, String> start() {
        try {
            server.setSecurityManager(new ActiveMQSecurityManager() {
                @Override
                public boolean validateUser(String user, String password) {
                    return true;
                }

                @Override
                public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
                    return true;
                }
            });
            server.start();
            await().timeout(30, TimeUnit.SECONDS).until(() -> server.getActiveMQServer().isStarted());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start embedded ActiveMQ broker.", e);
        }
        return Collections.emptyMap();
    }

    public void stop() {
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to stop embedded ActiveMQ broker.", e);
        }
    }

}
