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

import com.fs.starfarer.api.combat.CombatEngineAPI;
import org.lwjgl.util.vector.Vector2f;
import java.util.HashMap;

import com.fs.starfarer.api.combat.ShipAPI;

public class RTS_Context {
    private HashMap<String, Object> state;
    private HashMap<String, Object> assignment;

    public RTS_Context(Object state, Object assignment) {
        this.state = (HashMap<String, Object>)state;
        this.assignment = (HashMap<String, Object>)assignment;
    }
    /**
     * Where the ship wants top go If it isnt assigned an enemy.
     * @return A Vector2f representing the x,y coordinates of its destination.
     */
    public Vector2f getDest() { return((Vector2f)this.assignment.get("dest")); }
    /**
     * Where the ship is actually headed. if NOT null use instead.
     * @return A Vector2f representing the x,y coordinates of its destination. Can be null.
     */
    public Vector2f getModDest() { return((Vector2f)this.assignment.get("modDest")); }
    /**
     * A secondary Vector2f that has different purposes based on ships the assignment.
     * @return A Vector2f representing x,y coordinates. Can be null.
     */
    public Vector2f getDestSec() { return((Vector2f)this.assignment.get("destSec")); }
    /**
     * @return ShipApi for the ship being checked.
     */
    public ShipAPI getShip() { return((ShipAPI)this.assignment.get("ship")); }
    /**
     * @return Boolean, true if given a temporary assignment, false otherwise.
     */
    public Boolean getMoveAndRelease() { return((Boolean)this.assignment.get("moveAndRelease")); }
    /**
     * Calculated weapon ranges of the ship.
     * @return A Vector2f with y representing the shortest range weapon and x the longest. Does not include Point Defense or Missiles.
     */
    public Vector2f getWeaponRanges() { return((Vector2f)this.assignment.get("weaponRanges")); }
    /**
     * If the ship is assigned to attack at an angle, this will return relevent info.
     * @return A Vector2f with x representing the angle to the target and y the distance to the target. Can be null.
     */
    public Vector2f getAttackAngle() { return((Vector2f)assignment.get("attackAngle")); }
    /**
     * If the ship is assigned to attack an enemy, this returns that ship.
     * @return ShipApi of the target. Can be null.
     */
    public ShipAPI getTargetEnemy() { return((ShipAPI)this.assignment.get("targetEnemy")); }
    /**
     * If the ship is assigned to prioritise an enemy, this returns that ship.
     * @return ShipApi of the priority target. Can be null.
     */
    public ShipAPI getPriorityEnemy() { return((ShipAPI)this.assignment.get("priorityEnemy")); }
    /**
     * The instance of CombatEngineAPI that is utilised by RTSAssist.
     * @return CombatEngineApi utilised by RTSAssist.
     */
    public CombatEngineAPI getEngine() { return((CombatEngineAPI)this.state.get("engine")); }
    /**
     * Almost All information created by RTSAssist is stored in this object. Take care.
     * @return Hashmap, queried with STRINGS, returns OBJECTS.
     */
    public HashMap<String, Object> getState() { return((HashMap<String, Object>)this.state); }

}