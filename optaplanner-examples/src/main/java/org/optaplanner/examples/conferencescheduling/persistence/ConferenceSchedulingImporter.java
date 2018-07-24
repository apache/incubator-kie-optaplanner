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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

//TODO: set names to lower case when reading them
public class ConferenceSchedulingImporter {

    private static final Logger logger = LoggerFactory.getLogger(ConferenceSchedulingImporter.class);
    private static final String zoneId = "Europe/Warsaw";
    private static final String[] smallRoomsTypeNames = {"Quickie Sessions", "Quickie", "Hands-on Labs", "BOF (Bird of a Feather)"};
    private static final String[] mediumRoomsTypeNames = {"Tools-in-Action", "University", "Conference", "Deep Dive"};
    private static final String[] largeRoomsTypeNames = {"Opening Keynote", "Closing Keynote", "Keynote"};

    private ConferenceSchedulingRESTEndpoints endpoints;
    private Map<String, TalkType> talkTypeNameToTalkTypeMap;
    private Map<String, Room> roomIdToRoomMap;
    private Map<String, Speaker> speakerIdToSpeakerMap;
    private Map<String, Speaker> speakerNameToSpeakerMap;
    private Map<String, Talk> talkCodeToTalkMap;
    private Set<String> talkUrlSet;

    private ConferenceSolution solution;

    public ConferenceSchedulingImporter() {
        this.endpoints = new ConferenceSchedulingRESTEndpoints();
//        this.endpoints.setBaseUrl("https://dvbe18.confinabox.com/api/conferences/DVBE18");
        this.endpoints.setBaseUrl("https://cfp.devoxx.fr/api/conferences/DevoxxFR2018");
//        this.endpoints.setBaseUrl("https://cfp.devoxx.pl/api/conferences/DevoxxPL2018");
//        this.endpoints.setBaseUrl("https://cfp.devoxx.co.uk/api/conferences/DV18");
        this.endpoints.setRoomsEndpoint("/rooms/");
        this.endpoints.setSpeakersEndpoint("/speakers");
        this.endpoints.setSchedulesEndpoint("/schedules/");
        this.endpoints.setTalkTypesEndpoint("/proposalTypes");
    }

    public ConferenceSolution importSolution() {
        solution = new ConferenceSolution();
        solution.setId(0L);
        solution.setConferenceName(getConferenceName());
        ConferenceParametrization parametrization = new ConferenceParametrization();
        parametrization.setId(0L);
        solution.setParametrization(parametrization);

        importTalkTypeList();
        importRoomList();
        importSpeakerList();
        importTalkList();
        importTimeslotList();

        return solution;
    }

    private String getConferenceName() {
        logger.info("Sending a request to: " + endpoints.getBaseUrl());
        JsonObject conferenceObject = readJsonObject(endpoints.getBaseUrl());

        return conferenceObject.getString("label");
    }

    private void importTalkTypeList() {
        this.talkTypeNameToTalkTypeMap = new HashMap<>();
        List<TalkType> talkTypeList = new ArrayList<>();
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getTalkTypesEndpoint());
        JsonObject rootObject = readJsonObject(endpoints.getBaseUrl() + endpoints.getTalkTypesEndpoint());
        JsonArray talkTypeArray = rootObject.getJsonArray("proposalTypes");
        for (int i = 0; i < talkTypeArray.size(); i++) {
            JsonObject talkTypeObject = talkTypeArray.getJsonObject(i);
            String talkTypeName = talkTypeObject.getString("label");
            if (talkTypeNameToTalkTypeMap.keySet().contains(talkTypeName)) {
                logger.warn("Duplicate talk type in " + endpoints.getBaseUrl() + endpoints.getTalkTypesEndpoint()
                        + " at index " + i + ".");
                continue;
            }

            TalkType talkType = new TalkType((long) i, talkTypeName);
            talkType.setCompatibleRoomSet(new HashSet<>());
            talkType.setCompatibleTimeslotSet(new HashSet<>());

            talkTypeList.add(talkType);
            talkTypeNameToTalkTypeMap.put(talkTypeName, talkType);
        }

        solution.setTalkTypeList(talkTypeList);
    }

    private void importRoomList() {
        this.roomIdToRoomMap = new HashMap<>();
        List<Room> roomList = new ArrayList<>();
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getRoomsEndpoint());
        JsonObject rootObject = readJsonObject(endpoints.getBaseUrl() + endpoints.getRoomsEndpoint());
        JsonArray roomArray = rootObject.getJsonArray("rooms");
        for (int i = 0; i < roomArray.size(); i++) {
            JsonObject roomObject = roomArray.getJsonObject(i);
            String id = roomObject.getString("id");
            String name = roomObject.getString("name");
            int capacity = roomObject.getInt("capacity");

            Room room = new Room((long) i);
            room.setName(name);
            room.setCapacity(capacity);
            room.setTalkTypeSet(getTalkTypeSetForCapacity(capacity));
            room.setTagSet(new HashSet<>());
            room.setUnavailableTimeslotSet(new HashSet<>());
            roomList.add(room);
            roomIdToRoomMap.put(id, room);
        }

        solution.setRoomList(roomList);
    }

    private void importSpeakerList() {
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
            String speakerName = (speakerObject.getString("firstName") + " " + speakerObject.getString("lastName")).toLowerCase();

            Speaker speaker = new Speaker((long) i);
            speaker.setName(speakerName);
            speaker.withPreferredRoomTagSet(new HashSet<>())
                    .withPreferredTimeslotTagSet(new HashSet<>())
                    .withProhibitedRoomTagSet(new HashSet<>())
                    .withProhibitedTimeslotTagSet(new HashSet<>())
                    .withRequiredRoomTagSet(new HashSet<>())
                    .withRequiredTimeslotTagSet(new HashSet<>())
                    .withUnavailableTimeslotSet(new HashSet<>())
                    .withUndesiredRoomTagSet(new HashSet<>())
                    .withUndesiredTimeslotTagSet(new HashSet<>());
            speakerList.add(speaker);
            speakerIdToSpeakerMap.put(speakerId, speaker);
//            if (speakerNameToSpeakerMap.get(speakerName) != null) { //TODO: use more efficient way to get existing speakerId
//                throw new IllegalStateException("Speaker (" + speakerName + ") with id (" + speakerId
//                        + ") already exists with an id (" + speakerIdToSpeakerMap.keySet().stream().filter(key -> speakerIdToSpeakerMap.get(speakerId).getName().equals(speakerName)).toString() + ").");
//            }
            speakerNameToSpeakerMap.put(speakerName, speaker);

            JsonArray speakerTalksArray = speakerObject.getJsonArray("acceptedTalks");
            for (int j = 0; j < speakerTalksArray.size(); j++) {
                String talkUrl = speakerTalksArray.getJsonObject(j).getJsonArray("links").getJsonObject(0).getString("href");
                talkUrlSet.add(talkUrl);
            }
        }

        solution.setSpeakerList(speakerList);
    }

    private void importTalkList() {
        this.talkCodeToTalkMap = new HashMap<>();
        List<Talk> talkList = new ArrayList<>();
        Long talkId = 0L;

        for (String talkUrl : this.talkUrlSet) {
            logger.info("Sending a request to: " + talkUrl);
            JsonObject talkObject = readJsonObject(talkUrl);

            String code = talkObject.getString("id");
            String title = talkObject.getString("title");
            String talkTypeName = talkObject.getString("talkType");
            Set<String> themeTrackSet = new HashSet<>(Arrays.asList(talkObject.getString("track")));
            String languageg = talkObject.getString("lang");
            List<Speaker> speakerList = talkObject.getJsonArray("speakers").stream()
                    .map(speakerJson -> {
                        String speakerName = speakerJson.asJsonObject().getString("name").toLowerCase();
                        Speaker speaker = speakerNameToSpeakerMap.get(speakerName);
                        if (speaker == null) {
                            throw new IllegalStateException("The talk (" + title + ") with id (" + code
                                    + ") contains a speaker (" + speakerName
                                    + ") that doesn't exist in speaker list.");
                        }
                        return speaker;
                    })
                    .collect(Collectors.toList());

            Talk talk = new Talk(talkId++);
            talk.setCode(code);
            talk.setTitle(title);
            TalkType talkType = talkTypeNameToTalkTypeMap.get(talkTypeName);
            if (talkType == null) {
                throw new IllegalStateException("The talk (" + title + ") with id (" + code
                + ") has a talkType (" + talkTypeName + ") that doesn't exist in the talkType list.");
            }
            talk.setTalkType(talkType);
            talk.withThemeTrackTagSet(themeTrackSet)
                    .withLanguage(languageg)
                    .withSpeakerList(speakerList)
                    .withAudienceLevel(1)
                    .withAudienceTypeSet(new HashSet<>())
                    .withContentTagSet(new HashSet<>())
                    .withPreferredRoomTagSet(new HashSet<>())
                    .withPreferredTimeslotTagSet(new HashSet<>())
                    .withProhibitedRoomTagSet(new HashSet<>())
                    .withProhibitedTimeslotTagSet(new HashSet<>())
                    .withRequiredRoomTagSet(new HashSet<>())
                    .withRequiredTimeslotTagSet(new HashSet<>())
                    .withSectorTagSet(new HashSet<>())
                    .withUndesiredRoomTagSet(new HashSet<>())
                    .withUndesiredTimeslotTagSet(new HashSet<>());

            talkCodeToTalkMap.put(code, talk);
            talkList.add(talk);
        }

        solution.setTalkList(talkList);
    }

    private void importTimeslotList() {
        List<Timeslot> timeslotList = new ArrayList<>();
        Map<Timeslot, Room> timeslotToAvailableRoomMap = new HashMap<>();

        Long timeSlotId = 0L;
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getSchedulesEndpoint());
        JsonArray daysArray = readJsonObject(endpoints.getBaseUrl() + endpoints.getSchedulesEndpoint()).getJsonArray("links");
        for (int i = 0; i < daysArray.size(); i++) {
            JsonObject dayObject = daysArray.getJsonObject(i);
            String dayUrl = dayObject.getString("href");

            logger.info("Sending a request to: " + dayUrl);
            JsonArray daySlotsArray = readJsonObject(dayUrl).getJsonArray("slots");

            for (int j = 0; j < daySlotsArray.size(); j++) {
                JsonObject timeslotObject = daySlotsArray.getJsonObject(j);

                LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeslotObject.getJsonNumber("fromTimeMillis").longValue()),
                        ZoneId.of(zoneId));
                LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeslotObject.getJsonNumber("toTimeMillis").longValue()),
                        ZoneId.of(zoneId));

                Room room = roomIdToRoomMap.get(timeslotObject.getString("roomId"));
                if (room == null) {
                    throw new IllegalStateException("The timeslot (" + timeslotObject.getString("slotId") + ") has a roomId + (" + timeslotObject.getString("roomId")
                            + ") that does not exist in the rooms list");
                }

                Timeslot timeslot = new Timeslot(timeSlotId++);
                timeslot.withStartDateTime(startDateTime)
                        .withEndDateTime(endDateTime)
                        .withTalkTypeSet(getTalkTypeSetForCapacity(room.getCapacity()));
                timeslot.setTagSet(new HashSet<>());

                timeslotList.add(timeslot);
                timeslotToAvailableRoomMap.put(timeslot, room);
                for (TalkType talkType: timeslot.getTalkTypeSet()) {
                    talkType.getCompatibleTimeslotSet().add(timeslot);
                }

                if (!timeslotObject.isNull("talk")) {
                    Talk talk = talkCodeToTalkMap.get(timeslotObject.getJsonObject("talk").getString("id"));
                    if (talk == null) {
                        throw new IllegalStateException("The timeslot (" + timeslotObject.getString("slotId")
                                + ") has a talk (" + timeslotObject.getJsonObject("talk").getString("id")
                                + ") that does not exist in the talk list");
                    }
                    talk.setRoom(room);
                    talk.setTimeslot(timeslot);
                    talk.setPinnedByUser(true);
                }
            }
        }

        for (Room room : solution.getRoomList()) {
            room.setUnavailableTimeslotSet(timeslotList.stream()
                    .filter(timeslot -> !timeslotToAvailableRoomMap.get(timeslot).equals(room))
                    .collect(Collectors.toSet()));
        }

        solution.setTimeslotList(timeslotList);
    }

    private Set<TalkType> getTalkTypeSetForCapacity(int capacity) {
        Set<TalkType> talkTypeSet = new HashSet<>();
        List<String> typeNames;
        if (capacity < 100) {
            typeNames = Arrays.asList(smallRoomsTypeNames);
        } else if (capacity < 1000) {
            typeNames = Arrays.asList(mediumRoomsTypeNames);
        } else {
            typeNames = Arrays.asList(largeRoomsTypeNames);
        }

        for (String talkTypeName : typeNames) {
            TalkType talkType = talkTypeNameToTalkTypeMap.get(talkTypeName);
            if (talkType != null) {
                talkTypeSet.add(talkType);
            }
        }

        return talkTypeSet;
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
        try (InputStream inputStream = openConnectionCheckRedirects(new URL(url).openConnection())) {
            JsonReader jsonReader = Json.createReader(inputStream);
            return jsonReader.readObject();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").", e);
        }
    }

    private JsonArray readJsonArray(String url) {
        try (InputStream inputStream = openConnectionCheckRedirects(new URL(url).openConnection())) {
            JsonReader jsonReader = Json.createReader(inputStream);
            return jsonReader.readArray();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").", e);
        }
    }

    private InputStream openConnectionCheckRedirects(URLConnection connection) throws IOException { // credits for https://www.cs.mun.ca/java-api-1.5/guide/deployment/deployment-guide/upgrade-guide/article-17.html
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