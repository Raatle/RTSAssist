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

import org.lwjgl.util.vector.Vector2f;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.combat.ShipCommand;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

import data.scripts.plugins.Utils.RTS_StatefulClasses;
import data.scripts.plugins.Utils.RTS_Draw;

public class RTS_ParseInput extends RTS_StatefulClasses {

    public RTS_ParseInput(HashMap<String, Object> state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put("screenSpace", null);
            put("worldSpace", null);
            put("isZooming", 0);
            put("currentSelection", null);
            put("rightClickStore", null);
            put("leftClickStore", null);
            put("isAltDown", false);
            put("isCtrlDown", false);
        }};
//        init.put("inputManager", new RTSInputManager(this.returnState()));
        this.setState(init);
//        Binds buildHotkeys = new Binds(this.returnState());
//        buildHotkeys.createBinds((RTSInputManager)this.getState("inputManager"));
    }

    public void update (float amount, List<InputEventAPI> events) {
//        ((RTSInputManager)this.getState("inputManager")).listenAndExecute(amount, events);
        if (((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI() ||
                ((CombatEngineAPI)this.getState("engine")).isUIShowingDialog()) {
            HashMap<String, Object> hold = new HashMap<>();
            hold.put("rightClickStore", null);
            hold.put("leftClickStore", null);
            this.setState(hold);
            return;
        }
        HashMap<String, Object> hotKeys = (HashMap<String, Object>)this.getState("hotKeys");
        boolean DRMBFlag = false;
        this.setState("isZooming", 0);
        boolean altFlag = false;
        boolean ctrlFlag = false;
        boolean shiftFlag = false;
        for (InputEventAPI x: events) {
            if (x.isAltDown()) altFlag = true;
            this.setState("isAltDown", x.isAltDown() ? true : altFlag ? true : false);
            if (x.isCtrlDown()) ctrlFlag = true;
            this.setState("isCtrlDown", x.isCtrlDown() ? true : ctrlFlag ? true : false);
            if (x.isShiftDown()) shiftFlag = true;
            this.setState("isShiftDown", x.isShiftDown() ? true : shiftFlag ? true : false);
            if (x.isConsumed()) continue;
            if (x.isKeyDownEvent()
                    && (x.getEventChar() == (char)hotKeys.get("enable_disable")
                    || x.getEventChar() == (char)((char)hotKeys.get("enable_disable") + 32))
            ) {
                System.out.println("RTSAssist: You attempted to enable disable mod functionality mid battle. This is not ready yet");
//                this.setState("isEnabled", !(Boolean)this.getState("isEnabled"));
//                x.consume();
                continue;
            }
            if ((Boolean)this.getState("isEnabled")) {
                ((CombatEngineAPI)this.getState("engine")).getCombatUI().setDisablePlayerShipControlOneFrame(true);
                if (((CombatEngineAPI)this.getState("engine")).getPlayerShip().getAI() == null)
                    ((CombatEngineAPI) this.getState("engine")).getPlayerShip().resetDefaultAI();
            }
            if (x.isMouseMoveEvent()) {
                this.setState("screenSpace", new Vector2f((float)x.getX(), (float)x.getY()));
                this.setState("worldSpace", CombatUtils.toWorldCoordinates((Vector2f)this.getState("screenSpace")));
            }
            else if (x.isMouseScrollEvent())
                this.setState("isZooming", x.getEventValue() > 0 ? 1 : -1);
            else if ((Boolean)this.getState("isEnabled")) {
                if (x.isKeyboardEvent()) {
                    if (x.getEventChar() == (char)hotKeys.get("saveLayout")
                            || x.getEventChar() == (char) ((char) hotKeys.get("saveLayout") + 32)
                    ) {
                        ((RTS_TaskManager)this.getState("taskManager")).saveAssignments();
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get("loadLayout")
                            || x.getEventChar() == (char)((char) hotKeys.get("loadLayout") + 32)
                    ) {
                        ((RTS_TaskManager)this.getState("taskManager")).
                                loadAssignments((Vector2f) this.getState("worldSpace"));
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get("vent")
                            || x.getEventChar() == (char)((char) hotKeys.get("vent") + 32)
                    ) {
                        if (this.getState("currentSelection") != null)
                            for(ShipAPI ship: (List<ShipAPI>)this.getState("currentSelection"))
                                ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get("deleteAssignments")
                            || x.getEventChar() == (char)((char) hotKeys.get("deleteAssignments") + 32)
                    ) {
                        ((RTS_TaskManager)this.getState("taskManager")).deleteAssignments(
                                (List<ShipAPI>)this.getState("currentSelection")
                        );
                        x.consume();
                    } else if (x.getEventValue() == 45) {
                        ((RTS_TaskManager)this.getState("taskManager")).editPriorityEnemy(
                                (List<ShipAPI>)this.getState("currentSelection"),
                                (Vector2f)this.getState("worldSpace"),
                                false
                        );
                        x.consume();
                    } else if (x.getEventValue() >= 2 && x.getEventValue() <= 6 && x.isKeyDownEvent()) {
                        if ((boolean)this.getState("isCtrlDown"))
                            ((RTS_SelectionListener)this.getState("selectionListener")).controlGroupHandler((Integer)x.getEventValue(), true);
                        else
                            ((RTS_SelectionListener)this.getState("selectionListener")).controlGroupHandler((Integer)x.getEventValue() ,false);
                        x.consume();
                    }
                }
                if (!(boolean)this.getState("isAltDown"))
                    this.setState("leftClickStore", null);
                if ((boolean)this.getState("isAltDown")) {
                    ((RTS_SelectionListener)this.getState("selectionListener")).prematurelyEndEvent();
                    this.setState("rightClickStore", null);
                    if (x.isLMBDownEvent()) {
                        this.setState("leftClickStore", this.getState("worldSpace"));
                        x.consume();
                    }
                    else if(x.isLMBUpEvent()) {
                        if ((boolean)this.getState("isAltDown") && !(boolean)this.getState("isCtrlDown"))
                            ((RTS_TaskManager)this.getState("taskManager")).translateAssignments(
                                    (List<ShipAPI>)this.getState("currentSelection"),
                                    (Vector2f)this.getState("leftClickStore"),
                                    (Vector2f)this.getState("worldSpace"),
                                    false
                            );
                        else if ((boolean)this.getState("isAltDown") && (boolean)this.getState("isCtrlDown"))
                            ((RTS_TaskManager)this.getState("taskManager")).rotateAssignments(
                                    (List <ShipAPI>)this.getState("currentSelection"),
                                    (Vector2f)this.getState("leftClickStore"),
                                    (Vector2f)this.getState("worldSpace"),
                                    false
                            );
                        this.setState("leftClickStore", null);
                        x.consume();
                    }
                }
                else if ((boolean)this.getState("isCtrlDown")) {
                    if (x.isLMBDownEvent()) {
                        ((RTS_TaskManager) this.getState("taskManager")).editPriorityEnemy(
                                (List<ShipAPI>) this.getState("currentSelection"),
                                (Vector2f) this.getState("worldSpace"),
                                true
                        );
                        x.consume();
                    }
                }
                else if (x.isLMBDownEvent()) {
                    ((RTS_SelectionListener) this.getState("selectionListener")).beginEvent(x);
                    x.consume();
                }
                else if (x.isLMBUpEvent()) {
                    this.setState(
                            "currentSelection",
                            ((RTS_SelectionListener)this.getState("selectionListener")).createSelectionEvent(
                                    (List<ShipAPI>)this.getState("currentSelection")
                            ));
                    x.consume();
                }
                if (x.isRMBDownEvent()) {
                    if (x.isDoubleClick()) DRMBFlag = true;
                    if (this.getState("currentSelection") != null && (((boolean)this.getState("combatStarted"))
                            || ((List<ShipAPI>)this.getState("currentSelection")).size() == 1))
                        ((RTS_TaskManager)this.getState("taskManager")).createAssignments(
                            (List<ShipAPI>)this.getState("currentSelection"),
                            (Vector2f)this.getState("worldSpace"),
                            null,
                            DRMBFlag ? false : true
                        );
                    this.setState("rightClickStore", this.getState("worldSpace"));
                    this.setDeepState(
                            Arrays.asList("amount", "rightClickStore"),
                            this.getDeepState(Arrays.asList("amount", "start"))
                    );
                    x.consume();
                }
                else if (x.isRMBUpEvent() && this.getState("rightClickStore") != null) {
                    if (this.getState("currentSelection") != null &&
                            ((List<ShipAPI>)this.getState("currentSelection")).size() > 1)
                        System.out.println("RTSAssist: You attempted to RClick and drag multiple ships. This is not ready yet");
                    else if ((MathUtils.getDistance((Vector2f)this.getState("worldSpace"), (Vector2f)this.getState("rightClickStore")) > 100f)
                            && ((float)this.getDeepState(Arrays.asList("amount", "start")) - (this.getDeepState(Arrays.asList("amount", "rightClickStore")) == null
                            ? 0f : (float)this.getDeepState(Arrays.asList("amount", "rightClickStore"))) > 0.2f))
                        ((RTS_TaskManager)this.getState("taskManager")).createAssignments(
                                (List<ShipAPI>)this.getState("currentSelection"),
                                (Vector2f)this.getState("rightClickStore"),
                                (Vector2f)this.getState("worldSpace"),
                                DRMBFlag
                        );
                    this.setState("rightClickStore", null);
                    this.setDeepState(Arrays.asList("amount", "rightClickStore"), null);
                    x.consume();
                }
            }
        }
    }

    public void tempInterface() {
        if (this.getState("leftClickStore") == null && this.getState("rightClickStore") == null)
            return;
        if (this.getState("rightClickStore") != null)
            ((RTS_Draw)this.getState("draw")).rudeDrawLine(
                    CombatUtils.toScreenCoordinates((Vector2f)this.getState("rightClickStore")),
                    (Vector2f)this.getState("screenSpace")
            );
        if (this.getState("leftClickStore") != null) {
            if ((boolean)this.getState("isAltDown") && !(boolean)this.getState("isCtrlDown"))
                ((RTS_TaskManager) this.getState("taskManager")).translateAssignments(
                        (List<ShipAPI>) this.getState("currentSelection"),
                        (Vector2f) this.getState("leftClickStore"),
                        (Vector2f) this.getState("worldSpace"),
                        true
                );
            else if ((boolean)this.getState("isAltDown") && (boolean)this.getState("isCtrlDown"))
                ((RTS_TaskManager)this.getState("taskManager")).rotateAssignments(
                        (List <ShipAPI>)this.getState("currentSelection"),
                        (Vector2f)this.getState("leftClickStore"),
                        (Vector2f)this.getState("worldSpace"),
                        true
                );
            else
                ((RTS_Draw)this.getState("draw")).rudeDrawLine(
                        CombatUtils.toScreenCoordinates((Vector2f) this.getState("leftClickStore")),
                        (Vector2f) this.getState("screenSpace")
                );
        }
    }
}