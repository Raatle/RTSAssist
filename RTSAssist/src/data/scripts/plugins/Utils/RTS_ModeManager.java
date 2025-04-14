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

package data.scripts.plugins.Utils;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.plugins.RTS_SelectionListener;
import data.scripts.plugins.RTS_TaskManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RTS_ModeManager extends RTS_StatefulClasses {

    public RTS_ModeManager(Object state) {
        super(state);
    }

    String newShipListenerID;
    String deadShipListenerID;
    ShipAPI newPlayerShip;

    public void buildListeners() {
        newShipListenerID = ((RTS_EventManager)this.getState("eventManager")).addListener(new RTS_Listener() {
            @Override
            public String type() {
                return ("newShipsDeployed");
            }

            @Override
            public void run(HashMap<String, Object> e) {
                List<ShipAPI> newShips = (List<ShipAPI>)e.get("newShipsDeployed");
                for (ShipAPI ship : newShips)
                    if (ship.getCaptain() != null && ship.getCaptain().isPlayer())
                        playerShipEntersMapAfterFirstFrame(ship);
                newShips.clear();
                e.clear();
            }

            @Override
            public boolean removeOnCompletion() {
                return (false);
            }
        });
        deadShipListenerID = ((RTS_EventManager)this.getState("eventManager")).addListener(new RTS_Listener() {
            @Override
            public String type() {
                return ("shipsNoLongerActive");
            }

            @Override
            public void run(HashMap<String, Object> e) {
                List<ShipAPI> newShips = (List<ShipAPI>)e.get("shipsNoLongerActive");
                for (ShipAPI ship : newShips)
                    if (ship.getCaptain() != null && ship.getCaptain().isPlayer())
                        playerDies(ship);
                newShips.clear();
                e.clear();
            }

            @Override
            public boolean removeOnCompletion() {
                return (false);
            }
        });
    }

    public void triggerChangesMode(boolean dontPause) {
        if ((boolean)this.getState("RTSMode")) {
            if ((boolean)this.getState("pauseUnpause")
                    && !((CombatEngineAPI)this.getState("engine")).isPaused()
                    && !dontPause
            )
                ((CombatEngineAPI)this.getState("engine")).setPaused(true);
            this.setState("playerShipHold", ((CombatEngineAPI)this.getState("engine")).getPlayerShip());
            this.setState(
                    "currentSelection",
                    Arrays.asList((ShipAPI)this.getState("playerShipHold"))
            );
        }
        else {
            this.setState("rightClickStore", null);
            this.setState("leftClickStore", null);
            this.setState("currentSelection", null);
            this.setDeepState(
                    Arrays.asList("amount", "rightClickStore"),
                    null
            );
            if ((boolean)this.getState("pauseUnpause")
                    && ((CombatEngineAPI)this.getState("engine")).isPaused())
                ((CombatEngineAPI)this.getState("engine")).setPaused(false);
            ((RTS_SelectionListener)this.getState("selectionListener")).prematurelyEndEvent();
            ((RTS_SelectionListener)this.getState("selectionListener")).
                    showShipDetails(Arrays.asList((ShipAPI)this.getState("playerShipHold")));
            ((RTS_TaskManager)this.getState("taskManager"))
                    .deleteAssignments((ShipAPI)this.getState("playerShipHold"));
        }
    }

    public void playerChangesMode() {
        if ((boolean)this.getState("RTSMode")
                && ((ShipAPI)this.getState("playerShipHold")).getName().equals("Command Shuttle"))
            return;
        this.setState("RTSMode", !(Boolean)this.getState("RTSMode"));
        if ((boolean)this.getState("RTSMode")) {
            if ((boolean)this.getState("pauseUnpause")
                    && !((CombatEngineAPI)this.getState("engine")).isPaused())
                ((CombatEngineAPI)this.getState("engine")).setPaused(true);
            this.setState("playerShipHold", ((CombatEngineAPI)this.getState("engine")).getPlayerShip());
            this.setState(
                    "currentSelection",
                    Arrays.asList((ShipAPI)this.getState("playerShipHold"))
            );
        }
        else {
            this.setState("rightClickStore", null);
            this.setState("leftClickStore", null);
            this.setState("currentSelection", null);
            this.setDeepState(
                    Arrays.asList("amount", "rightClickStore"),
                    null
            );
            if ((boolean)this.getState("pauseUnpause")
                    && ((CombatEngineAPI)this.getState("engine")).isPaused())
                ((CombatEngineAPI)this.getState("engine")).setPaused(false);
            ((RTS_SelectionListener)this.getState("selectionListener")).prematurelyEndEvent();
            ((RTS_SelectionListener)this.getState("selectionListener")).
                    showShipDetails(Arrays.asList((ShipAPI)this.getState("playerShipHold")));
            ((RTS_TaskManager)this.getState("taskManager"))
                    .deleteAssignments((ShipAPI)this.getState("playerShipHold"));
        }
    }

    public void makePlayerShipAi() {
        if ((Boolean)this.getState("RTSMode")) {
            ((CombatEngineAPI)this.getState("engine")).getCombatUI().setDisablePlayerShipControlOneFrame(true);
            if (((CombatEngineAPI)this.getState("engine")).getPlayerShip().getAI() == null)
                ((CombatEngineAPI)this.getState("engine")).getPlayerShip().resetDefaultAI();
        }
    }

    public void firstFrameOfBattle() {
        this.setState("playerShipHold", ((CombatEngineAPI)this.getState("engine")).getPlayerShip());
        if (!(boolean)this.getState("RTSMode")
                && ((CombatEngineAPI)this.getState("engine")).getPlayerShip().getName().equals("Command Shuttle"))
            this.setState("RTSMode", true);
        this.buildListeners();
    }

    public void playerShipEntersMapAfterFirstFrame(ShipAPI ship) {
        this.setState("playerShipHold", ship);
    }

    public void playerDies(ShipAPI ship) {
        if (!(boolean)this.getState("RTSMode")) {
            this.setState("RTSMode", true);
            this.triggerChangesMode(true);
        }
        this.setState("playerShipHold", ((CombatEngineAPI)this.getState("engine")).getPlayerShip());
    }

    public void playerSwitchesToDifferentShip() {
        if (((CombatEngineAPI)this.getState("engine")).getShipPlayerIsTransferringCommandTo() != null)
            this.newPlayerShip = ((CombatEngineAPI)this.getState("engine")).getShipPlayerIsTransferringCommandTo();
        if (this.newPlayerShip != null && this.newPlayerShip.getCaptain().isPlayer()) {
            this.setState("playerShipHold", this.newPlayerShip);
            this.newPlayerShip = null;
        }
    }
}