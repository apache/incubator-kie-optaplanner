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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.optaplanner.examples.conferencescheduling.domain.ConferenceParametrization;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceSolution;
import org.optaplanner.examples.conferencescheduling.domain.Room;
import org.optaplanner.examples.conferencescheduling.domain.Speaker;
import org.optaplanner.examples.conferencescheduling.domain.Talk;
import org.optaplanner.examples.conferencescheduling.domain.TalkType;
import org.optaplanner.examples.conferencescheduling.domain.Timeslot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.optaplanner.examples.common.persistence.AbstractXlsxSolutionFileIO.TIME_FORMATTER;

public class ConferenceSchedulingImporter {

    private static final Logger logger = LoggerFactory.getLogger(ConferenceSchedulingImporter.class);

    private ConferenceSchedulingRESTEndpoints endpoints;
    private Map<String, Room> roomIdToRoomMap;
    private Map<String, TalkType> talkTypeNameToTalkTypeMap;
    private Map<String, Speaker> speakerIdToSpeakerMap;
    private Map<String, Speaker> speakerNameToSpeakerMap;
    private Map<String, Talk> talkCodeToTalkMap;

    private Set<String> talkUrlSet;

    public ConferenceSchedulingImporter() {
        this.endpoints = new ConferenceSchedulingRESTEndpoints();
        this.endpoints.setBaseUrl("http://cfp.devoxx.fr/api/conferences/DevoxxFR2018");
        this.endpoints.setRoomsEndpoint("/rooms/");
        this.endpoints.setSpeakersEndpoint("/speakers");
        this.endpoints.setSchedulesEndpoint("/schedules/");
        this.endpoints.setTalkTypesEndpoint("/proposalTypes");
    }

    public ConferenceSolution importSolution() {
        ConferenceSolution solution = new ConferenceSolution();
        solution.setId(0L);
        solution.setConferenceName(getConferenceName());
        ConferenceParametrization parametrization = new ConferenceParametrization();
        parametrization.setId(0L);
        solution.setParametrization(parametrization);


        importRoomAndTalkTypeLists(solution);
        importSpeakerList(solution);
        importTalkList(solution);
        importTimeslotList(solution);

        return solution;
    }

    private String getConferenceName() {
        logger.info("Sending a request to: " + endpoints.getBaseUrl());
        JsonObject conferenceObject = readJsonObject(endpoints.getBaseUrl());

        return conferenceObject.getString("label");
    }

    private void importRoomAndTalkTypeLists(ConferenceSolution solution) {
        this.roomIdToRoomMap = new HashMap<>();
        this.talkTypeNameToTalkTypeMap = new HashMap<>();
        List<Room> roomList = new ArrayList<>();
        List<TalkType> talkTypeList = new ArrayList<>();
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getRoomsEndpoint());
        JsonObject rootObject = readJsonObject(endpoints.getBaseUrl() + endpoints.getRoomsEndpoint());
        JsonArray roomArray = rootObject.getJsonArray("rooms");
        for (int i = 0; i < roomArray.size(); i++) {
            JsonObject roomObject = roomArray.getJsonObject(i);
            String id = roomObject.getString("id");
            String name = roomObject.getString("name");
            String talkTypeName = roomObject.getString("setup");

            Room room = new Room((long) i);
            room.setName(name);
            roomList.add(room);
            roomIdToRoomMap.put(id, room);

            if (!talkTypeNameToTalkTypeMap.containsKey(talkTypeName)) {
                TalkType talkType = new TalkType(talkTypeNameToTalkTypeMap.size(), talkTypeName);
                talkTypeList.add(talkType);
                talkTypeNameToTalkTypeMap.put(talkTypeName, talkType);
            }
        }

        solution.setRoomList(roomList);
        solution.setTalkTypeList(talkTypeList);
    }

    private void importSpeakerList(ConferenceSolution solution) {
        this.speakerIdToSpeakerMap = new HashMap<>();
        this.speakerNameToSpeakerMap = new HashMap<>();
        this.talkUrlSet = new HashSet<>();
        List<Speaker> speakerList = new ArrayList<>();

        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getSpeakersEndpoint());
        JsonArray speakerArray = readJsonArray(endpoints.getBaseUrl() + endpoints.getSpeakersEndpoint());
        for (int i = 0; i < speakerArray.size(); i++) {
            String speakerUrl = speakerArray.getJsonObject(i).getJsonArray("links").getJsonObject(0).getString("href");
            logger.info("Sending a request to: " + speakerUrl);
            JsonObject speakerObject = readJsonObject(speakerUrl);

            String speakerId = speakerObject.getString("uuid");
            String speakerName = speakerObject.getString("firstName") + " " + speakerObject.getString("lastName");

            Speaker speaker = new Speaker((long) i);
            speaker.setName(speakerName);
            speakerList.add(speaker);
            speakerIdToSpeakerMap.put(speakerId, speaker);
            speakerNameToSpeakerMap.put(speakerName, speaker);

            JsonArray speakerTalksArray = speakerObject.getJsonArray("acceptedTalks");
            for (int j = 0; j < speakerTalksArray.size(); j++) {
                String talkUrl = speakerTalksArray.getJsonObject(j).getJsonArray("links").getJsonObject(0).getString("href");
                talkUrlSet.add(talkUrl);
            }
        }

        solution.setSpeakerList(speakerList);
    }

    private void importTalkList(ConferenceSolution solution) {
        this.talkCodeToTalkMap = new HashMap<>();
        List<Talk> talkList = new ArrayList<>();
        Long talkId = 0L;

        for (String talkUrl : this.talkUrlSet) {
            logger.info("Sending a request to: " + talkUrl);
            JsonObject talkObject = readJsonObject(talkUrl);

            String code = talkObject.getString("id");
            String title = talkObject.getString("title");
            Set<String> themeTrackSet = new HashSet<>(Arrays.asList(talkObject.getString("track")));
            String languageg = talkObject.getString("lang");
            List<Speaker> speakerList = talkObject.getJsonArray("speakers").stream()
                                                .map(speakerJson -> {
                                                    String speakerName = speakerJson.asJsonObject().getString("name");
                                                    Speaker speaker = speakerNameToSpeakerMap.get(speakerName);
//                                                    if (speaker == null) {
//                                                        throw new IllegalStateException("The talk (" + title + ") contains a speaker (" + speakerName
//                                                                                                + ") that doesn't exist in speaker list.");
//                                                    }
                                                    return speaker;
                                                })
                                                .collect(Collectors.toList());

            Talk talk = new Talk(talkId++);
            talk.setCode(code);
            talk.setTitle(title);
            talk.withThemeTrackTagSet(themeTrackSet)
                    .withLanguage(languageg)
                    .withSpeakerList(speakerList);

            talkCodeToTalkMap.put(code, talk);
            talkList.add(talk);
        }

        solution.setTalkList(talkList);
    }

    private void importTimeslotList(ConferenceSolution solution) {
        List<Timeslot> timeslotList = new ArrayList<>();
        Long  timeSlotId = 0L;
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getSchedulesEndpoint());
        JsonArray daysArray = readJsonObject(endpoints.getBaseUrl() + endpoints.getSchedulesEndpoint()).getJsonArray("links");
        for (int i = 0; i < daysArray.size(); i++) {
            String dayUrl = daysArray.getJsonObject(i).getString("href");
            logger.info("Sending a request to: " + dayUrl);
            JsonArray daySlotsArray = readJsonObject(dayUrl).getJsonArray("slots");

            for (int j = 0; j < daySlotsArray.size(); j++) {
                JsonObject timeslotObject = daySlotsArray.getJsonObject(j);

                LocalDateTime startDateTime = (new Timestamp((long) timeslotObject.getInt("fromTimeMillis"))).toLocalDateTime();
                LocalDateTime endDateTime = (new Timestamp((long) timeslotObject.getInt("toTimeMillis"))).toLocalDateTime();
                String talkTypeName = timeslotObject.getString("roomSetup");
                TalkType talkType = talkTypeNameToTalkTypeMap.get(talkTypeName);
                if (talkType == null) {
                    throw new IllegalStateException("The timeslot (" + startDateTime + ") has a talk type + (" + talkTypeName
                    + ") that does not exist in the talkType list");
                }
                Set<TalkType> talkTypeSet = new HashSet<>(Arrays.asList(talkType));
                // TODO: verify the room exists

                Timeslot timeslot = new Timeslot(timeSlotId++);
                timeslot.withStartDateTime(startDateTime)
                        .withEndDateTime(endDateTime)
                        .withTalkTypeSet(talkTypeSet);

                timeslotList.add(timeslot);
                //TODO: set the talks and rooms associated with this timeslot
            }
        }
        solution.setTimeslotList(timeslotList);
    }



/*
    private void importTalkTypeList(ConferenceSolution solution) {
        this.talkTypeMap = new HashMap<>();
        List<TalkType> talkTypeList = new ArrayList<>();
        JsonObject rootObject = readJsonObject(endpoints.getBaseUrl() + endpoints.getTalkTypesEndpoint());
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
*/

    private JsonObject readJsonObject(String url) {
        try (InputStream inputStream = new URL(url).openConnection().getInputStream()) {
            JsonReader jsonReader = Json.createReader(inputStream);
            return jsonReader.readObject();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").", e);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").", e);
        }
    }

    private JsonArray readJsonArray(String url) {
        try (InputStream inputStream = new URL(url).openConnection().getInputStream()) {
            JsonReader jsonReader = Json.createReader(inputStream);
            return jsonReader.readArray();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").", e);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").", e);
        }
    }
}