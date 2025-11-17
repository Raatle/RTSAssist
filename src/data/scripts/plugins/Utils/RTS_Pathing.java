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

import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.RTS_TaskManager;
import org.lwjgl.util.vector.Vector2f;
import java.util.ArrayList;
import java.util.HashMap;

import com.fs.starfarer.api.combat.*;

import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.MathUtils;

public class RTS_Pathing extends RTS_StatefulClasses {

    public class classidentifiers {
        public String searchAreaForN = RTS_StatefulClasses.getUniqueIdentifier();
        public String searchCoolDown = RTS_StatefulClasses.getUniqueIdentifier();
        public String castDistance = RTS_StatefulClasses.getUniqueIdentifier();
        public String castWidthDelta = RTS_StatefulClasses.getUniqueIdentifier();
        public String isStopped = RTS_StatefulClasses.getUniqueIdentifier();
        public String targetEnemy = RTS_StatefulClasses.getUniqueIdentifier();

    }
    public classidentifiers stNames = new classidentifiers();

    public RTS_Pathing(Object state) {
        super(state, true);
        HashMap<String, Object> init = new HashMap<String, Object>();
        init.put(this.stNames.searchAreaForN, 10000f);
        init.put(this.stNames.searchCoolDown, 0.1f);
        init.put(this.stNames.castDistance, 1000f);
        init.put(this.stNames.castWidthDelta, 2f);
        init.put(this.stNames.isStopped, null);
        init.put(this.stNames.targetEnemy, null);;
        this.hullPriority.put("FRIGATE", 1f);
        this.hullPriority.put("DESTROYER", 2f);
        this.hullPriority.put("CRUISER", 3f);
        this.hullPriority.put("CAPITAL_SHIP", 4f);
        this.setState(init);
        init.clear();
    }

    private Vector2f newVecHold = null;
    private float elapsedTime = 0;
    private HashMap<String, Float> hullPriority = new HashMap<>();

    public Vector2f adjustDestCoord(ShipAPI ship, Vector2f coOrd, ShipAPI targetEnemy) {
        if (targetEnemy != null)
            this.setState(this.stNames.targetEnemy, targetEnemy);
        if (handleStopStatus(ship))
            return((Vector2f)this.getState(this.stNames.isStopped));
        else if (((HashMap<String, Object>)this.getState(
                RTS_CollisionManager.stNames.adjustedShips,
                true
        )).containsKey(ship.getId()))
            return(findAdjustedRoute(ship, coOrd));
        else
            return(findStandardRoute(ship, coOrd));
    }

    public void cleanUp() {
        this.cleanState();
        this.hullPriority.clear();
    }

    private boolean handleStopStatus(ShipAPI ship) {
        for (String id : (ArrayList<String>)this.getState(RTS_CollisionManager.stNames.stoppedShips ,true))
            if (id.equals(ship.getId())) {
                if (this.getState(this.stNames.isStopped) == null)
                    this.setState(this.stNames.isStopped, new Vector2f(
                            ship.getLocation().getX(),
                            ship.getLocation().getY()
                    ));
                return(true);
            }
        return(false);
    }

    private Vector2f findStandardRoute(ShipAPI ship, Vector2f coOrd) {
        if (findCollisions(ship, coOrd) == -1)
            return(coOrd);
        if ((findCollisions(ship, coOrd) == 0) && !(newVecHold != null && (findCollisions(ship, newVecHold) != 0)))
            return(coOrd);
        if (((((CombatEngineAPI)this.getState(RTSAssist.stNames.engine, true))
                .getTotalElapsedTime(true) - elapsedTime)
                < (float)this.getState(this.stNames.searchCoolDown))
                && (newVecHold != null)
        )
            return (newVecHold); // ?pointless?
        elapsedTime = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine, true))
                .getTotalElapsedTime(true);
        Vector2f newVector = null;
        float castWidthDelta = (float)this.getState(this.stNames.castWidthDelta);
        float CWDMagnitude = 0;
        Vector2f rotatedCoOrd = new Vector2f();
        while (true) {
            rotatedCoOrd.set(coOrd.getX(), coOrd.getY());
            CWDMagnitude++;
            rotatedCoOrd = VectorUtils.rotateAroundPivot(
                    rotatedCoOrd,
                    ship.getLocation(),
                    (CWDMagnitude * castWidthDelta)
            );
            if (findCollisions(ship, rotatedCoOrd) == 0) {
                rotatedCoOrd = VectorUtils.rotateAroundPivot(rotatedCoOrd, ship.getLocation(), 5f);
                newVector = rotatedCoOrd;
                this.setState(this.stNames.isStopped, null);
                break;
            }
            rotatedCoOrd = VectorUtils.rotateAroundPivot(
                    rotatedCoOrd,
                    ship.getLocation(),
                    ((CWDMagnitude * castWidthDelta) * -2)
            );
            if (findCollisions(ship, rotatedCoOrd) == 0) {
                rotatedCoOrd = VectorUtils.rotateAroundPivot(rotatedCoOrd, ship.getLocation(), -5f);
                newVector = rotatedCoOrd;
                this.setState(this.stNames.isStopped, null);
                break;
            }
            if ((castWidthDelta * CWDMagnitude) >= 180f) {
                if (this.getState(this.stNames.isStopped) != null)
                    newVector = (Vector2f)this.getState(this.stNames.isStopped);
                else {
                    newVector = new Vector2f(
                            ship.getLocation().getX(),
                            ship.getLocation().getY()
                    );
                    this.setState(this.stNames.isStopped, newVector);
                }
                break;
            }
        }
        newVecHold = newVector;
        return(newVector);
    }

    private Vector2f findAdjustedRoute(ShipAPI ship, Vector2f coOrd) {
        if (!(newVecHold != null && (findCollisions(ship, newVecHold) != 0)))
            return(coOrd);

        if (((((CombatEngineAPI)this.getState(RTSAssist.stNames.engine, true))
                .getTotalElapsedTime(true) - elapsedTime)
                <
                (float)this.getState(this.stNames.searchCoolDown))
                && (newVecHold != null)
        )
            return (newVecHold); // ?pointless? Possibly because all ships will still calculate their new paths on the same frame. However since this is dependant on user input most probably unlikely but still very possible. Replace with a scheduler.
        elapsedTime = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine, true))
                .getTotalElapsedTime(true);

        Vector2f newVector;
        float castWidthDelta = (float)this.getState(this.stNames.castWidthDelta);
        float CWDMagnitude = 0;
        Vector2f rotatedCoOrd = new Vector2f();
        Vector2f searchWindow = ((HashMap<String, Vector2f>)this.getState(
                RTS_CollisionManager.stNames.adjustedShips,
                true)
        ).get(ship.getId());
        while (true) {
            rotatedCoOrd.set(coOrd.getX(), coOrd.getY());
            CWDMagnitude++;
            rotatedCoOrd = VectorUtils.rotateAroundPivot(
                    rotatedCoOrd,
                    ship.getLocation(),
                    (CWDMagnitude * castWidthDelta)
            );
            if (((VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), rotatedCoOrd))
                    < searchWindow.getX())
                    && (VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), rotatedCoOrd))
                    > searchWindow.getY()))
                    && (findCollisionsNoReport(ship, rotatedCoOrd) == 0)
            ) {
                newVector = rotatedCoOrd;
                this.setState(this.stNames.isStopped, null);
                break;
            }
            rotatedCoOrd = VectorUtils.rotateAroundPivot(
                    rotatedCoOrd,
                    ship.getLocation(),
                    ((CWDMagnitude * castWidthDelta) * -2)
            );
            if (((VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), rotatedCoOrd))
                    < searchWindow.getX())
                    && (VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), rotatedCoOrd))
                    > searchWindow.getY()))
                    && (findCollisionsNoReport(ship, rotatedCoOrd) == 0)
            ) {
                newVector = rotatedCoOrd;
                this.setState(this.stNames.isStopped, null);
                break;
            }
            if ((castWidthDelta * CWDMagnitude) >= 180f) {
                if (this.getState(this.stNames.isStopped) != null)
                    newVector = (Vector2f)this.getState(this.stNames.isStopped);
                else {
                    newVector = new Vector2f(
                            ship.getLocation().getX(),
                            ship.getLocation().getY()
                    );
                    this.setState(this.stNames.isStopped, newVector);
                }
                break;
            }
        }
        newVecHold = newVector;
        return(newVector);
    }

    private float findCollisions(ShipAPI ship, Vector2f coOrd) {
        ArrayList<ShipAPI> neighbours = ((RTS_ShipLocAPI)this.getState(
                RTS_TaskManager.stNames.ShipLocAPI,
                true
        )).findAlliedShipNeighbours(ship, (float)this.getState(this.stNames.searchAreaForN), false);
        if (neighbours.isEmpty())
            return(0);
        int hold;
        float flag = 0;
        for (ShipAPI neighbour : neighbours) {
            hold = RCCast(ship, neighbour, coOrd);
            if (hold != 0) {
                if (hold == -1) {
                    ((RTS_CollisionManager)this.getState(
                            RTS_TaskManager.stNames.RTSCollisionManager,
                            true
                    )).addCollision(
                            ship,
                            neighbour
                    );
                    flag = -1;
                }
                else flag = 1;
            }
        }
        if (this.getState(this.stNames.targetEnemy) != null){
            hold = RCCast(ship, (ShipAPI)this.getState(this.stNames.targetEnemy), coOrd);
            if (hold != 0) {
                if (hold == -1) {
                    ((RTS_CollisionManager)this.getState(
                            RTS_TaskManager.stNames.RTSCollisionManager,
                            true
                    )).addCollision(
                            ship,
                            (ShipAPI)this.getState(this.stNames.targetEnemy)
                    );
                    flag = -1;
                }
                else flag = 1;
            }
        }
        return (flag);
    }

    private float findCollisionsNoReport(ShipAPI ship, Vector2f coOrd) {
        ArrayList<ShipAPI> neighbours = ((RTS_ShipLocAPI)this.getState(
                RTS_TaskManager.stNames.ShipLocAPI,
                true
        )).findAlliedShipNeighbours(ship,(float)this.getState(this.stNames.searchAreaForN), false);
        if (neighbours.isEmpty())
            return (0);
        for (int i = 0; i < neighbours.size(); i++)
            if (smallRadiusRCCast(ship, neighbours.get(i), coOrd) != 0)
                return (1);
        if (this.getState(this.stNames.targetEnemy) != null)
            if (smallRadiusRCCast(ship, (ShipAPI)this.getState(this.stNames.targetEnemy), coOrd) != 0)
                return (1);
        return(0);
    }

    private int RCCast(ShipAPI ship, ShipAPI testShip, Vector2f coOrd) {
        if (this.hullPriority.get(ship.getHullSize().name()) > this.hullPriority.get(testShip.getHullSize().name()))
            return (0);
        float rtTotal = ship.getShieldRadiusEvenIfNoShield() + testShip.getShieldRadiusEvenIfNoShield();
        Vector2f closestPoint = MathUtils.getNearestPointOnLine(testShip.getLocation(), ship.getLocation(), coOrd);
        float tShipToPointSquared = MathUtils.getDistanceSquared(testShip.getLocation(), closestPoint);
        float shipToPointSquared = MathUtils.getDistanceSquared(ship.getLocation(), closestPoint);
        float shipToShipSquared = MathUtils.getDistanceSquared(ship.getLocation(), testShip.getLocation());
        if (shipToShipSquared < (rtTotal * rtTotal))
            return (-1);
        float castDistance = (float)this.getState(this.stNames.castDistance);
        if ((tShipToPointSquared < (rtTotal * rtTotal))
                && (shipToPointSquared < castDistance * castDistance)) {
            if (
                    VectorUtils.getAngle(ship.getLocation(), coOrd)
                    - VectorUtils.getAngle(ship.getLocation(), testShip.getLocation())
                    > 0
            )
                return(2);
            else
                return(1);
        }
        return (0);
    }

    private int smallRadiusRCCast(ShipAPI ship, ShipAPI testShip, Vector2f coOrd) {
        if (this.hullPriority.get(ship.getHullSize().name()) > this.hullPriority.get(testShip.getHullSize().name()))
            return (0);
        float rtTotal = 10f;
        Vector2f closestPoint = MathUtils.getNearestPointOnLine(testShip.getLocation(), ship.getLocation(), coOrd);
        float tShipToPointSquared = MathUtils.getDistanceSquared(testShip.getLocation(), closestPoint);
        float shipToPointSquared = MathUtils.getDistanceSquared(ship.getLocation(), closestPoint);
        float castDistance = (float)this.getState(this.stNames.castDistance) / 2;
        if ((tShipToPointSquared < (rtTotal * rtTotal))
                && (shipToPointSquared < castDistance * castDistance)) {
            if (
                    VectorUtils.getAngle(ship.getLocation(), coOrd)
                            - VectorUtils.getAngle(ship.getLocation(), testShip.getLocation())
                            > 0
            )
                return(2);
            else
                return(1);
        }
        return (0);
    }
}