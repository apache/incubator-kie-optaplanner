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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class ConnectionFollowRedirects {

    String url;

    public ConnectionFollowRedirects(String url) {
        this.url = url;
    }

    public InputStream getInputStream() throws IOException { // credits for https://www.cs.mun.ca/java-api-1.5/guide/deployment/deployment-guide/upgrade-guide/article-17.html
        URLConnection connection = new URL(url).openConnection();
        boolean isRedirect;
        int redirects = 0;
        InputStream in = null;
        do {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
            }
            // We want to open the input stream before getting headers
            // because getHeaderField() et al swallow IOExceptions.
            in = connection.getInputStream();
            isRedirect = false;
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection) connection;
                int stat = http.getResponseCode();
                if (stat >= 300 && stat <= 307 && stat != 306 &&
                        stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    URL base = http.getURL();
                    String loc = http.getHeaderField("Location");
                    URL target = null;
                    if (loc != null) {
                        target = new URL(base, loc);
                    }
                    http.disconnect();
                    // Redirection should be allowed only for HTTP and HTTPS
                    // and should be limited to 5 redirections at most.
                    if (target == null || !(target.getProtocol().equals("http")
                            || target.getProtocol().equals("https"))
                            || redirects >= 5) {
                        throw new SecurityException("illegal URL redirect");
                    }
                    isRedirect = true;
                    connection = target.openConnection();
                    redirects++;
                }
            }
        }
        while (isRedirect);
        return in;
    }
}
