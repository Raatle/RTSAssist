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

package data.scripts.plugins.DevTools;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RTS_ShipTestSuite extends RTS_StatefulClasses {
    public RTS_ShipTestSuite(Object state) {
        super(state, true);
    }

    String testShipSpecId = "diable_riptide_assault";

    Iterator<String> shipList = new ArrayList<String>(){{
        add("eis_vanguard_xiv_elite");
    }}.iterator();

    List<String> problemList = new ArrayList<String>(){{
        add("eis_valorous_bond");
        add("eis_tyrant_standard");
        add("eis_indomitable_assault");
        add("eis_flagellator_elite");
        add("eis_audacious_overdriven");
        add("eis_endeavor_attack");
    }};

    public void spawnShipIter(Vector2f worldSpace) {
        if (shipList.hasNext())
            ((CombatEngineAPI)this.getState("engine", true)).getFleetManager(0).spawnShipOrWing(
                    shipList.next(),
                    worldSpace, 90f
            );
    }

    public void spawnShip(Vector2f worldSpace) {
        String specId = this.testShipSpecId;
        ((CombatEngineAPI)this.getState("engine", true)).getFleetManager(0).spawnShipOrWing(specId, worldSpace, 90f);
    }

    public void soutShipDetails (List<ShipAPI> currentSelection) {
        if (currentSelection == null)
            return;
        for (ShipAPI ship : currentSelection) {
            System.out.println("START");
            System.out.println("getName: " + ship.getName());
            System.out.println("getId: " + ship.getId());
            System.out.println("getHullSize -> name: " + ship.getHullSize().name());
            System.out.println("getHullSpec -> getBaseHullId: " + ship.getHullSpec().getBaseHullId());
            System.out.println("getSystem -> getId: " + ship.getSystem().getId());
            System.out.println("END");
        }
    }

    public void deleteShips(List<ShipAPI> selection) {
        for (ShipAPI ship : selection)
            ((CombatEngineAPI)this.getState("engine", true)).getFleetManager(0).removeDeployed(ship, false);

    }
}























