/****************************************************************************************
 * RTSAssist version 0.1.5
 * Copyright (C) 2025, Raatle

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 ****************************************************************************************/

package data.scripts.plugins.Utils;

import java.util.*;

public class RTS_EventManager extends RTS_StatefulClasses {

    String badAddEvent = "RTSAssist: You attempted to add an event, with updateNow set to true, during an eventsUpdate operation. This is not allowed";
    String badAddListener = "RTSAssist: You attempted to add a listener, with updateNow set to true, during an eventsUpdate operation. This is not allowed";

    public RTS_EventManager(Object state) { super(state); }

    HashMap<String, RTS_Event> events = new HashMap<>();
    HashMap<String, RTS_Event> queuedEvents = new HashMap<>();
    HashMap<String, HashMap<String, RTS_Listener>> listeners = new HashMap<>();
    HashMap<String, RTS_Listener> queuedListeners = new HashMap<>();
    List<String> eventsToBeDeleted = new ArrayList<>();
    List<List<String>> listenersToBeDeleted = new ArrayList<>();
    boolean nextupdate = false;

    /*
    * Nasty bug:
    * When a ship dies an event is triggered. This event triggers all assignments assosciated with those dead
    * ships to be deleted. This triggers ShipMoveToPosition cleanup method which in turn calls BlockSystemPlugin
    * execCleanup. This in turn calls all atCleanup callbacks. Some of these callbacks have and addEvent method.
    * Since this all occurs inside this.update, inside the events for loop, this causes a ConcurrentModificationException.
    * */

    public void update() {
        boolean listenerFlag;
        this.nextupdate = true;
        for (Map.Entry<String, RTS_Event> event: this.events.entrySet()) {
            if (event.getValue().shouldExecute(this.returnState())) {
                listenerFlag = false;
                for (Map.Entry<String, HashMap<String, RTS_Listener>> listenerTypeObj : this.listeners.entrySet()) {
                    if (event.getValue().type().equals(listenerTypeObj.getKey())) {
                        for (Map.Entry<String, RTS_Listener> listener : listenerTypeObj.getValue().entrySet()) {
                            listener.getValue().run(event.getValue().run(true));
                            if (listener.getValue().removeOnCompletion())
                                this.deleteListener(listenerTypeObj.getKey(), listener.getKey(), false);
                            listenerFlag = true;
                        }
                    }
                }
                if (!listenerFlag)
                    event.getValue().run();
                if (event.getValue().removeOnCompletion())
                    this.deleteEvent(event.getKey(), true);
            }
        }
        this.nextupdate = false;
        this.cleanup();
    }

    public boolean isUpdating () {
        return (this.nextupdate);
    }

    public String addEvent(RTS_Event newEvent) { return(this.addEvent(newEvent, false)); }
    public String addEvent(RTS_Event newEvent, boolean updateNow) {
        String identifier = UUID.randomUUID().toString();
        if (this.nextupdate) {
            if (updateNow)
                throw new RuntimeException(this.badAddEvent);
            this.queuedEvents.put(identifier, newEvent);
        }
        else {
            this.events.put(identifier, newEvent);
            if (updateNow)
                this.update();
        }
        return(identifier);
    }

    public String addListener(RTS_Listener newListener) { return (this.addListener(newListener, false, null)); }
    public String addListener(RTS_Listener newListener, boolean updateNow) { return (this.addListener(newListener, updateNow, null)); }
    private String addListener(RTS_Listener newListener, boolean updateNow, String customIdentifier) {
        String identifier = customIdentifier != null ? customIdentifier : UUID.randomUUID().toString();
        if (this.nextupdate) {
            if (updateNow)
                throw new RuntimeException(this.badAddListener);
            this.queuedListeners.put(identifier, newListener);
        }
        else {
            if (!this.listeners.containsKey(newListener.type()))
                this.listeners.put(newListener.type(), new HashMap<>());
            this.listeners.get(newListener.type()).put(identifier, newListener);
            if (updateNow)
                this.update();
        }
        return(identifier);
    }

    private void cleanup() {
        for (String identifier: this.eventsToBeDeleted)
            this.events.remove(identifier);
        this.eventsToBeDeleted.clear();

        for (List<String> keyValuePair : this.listenersToBeDeleted)
            this.listeners.get(keyValuePair.get(0)).remove(keyValuePair.get(1));
        for (List<String> keyValuePair : this.listenersToBeDeleted)
            keyValuePair.clear();
        this.listenersToBeDeleted.clear();

        this.events.putAll(this.queuedEvents);
        this.queuedEvents.clear();

        for (Map.Entry<String, RTS_Listener> listener : this.queuedListeners.entrySet())
            this.addListener(listener.getValue(), false, listener.getKey());
        this.queuedListeners.clear();
    }

    public void deleteEvent(String identifier) {
        if (this.nextupdate)
            this.deleteEvent(identifier, true);
        else
            this.events.remove(identifier);
    }
    private void deleteEvent(String identifier, boolean cleanlater) {
        if (!this.eventsToBeDeleted.contains(identifier))
            this.eventsToBeDeleted.add(identifier);
    }

    public void deleteListener(String type, String identifier) {
        if (this.nextupdate)
            this.deleteListener(type, identifier, false);
        else
            this.deleteListener(type, identifier, true);
    }
    private void deleteListener(String type, String identifier, boolean cleanNow) {
        for (List<String> x : this.listenersToBeDeleted)
            if (x.get(1).equals(identifier))
                return;
        List<String> hold = new ArrayList<>();
        hold.add(type);
        hold.add(identifier);
        this.listenersToBeDeleted.add(hold);
        if (cleanNow)
            this.cleanup();
    }

    public RTS_Event getEvent(String Identifier) {
        return (this.events.get(Identifier));
    }
}

