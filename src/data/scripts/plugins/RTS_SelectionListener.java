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

package data.scripts.plugins;

import java.util.*;
import java.util.List;

import com.fs.starfarer.api.combat.ShipAIPlugin;
import data.scripts.plugins.Utils.RTS_EventManager;
import data.scripts.plugins.Utils.RTS_Listener;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.MathUtils;

import data.scripts.plugins.Utils.RTS_Draw;
import data.scripts.plugins.Utils.RTS_StatefulClasses;


public class RTS_SelectionListener extends RTS_StatefulClasses {

    public RTS_SelectionListener(Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put("hideShip", null);
            put("beginEvent", null);
            put("previewSelection", null);
            put("selectionCounter", 0);
            put("hideShipInfo", false);
            put("controlGroups", new HashMap<>());
        }};
        this.setState(init);
        if ((float)this.getDeepState(Arrays.asList("config", "screenScaling")) != 100f)
            this.UIS = (float)this.getDeepState(Arrays.asList("config", "screenScaling")) / 100f;
        else
            this.UIS = Global.getSettings().getScreenScaleMult();
    }

    float UIS;

    public void buildListeners () {
        RTS_Listener deadShipListener = new RTS_Listener() {
            @Override
            public String type() {
                return ("shipsNoLongerActive");
            }

            @Override
            public void run(HashMap<String, Object> e) {
                List<ShipAPI> deadShips = (List<ShipAPI>)e.get("shipsNoLongerActive");
                for (ShipAPI ship : deadShips) {
                    if (getState("currentSelection") != null)
                        ((List<ShipAPI>)getState("currentSelection")).remove(ship);
                }
                e.clear();
            }

            @Override
            public boolean removeOnCompletion() {
                return (false);
            }
        };
        ((RTS_EventManager)this.getState("eventManager")).addListener(deadShipListener);
    }



    public void controlGroupHandler(Integer group, boolean add) {
        HashMap<Integer, List<ShipAPI>> controlGroups = (HashMap<Integer, List<ShipAPI>>)this.getState("controlGroups");
        if (add) {
            controlGroups.put(group, (List<ShipAPI>)this.getState("currentSelection"));
            this.setState("controlGroups", controlGroups);
        }
        else if (controlGroups.containsKey(group)) {
            this.showShipDetails(controlGroups.get(group));
            this.setState("currentSelection", controlGroups.get(group));
        }
    }

    public List<ShipAPI> getControlGroupList (Integer group) {
        HashMap<Integer, List<ShipAPI>> controlGroups = (HashMap<Integer, List<ShipAPI>>)this.getState("controlGroups");
        if (controlGroups.containsKey(group))
            return(controlGroups.get(group));
        return (null);
    }

    public void drawSelection (Vector2f screenSpace, List<ShipAPI> currentlySelected) {
        if (screenSpace == null) return;
        if (((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI())
            this.setState("beginEvent", null);
        this.setState("previewSelection", this.createSelection(screenSpace));
        this.drawSelected((List<ShipAPI>)this.getState("previewSelection"));
        this.drawSelected(currentlySelected);
        this.drawSelectionLines(screenSpace);
        if ((boolean)this.getState("hideShipInfo"))
            ((CombatEngineAPI)this.getState("engine")).getCombatUI().hideShipInfo();
    }

    private List <ShipAPI> createSelection (Vector2f coOrd) {
        if (this.getState("beginEvent") == null) return (null);

        int xStart;
        int xEnd;
        int yStart;
        int yEnd;
        Vector2f start = (Vector2f)this.getState("beginEvent");
        Vector2f end = CombatUtils.toWorldCoordinates(coOrd);

        xStart = start.getX() <= end.getX() ? (int)start.getX() : (int)end.getX();
        xEnd = start.getX() < end.getX() ? (int)end.getX() : (int)start.getX();
        yStart = start.getY() <= end.getY() ? (int)start.getY() : (int)end.getY();
        yEnd = start.getY() < end.getY() ? (int)end.getY() : (int)start.getY();
        if (Math.abs(xStart - xEnd) < 50 && Math.abs(yStart - yEnd) < 50)
            return(getSelectedShip(xStart, yStart));
        return (getSelectedShips(xStart, xEnd, yStart, yEnd));
    }

    public boolean dontSelect(ShipAPI x) {
        return (
                   x.isFighter()
                || x.getOriginalOwner() == 1
                || x.isHulk()
                || x.isStation()
                || x.isAlly()
                || x.getName() == null
                || x.getName().equals("Command Shuttle")
                || x.getHullSize() == null
        );
    }

    public boolean dontSelect(ShipAPI x, boolean includeTheDead) {
        return (
                x.isFighter()
                        || x.getOriginalOwner() == 1
                        || x.isStation()
                        || x.isAlly()
                        || x.getName() == null
                        || x.getName().equals("Command Shuttle")
                        || x.getHullSize() == null
        );
    }

    private List <ShipAPI> getSelectedShip(int xCoOrd, int yCoOrd) {
        Vector2f target = new Vector2f((float)xCoOrd, (float)yCoOrd);
        List <ShipAPI> hold = new ArrayList<>();
        for (ShipAPI x: ((CombatEngineAPI)this.getState("engine")).getShips()) {
            if (this.dontSelect(x))
                continue;
            if (MathUtils.getDistanceSquared(x, target) == 0f)
                hold.add(x);
        }
        if (hold.size() > 1) {
            ShipAPI store = hold.get(0);
            float correctPick = 100;
            float comp;
            for (ShipAPI x: hold) {
                comp = MathUtils.getDistanceSquared(x.getLocation(), target) /
                        (float)Math.pow(x.getCollisionRadius(), 2);
                if (correctPick > comp) {
                    correctPick = comp;
                    store = x;
                }
            }
            List<ShipAPI> shipHold = new ArrayList();
            shipHold.add(store);
            return(shipHold);
        }
        return (hold.size() > 0 ? hold : null);
    }

    private List <ShipAPI> getSelectedShips (int xStart, int xEnd, int yStart, int yEnd) {
        List <ShipAPI> hold = new ArrayList<>();
        int xHold;
        int yHold;
        for (ShipAPI x: ((CombatEngineAPI)this.getState("engine")).getShips()) {
            if (this.dontSelect(x))
                continue;
            xHold = (int)x.getLocation().getX();
            yHold = (int)x.getLocation().getY();
            if (xEnd > xHold && xHold > xStart && yEnd > yHold && yHold > yStart)
                hold.add(x);
        }
        if (hold.size() != (int)this.getState("selectionCounter")) {
            this.setState("selectionCounter", hold.size());
            if (hold.size() != 0) Global.getSoundPlayer().playUISound(
                    "ui_type", 1f, 0.3f
            );
        }
        return (hold.size() > 0 ? hold : null);
    }

    public void beginEvent (InputEventAPI mouseEvent) {
        this.setState("beginEvent", CombatUtils.toWorldCoordinates(
                new Vector2f(mouseEvent.getX(), mouseEvent.getY()))
        );
    }

    public void prematurelyEndEvent () {
        this.setState("beginEvent", null);
    }

    public List <ShipAPI> createSelectionEvent (List<ShipAPI> currentSelection) {
        if (this.getState("beginEvent") == null) return (null);
        this.setState("beginEvent", null);
        if (this.getState("previewSelection") != null) Global.getSoundPlayer().playUISound(
                "ui_command_select_command_ui_icon", 1f, 0.3f
        );
        else if (currentSelection != null) Global.getSoundPlayer().playUISound(
                "ui_command_selection_cleared", 1f, 0.3f
        );
        if ((boolean)this.getState("isShiftDown")){
            List<ShipAPI> joinedList = new ArrayList<ShipAPI>();
            if (currentSelection != null)
                joinedList.addAll(currentSelection);
            if (this.getState("previewSelection") != null) {
                joinedList.addAll((List<ShipAPI>)this.getState("previewSelection"));
                if (currentSelection != null)
                    for (ShipAPI x: (List<ShipAPI>)this.getState("previewSelection")) {
                        if (currentSelection.contains(x)) {
                            joinedList.remove(x);
                            joinedList.remove(x);
                        }
                    }
            }
            this.showShipDetails(joinedList);
            return(joinedList.isEmpty() ? null : joinedList);
        }
        else {
            this.showShipDetails((List<ShipAPI>)this.getState("previewSelection"));
            return ((List<ShipAPI>)this.getState("previewSelection"));
        }
    }

    public void showShipDetails (List<ShipAPI> concludedSelection) {
        CombatEngineAPI engine = (CombatEngineAPI)this.getState("engine");
        if (concludedSelection != null && concludedSelection.size() == 1)
        {
            this.setState("hideShipInfo", false);
            if (this.getState("hideShip") == null
                    || !((ShipAPI)this.getState("hideShip")).getName().equals(
                            concludedSelection.get(0).getName())
            ) {
                ShipAIPlugin hold = concludedSelection.get(0).getShipAI();
                engine.setPlayerShipExternal(concludedSelection.get(0));
                concludedSelection.get(0).setShipAI(hold);
                this.setState("hideShip", concludedSelection.get(0));
            }
            else
                engine.getCombatUI().reFanOutShipInfo();
        }
        else {
            if (this.getState("commandShuttle") != null) {
                engine.setPlayerShipExternal((ShipAPI)this.getState("commandShuttle"));
                this.setState("hideShip", null);
            }
            this.setState("hideShipInfo", true);
        }
    }

    public void drawSelected (List<ShipAPI> currentlySelected) {
        if (currentlySelected == null) return;
        for (ShipAPI x: currentlySelected) {
            ((RTS_Draw)this.getState("draw")).drawSelectionCircle(x, (float)this.getState("targetZoom"));
        }
    }

    private void drawSelectionLines(Vector2f screenSpace) {
        if(this.getState("beginEvent") == null) return;
        Vector2f sBeginEvent = CombatUtils.toScreenCoordinates((Vector2f)this.getState("beginEvent"));
        float UIS = this.UIS;

        GL11.glColor4ub((byte)155,(byte)189, (byte)0, (byte)150);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2d(
                (double)(sBeginEvent.getX() * UIS),
                (double)(sBeginEvent.getY() * UIS)
        );
        GL11.glVertex2d(
                (double)(sBeginEvent.getX() * UIS),
                (double)(screenSpace.getY() * UIS)
        );
        GL11.glVertex2d(
                (double)(screenSpace.getX() * UIS),
                (double)(screenSpace.getY() * UIS)
        );
        GL11.glVertex2d(
                (double)(screenSpace.getX() * UIS),
                (double)(sBeginEvent.getY() * UIS)
        );
        GL11.glVertex2d(
                (double)(sBeginEvent.getX() * UIS),
                (double)(sBeginEvent.getY() * UIS)
        );
        GL11.glEnd();
    }
}