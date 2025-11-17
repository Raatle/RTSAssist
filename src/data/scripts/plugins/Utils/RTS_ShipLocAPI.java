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
import org.lwjgl.util.vector.Vector2f;
import java.util.*;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class RTS_ShipLocAPI extends RTS_StatefulClasses {

    public static class classidentifiers {
        public String shipGridCellSize = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static classidentifiers stNames = new classidentifiers();

    public static class eventIdentifiers {
        public String newShipsDeployed = RTS_StatefulClasses.getUniqueIdentifier();
        public String shipsNoLongerActive = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static eventIdentifiers evNames = new eventIdentifiers();

    public RTS_ShipLocAPI(Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>(){{
            put(RTS_ShipLocAPI.stNames.shipGridCellSize, 500f);
        }};
        this.setState(init);
        getShipList();
        buildArenaGrid();
    }

    private List<ShipAPI> shipList = new ArrayList<>();
    private Vector2f arenaGrid;
    private HashMap<Float, mapCell> spatialMap;
    private HashMap<String, Float> inverseSpatialMap;

    private List<ShipAPI> addedShips = new ArrayList<>();
    private List<ShipAPI> removedShips = new ArrayList<>();
    private RTS_Event newShipsEvent = new RTS_Event() {
        @Override
        public String type() {
            return (RTS_ShipLocAPI.evNames.newShipsDeployed);
        }

        @Override
        public HashMap<String, Object> run(boolean waitForListener) {
            HashMap<String, Object> hold = new HashMap<>();
            hold.put(RTS_ShipLocAPI.evNames.newShipsDeployed, addedShips);
            return (hold);
        }
    };
    private RTS_Event lostShipsEvent = new RTS_Event() {
        @Override
        public String type() {
            return (RTS_ShipLocAPI.evNames.shipsNoLongerActive);
        }

        @Override
        public HashMap<String, Object> run(boolean waitForListener) {
            HashMap<String, Object> hold = new HashMap<>();
            hold.put(RTS_ShipLocAPI.evNames.shipsNoLongerActive, removedShips);
            return (hold);
        }
    };

    private class mapCell {
        Vector2f cellCoOrd;
        ArrayList<ShipAPI> cellShipList;
    }

    public void update() {
        getShipList();
        buildSpatialMap();
        buildInverseSpatialMap();
    }

    public ArrayList<ShipAPI> findAlliedShipNeighbours (ShipAPI ship, float radius, boolean precise) {
        return (precise
                ? this.addPrecision(ship.getLocation(), this.findAlliedShipNeighbours(ship, null, radius), radius)
                : this.findAlliedShipNeighbours(ship, null, radius)
        );
    }
    public ArrayList<ShipAPI> findAlliedShipNeighbours (Vector2f location, float radius, boolean precise) {
        return (precise
                ? this.addPrecision(location, this.findAlliedShipNeighbours(null, location, radius), radius)
                : this.findAlliedShipNeighbours(null, location, radius)
        );
    }
    private ArrayList<ShipAPI> findAlliedShipNeighbours (ShipAPI ship, Vector2f location, float radius) {
        ArrayList<ShipAPI> alliedShipNeighbours = ship == null
                ? findShipNeighbours(null, location, radius)
                : findShipNeighbours(ship, null, radius);
        ArrayList<ShipAPI> finalSubset = new ArrayList<>();
        for (ShipAPI unSortedShip : alliedShipNeighbours) {
            if (unSortedShip.getOriginalOwner() == 1)
                continue;
            finalSubset.add(unSortedShip);
        }
        alliedShipNeighbours.clear();
        return (finalSubset);
    }

    public ArrayList<ShipAPI> findEnemyShipNeighbours (ShipAPI ship, float radius, boolean precise) {
        return (precise
                ? this.addPrecision(ship.getLocation(), this.findEnemyShipNeighbours(ship, null, radius), radius)
                : this.findEnemyShipNeighbours(ship, null, radius)
        );
    }
    public ArrayList<ShipAPI> findEnemyShipNeighbours (Vector2f location, float radius, boolean precise) {
        return (precise
                ? this.addPrecision(location, this.findEnemyShipNeighbours(null, location, radius), radius)
                :this.findEnemyShipNeighbours(null, location, radius)
        );
    }
    private ArrayList<ShipAPI> findEnemyShipNeighbours (ShipAPI ship, Vector2f location,  float radius) {
        ArrayList<ShipAPI> enemyShipNeighbours = ship == null
                ? findShipNeighbours(null, location, radius)
                : findShipNeighbours(ship, null, radius);
        ArrayList<ShipAPI> finalSubset = new ArrayList<>();
        for (ShipAPI unSortedShip : enemyShipNeighbours) {
            if (unSortedShip.getOriginalOwner() == 0 || unSortedShip.isAlly())
                continue;
            finalSubset.add(unSortedShip);
        }
        enemyShipNeighbours.clear();
        return (finalSubset);
    }

    private mapCell getCellfromVector2f(Vector2f location) {
        CombatEngineAPI engine = (CombatEngineAPI)this.getState(RTSAssist.stNames.engine);
        Float shipGridCellSize = (Float)this.getState(RTS_ShipLocAPI.stNames.shipGridCellSize);
        Vector2f holdCoOrd = new Vector2f();
        holdCoOrd.setX((float)Math.floor(location.getX() + (engine.getMapWidth() / 2)) / shipGridCellSize);
        holdCoOrd.setY((float)Math.floor(location.getY() + (engine.getMapHeight() / 2)) / shipGridCellSize);
        float targetCell = ((arenaGrid.getX()) * (holdCoOrd.getY() + 1)) + holdCoOrd.getX();
        if (spatialMap.get(targetCell) != null)
            return (spatialMap.get(targetCell));
        else {
            mapCell tempCell = new mapCell();
            tempCell.cellCoOrd = holdCoOrd;
            tempCell.cellShipList = new ArrayList<ShipAPI>();
            return (tempCell);
        }
    }

    private ArrayList<ShipAPI> findShipNeighbours (ShipAPI ship, Vector2f location, float radius) {
        ArrayList<ShipAPI> shipNeighbours = new ArrayList<>();
        Float lookUpLength = (float)Math.floor(radius / (float)this.getState(RTS_ShipLocAPI.stNames.shipGridCellSize)) + 1f;
        Vector2f sMI;
        if (ship != null)
            sMI = spatialMap.get(inverseSpatialMap.get(ship.getId())).cellCoOrd;
        else
            sMI = getCellfromVector2f(location).cellCoOrd;
        for (HashMap.Entry<Float, mapCell> set : spatialMap.entrySet())
            if ((set.getValue().cellCoOrd.getX() > (sMI.getX() + (lookUpLength  * -1)))
                    && (set.getValue().cellCoOrd.getX() < (sMI.getX() + lookUpLength))
                    && (set.getValue().cellCoOrd.getY() > (sMI.getY() + (lookUpLength  * -1)))
                    && (set.getValue().cellCoOrd.getY() < (sMI.getY() + lookUpLength))
            )
                shipNeighbours.addAll(spatialMap.get(set.getKey()).cellShipList);
        if (ship != null)
            for (ShipAPI testShip : shipNeighbours)
                if (testShip.getId().equals(ship.getId())) {
                    shipNeighbours.remove(testShip);
                    break;
                }
        return (shipNeighbours);
    }

    private void buildInverseSpatialMap () {
        HashMap<String, Float> ISMHold = new HashMap<>();
        for (HashMap.Entry<Float, mapCell> set : spatialMap.entrySet())
            for(int i = 0; i < set.getValue().cellShipList.size(); i++)
               ISMHold.put(set.getValue().cellShipList.get(i).getId(), set.getKey());
        if (inverseSpatialMap != null)
            inverseSpatialMap.clear();
        inverseSpatialMap = ISMHold;
    }

    private void buildSpatialMap () {
        CombatEngineAPI engine = (CombatEngineAPI) this.getState(RTSAssist.stNames.engine);
        HashMap<Float, mapCell> spatialMapHold = new HashMap<>();
        Vector2f holdCoOrd = new Vector2f();
        Float holdBlockCount = 0f;
        Float shipGridCellSize = (Float)this.getState(RTS_ShipLocAPI.stNames.shipGridCellSize);
        for (int i = 0; i < shipList.size(); i++) {
            holdCoOrd.setX((float)Math.floor((shipList.get(i).getLocation().getX()
                    + (engine.getMapWidth() / 2)) / shipGridCellSize));
            holdCoOrd.setY((float)Math.floor((shipList.get(i).getLocation().getY()
                    + (engine.getMapHeight() / 2)) / shipGridCellSize));
            holdBlockCount = ((arenaGrid.getX()) * (holdCoOrd.getY() + 1)) + holdCoOrd.getX(); // think about leftovers
            if (spatialMapHold.get(holdBlockCount) == null) {
                spatialMapHold.put(holdBlockCount, new mapCell());
                spatialMapHold.get(holdBlockCount).cellCoOrd = new Vector2f(
                        holdCoOrd.getX(),
                        holdCoOrd.getY()
                );
                spatialMapHold.get(holdBlockCount).cellShipList = new ArrayList<ShipAPI>();
            }
            spatialMapHold.get(holdBlockCount).cellShipList.add(shipList.get(i));
        }
        if (spatialMap != null)
            spatialMap.clear();
        spatialMap = spatialMapHold;
    }

    private void buildArenaGrid () {
        CombatEngineAPI engine = (CombatEngineAPI)this.getState(RTSAssist.stNames.engine);
        Vector2f mapDimensions = new Vector2f(
                engine.getMapWidth(),
                engine.getMapHeight()
        );
        Float shipGridCellSize = (Float)this.getState(RTS_ShipLocAPI.stNames.shipGridCellSize);
        this.arenaGrid = new Vector2f(
                (float)Math.floor(mapDimensions.getX() / shipGridCellSize),
                (float)Math.floor(mapDimensions.getY() / shipGridCellSize)
        );
    }

    private void getShipList () {
        List<ShipAPI> sortedShipList = new ArrayList<>();
        // reset new/lost ship lists
        if (!this.addedShips.isEmpty())
            this.addedShips.clear();
        if (!this.removedShips.isEmpty())
            this.removedShips.clear();
        // populate with all ships on the map that are viable entities
        for (ShipAPI ship : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips()) {
            if (ship.isFighter() || ship.isHulk() || ship.getHullSize().name().equals("FIGHTER"))
                continue;
            sortedShipList.add(ship);
        }
        // populate filter ship lists
        this.addedShips.addAll(sortedShipList);
        this.addedShips.removeAll(this.shipList);
        this.removedShips.addAll(this.shipList);
        this.removedShips.removeAll(sortedShipList);
        // If there are new ships, register an event for it. do the same for lost ships
        if (!addedShips.isEmpty()) {
            ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).addEvent(this.newShipsEvent, true);
        }
        if (!removedShips.isEmpty())
            ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).addEvent(this.lostShipsEvent, true);
        // Finish
        if (this.shipList != null)
            this.shipList.clear();
        this.shipList = sortedShipList;
    }

    private ArrayList<ShipAPI> addPrecision(Vector2f testLoc, List<ShipAPI> ships, float radius) {
        ArrayList<ShipAPI> preciseSubset = new ArrayList<>();
        for (ShipAPI ship : ships) {
            if (RTS_AssortedFunctions.getDistanceSquared(ship.getLocation(), testLoc) > Math.pow(radius, 2))
                continue;
            preciseSubset.add(ship);
        }
        ships.clear();
        return (preciseSubset);
    }

    public boolean isShipInPlay (ShipAPI ship) {
        if (this.shipList.contains(ship))
            return (true);
        else
            return (false);
    }
}