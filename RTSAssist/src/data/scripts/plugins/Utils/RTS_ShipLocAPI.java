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
import java.util.*;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class RTS_ShipLocAPI extends RTS_StatefulClasses {

    public RTS_ShipLocAPI(Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>(){{
            put("shipGridCellSize", 500f);
        }};
        this.setState(init);
        getShipList();
        buildArenaGrid();
    }

    class mapCell {
        Vector2f cellCoOrd;
        ArrayList<ShipAPI> cellShipList;
    }

    private List<ShipAPI> shipList = new ArrayList<>();
    private Vector2f arenaGrid;
    private HashMap<Float, mapCell> spatialMap;
    private HashMap<String, Float> inverseSpatialMap;
    List<ShipAPI> addedShips = new ArrayList<>();
    List<ShipAPI> removedShips = new ArrayList<>();

    public void update() {
        getShipList();
        buildSpatialMap();
        buildInverseSpatialMap();
    }

    public ArrayList<ShipAPI> findAlliedShipNeighbours (ShipAPI ship, float radius) {
        return (this.findAlliedShipNeighbours(ship, null, radius));
    }
    public ArrayList<ShipAPI> findAlliedShipNeighbours (Vector2f location, float radius) {
        return (this.findAlliedShipNeighbours(null, location, radius));
    }
    private ArrayList<ShipAPI> findAlliedShipNeighbours (ShipAPI ship, Vector2f location, float radius) {
        ArrayList<ShipAPI> alliedShipNeighbours = ship == null
                ? findShipNeighbours(null, location, radius)
                : findShipNeighbours(ship, null, radius);
        int stop = 0;
        while(stop == 0) {
            stop = 1;
            for (int i = 0; i < alliedShipNeighbours.size(); i++)
                if (alliedShipNeighbours.get(i).getOriginalOwner() == 1) {
                    stop = 0;
                    alliedShipNeighbours.remove(i);
                    break;
                }
        }
        return (alliedShipNeighbours);
    }

    public ArrayList<ShipAPI> findEnemyShipNeighbours (ShipAPI ship, float radius) {
        return (this.findEnemyShipNeighbours(ship, null, radius));
    }
    public ArrayList<ShipAPI> findEnemyShipNeighbours (Vector2f location, float radius) {
        return (this.findEnemyShipNeighbours(null, location, radius));
    }
    private ArrayList<ShipAPI> findEnemyShipNeighbours (ShipAPI ship, Vector2f location,  float radius) {
        ArrayList<ShipAPI> enemyShipNeighbours = ship == null
                ? findShipNeighbours(null, location, radius)
                : findShipNeighbours(ship, null, radius);
        int stop = 0;
        while(stop == 0) {
            stop = 1;
            for (int i = 0; i < enemyShipNeighbours.size(); i++)
                if (enemyShipNeighbours.get(i).getOriginalOwner() == 0 || enemyShipNeighbours.get(i).isAlly()) {
                    stop = 0;
                    enemyShipNeighbours.remove(i);
                    break;
                }
        }
        return (enemyShipNeighbours);
    }

    private mapCell getCellfromVector2f(Vector2f location) {
        CombatEngineAPI engine = (CombatEngineAPI)this.getState("engine");
        Float shipGridCellSize = (Float)this.getState("shipGridCellSize");
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

    public ArrayList<ShipAPI> findShipNeighbours (ShipAPI ship, Vector2f location, float radius) {
        ArrayList<ShipAPI> shipNeighbours = new ArrayList<>();
        Float lookUpLength = (float)Math.floor(radius / (float)this.getState("shipGridCellSize")) + 1f;
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
            for (int i = 0; i < shipNeighbours.size(); i++)
                if (shipNeighbours.get(i).getId().equals(ship.getId())) {
                    shipNeighbours.remove(i);
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

    private void buildSpatialMap() {
        CombatEngineAPI engine = (CombatEngineAPI) this.getState("engine");
        HashMap<Float, mapCell> spatialMapHold = new HashMap<>();
        Vector2f holdCoOrd = new Vector2f();
        Float holdBlockCount = 0f;
        Float shipGridCellSize = (Float)this.getState("shipGridCellSize");
        for (int i = 0; i < shipList.size(); i++) {
            holdCoOrd.setX((float)Math.floor((shipList.get(i).getLocation().getX() + (engine.getMapWidth() / 2)) / shipGridCellSize));
            holdCoOrd.setY((float)Math.floor((shipList.get(i).getLocation().getY() + (engine.getMapHeight() / 2)) / shipGridCellSize));
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

    private void buildArenaGrid() {
        CombatEngineAPI engine = (CombatEngineAPI)this.getState("engine");
        Vector2f mapDimensions = new Vector2f(
                engine.getMapWidth(),
                engine.getMapHeight()
        );
        Float shipGridCellSize = (Float)this.getState("shipGridCellSize");
        this.arenaGrid = new Vector2f(
                (float)Math.floor(mapDimensions.getX() / shipGridCellSize),
                (float)Math.floor(mapDimensions.getY() / shipGridCellSize)
        );
    }

    private void getShipList() {
        List<ShipAPI> sortedShipList = new ArrayList<>();
        if (!this.addedShips.isEmpty())
            this.addedShips.clear();
        if (!this.removedShips.isEmpty())
            this.removedShips.clear();
        for (ShipAPI ship : ((CombatEngineAPI)this.getState("engine")).getShips()) {
            if (ship.isFighter() || ship.isHulk() || ship.getHullSize().name().equals("FIGHTER"))
                continue;
            sortedShipList.add(ship);
            if (!this.shipList.contains(ship))
                addedShips.add(ship);
        }
        for (ShipAPI ship: this.shipList)
            if (!sortedShipList.contains(ship))
                removedShips.add(ship);
        if (!addedShips.isEmpty())
            ((RTS_EventManager)this.getState("eventManager")).addEvent(new RTS_Event() {
                @Override
                public String type() {
                    return ("newShipsDeployed");
                }

                @Override
                public HashMap<String, Object> run(boolean waitForListener) {
                    HashMap<String, Object> hold = new HashMap<>();
                    hold.put("newShipsDeployed", addedShips);
                    return (hold);
                }
            });
        if (!removedShips.isEmpty())
            ((RTS_EventManager)this.getState("eventManager")).addEvent(new RTS_Event() {
                @Override
                public String type() {
                    return ("shipsNoLongerActive");
                }

                @Override
                public HashMap<String, Object> run(boolean waitForListener) {
                    HashMap<String, Object> hold = new HashMap<>();
                    hold.put("shipsNoLongerActive", removedShips);
                    return (hold);
                }
            });
        if (this.shipList != null)
            this.shipList.clear();
        this.shipList = sortedShipList;
    }
}