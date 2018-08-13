/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.conferencescheduling.persistence;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ConnectionFollowRedirectsTest {

    private final String host = "localhost";
    private final int port = 1080;

    private MockServerClient mockServerClient;
    private ClientAndServer mockServer;

    @Before
    public void startServer() {
        mockServer = startClientAndServer(port);
    }

    @After
    public void stopServer() {
        mockServer.stop();
    }

    @Test
    public void shouldRespondWithoutRedirection() {
        mockServerClient = new MockServerClient(host, port);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/path")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );

        try {
            ConnectionFollowRedirects connectionFollowRedirects = new ConnectionFollowRedirects(getUrl("path"));
            assertTrue(connectionFollowRedirects.getConnection() instanceof HttpURLConnection);
            connectionFollowRedirects.getInputStream().close();
            assertEquals(connectionFollowRedirects.getRedirects(), 0);
            assertEquals(((HttpURLConnection) connectionFollowRedirects.getConnection()).getResponseCode(), 200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldRedirectHttpOnce() {
        mockServerClient = new MockServerClient(host, port);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/path")
                )
                .respond(
                        response()
                                .withStatusCode(301)
                                .withHeader("location", getUrl("anotherPath"))
                );
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/anotherPath")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );

        try {
            ConnectionFollowRedirects connectionFollowRedirects = new ConnectionFollowRedirects(getUrl("path"));
            assertTrue(connectionFollowRedirects.getConnection() instanceof HttpURLConnection);
            connectionFollowRedirects.getInputStream().close();
            assertEquals(connectionFollowRedirects.getRedirects(), 1);
            assertEquals(((HttpURLConnection) connectionFollowRedirects.getConnection()).getResponseCode(), 200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUrl(String path) {
        return String.format("http://%s:%d/%s", host, port, path);
    }
}
