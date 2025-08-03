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

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.RTS_ParseInput;
import data.scripts.plugins.RTS_SelectionListener;
import data.scripts.plugins.RTS_TaskManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RTS_ModeManager extends RTS_StatefulClasses {

    public RTS_ModeManager(Object state) {
        super(state);
    }

    String newShipListenerID;
    String deadShipListenerID;
    boolean newPlayerShip = false;
    HashMap<WeaponGroupAPI, Boolean> weaponGroups = new HashMap<>();
    Integer activeWeaponGroup = null;
    String changeGroupEventString;

    public void buildListeners() {
        newShipListenerID = ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager))
                .addListener(new RTS_Listener() {
            @Override
            public String type() {
                return (RTS_ShipLocAPI.evNames.newShipsDeployed);
            }

            @Override
            public void run(HashMap<String, Object> e) {
                List<ShipAPI> newShips = (List<ShipAPI>)e.get(RTS_ShipLocAPI.evNames.newShipsDeployed);
                for (ShipAPI ship : newShips)
                    if (ship.getCaptain() != null
                            && ship.getCaptain().isPlayer()
                            && !((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener)).dontSelect(ship) // Some mods like to create ship modules that inherit their pilots capapbilities
                    )
                        playerShipEntersMapAfterFirstFrame(ship);
            }

            @Override
            public boolean removeOnCompletion() {
                return (false);
            }
        });
        deadShipListenerID = ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager))
                .addListener(new RTS_Listener() {
            @Override
            public String type() {
                return (RTS_ShipLocAPI.evNames.shipsNoLongerActive);
            }

            @Override
            public void run(HashMap<String, Object> e) {
                List<ShipAPI> deadShips = (List<ShipAPI>)e.get(RTS_ShipLocAPI.evNames.shipsNoLongerActive);
                for (ShipAPI ship : deadShips) {
                    if (ship.getCaptain() != null
                            && ship.getCaptain().isPlayer()
                            && !((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener)).dontSelect(ship, true) // Some mods like to create ship modules(armour plates in kol) that inherit their pilots capapbilities
                    )
                        playerDies();
                }
            }

            @Override
            public boolean removeOnCompletion() {
                return (false);
            }
        });
    }

    // This function requires abstraction

    public void playerChangesMode() {
        if ((boolean)this.getState(RTS_ParseInput.stNames.RTSMode)
                && ((ShipAPI)this.getState(RTS_ParseInput.stNames.playerShipHold)).getName().equals("Command Shuttle"))
            return;
        this.setState(RTS_ParseInput.stNames.RTSMode, !(Boolean)this.getState(RTS_ParseInput.stNames.RTSMode));
        /* We've switched to RTS mode */
        if ((boolean)this.getState(RTS_ParseInput.stNames.RTSMode)) {
            this.weaponGroups.clear();
            WeaponGroupAPI setPointer;
            /* Save weapon group configuration */
            for (int i = 0; i < ((ShipAPI)this.getState(RTS_ParseInput.stNames.playerShipHold)).getWeaponGroupsCopy().size(); i++) {
                setPointer = ((ShipAPI)this.getState(RTS_ParseInput.stNames.playerShipHold)).getWeaponGroupsCopy().get(i);
                this.weaponGroups.put(setPointer, setPointer.isAutofiring());
                if (setPointer.equals(((ShipAPI)this.getState(RTS_ParseInput.stNames.playerShipHold)).getSelectedGroupAPI())){
                    this.activeWeaponGroup = i;
                }
            }
            /* Handle autopause */
            if ((boolean)this.getState(RTS_ParseInput.stNames.pauseUnpause)
                    && !((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isPaused())
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).setPaused(true);
            /* Store player ship */
            this.setState(
                    RTS_ParseInput.stNames.playerShipHold,
                    ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip()
            );
            /* Add player ship to selection */
            List<ShipAPI> hold = new ArrayList();
            hold.add((ShipAPI)this.getState(RTS_ParseInput.stNames.playerShipHold));
            this.setState(
                    RTS_ParseInput.stNames.currentSelection,
                    hold
            );
        }
        /* We've switched to Vanilla mode */
        else {
            if (((ShipAPI)this.getState(RTS_ParseInput.stNames.playerShipHold)).getName().equals("Command Shuttle"))
                return;
            /* Reset all RTS buffers */
            this.setState(RTS_ParseInput.stNames.rightClickStore, null);
            this.setState(RTS_ParseInput.stNames.leftClickStore, null);
            this.setState(RTS_ParseInput.stNames.currentSelection, null);
            this.setDeepState(
                    Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.rightClickDelta),
                    null
            );
            ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).prematurelyEndEvent();
            /* Handle autopause */
            if ((boolean)this.getState(RTS_ParseInput.stNames.pauseUnpause)
                    && ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isPaused())
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).setPaused(false);
            /* Reset main ship widget */
            ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener))
                    .showShipDetails(Arrays.asList((ShipAPI)this.getState(RTS_ParseInput.stNames.playerShipHold)));
            /* Delete any player ship assignments */
            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager))
                    .deleteAssignments(
                            Arrays.asList((ShipAPI)this.getState(RTS_ParseInput.stNames.playerShipHold)),
                            true
                    );
            /*
            * When switching back to Vanilla mode, until the player creates input, the ship is still under AI control.
            * Here we create an event that waits for the ship to be under player control and then reconfigures the weapon
            * configuration he had when he switched to RTSmode, if he did.
            * */
            if (this.changeGroupEventString != null)
                ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).deleteEvent(this.changeGroupEventString);
            if (this.activeWeaponGroup != null) {
                this.changeGroupEventString = ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).addEvent(new RTS_Event() {
                    ShipAPI ship = (ShipAPI)getState(RTS_ParseInput.stNames.playerShipHold);

                    @Override
                    public boolean shouldExecute(Object state) {
                        return (ship.getAI() == null);
                    }

                    @Override
                    public void run() {
                        for (WeaponGroupAPI set : ship.getWeaponGroupsCopy()) {
                            if (weaponGroups.containsKey(set)) {
                                if (weaponGroups.get(set))
                                    set.toggleOn();
                                else
                                    set.toggleOff();
                            }
                        }
                        ship.giveCommand(ShipCommand.SELECT_GROUP, null, activeWeaponGroup);
                        changeGroupEventString = null;
                    }
                });
            }
        }
        /* ?possibly prevent locked strafing on mode switch? */
        HashMap<String, Boolean> strafeKeys = (HashMap<String, Boolean>)getState(RTS_ParseInput.stNames.strafeKeys);
        strafeKeys.put(RTS_ParseInput.skNames.left, false);
        strafeKeys.put(RTS_ParseInput.skNames.right, false);
        strafeKeys.put(RTS_ParseInput.skNames.up, false);
        strafeKeys.put(RTS_ParseInput.skNames.down, false);
    }

    public void makePlayerShipAi() {
        if ((Boolean)this.getState(RTS_ParseInput.stNames.RTSMode)) {
            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine))
                    .getCombatUI().setDisablePlayerShipControlOneFrame(true);
            if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip().getAI() == null) {
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip().resetDefaultAI();
            }
        }
        else if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip().getAI() != null)
            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip()
                    .blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
    }

    public void firstFrameOfBattle() {
        this.setState(
                RTS_ParseInput.stNames.playerShipHold,
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip()
        );
        if (!(boolean)this.getState(RTS_ParseInput.stNames.RTSMode)
                && ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine))
                        .getPlayerShip().getName().equals("Command Shuttle")
        )
            this.setState(RTS_ParseInput.stNames.RTSMode, true);
        this.buildListeners();
    }

    public void playerShipEntersMapAfterFirstFrame(ShipAPI ship) {
        this.setState(RTS_ParseInput.stNames.playerShipHold, ship);
    }

    public void playerDies() {
        if (!(boolean)this.getState(RTS_ParseInput.stNames.RTSMode)) {
            this.setState(RTS_ParseInput.stNames.RTSMode, true);
            if (!((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isPaused())
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).setPaused(true);
        }
        this.setState(RTS_ParseInput.stNames.playerShipHold, this.getState(RTS_TaskManager.stNames.commandShuttle));
    }

    public void playerSwitchesToDifferentShip() {
        if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShipPlayerIsTransferringCommandTo() != null)
            this.newPlayerShip = true;
        else if (this.newPlayerShip) {
            for (ShipAPI ship : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips()) {
                if (((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).dontSelect(ship))
                    continue;
                if (ship.getCaptain().isPlayer()) {
                    this.setState(RTS_ParseInput.stNames.playerShipHold, ship);
                    this.newPlayerShip = false;
                    break;
                }
            }
        }
    }
}