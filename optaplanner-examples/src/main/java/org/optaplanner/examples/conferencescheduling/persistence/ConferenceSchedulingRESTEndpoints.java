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

public class ConferenceSchedulingRESTEndpoints {
    private String baseUrl;
    private String roomsEndpoint;
    private String speakersEndpoint;
    private String schedulesEndpoint;
    private String talkTypesEndpoint;

    public ConferenceSchedulingRESTEndpoints() {

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getRoomsEndpoint() {
        return roomsEndpoint;
    }

    public void setRoomsEndpoint(String roomsEndpoint) {
        this.roomsEndpoint = roomsEndpoint;
    }

    public String getSpeakersEndpoint() {
        return speakersEndpoint;
    }

    public void setSpeakersEndpoint(String speakersEndpoint) {
        this.speakersEndpoint = speakersEndpoint;
    }

    public String getSchedulesEndpoint() {
        return schedulesEndpoint;
    }

    public void setSchedulesEndpoint(String schedulesEndpoint) {
        this.schedulesEndpoint = schedulesEndpoint;
    }

    public String getTalkTypesEndpoint() {
        return talkTypesEndpoint;
    }

    public void setTalkTypesEndpoint(String talkTypesEndpoint) {
        this.talkTypesEndpoint = talkTypesEndpoint;
    }


}
