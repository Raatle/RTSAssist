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
import data.scripts.plugins.RTS_TaskManager;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RTS_MoveTogetherManager {

    public RTS_MoveTogetherManager (List<ShipAPI> selection, HashMap<String, Object> assignments) {
        this.assignments = assignments;
        HashMap<String, Object> assPointer;
        for (ShipAPI ship : selection) {
            assPointer = (HashMap<String, Object>)assignments.get(ship.getId());
            if (assPointer != null)
                if (
                        (RTS_TaskManager.assType)assPointer.get("assType") == RTS_TaskManager.assType.moveAndHold
                        || (RTS_TaskManager.assType)assPointer.get("assType") == RTS_TaskManager.assType.moveAndRelease
                        || (RTS_TaskManager.assType)assPointer.get("assType") == RTS_TaskManager.assType.dynamicHoldOnFlux
                        || (RTS_TaskManager.assType)assPointer.get("assType") == RTS_TaskManager.assType.attackMove
                ) {
                    this.group.add(ship);
                    this.assMatcher.put(ship, (String)assPointer.get("UUID"));
                }
        }
    }

    List<ShipAPI> group = new ArrayList<>();
    HashMap<String, Object> assignments;
    HashMap<ShipAPI, String> assMatcher = new HashMap<>();
    float targetPercentageOfAss = 0f;
    float slowestShip = 0f;
    boolean deleteMe = false;

    public void update () {
        this.slowestShip = 10000f;
        if (this.group.isEmpty())
            return;
        float dHold;
        float furthestShipDisSquared = 0f;
        ShipAPI furthestShip = null;
        HashMap<String, Object> assPointer;
        List<ShipAPI> cleanup = new ArrayList<>();
        for (ShipAPI ship : this.group) {
            if (this.slowestShip > ship.getMaxSpeed())
                this.slowestShip = ship.getMaxSpeed();
            assPointer = (HashMap<String, Object>)this.assignments.get(ship.getId());
            if (assPointer != null) {
                if (!this.assMatcher.get(ship).equals((String)assPointer.get("UUID"))) {
                    this.assMatcher.remove(ship);
                    cleanup.add(ship);
                }
                else {
                    dHold = (float)RTS_AssortedFunctions.getDistanceSquared(
                            ship.getLocation(),
                            (Vector2f)assPointer.get("dest")
                    );
                    if (furthestShipDisSquared < dHold) {
                        furthestShipDisSquared = dHold;
                        furthestShip = (ShipAPI)assPointer.get("ship");
                    }
                }
            }
            else
                cleanup.add(ship);
        }
        this.group.removeAll(cleanup);
        if (furthestShip != null)
            this.targetPercentageOfAss = RTS_AssortedFunctions.fastGetDistance(
                    furthestShip.getLocation(),
                    (Vector2f)((HashMap<String, Object>)this.assignments.get(furthestShip.getId())).get("dest")
            ) / (float)((Integer)((HashMap<String, Object>)this.assignments
                    .get(furthestShip.getId())).get("iniDisToAssignment"));

    }

    public void cleanup () {}

    public boolean queueForDeletion () {
        return (this.deleteMe || this.group.size() < 2);
    }

    public boolean addShip (List<ShipAPI> ships) {
        HashMap<String, Object> assPointer;
        for (ShipAPI ship : ships) {
            assPointer = (HashMap<String, Object>)this.assignments.get(ship.getId());
            this.assMatcher.put(ship, (String)assPointer.get("UUID"));
        }
        return (this.group.addAll(ships));
    }

    public boolean removeShip (List<ShipAPI> ships) {
        for (ShipAPI ship : ships)
            this.assMatcher.remove(ship);
        return (this.group.removeAll(ships));
    }

    public Vector2f returnNewCoOrd (ShipAPI ship, Vector2f coOrd, HashMap<String, Object> assignment) {
        if (!this.group.contains(ship))
            return (coOrd);
        float aheadVec = ((float)((Integer)assignment.get("iniDisToAssignment")) * this.targetPercentageOfAss)
                - MathUtils.clamp(this.slowestShip * 1.5f, 200f, 400f);
        if (aheadVec < 0f
                || ((RTS_AssortedFunctions.getDistanceSquared(ship.getLocation(), coOrd) - 200000f) > Math.pow(aheadVec, 2)))
            return coOrd;
        Vector2f vHold = VectorUtils.getDirectionalVector(coOrd, (Vector2f)assignment.get("assLocation"));
        vHold = VectorUtils.resize(
                vHold,
                Math.pow(aheadVec, 2) < RTS_AssortedFunctions.getDistanceSquared(ship.getLocation(), coOrd)
                        ? aheadVec
                        : RTS_AssortedFunctions.fastGetDistance(ship.getLocation(), coOrd) + 10f
        );
        return (new Vector2f(
                coOrd.getX() + vHold.getX(),
                coOrd.getY() + vHold.getY()
        ));
    }
}