/****************************************************************************************
 * RTSAssist version 0.1.0
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

package data.scripts.plugins.AISystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.combat.ai.BasicShipAI;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.plugins.Utils.RTS_StatefulClasses;

import java.util.*;

public class RTS_AIInjector extends RTS_StatefulClasses {
    public RTS_AIInjector(Object state) {
        super(state);
        this.eventStore = new HashMap<>();
        this.eventStore.put(eventType.PREADVANCE, new HashMap<>());
        this.eventStore.put(eventType.POSTADVANCE, new HashMap<>());
    }

    public enum eventType {
        PREADVANCE,
        POSTADVANCE,
    }

    HashMap<String, List<ShipAIPlugin>> AIStore = new HashMap<>();
    HashMap<eventType, HashMap<String, HashMap<String, RTS_AIInjectedEvent>>> eventStore;
    HashMap<eventType, String> eventDir = new HashMap<>(); // Stores registered events.
    HashMap<eventType, HashMap<String, String>> injectionsQueuedForDeletion = new HashMap<>(); // A temp hold.
    HashMap<ShipAPI, Integer> hookedShips = new HashMap<>(); // Used to queue ships to be rehooked if their respective wrapper is no longer calling itself. i.e., its been replaced
    HashMap<ShipAPI, ShipAIPlugin> wrapperStore = new HashMap<>(); // Stores our wrappers.

    public void update() {
        if (!((CombatEngineAPI)this.getState("engine")).isPaused())
            for (HashMap.Entry<ShipAPI, Integer> set: this.hookedShips.entrySet()) {
                if (set.getValue() < 5)
                    this.hookedShips.put(set.getKey(), set.getValue() + 1);
                this.hookAI(set.getKey());
            }
    }

    public String registerInjection(ShipAPI ship, eventType type, RTS_AIInjectedEvent event) {
        if (!this.hookedShips.containsKey(ship))
            return (null);
        String eventId = UUID.randomUUID().toString();
        if (this.eventStore.get(type).get(ship.getId()) == null)
            this.eventStore.get(type).put(ship.getId(), new HashMap<>());
        this.eventStore.get(type).get(ship.getId()).put(eventId, event);
        this.eventDir.put(type, eventId);
        return (eventId);
    }

    private void executeInjections(ShipAPI ship, eventType type){
        if (this.AIStore.get(ship.getId()) == null
                || this.eventStore.get(type).get(ship.getId()) == null
        )
            return;
        for (HashMap.Entry<String, RTS_AIInjectedEvent> event : this.eventStore.get(type).get(ship.getId()).entrySet()) {
            if (event.getValue().shouldExecute())
                event.getValue().run();
            if (event.getValue().removeOnCompletion())
                this.queueInjectionForDeletion(event.getKey(), type, ship);
        }
        this.deleteInjections();
    }

    private void deleteInjections() {
        for (HashMap.Entry<eventType, HashMap<String, String>> eventIdMap : this.injectionsQueuedForDeletion.entrySet()) {
            for (HashMap.Entry<String, String> pair: eventIdMap.getValue().entrySet()) {
                this.eventStore.get(eventIdMap.getKey()).get(pair.getKey()).remove(pair.getValue());
            }
        }
        for (HashMap.Entry<eventType, HashMap<String, String>> eventIdMap : this.injectionsQueuedForDeletion.entrySet())
            eventIdMap.getValue().clear();
        this.injectionsQueuedForDeletion.clear();
    }

    public void removeInjection(ShipAPI ship, String eventId, eventType type) {
        if (eventId == null || this.eventStore.get(type).get(ship.getId()) == null)
            return;
        this.eventStore.get(type).get(ship.getId()).remove(eventId);
    }

    private void queueInjectionForDeletion(String eventId, eventType type, ShipAPI ship) {
        if (!this.injectionsQueuedForDeletion.containsKey(type))
            this.injectionsQueuedForDeletion.put(type, new HashMap<>());
        this.injectionsQueuedForDeletion.get(type).put(ship.getId(), eventId);
    }

    /*
    * PROBLEM: If the core takes the ships AI offline and replaces it with its own wrapper, RTSAssist will think its
    * a new AI and queue that onto the AIList. When the AI is called, out wrapper will try to retrieve its AI. The AI
    * will be the cores wrapper which will then point at our wrapper which will then point back at it. Line 118
    * checked to see if the AI we just pulled is our wrapper but cant see if it is another wrapper which points to
    * our wrapper.
    * SOLUTION: when asked to retrieve an AI, we grab out last list instance, set a flag to true and call getConfig on the
    * AI instance. If the flag remains true, we return that AI as we know it is an intended AI, as if the AI is wrapped by us, it
    * will set the flag to false, in which case we roll back our AI list and pass that instead. If the rolled back AI is also our
    * wrapper, the process should repeat but the AIList will be trimmed on each iteration. This should continue until we
    * have trimmed our AI list of our own wrappers.
    */

    Boolean recFlag = false;
    private ShipAIPlugin retrieveAI(ShipAPI ship) {
        List<ShipAIPlugin> AIList = this.AIStore.get(ship.getId());
        if (this.recFlag) {
            this.recFlag = false;
            return (AIList.get(AIList.size() - 1));
        }
        ShipAIPlugin AIhold = AIList.get(AIList.size() - 1);
        recFlag = true;
        AIhold.getConfig();
        if (this.recFlag) {
            this.recFlag = false;
            return (AIhold);
        }
        this.recFlag = false;
        if (AIList.size() > 1) {
            AIList.remove(AIList.size() - 1);
            System.out.println("RTSAssist->AIInjector(" + ship.getId() + "): AIlist trimmed. Retry attempted");
        }
        return (AIList.get(AIList.size() - 1));
    }

    public boolean hookAI(ShipAPI ship) {
        if (this.hookedShips.get(ship) != null && (this.hookedShips.get(ship) < 5 || this.hookedShips.get(ship) == 6))
            return (false);
        if (!this.hookedShips.containsKey(ship)) {
            this.hookedShips.put(ship, 0);
            return (false);
        }
        if (ship.getShipAI() == null)
            return (false);
        if (!this.AIStore.containsKey(ship.getId()))
            this.AIStore.put(ship.getId(), new ArrayList<>());
        this.AIStore.get(ship.getId()).add(this.AIStore.get(ship.getId()).size(), ship.getShipAI());
        this.hookedShips.put(ship, 6);
        if (!this.wrapperStore.containsKey(ship))
            this.wrapperStore.put(ship, new ShipAIPlugin() {
                @Override
                public void setDoNotFireDelay(float amount) {
                    retrieveAI(ship).setDoNotFireDelay(amount);
                }

                @Override
                public void forceCircumstanceEvaluation() {
                    retrieveAI(ship).forceCircumstanceEvaluation();
                }

                @Override
                public void advance(float amount) {
                    hookedShips.put(ship, 0);
                    executeInjections(ship, eventType.PREADVANCE);
                    ship.setShipAI(retrieveAI(ship));
                    retrieveAI(ship).advance(amount);
                    ship.setShipAI(this);
                    executeInjections(ship, eventType.POSTADVANCE);
                }

                @Override
                public boolean needsRefit() {
                    return false;
                }

                @Override
                public ShipwideAIFlags getAIFlags() {
                    return(retrieveAI(ship).getAIFlags());
                }

                @Override
                public void cancelCurrentManeuver() {
                    retrieveAI(ship).cancelCurrentManeuver();
                }

                @Override
                public ShipAIConfig getConfig() {
                    return (retrieveAI(ship).getConfig());

                }
            });
        ship.setShipAI(this.wrapperStore.get(ship));
        return (true);
    }

}