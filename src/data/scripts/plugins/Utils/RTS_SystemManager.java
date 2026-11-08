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

package data.scripts.plugins.Utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import data.scripts.plugins.RTSAssist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RTS_SystemManager extends RTS_StatefulClasses {

    public RTS_SystemManager (Object state) {
        super(state, true);
        this.populateNaughtyShips();
    }

    HashMap<String, String> eventHold = new HashMap<>();
    List<String> NO = new ArrayList<>();

    public void queueSystemActivation (ShipAPI ship) {
        if (
                this.NO.contains(ship.getHullSpec().getHullId())
                || ship.getSystem() == null
        )
            return;
        ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager, true)).playBasicUISound(
                "GeneralUIFeedback01",
                1f,
                0.2f
        );
        if (
                ship.getSystem().getState() == ShipSystemAPI.SystemState.IN
                || ship.getSystem().getState() == ShipSystemAPI.SystemState.OUT
        )
            return;
        if (this.eventHold.containsKey(ship.getId())) {
            ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager, true))
                    .deleteEvent(eventHold.get(ship.getId()));
        }
        if (ship.getSystem().getState() == ShipSystemAPI.SystemState.ACTIVE) {
            if (ship.getSystem().getSpecAPI().isToggle()) {
                ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT, 0f);
                ship.setCustomData(RTSAssist.shipCNames.overrideBlockSystem, null);
                if (this.eventHold.containsKey(ship.getId())) {
                    ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager, true))
                            .deleteEvent(this.eventHold.get(ship.getId()));
                    this.eventHold.remove(ship.getId());
                }
            }
            return;
        }
        ship.setCustomData(RTSAssist.shipCNames.overrideBlockSystem, true);
        this.eventHold.put(
                ship.getId(),
                ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager, true))
                        .addEvent(new RTS_Event() {
                    float holdTime = (float)getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.elapsedPlay), true);
                    ShipAPI shipHold = ship;
                    boolean jobDone = false;


                    @Override
                    public boolean shouldExecute(Object state) {
                        if (ship.isHulk()) {
                            ((RTS_EventManager)getState(RTSAssist.stNames.eventManager, true)).deleteEvent(eventHold.get(ship.getId()));
                            return (false);
                        }
                        if (this.shipHold.getSystem().isActive())
                            this.jobDone = true;
                        if (this.jobDone && !this.shipHold.getSystem().isActive())
                            return (true);
                        float currentTime = (float)getDeepState(
                                Arrays.asList(
                                    RTSAssist.stNames.amount,
                                    RTSAssist.amNames.elapsedPlay),
                                true
                        );
                        if (!this.jobDone && (currentTime - this.holdTime > 0.2f))
                            return (true);
                        else if (this.shipHold.getSystem().canBeActivated())
                            this.shipHold.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
                        return (false);
                    }

                    @Override
                    public void run() {
                        shipHold.setCustomData(RTSAssist.shipCNames.overrideBlockSystem, null);
                        eventHold.remove(shipHold.getId());
                    }
                }
        ));
    }

    private void populateNaughtyShips() {
        this.NO.add("ii_titan");
    }
}
