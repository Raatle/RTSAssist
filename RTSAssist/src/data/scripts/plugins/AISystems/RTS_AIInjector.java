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

    HashMap<String, ShipAIPlugin> AIStore = new HashMap<>();
    HashMap<eventType, HashMap<String, HashMap<String, RTS_AIInjectedEvent>>> eventStore;
    HashMap<eventType, String> eventDir = new HashMap<>();
    HashMap<eventType, HashMap<String, String>> hooksQueuedForDeletion = new HashMap<>();
    HashMap<ShipAPI, Integer> hookedShips = new HashMap<>();

    public void update() {
        if (((CombatEngineAPI)this.getState("engine")).isPaused())
            return;
        for (HashMap.Entry<ShipAPI, Integer> set: this.hookedShips.entrySet()) {
            if (set.getValue() < 5)
                this.hookedShips.put(set.getKey(), set.getValue() + 1);
            this.hookAI(set.getKey());
        }
    }

    public String registerHook(ShipAPI ship, eventType type, RTS_AIInjectedEvent event) {
        if (!this.AIStore.containsKey(ship.getId())) {
//            System.out.println("RTS_LOG: The ship you tried to register has not been hooked. Either hook the ai or wait for it to receive its hook");
            return (null);
        }
        String eventId = UUID.randomUUID().toString();
        if (this.eventStore.get(type).get(ship.getId()) == null)
            this.eventStore.get(type).put(ship.getId(), new HashMap<>());
        this.eventStore.get(type).get(ship.getId()).put(eventId, event);
        this.eventDir.put(type, eventId);
        return (eventId);
    }

    public void executeHooks(ShipAPI ship, eventType type){
        if (this.AIStore.get(ship.getId()) == null
                || this.eventStore.get(type).get(ship.getId()) == null
        )
            return;
        for (HashMap.Entry<String, RTS_AIInjectedEvent> event : this.eventStore.get(type).get(ship.getId()).entrySet()) {
            if (event.getValue().shouldExecute())
                event.getValue().run();
            if (event.getValue().removeOnCompletion())
                this.queueHookForDeletion(event.getKey(), type, ship);
        }
        this.deleteHooks();
    }

    private void deleteHooks() {
        for (HashMap.Entry<eventType, HashMap<String, String>> eventIdMap : this.hooksQueuedForDeletion.entrySet()) {
            for (HashMap.Entry<String, String> pair: eventIdMap.getValue().entrySet()) {
                this.eventStore.get(eventIdMap.getKey()).get(pair.getKey()).remove(pair.getValue());
            }
        }
        for (HashMap.Entry<eventType, HashMap<String, String>> eventIdMap : this.hooksQueuedForDeletion.entrySet())
            eventIdMap.getValue().clear();
        this.hooksQueuedForDeletion.clear();
    }

    public void removeHook(ShipAPI ship, String eventId, eventType type) {
        if (eventId == null || this.eventStore.get(type).get(ship.getId()) == null)
            return;
        this.eventStore.get(type).get(ship.getId()).remove(eventId);
    }

    private void queueHookForDeletion(String eventId, eventType type, ShipAPI ship) {
        if (!this.hooksQueuedForDeletion.containsKey(type))
            this.hooksQueuedForDeletion.put(type, new HashMap<>());
        this.hooksQueuedForDeletion.get(type).put(ship.getId(), eventId);
    }

    public boolean hookAI(ShipAPI ship) {
        if (this.hookedShips.get(ship) != null && this.hookedShips.get(ship) < 5)
            return (false);
        if (!this.hookedShips.containsKey(ship)) {
            this.hookedShips.put(ship, 0);
            return (false);
        }
        if (ship.getShipAI() == null)
            return (false);
        this.AIStore.put(ship.getId(), ship.getShipAI());
        ship.setShipAI(new ShipAIPlugin() {
            @Override
            public void setDoNotFireDelay(float amount) {
                AIStore.get(ship.getId()).setDoNotFireDelay(amount);
            }

            @Override
            public void forceCircumstanceEvaluation() {
                AIStore.get(ship.getId()).forceCircumstanceEvaluation();
            }

            @Override
            public void advance(float amount) {
                hookedShips.put(ship, 0);
                executeHooks(ship, eventType.PREADVANCE);
                ship.setShipAI(AIStore.get(ship.getId()));
                AIStore.get(ship.getId()).advance(amount);
                ship.setShipAI(this);
                executeHooks(ship, eventType.POSTADVANCE);
            }

            @Override
            public boolean needsRefit() {
                return false;
            }

            @Override
            public ShipwideAIFlags getAIFlags() {
                return(AIStore.get(ship.getId()).getAIFlags());
            }

            @Override
            public void cancelCurrentManeuver() {
                AIStore.get(ship.getId()).cancelCurrentManeuver();
            }

            @Override
            public ShipAIConfig getConfig() {
                return (AIStore.get(ship.getId()).getConfig());
            }
        });
        return (true);
    }

}