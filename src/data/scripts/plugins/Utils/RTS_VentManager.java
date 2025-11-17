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

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import data.scripts.plugins.RTSAssist;

import java.util.ArrayList;
import java.util.List;

public class RTS_VentManager extends RTS_StatefulClasses {

    public RTS_VentManager (Object state) {
        super(state);
    }

    private List<ShipAPI> isVenting = new ArrayList<>();

    public void update () {
        if (this.isVenting.isEmpty())
            return;
        List<ShipAPI> shipsToRemove = new ArrayList<>();
        for (ShipAPI ship : this.isVenting) {
            if (ship.getFluxLevel() > 0.05f) {
                if (ship.getShield() != null && ship.getShield().isOn()) {
                    ship.getShield().toggleOff();
                    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                }
                else
                    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            else
                shipsToRemove.add(ship);
        }
        this.isVenting.remove(shipsToRemove);
        shipsToRemove.clear();
    }

    public void ventShip (ShipAPI ship) {
        ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                "GeneralUIFeedback01",
                1f,
                0.2f
        );
        /* Fix this */
        if (ship.getVariant().hasHullMod("safetyoverrides")) {
            return;
//            if (!this.isVenting.contains(ship))
//                this.isVenting.add(ship);
//            else
//                this.isVenting.remove(ship);
        }
        else
            ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
    }
}