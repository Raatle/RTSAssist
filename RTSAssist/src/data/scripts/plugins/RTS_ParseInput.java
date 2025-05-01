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

import data.scripts.plugins.DevTools.RTS_ShipTestSuite;
import data.scripts.plugins.Utils.*;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.combat.ShipCommand;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

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
            put("controlGroupStore", null);
            put("isAltDown", false);
            put("isCtrlDown", false);
            put("isShiftDown", false);
            put("RTSMode", null);
            put("pauseUnpause", null);
            put("playerShipHold", null);
            put("strafeKeys", null);
        }};
        HashMap<String, Boolean> strafeKeys = new HashMap<>();
        strafeKeys.put("left", false);
        strafeKeys.put("right", false);
        strafeKeys.put("up", false);
        strafeKeys.put("down", false);
        init.put("strafeKeys", strafeKeys);
        init.put("testSuite", new RTS_ShipTestSuite(this.returnState()));
        init.put("RTSMode", (boolean)((HashMap<String, Object>)this.getState("config")).get("defaultModeIsRTS"));
        init.put("pauseUnpause", (boolean)((HashMap<String, Object>)this.getState("config")).get("pauseUnpause"));
//        init.put("inputManager", new RTSInputManager(this.returnState()));
        this.setState(init);
//        Binds buildHotkeys = new Binds(this.returnState());
//        buildHotkeys.createBinds((RTSInputManager)this.getState("inputManager"));
    }

    public void update (float amount, List<InputEventAPI> events) {
//        ((RTSInputManager)this.getState("inputManager")).listenAndExecute(amount, events);
        if (((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI()
                || ((CombatEngineAPI)this.getState("engine")).isUIShowingDialog()
                || this.getState("playerShipHold") == null
        ) {
            this.setState("rightClickStore", null);
            this.setState("leftClickStore", null);
            return;
        }
        HashMap<String, Object> hotKeys = (HashMap<String, Object>)this.getState("hotKeys");
        boolean DRMBFlag = false;
        this.setState("isZooming", 0);
        boolean altFlag = false;
        boolean ctrlFlag = false;
        boolean shiftFlag = false;
        HashMap<String, Boolean> strafeKeys = (HashMap<String, Boolean>)this.getState("strafeKeys");
        for (InputEventAPI x: events) {
            if (x == null) {
                System.out.println("test");
            }
            if (x.isAltDown()) altFlag = true;
            this.setState("isAltDown", altFlag);
            if (x.isCtrlDown()) ctrlFlag = true;
            this.setState("isCtrlDown", ctrlFlag);
            if (x.isShiftDown()) shiftFlag = true;
            this.setState("isShiftDown", shiftFlag);
            if (x.isConsumed()) continue;
            if (x.isKeyDownEvent())
                if ((hotKeys.get("enable_RTSMode") == null && x.getEventValue() == 58)
                        || (hotKeys.get("enable_RTSMode") != null
                        && (x.getEventChar() == (char)hotKeys.get("enable_RTSMode")
                        || x.getEventChar() == (char)((char)hotKeys.get("enable_RTSMode") + 32)))
                ) {
                    ((RTS_ModeManager)this.getState("modeManager")).playerChangesMode();
                    x.consume();
                    break;
                }
            if (x.isMouseMoveEvent()) {
                this.setState("screenSpace", new Vector2f((float)x.getX(), (float)x.getY()));
                this.setState("worldSpace", CombatUtils.toWorldCoordinates((Vector2f)this.getState("screenSpace")));
            }
            else if (x.isMouseScrollEvent()) {
                this.setState("isZooming", x.getEventValue() > 0 ? 1 : -1);
                x.consume();
            }
            else if ((Boolean)this.getState("RTSMode")) {
                if (x.isKeyboardEvent()) {
                    if ((boolean)this.getState("inDevelopment")
                            && (x.getEventChar() == (char)hotKeys.get("param1")
                            || x.getEventChar() == (char)((char)hotKeys.get("param1") + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState("testSuite")).spawnShip(
                                (Vector2f)this.getState("worldSpace")
                        );
                        x.consume();
                    } else if ((boolean)this.getState("inDevelopment")
                            && (x.getEventChar() == (char)hotKeys.get("param2")
                            || x.getEventChar() == (char)((char)hotKeys.get("param2") + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState("testSuite")).soutShipDetails(
                                (List<ShipAPI>)this.getState("currentSelection")
                        );
                        x.consume();
                    } else if ((boolean)this.getState("inDevelopment")
                            && (x.getEventChar() == (char)hotKeys.get("param3")
                            || x.getEventChar() == (char)((char)hotKeys.get("param3") + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState("testSuite")).spawnShipIter(
                                (Vector2f)this.getState("worldSpace")
                        );
                        x.consume();
                    } else if (x.getEventValue() == 30
                    ) {
                        strafeKeys.put("left", !x.isKeyUpEvent());
                        x.consume();
                    } else if (x.getEventValue() == 32
                    ) {
                        strafeKeys.put("right", !x.isKeyUpEvent());
                        x.consume();
                     } else if (x.getEventValue() == 17
                    ) {
                        strafeKeys.put("up", !x.isKeyUpEvent());
                        x.consume();
                    } else if (x.getEventValue() == 31
                    ) {
                        strafeKeys.put("down", !x.isKeyUpEvent());
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get("saveLayout")
                            || x.getEventChar() == (char)((char) hotKeys.get("saveLayout") + 32)
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
                            ((RTS_SelectionListener)this.getState("selectionListener"))
                                    .controlGroupHandler((Integer)x.getEventValue(), true);
                        else {
                            ((RTS_SelectionListener)this.getState("selectionListener"))
                                    .controlGroupHandler((Integer)x.getEventValue(), false);
                            if (this.getState("controlGroupStore") != null) {
                                if (((Vector2f)this.getState("controlGroupStore")).getX() == (float)x.getEventValue()
                                        && ((float)this.getDeepState(Arrays.asList("amount", "start"))
                                                - ((Vector2f)this.getState("controlGroupStore")).getY() < 0.2f))
                                    ((RTS_CameraRework)this.getState("cameraRework"))
                                            .zoomToControlGroup((Integer)x.getEventValue());
                            }
                            if (this.getState("controlGroupStore") == null)
                                this.setState("controlGroupStore", new Vector2f());
                            ((Vector2f)this.getState("controlGroupStore")).set(
                                    x.getEventValue(),
                                    (float)this.getDeepState(Arrays.asList("amount", "start"))
                            );
                        }
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
                    if (this.getState("currentSelection") != null
                            && (((boolean)this.getState("combatStarted"))
                            || ((List<ShipAPI>)this.getState("currentSelection")).size() == 1)
                    )
                        ((RTS_TaskManager)this.getState("taskManager")).createAssignments(
                                (List<ShipAPI>)this.getState("currentSelection"),
                                (Vector2f)this.getState("worldSpace"),
                                null,
                                !DRMBFlag
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