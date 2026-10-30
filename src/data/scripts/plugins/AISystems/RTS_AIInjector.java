/*
  **********************************************************************************************************
  * RTSAssist version 0.2.04exp
  * Copyright (C) 2025-2026, Raatle

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
  **********************************************************************************************************
 */

package data.scripts.plugins.AISystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.combat.ai.BasicShipAI;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.plugins.RTSAssist;
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
        if (!((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isPaused())
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

    public boolean hookAI(ShipAPI ship) {
        if (this.hookedShips.get(ship) != null && (this.hookedShips.get(ship) < 5 || this.hookedShips.get(ship) == 6))
            return (false);
        if (!this.hookedShips.containsKey(ship)) {
            this.hookedShips.put(ship, 0);
            return (false);
        }
        if (ship.getShipAI() == null)
            return (false);
        /* The core may hand our own wrapper back to us (possibly inside one of its ShipAIWrappers).
           Reinstalling it directly is enough; pushing it onto the list would only have to be
           trimmed back off again by retrieveAI. */
        if (this.isOurWrapper(ship, ship.getShipAI())) {
            ship.setShipAI(this.wrapperStore.get(ship));
            this.hookedShips.put(ship, 6);
            return (false);
        }
        if (!this.AIStore.containsKey(ship.getId()))
            this.AIStore.put(ship.getId(), new ArrayList<>());
        this.AIStore.get(ship.getId()).add(this.AIStore.get(ship.getId()).size(), ship.getShipAI());
        this.hookedShips.put(ship, 6);
        if (!this.wrapperStore.containsKey(ship))
            this.wrapperStore.put(ship, new ShipAIPlugin() {
                ShipAIPlugin AIPointer = null;

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
                    executeInjections(ship, eventType.PREADVANCE);
                    this.AIPointer = retrieveAI(ship);
                    /* Compatibility (e.g. AI Tweaks' ExtendedShipAI): the core or another mod can
                       reassign the ship's AI (a fresh instance) at any time, including mid-frame
                       while our injections run. Advancing a stale copy trips vanilla
                       BasicShipAI's "is being controlled by an AI that is assigned to something
                       else" guard, so prefer whatever AI is currently assigned to this ship. */
                    ShipAIPlugin liveAI = ship.getShipAI();
                    if (liveAI != null && !isOurWrapper(ship, liveAI) && liveAI != this.AIPointer) {
                        this.AIPointer = liveAI;
                        List<ShipAIPlugin> AIList = AIStore.get(ship.getId());
                        if (AIList != null) {
                            if (!AIList.isEmpty())
                                AIList.set(AIList.size() - 1, liveAI);
                            else
                                AIList.add(liveAI);
                        }
                    }
                    if (this.AIPointer != null) {
                        ship.setShipAI(this.AIPointer);
                        if (ship.getCustomData().get(RTSAssist.shipCNames.blockAi) == null) {
                            try {
                                this.AIPointer.advance(amount);
                            } catch (RuntimeException e) {
                                /* Last-resort self-heal: if we still raced another mod's AI
                                   reassignment, drop the stale entry, restore our wrapper and
                                   let the next frame re-sync instead of killing the advance
                                   loop. Anything else is a genuine bug and rethrown. */
                                if (String.valueOf(e.getMessage())
                                        .contains("is being controlled by an AI that is assigned to something else")) {
                                    List<ShipAIPlugin> AIList = AIStore.get(ship.getId());
                                    if (AIList != null && !AIList.isEmpty())
                                        AIList.remove(AIList.size() - 1);
                                    Global.getLogger(RTS_AIInjector.class).warn(
                                            "RTS_AIInjector: ship " + ship.getName()
                                                    + " had an AI that was reassigned elsewhere; dropping it and re-hooking."
                                    );
                                }
                                else {
                                    throw (e);
                                }
                            }
                        }
                        ship.setShipAI(this);
                    }
                    executeInjections(ship, eventType.POSTADVANCE);
                }

                @Override
                public boolean needsRefit() {
                    return (retrieveAI(ship).needsRefit());
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

    private ShipAIPlugin retrieveAI(ShipAPI ship) {
        this.hookedShips.put(ship, 0);
        List<ShipAIPlugin> AIList = this.AIStore.get(ship.getId());
        if (AIList == null)
            return (null);
        /* Trim any of our own wrappers (or core ShipAIWrappers around them) off the top of the
           list. This replaces the old getConfig() probe trick, which could leave duplicates
           behind and hand our wrapper itself back as the "AI" to advance. */
        while (!AIList.isEmpty() && this.isOurWrapper(ship, AIList.get(AIList.size() - 1)))
            AIList.remove(AIList.size() - 1);
        if (!AIList.isEmpty())
            return (AIList.get(AIList.size() - 1));
        /* List exhausted: fall back to whatever AI the ship currently has, as long as it is not
           our own wrapper (that would recurse). Only then mint a fresh vanilla AI. */
        ShipAIPlugin liveAI = ship.getShipAI();
        if (liveAI != null && !this.isOurWrapper(ship, liveAI))
            return (liveAI);
        ship.setShipAI(new BasicShipAI((Ship)ship));
        return (ship.getShipAI());
    }

    /* Unwraps the core's ShipAIWrapper layers and returns the underlying plugin. */
    private ShipAIPlugin unwrapAI(ShipAIPlugin ai) {
        while (ai instanceof Ship.ShipAIWrapper)
            ai = ((Ship.ShipAIWrapper)ai).getAI();
        return (ai);
    }

    /* True if the given plugin (possibly wrapped by the core) is our own wrapper for this ship. */
    private boolean isOurWrapper(ShipAPI ship, ShipAIPlugin ai) {
        return (ai != null && this.unwrapAI(ai) == this.wrapperStore.get(ship));
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
     * RESOLVED PROBLEM >retrieveAI<: If the core takes the ships AI offline and replaces it with its own wrapper, RTSAssist will think its
     * a new AI and queue that onto the AIList. When the AI is called, out wrapper will try to retrieve its AI. The AI
     * will be the cores wrapper which will then point at our wrapper which will then point back at it.
     *
     * SOLUTION: The AIList is trimmed of any of our own wrappers (directly, or wrapped in the core's
     * ShipAIWrapper) by identity (unwrapAI/isOurWrapper) before the last entry is returned. The old
     * getConfig() probe trick could leave duplicate wrappers behind and hand our wrapper itself back
     * as the AI to advance.
     *
     * PROBLEM 2: Under some circumstance??, the core seems to be replacing the initial pointer to the base AI with a
     * wrapper that now points to our wrapper. This means we have completely lost touch with base AI and a recursive fault
     * occurs as we have no AI to roll back to.
     *
     * SOLUTION 2: If retrieveAI trims our list to empty, fall back to whatever AI the ship currently has.
     * Only if that too is our own wrapper do we mint a fresh vanilla BasicShipAI.
     *
     * PROBLEM 3 (0.2.20exp): Other mods (e.g. AI Tweaks) reassign the ship's AI — a fresh instance each
     * time — whenever the core re-picks. Advancing our stored (now stale) copy trips vanilla
     * BasicShipAI's "is being controlled by an AI that is assigned to something else" guard.
     *
     * SOLUTION 3: The wrapper's advance() adopts the AI currently assigned to the ship (if it is not
     * our own wrapper) before the temporary swap, so we only ever advance the AI that the core
     * currently considers assigned. If a reassignment still races us, the vanilla guard's
     * RuntimeException is caught, the stale entry dropped, and the next frame re-syncs.
     */
}