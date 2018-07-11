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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.optaplanner.examples.conferencescheduling.domain.ConferenceParametrization;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceSolution;
import org.optaplanner.examples.conferencescheduling.domain.TalkType;

public class ConferenceSchedulingImporter {

    private ConferenceSchedulingRESTEndpoints endpoints;
    private Map<String, TalkType> talkTypeMap;

    public ConferenceSchedulingImporter() {
        this.endpoints = new ConferenceSchedulingRESTEndpoints();
        this.endpoints.setBaseUrl("http://cfp.devoxx.fr/api/conferences/DevoxxFR2018");
        this.endpoints.setTalkTypesEndpoint("/proposalTypes");
    }

    public ConferenceSolution importSolution() {
        ConferenceSolution solution = new ConferenceSolution();
        solution.setId(0L);
        solution.setConferenceName(getConferenceName());
        solution.setParametrization(new ConferenceParametrization());

        importTalkTypeList(solution);
//        importTimeslotList();
//        importRoomList();
//        importSpeakerList();
//        importTalkList();

        return solution;
    }

    private String getConferenceName() {
        JsonObject conferenceObject = readJsonObject(endpoints.getBaseUrl());

        return conferenceObject.getString("label");
    }

    private void importTalkTypeList(ConferenceSolution solution) {
        this.talkTypeMap = new HashMap<>();
        List<TalkType> talkTypeList = new ArrayList<>();
        JsonObject rootObject = readJsonObject(endpoints.getBaseUrl() +  endpoints.getTalkTypesEndpoint());
        JsonArray talkTypesArray = rootObject.getJsonArray("proposalTypes");
        for (int i = 0; i < talkTypesArray.size(); i++) {
            JsonObject proposalTypeObject = talkTypesArray.getJsonObject(i);
            String typeName = proposalTypeObject.getString("label");

            TalkType talkType = new TalkType(new Long(i), typeName);
            talkTypeMap.put(typeName, talkType);
            talkTypeList.add(talkType);
        }
        solution.setTalkTypeList(talkTypeList);

    }

    private JsonObject readJsonObject(String url) {
        try (InputStream inputStream = new URL(url).openConnection().getInputStream()) {
            JsonReader jsonReader = Json.createReader(inputStream);
            return jsonReader.readObject();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").", e);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").",e);
        }
    }
}