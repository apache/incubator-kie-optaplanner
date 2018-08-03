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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.lang3.tuple.Pair;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceParametrization;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceSolution;
import org.optaplanner.examples.conferencescheduling.domain.Room;
import org.optaplanner.examples.conferencescheduling.domain.Speaker;
import org.optaplanner.examples.conferencescheduling.domain.Talk;
import org.optaplanner.examples.conferencescheduling.domain.TalkType;
import org.optaplanner.examples.conferencescheduling.domain.Timeslot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConferenceSchedulingImporter {

    private static final Logger logger = LoggerFactory.getLogger(ConferenceSchedulingImporter.class);
    private static final String zoneId = "Europe/Paris";
    private static final String[] smallRoomsTypeNames = {"Quickie Sessions", "Quickie", "Hands-on Labs", "BOF (Bird of a Feather)"};
    private static final String[] mediumRoomsTypeNames = {"Tools-in-Action", "University", "Conference", "Deep Dive", "Opening Keynote", "Closing Keynote", "Keynote"};
    private static final String[] largeRoomsTypeNames = {"break"};
    private static final String SMALL_ROOM_TAG = "small";
    private static final String MEDIUM_ROOM_TAG = "medium";
    private static final String LARGE_ROOM_TAG = "large";

    private ConferenceSchedulingRESTEndpoints endpoints;
    private Map<String, TalkType> talkTypeNameToTalkTypeMap;
    private Map<String, Room> roomIdToRoomMap;
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
        JsonObject conferenceObject = readJson(endpoints.getBaseUrl(), JsonReader::readObject);
        return conferenceObject.getString("label");
    }

    private void importTalkTypeList() {
        this.talkTypeNameToTalkTypeMap = new HashMap<>();
        List<TalkType> talkTypeList = new ArrayList<>();
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getTalkTypesEndpoint());
        JsonObject rootObject = readJson(endpoints.getBaseUrl() + endpoints.getTalkTypesEndpoint(), JsonReader::readObject);
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

        TalkType breakTalkType = new TalkType((long) talkTypeArray.size(), "break");
        breakTalkType.setCompatibleRoomSet(new HashSet<>());
        breakTalkType.setCompatibleTimeslotSet(new HashSet<>());
        talkTypeList.add(breakTalkType);
        talkTypeNameToTalkTypeMap.put("break", breakTalkType);

        solution.setTalkTypeList(talkTypeList);
    }

    private void importRoomList() {
        this.roomIdToRoomMap = new HashMap<>();
        List<Room> roomList = new ArrayList<>();

        // TODO: Workaround inconsistent data in DevoxxFr, use local updated files
        // FIXME : RESOURCES.../persistence/devoxxFrance modify all the files, searching for urls that starts with "file:/" and replace the url to the resource folder with the correct one
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getRoomsEndpoint());
        JsonObject rootObject = readJson(endpoints.getBaseUrl() + endpoints.getRoomsEndpoint(), JsonReader::readObject);
//        logger.info("Sending a request to: " + getClass().getResource("devoxxFrance/rooms.json").toString());
//        JsonObject rootObject = readJsonObject(getClass().getResource("devoxxFrance/rooms.json").toString());

        JsonArray roomArray = rootObject.getJsonArray("rooms");
        for (int i = 0; i < roomArray.size(); i++) {
            JsonObject roomObject = roomArray.getJsonObject(i);
            String id = roomObject.getString("id");
            int capacity = roomObject.getInt("capacity");

            Room room = new Room((long) i);
            room.setName(id);
            room.setCapacity(capacity);
            room.setTalkTypeSet(getTalkTypeSetForCapacity(capacity));
            room.setTagSet(getRoomTagSetOfCapacity(capacity));
            room.setUnavailableTimeslotSet(new HashSet<>());
            roomList.add(room);
            roomIdToRoomMap.put(id, room);
        }

        solution.setRoomList(roomList);
    }

    private Set<String> getRoomTagSetOfCapacity(int capacity) {
        Set<String> roomTagSet = new HashSet<>();
        if (capacity < 100) {
            roomTagSet.add(SMALL_ROOM_TAG);
        } else if (capacity < 1000) {
            roomTagSet.add(MEDIUM_ROOM_TAG);
        } else {
            roomTagSet.add(LARGE_ROOM_TAG);
        }

        return roomTagSet;
    }

    private void importSpeakerList() {
        this.speakerNameToSpeakerMap = new HashMap<>();
        this.talkUrlSet = new HashSet<>();
        List<Speaker> speakerList = new ArrayList<>();

        // TODO: Workaround inconsistent data in DevoxxFr, use local updated files
        // FIXME : RESOURCES.../persistence/devoxxFrance modify all the files, searching for urls that starts with "file:/" and replace the url to the resource folder with the correct one
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getSpeakersEndpoint());
        JsonArray speakerArray = readJson(endpoints.getBaseUrl() + endpoints.getSpeakersEndpoint(), JsonReader::readArray);
//        logger.info("Sending a request to: " + getClass().getResource("devoxxFrance/speakers.json").toString());
//        JsonArray speakerArray = readJsonArray(getClass().getResource("devoxxFrance/speakers.json").toString());

        for (int i = 0; i < speakerArray.size(); i++) {
            String speakerUrl = speakerArray.getJsonObject(i).getJsonArray("links").getJsonObject(0).getString("href");
            logger.info("Sending a request to: " + speakerUrl);
            JsonObject speakerObject = readJson(speakerUrl, JsonReader::readObject);

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
            if (speakerNameToSpeakerMap.keySet().contains(speakerName)) {
                throw new IllegalStateException("Speaker (" + speakerName + ") with id (" + speakerId
                        + ") already exists in the speaker list");
            }
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
            JsonObject talkObject = readJson(talkUrl, JsonReader::readObject);

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
                                    + ") contains a speaker (" + speakerName + ", " + speakerJson.asJsonObject().getJsonObject("link").getString("href")
                                    + ") that doesn't exist in speaker list.");
                        }
                        return speaker;
                    })
                    .collect(Collectors.toList());

            Talk talk = createTalk(talkId++, code, title, talkTypeName, themeTrackSet, languageg, speakerList);

            talkCodeToTalkMap.put(code, talk);
            talkList.add(talk);
        }

        solution.setTalkList(talkList);
    }

    private void importTimeslotList() {
        List<Timeslot> timeslotList = new ArrayList<>();
        Map<Timeslot, List<Room>> timeslotToAvailableRoomsMap = new HashMap<>();
        Map<Pair<LocalDateTime, LocalDateTime>, Timeslot> startAndEndTimeToTimeslotMap = new HashMap<>();

        Long timeSlotId = 0L;
        Long talkIdForBreak = (long) solution.getTalkList().size();
        logger.info("Sending a request to: " + endpoints.getBaseUrl() + endpoints.getSchedulesEndpoint());
        JsonArray daysArray = readJson(endpoints.getBaseUrl() + endpoints.getSchedulesEndpoint(), JsonReader::readObject).getJsonArray("links");
        for (int i = 0; i < daysArray.size(); i++) {
            JsonObject dayObject = daysArray.getJsonObject(i);
            String dayUrl = dayObject.getString("href");

            logger.info("Sending a request to: " + dayUrl);
            JsonArray daySlotsArray = readJson(dayUrl, JsonReader::readObject).getJsonArray("slots");

            for (int j = 0; j < daySlotsArray.size(); j++) {
                JsonObject timeslotObject = daySlotsArray.getJsonObject(j);

                LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeslotObject.getJsonNumber("fromTimeMillis").longValue()),
                        ZoneId.of(zoneId));
                LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeslotObject.getJsonNumber("toTimeMillis").longValue()),
                        ZoneId.of(zoneId));

                Room room = roomIdToRoomMap.get(timeslotObject.getString("roomId"));
                if (room == null) {
                    throw new IllegalStateException("The timeslot (" + timeslotObject.getString("slotId") + ") has a roomId (" + timeslotObject.getString("roomId")
                            + ") that does not exist in the rooms list");
                }

                Timeslot timeslot;
                if (startAndEndTimeToTimeslotMap.keySet().contains(Pair.of(startDateTime, endDateTime))) {
                    timeslot = startAndEndTimeToTimeslotMap.get(Pair.of(startDateTime, endDateTime));
                    timeslotToAvailableRoomsMap.get(timeslot).add(room);
                    timeslot.getTalkTypeSet().addAll(getTalkTypeSetForCapacity(room.getCapacity()));
                } else {
                    timeslot = new Timeslot(timeSlotId++);
                    timeslot.withStartDateTime(startDateTime)
                            .withEndDateTime(endDateTime)
                            .withTalkTypeSet(getTalkTypeSetForCapacity(room.getCapacity()));
                    timeslot.setTagSet(new HashSet<>());

                    timeslotList.add(timeslot);
                    timeslotToAvailableRoomsMap.put(timeslot, new ArrayList<>(Arrays.asList(room)));
                    startAndEndTimeToTimeslotMap.put(Pair.of(startDateTime, endDateTime), timeslot);
                }

                if (!timeslotObject.isNull("talk")) {
                    scheduleTalk(timeslotObject, room, timeslot);
                }

                if (!timeslotObject.isNull("break")) {
                    createNewBreak(talkIdForBreak++, timeslotObject, timeslot);
                }

                for (TalkType talkType : timeslot.getTalkTypeSet()) {
                    talkType.getCompatibleTimeslotSet().add(timeslot);
                }
            }
        }

        for (Room room : solution.getRoomList()) {
            room.setUnavailableTimeslotSet(timeslotList.stream()
                    .filter(timeslot -> !timeslotToAvailableRoomsMap.get(timeslot).contains(room))
                    .collect(Collectors.toSet()));
        }

        solution.setTimeslotList(timeslotList);
    }

    private void scheduleTalk(JsonObject timeslotObject, Room room, Timeslot timeslot) {
        Talk talk = talkCodeToTalkMap.get(timeslotObject.getJsonObject("talk").getString("id"));
        if (talk == null) {
            throw new IllegalStateException("The timeslot (" + timeslotObject.getString("slotId")
                    + ") has a talk (" + timeslotObject.getJsonObject("talk").getString("id")
                    + ") that does not exist in the talk list");
        }
        if (talk.isPinnedByUser()) {
            throw new IllegalStateException("The timeslot (" + timeslotObject.getString("slotId")
                    + ") has a talk (" + timeslotObject.getJsonObject("talk").getString("id")
                    + ") that is already pinned by user at another timeslot (" + talk.getTimeslot().toString() + ").");
        }
        talk.setRoom(room);
        talk.setTimeslot(timeslot);
        talk.setPinnedByUser(true);
    }

    private void createNewBreak(Long talkIdForBreak, JsonObject timeslotObject, Timeslot timeslot) {
        JsonObject breakObject = timeslotObject.getJsonObject("break");
        String code = timeslotObject.getString("slotId");
        String title = breakObject.getString("nameEN") + ", " + breakObject.getString("nameFR");
        Room breakRoom = roomIdToRoomMap.get(breakObject.getJsonObject("room").getString("id"));

        Talk breakTalk = createTalk(talkIdForBreak, code, title, "break", new HashSet<>(), "", new ArrayList<>());
        breakTalk.setRoom(breakRoom);
        breakTalk.setTimeslot(timeslot);
        breakTalk.setPinnedByUser(true);

        timeslot.getTalkTypeSet().add(talkTypeNameToTalkTypeMap.get("break"));
        talkCodeToTalkMap.put(code, breakTalk);
        solution.getTalkList().add(breakTalk);
    }

    private Talk createTalk(Long talkId, String code, String title, String talkTypeName, Set<String> themeTrackSet,
                            String languageg, List<Speaker> speakerList) {
        Talk talk = new Talk(talkId);
        talk.setCode(code);
        talk.setTitle(title);
        if (talkTypeNameToTalkTypeMap.get(talkTypeName) == null) {
            throw new IllegalStateException("The talk (" + title + ") with id (" + code
                    + ") has a talkType (" + talkTypeName + ") that doesn't exist in the talkType list.");
        }
        talk.setTalkType(talkTypeNameToTalkTypeMap.get(talkTypeName));
        talk.withThemeTrackTagSet(themeTrackSet)
                .withLanguage(languageg)
                .withSpeakerList(speakerList)
                .withAudienceLevel(1)
                .withAudienceTypeSet(new HashSet<>())
                .withContentTagSet(new HashSet<>())
                .withPreferredRoomTagSet(getPreferredRoomTagSetForTalkOfType(talkTypeName))
                .withPreferredTimeslotTagSet(new HashSet<>())
                .withProhibitedRoomTagSet(getProhibitedRoomTagSetForTalkOfType(talkTypeName))
                .withProhibitedTimeslotTagSet(new HashSet<>())
                .withRequiredRoomTagSet(new HashSet<>())
                .withRequiredTimeslotTagSet(new HashSet<>())
                .withSectorTagSet(new HashSet<>())
                .withUndesiredRoomTagSet(new HashSet<>())
                .withUndesiredTimeslotTagSet(new HashSet<>());
        return talk;
    }

    private Set<String> getPreferredRoomTagSetForTalkOfType(String talkTypeName) {
        Set<String> preferredRoomTagSet = new HashSet<>();
        if (Arrays.asList(smallRoomsTypeNames).contains(talkTypeName)) {
            preferredRoomTagSet.add(SMALL_ROOM_TAG);
        } else if (Arrays.asList(mediumRoomsTypeNames).contains(talkTypeName)) {
            preferredRoomTagSet.add(MEDIUM_ROOM_TAG);
        } else {
            preferredRoomTagSet.add(LARGE_ROOM_TAG);
        }
        return preferredRoomTagSet;
    }

    private Set<String> getProhibitedRoomTagSetForTalkOfType(String talkTypeName) {
        Set<String> prohibitedRoomTagSet = new HashSet<>();
        if (Arrays.asList(largeRoomsTypeNames).contains(talkTypeName)) {
            prohibitedRoomTagSet.add(SMALL_ROOM_TAG);
            prohibitedRoomTagSet.add(MEDIUM_ROOM_TAG);
        } else if (Arrays.asList(mediumRoomsTypeNames).contains(talkTypeName)) {
            prohibitedRoomTagSet.add(SMALL_ROOM_TAG);
        }

        return prohibitedRoomTagSet;
    }

    private Set<TalkType> getTalkTypeSetForCapacity(int capacity) {
        Set<TalkType> talkTypeSet = new HashSet<>();
        List<String> typeNames = new ArrayList<>();
        if (capacity < 100) {
            typeNames.addAll(Arrays.asList(smallRoomsTypeNames));
        } else if (capacity < 1000) {
            typeNames.addAll(Arrays.asList(mediumRoomsTypeNames));
            typeNames.addAll(Arrays.asList(smallRoomsTypeNames));
        } else {
            typeNames.addAll(Arrays.asList(largeRoomsTypeNames));
            typeNames.addAll(Arrays.asList(mediumRoomsTypeNames));
            typeNames.addAll(Arrays.asList(smallRoomsTypeNames));
        }

        for (String talkTypeName : typeNames) {
            TalkType talkType = talkTypeNameToTalkTypeMap.get(talkTypeName);
            if (talkType != null) {
                talkTypeSet.add(talkType);
            }
        }

        return talkTypeSet;
    }

    private <R> R readJson(String url, Function<JsonReader, R> mapper) {
        try (InputStream inputStream = new ConnectionFollowRedirects(url).getInputStream()) {
            JsonReader jsonReader = Json.createReader(inputStream);
            return mapper.apply(jsonReader);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Import failed on URL (" + url + ").", e);
        }
    }
}