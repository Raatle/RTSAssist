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

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;

import com.fs.starfarer.api.combat.ShipAPI;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class RTS_CollisionManager extends RTS_StatefulClasses {

    public static class classidentifiers {
        public String stoppedShips = RTS_StatefulClasses.getUniqueIdentifier();
        public String adjustedShips = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static classidentifiers stNames = new classidentifiers();

    public RTS_CollisionManager(Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put(RTS_CollisionManager.stNames.stoppedShips, new ArrayList<String>());
            put(RTS_CollisionManager.stNames.adjustedShips, new HashMap<String, Vector2f>());
        }};
        this.setState(init);
    }

    private HashMap<String, ArrayList<ShipAPI>> collidedShips = new HashMap<>();
    private HashMap<String, ArrayList<ShipAPI>> collatedCollisions = new HashMap<>();


    public void update() {
        preProcess();
        processCollisions();
        postProcess();
    }

    private void preProcess() {
        ((ArrayList<String>)this.getState(RTS_CollisionManager.stNames.stoppedShips)).clear();
        ((HashMap<String, Vector3f>)this.getState(RTS_CollisionManager.stNames.adjustedShips)).clear();
    }

    private void postProcess() {
        for (HashMap.Entry<String, ArrayList<ShipAPI>> set: collidedShips.entrySet()) {
            set.getValue().clear();
        }
        collidedShips.clear();
        for (HashMap.Entry<String, ArrayList<ShipAPI>> set: collatedCollisions.entrySet()) {
            set.getValue().clear();
        }
        collatedCollisions.clear();
    }

    private void processCollisions() {
        collateShips();
        Vector2f resultantVector;
        ShipAPI shipPointer;
        ShipAPI tShipPointer;
        float holdAngle;
        float holdTAngle;
        float dHigh;
        float dLow;
        for(HashMap.Entry<String, ArrayList<ShipAPI>> set : collatedCollisions.entrySet()) {
            shipPointer = set.getValue().get(0);
            tShipPointer = set.getValue().get(1);
            holdAngle = VectorUtils.getAngle(shipPointer.getLocation(), tShipPointer.getLocation());
            dHigh = 0f;
            dLow = 0f;
            for(int i = 2; i < set.getValue().size(); i++) {
                tShipPointer = set.getValue().get(i);
                holdTAngle = MathUtils.getShortestRotation(holdAngle, VectorUtils.getAngle(shipPointer.getLocation(), tShipPointer.getLocation()));
                if (holdTAngle > dHigh) dHigh = holdTAngle;
                if (holdTAngle < dLow) dLow = holdTAngle;
            }
            if ((dHigh + Math.abs(dLow)) >= 150f) {
                ((ArrayList<String>)this.getState(RTS_CollisionManager.stNames.stoppedShips)).add(set.getValue().get(0).getId());
                continue;
            }
            tShipPointer = set.getValue().get(1);
            if ((dHigh - Math.abs(dLow)) > 0f) {
                resultantVector = VectorUtils.rotate(VectorUtils.getDirectionalVector(shipPointer.getLocation(), tShipPointer.getLocation()),
                        ((dHigh - ((dHigh + Math.abs(dLow)) / 2f))) + 180f);
            }
            else
                resultantVector = VectorUtils.rotate(VectorUtils.getDirectionalVector(shipPointer.getLocation(), tShipPointer.getLocation()),
                        180f);
            ((HashMap<String, Vector2f>)this.getState(RTS_CollisionManager.stNames.adjustedShips)).put(set.getValue().get(0).getId(), new Vector2f(
                    VectorUtils.getFacing(resultantVector) + ((150f - ((dHigh + Math.abs(dLow))/2))/2),
                    VectorUtils.getFacing(resultantVector) - ((150f - ((dHigh + Math.abs(dLow))/2))/2)
            ));
        }
    }

    private void collateShips() {
        for(HashMap.Entry<String, ArrayList<ShipAPI>> set : collidedShips.entrySet()) {
            if (collatedCollisions.get(set.getKey()) == null) {
                collatedCollisions.put(set.getKey(), new ArrayList<ShipAPI>());
                collatedCollisions.get(set.getKey()).add(0, set.getValue().get(0));
            }
            collatedCollisions.get(set.getKey()).add(set.getValue().get(1));
        }
    }

    public void addCollision(ShipAPI ship, ShipAPI tShip) {
        ArrayList<ShipAPI> hold = new ArrayList<>();
        hold.add(0, ship);
        hold.add(1, tShip);
        collidedShips.put(ship.getId(), hold);
    }
}