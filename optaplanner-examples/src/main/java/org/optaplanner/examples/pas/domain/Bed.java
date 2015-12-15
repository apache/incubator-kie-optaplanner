/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.pas.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.common.swingui.components.Labeled;

@XStreamAlias("Bed")
public class Bed extends AbstractPersistable implements Labeled {

    private Room room;
    private int indexInRoom;

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getIndexInRoom() {
        return indexInRoom;
    }

    public void setIndexInRoom(int indexInRoom) {
        this.indexInRoom = indexInRoom;
    }

    public String getLabelInRoom() {
        if (indexInRoom > 'Z') {
            return Integer.toString(indexInRoom);
        }
        return Character.toString((char) ('A' + indexInRoom));
    }

    @Override
    public String getLabel() {
        return room.getDepartment().getName() + " " + room.getName() + " " + getLabelInRoom();
    }

    @Override
    public String toString() {
        return room + "(" + indexInRoom + ")";
    }

}
