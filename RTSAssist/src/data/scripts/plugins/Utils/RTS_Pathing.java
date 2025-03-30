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

import org.lwjgl.util.vector.Vector2f;
import java.util.ArrayList;
import java.util.HashMap;

import com.fs.starfarer.api.combat.*;

import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.MathUtils;

public class RTS_Pathing extends RTS_StatefulClasses {

    public RTS_Pathing(Object state) {
        super(state, true);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put("searchAreaForN", 10000f);
            put("searchCoolDown", 0.1f);
            put("castDistance", 1000f);
            put("castWidthDelta", 2f);
            put("isStopped", null);
            put("targetEnemy", null);
        }};
        this.setState(init);
    }

    private Vector2f newVecHold = null;
    private float elapsedTime = 0;

    public Vector2f adjustDestCoord(ShipAPI ship, Vector2f coOrd, ShipAPI targetEnemy) {
        if (targetEnemy != null)
            this.setState("targetEnemy", targetEnemy);
        if (checkStopStatus(ship))
            return((Vector2f)this.getState("isStopped"));
        if (checkAdjustedStatus(ship))
            return(findAdjustedRoute(ship, coOrd));
        return(findStandardRoute(ship, coOrd));
    }

    private boolean checkAdjustedStatus(ShipAPI ship) {
        HashMap<String, Vector2f> adjustedShips = (HashMap<String, Vector2f>)this.getState("adjustedShips", true);
        for (HashMap.Entry<String, Vector2f> set: adjustedShips.entrySet())
            if (set.getKey().equals(ship.getId()))
                return (true);
        return (false);
    }

    private boolean checkStopStatus(ShipAPI ship) {
        ArrayList<String> stoppedShips =  (ArrayList<String>)this.getState("stoppedShips", true);
        for(int i = 0; i < stoppedShips.size(); i++)
            if (stoppedShips.get(i).equals(ship.getId())) {
                if (this.getState("isStopped") == null)
                    this.setState("isStopped", new Vector2f(
                            ship.getLocation().getX(),
                            ship.getLocation().getY()
                    ));
                return(true);
            }
        return(false);
    }

    private Vector2f findAdjustedRoute(ShipAPI ship, Vector2f coOrd) {
        if (!(newVecHold != null && (findCollisions(ship, newVecHold) != 0)))
            return(coOrd);
        if (((((CombatEngineAPI)this.getState("engine", true)).getTotalElapsedTime(true) - elapsedTime) <
                (float)this.getState("searchCoolDown")) && (newVecHold != null))
            return (newVecHold); // pointless
        elapsedTime = ((CombatEngineAPI)this.getState("engine", true)).getTotalElapsedTime(true);
        Vector2f newVector = null;
        float castWidthDelta = (float)this.getState("castWidthDelta");
        float CWDMagnitude = 0;
        Vector2f rotatedCoOrd = new Vector2f();
        Vector2f searchWindow = ((HashMap<String, Vector2f>)this.getState("adjustedShips", true)).get(ship.getId());
        while (newVector == null) {
            rotatedCoOrd.set(coOrd.getX(), coOrd.getY());
            CWDMagnitude++;
            rotatedCoOrd = VectorUtils.rotateAroundPivot(rotatedCoOrd, ship.getLocation(), (CWDMagnitude * castWidthDelta));
            if (((VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), rotatedCoOrd)) < searchWindow.getX())
                    && (VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), rotatedCoOrd)) > searchWindow.getY()))
                    && (findCollisionsNoReport(ship, rotatedCoOrd) == 0)) {
                newVector = rotatedCoOrd;
                this.setState("isStopped", null);
                break;
            }
            rotatedCoOrd = VectorUtils.rotateAroundPivot(rotatedCoOrd, ship.getLocation(), ((CWDMagnitude * castWidthDelta) * -2));
            if (((VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), rotatedCoOrd)) < searchWindow.getX())
                    && (VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), rotatedCoOrd)) > searchWindow.getY()))
                    && (findCollisionsNoReport(ship, rotatedCoOrd) == 0)) {
                newVector = rotatedCoOrd;
                this.setState("isStopped", null);
                break;
            }
            if ((castWidthDelta * CWDMagnitude) >= 180f) {
                if (this.getState("isStopped") != null)
                    newVector = (Vector2f)this.getState("isStopped");
                else {
                    newVector = new Vector2f(
                            ship.getLocation().getX(),
                            ship.getLocation().getY()
                    );
                    this.setState("isStopped", newVector);
                }
                break;
            }
        }
        newVecHold = newVector;
        return(newVector);
    }

    private Vector2f findStandardRoute(ShipAPI ship, Vector2f coOrd) {
        if (findCollisions(ship, coOrd) == -1)
            return(coOrd);
        if ((findCollisions(ship, coOrd) == 0) && !(newVecHold != null && (findCollisions(ship, newVecHold) != 0)))
            return(coOrd);
        if (((((CombatEngineAPI)this.getState("engine", true)).getTotalElapsedTime(true) - elapsedTime) <
                (float)this.getState("searchCoolDown")) && (newVecHold != null))
            return (newVecHold); // pointless
        elapsedTime = ((CombatEngineAPI)this.getState("engine", true)).getTotalElapsedTime(true);
        Vector2f newVector = null;
        float castWidthDelta = (float)this.getState("castWidthDelta");
        float CWDMagnitude = 0;
        Vector2f rotatedCoOrd = new Vector2f();
        while (newVector == null) {
            rotatedCoOrd.set(coOrd.getX(), coOrd.getY());
            CWDMagnitude++;
            rotatedCoOrd = VectorUtils.rotateAroundPivot(rotatedCoOrd, ship.getLocation(), (CWDMagnitude * castWidthDelta));
            if (findCollisions(ship, rotatedCoOrd) == 0) {
                rotatedCoOrd = VectorUtils.rotateAroundPivot(rotatedCoOrd, ship.getLocation(), 5f);
                newVector = rotatedCoOrd;
                this.setState("isStopped", null);
                break;
            }
            rotatedCoOrd = VectorUtils.rotateAroundPivot(rotatedCoOrd, ship.getLocation(), ((CWDMagnitude * castWidthDelta) * -2));
            if (findCollisions(ship, rotatedCoOrd) == 0) {
                rotatedCoOrd = VectorUtils.rotateAroundPivot(rotatedCoOrd, ship.getLocation(), -5f);
                newVector = rotatedCoOrd;
                this.setState("isStopped", null);
                break;
            }
            if ((castWidthDelta * CWDMagnitude) >= 180f) {
                if (this.getState("isStopped") != null)
                    newVector = (Vector2f)this.getState("isStopped");
                else {
                    newVector = new Vector2f(
                            ship.getLocation().getX(),
                            ship.getLocation().getY()
                    );
                    this.setState("isStopped", newVector);
                }
                break;
            }
        }
        newVecHold = newVector;
        return(newVector);
    }

    private float findCollisionsNoReport(ShipAPI ship, Vector2f coOrd) {
        ArrayList<ShipAPI> neighbours = ((RTS_ShipLocAPI)this.getState("ShipLocAPI", true)).
                findAlliedShipNeighbours(ship,(float)this.getState("searchAreaForN"));
        if (neighbours.isEmpty())
            return (0);
        for (int i = 0; i < neighbours.size(); i++)
            if (smallRadiusRCCast(ship, neighbours.get(i), coOrd) != 0)
                return (1);
        if (this.getState("targetEnemy") != null)
            if (smallRadiusRCCast(ship, (ShipAPI)this.getState("targetEnemy"), coOrd) != 0)
                return (1);
        return(0);
    }

    private float findCollisions(ShipAPI ship, Vector2f coOrd) {
        ArrayList<ShipAPI> neighbours = ((RTS_ShipLocAPI)this.getState("ShipLocAPI", true)).
                findAlliedShipNeighbours(ship, (float)this.getState("searchAreaForN"));
        if(neighbours.isEmpty())
            return(0);
        int hold;
        float flag = 0;
        for (int i = 0; i < neighbours.size(); i++) {
            hold = RCCast(ship, neighbours.get(i), coOrd);
            if (hold != 0) {
                if (hold == -1) {
                    ((RTS_CollisionManager)this.getState("RTSCollisionManager", true)).addCollision(
                            ship,
                            neighbours.get(i)
                    );
                    flag = -1;
                }
                else flag = 1;
            }
        }
        if (this.getState("targetEnemy") != null){
            hold = RCCast(ship, (ShipAPI)this.getState("targetEnemy"), coOrd);
            if (hold != 0) {
                if (hold == -1) {
                    ((RTS_CollisionManager)this.getState("RTSCollisionManager", true)).addCollision(
                            ship,
                            (ShipAPI)this.getState("targetEnemy")
                    );
                    flag = -1;
                }
                else flag = 1;
            }
        }
        return (flag);
    }

    private int smallRadiusRCCast(ShipAPI ship, ShipAPI testShip, Vector2f coOrd) {
        float rtTotal = 10f;
        Vector2f closestPoint = MathUtils.getNearestPointOnLine(testShip.getLocation(), ship.getLocation(), coOrd);
        float tShipToPointSquared = MathUtils.getDistanceSquared(testShip.getLocation(), closestPoint);
        float shipToPointSquared = MathUtils.getDistanceSquared(ship.getLocation(), closestPoint);
        float castDistance = (float)this.getState("castDistance") / 2;
        if ((tShipToPointSquared < (rtTotal * rtTotal))
                && (shipToPointSquared < castDistance * castDistance)) {
            if (VectorUtils.getAngle(ship.getLocation(), coOrd) - VectorUtils.getAngle(ship.getLocation(), testShip.getLocation()) > 0)
                return(2);
            else
                return(1);
        }
        return (0);
    }

    private int RCCast(ShipAPI ship, ShipAPI testShip, Vector2f coOrd) {
        float rtTotal = ship.getShieldRadiusEvenIfNoShield() + testShip.getShieldRadiusEvenIfNoShield();
        Vector2f closestPoint = MathUtils.getNearestPointOnLine(testShip.getLocation(), ship.getLocation(), coOrd);
        float tShipToPointSquared = MathUtils.getDistanceSquared(testShip.getLocation(), closestPoint);
        float shipToPointSquared = MathUtils.getDistanceSquared(ship.getLocation(), closestPoint);
        float shipToShipSquared = MathUtils.getDistanceSquared(ship.getLocation(), testShip.getLocation());
        if (shipToShipSquared < (rtTotal * rtTotal))
            return (-1);
        float castDistance = (float)this.getState("castDistance");
        if ((tShipToPointSquared < (rtTotal * rtTotal))
                && (shipToPointSquared < castDistance * castDistance)) {
            if (VectorUtils.getAngle(ship.getLocation(), coOrd) - VectorUtils.getAngle(ship.getLocation(), testShip.getLocation()) > 0)
                return(2);
            else
                return(1);
        }
        return (0);
    }
}