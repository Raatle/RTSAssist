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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.combat.entities.Ship;
import com.fs.starfarer.coreui.refit.WeaponGroupDialog;
import com.fs.starfarer.coreui.refit.WeaponPickerDialog;
import com.fs.starfarer.coreui.refit.wgd2.WeaponGroupDialogV2;
import data.scripts.plugins.AISystems.RTS_AIInjector;
import data.scripts.plugins.RTS_SelectionListener;
import data.scripts.plugins.RTS_TaskManager;
import org.lazywizard.lazylib.combat.WeaponUtils;

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
        newShipListenerID = ((RTS_EventManager)this.getState("eventManager")).addListener(new RTS_Listener() {
            @Override
            public String type() {
                return ("newShipsDeployed");
            }

            @Override
            public void run(HashMap<String, Object> e) {
                List<ShipAPI> newShips = (List<ShipAPI>)e.get("newShipsDeployed");
                for (ShipAPI ship : newShips)
                    if (ship.getCaptain() != null
                            && ship.getCaptain().isPlayer()
                            && !((RTS_SelectionListener)getState("selectionListener")).dontSelect(ship) // Some mods like to create ship modules that inherit their pilots capapbilities
                    )
                        playerShipEntersMapAfterFirstFrame(ship);
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
                List<ShipAPI> deadShips = (List<ShipAPI>)e.get("shipsNoLongerActive");
                for (ShipAPI ship : deadShips) {
                    if (ship.getCaptain() != null
                            && ship.getCaptain().isPlayer()
                            && !((RTS_SelectionListener)getState("selectionListener")).dontSelect(ship, true) // Some mods like to create ship modules(armour plates in kol) that inherit their pilots capapbilities
                    )
                        playerDies();
                }
                e.clear();
            }

            @Override
            public boolean removeOnCompletion() {
                return (false);
            }
        });
    }

    public void playerChangesMode() {
        if ((boolean)this.getState("RTSMode")
                && ((ShipAPI)this.getState("playerShipHold")).getName().equals("Command Shuttle"))
            return;
        this.setState("RTSMode", !(Boolean)this.getState("RTSMode"));
        if ((boolean)this.getState("RTSMode")) {
            this.weaponGroups.clear();
            WeaponGroupAPI setPointer;
            for (int i = 0; i < ((ShipAPI)this.getState("playerShipHold")).getWeaponGroupsCopy().size(); i++) {
                setPointer = ((ShipAPI)this.getState("playerShipHold")).getWeaponGroupsCopy().get(i);
                this.weaponGroups.put(setPointer, setPointer.isAutofiring());
                if (setPointer.equals(((ShipAPI)this.getState("playerShipHold")).getSelectedGroupAPI())){
                    this.activeWeaponGroup = i;
                }
            }
            if ((boolean)this.getState("pauseUnpause")
                    && !((CombatEngineAPI)this.getState("engine")).isPaused())
                ((CombatEngineAPI)this.getState("engine")).setPaused(true);
            this.setState("playerShipHold", ((CombatEngineAPI)this.getState("engine")).getPlayerShip());
            List<ShipAPI> shipHold = new ArrayList<>();
            shipHold.add((ShipAPI)this.getState("playerShipHold"));
            this.setState(
                    "currentSelection",
                    shipHold
            );
        }
        else {
            if (((ShipAPI)this.getState("playerShipHold")).getName().equals("Command Shuttle"))
                return;
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
                    .deleteAssignments(Arrays.asList((ShipAPI)this.getState("playerShipHold")), true);
            if (changeGroupEventString != null)
                ((RTS_EventManager)this.getState("eventManager")).deleteEvent(changeGroupEventString);
            if (this.activeWeaponGroup != null) {
                ((RTS_EventManager)this.getState("eventManager")).addEvent(new RTS_Event() {
                    ShipAPI ship = (ShipAPI)getState("playerShipHold");
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
    }

    public void makePlayerShipAi() {
        if ((Boolean)this.getState("RTSMode")) {
            ((CombatEngineAPI)this.getState("engine")).getCombatUI().setDisablePlayerShipControlOneFrame(true);
            if (((CombatEngineAPI)this.getState("engine")).getPlayerShip().getAI() == null) {
                ((CombatEngineAPI)this.getState("engine")).getPlayerShip().resetDefaultAI();
            }
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

    public void playerDies() {
        if (!(boolean)this.getState("RTSMode")) {
            this.setState("RTSMode", true);
            if (!((CombatEngineAPI)this.getState("engine")).isPaused())
                ((CombatEngineAPI)this.getState("engine")).setPaused(true);
        }
        this.setState("playerShipHold", this.getState("commandShuttle"));
    }

    public void playerSwitchesToDifferentShip() {
        if (((CombatEngineAPI)this.getState("engine")).getShipPlayerIsTransferringCommandTo() != null)
            this.newPlayerShip = true;
        else if (this.newPlayerShip) {
            for (ShipAPI ship : ((CombatEngineAPI)this.getState("engine")).getShips()) {
                if (((RTS_SelectionListener)this.getState("selectionListener")).dontSelect(ship))
                    continue;
                if (ship.getCaptain().isPlayer()) {
                    this.setState("playerShipHold", ship);
                    this.newPlayerShip = false;
                    break;
                }

            }
        }
    }
}