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

package data.scripts.plugins;

import data.scripts.plugins.DevTools.RTS_ShipTestSuite;
import data.scripts.plugins.Utils.*;
import org.lwjgl.util.vector.Vector2f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

public class RTS_ParseInput extends RTS_StatefulClasses {

    public static class classidentifiers {
        public String screenSpace = RTS_StatefulClasses.getUniqueIdentifier();
        public String worldSpace = RTS_StatefulClasses.getUniqueIdentifier();
        public String isZooming = RTS_StatefulClasses.getUniqueIdentifier();
        public String currentSelection = RTS_StatefulClasses.getUniqueIdentifier();
        public String rightClickStore = RTS_StatefulClasses.getUniqueIdentifier();
        public String leftClickStore = RTS_StatefulClasses.getUniqueIdentifier();
        public String leftClickStoreSec = RTS_StatefulClasses.getUniqueIdentifier();
        public String controlGroupStore = RTS_StatefulClasses.getUniqueIdentifier();
        public String isAltDown = RTS_StatefulClasses.getUniqueIdentifier();
        public String isCtrlDown = RTS_StatefulClasses.getUniqueIdentifier();
        public String isShiftDown = RTS_StatefulClasses.getUniqueIdentifier();
        public String RTSMode = RTS_StatefulClasses.getUniqueIdentifier();
        public String pauseUnpause = RTS_StatefulClasses.getUniqueIdentifier();
        public String playerShipHold = RTS_StatefulClasses.getUniqueIdentifier();
        public String strafeKeys = RTS_StatefulClasses.getUniqueIdentifier();

    }
    public static classidentifiers stNames = new classidentifiers();

    public static class strafeIdentifiers {
        public String left = RTS_StatefulClasses.getUniqueIdentifier();
        public String right = RTS_StatefulClasses.getUniqueIdentifier();
        public String up = RTS_StatefulClasses.getUniqueIdentifier();
        public String down = RTS_StatefulClasses.getUniqueIdentifier();

    }
    public static strafeIdentifiers skNames = new strafeIdentifiers();

    public RTS_ParseInput(HashMap<String, Object> state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put(RTS_ParseInput.stNames.screenSpace, null);
            put(RTS_ParseInput.stNames.worldSpace, null);
            put(RTS_ParseInput.stNames.isZooming, 0);
            put(RTS_ParseInput.stNames.currentSelection, null);
            put(RTS_ParseInput.stNames.rightClickStore, null);
            put(RTS_ParseInput.stNames.leftClickStore, null);
            put(RTS_ParseInput.stNames.leftClickStoreSec, null);
            put(RTS_ParseInput.stNames.controlGroupStore, null);
            put(RTS_ParseInput.stNames.isAltDown, false);
            put(RTS_ParseInput.stNames.isCtrlDown, false);
            put(RTS_ParseInput.stNames.isShiftDown, false);
            put(RTS_ParseInput.stNames.RTSMode, null);
            put(RTS_ParseInput.stNames.pauseUnpause, null);
            put(RTS_ParseInput.stNames.playerShipHold, null);
            put(RTS_ParseInput.stNames.strafeKeys, null);
        }};
        HashMap<String, Boolean> strafeKeys = new HashMap<>();
        strafeKeys.put(RTS_ParseInput.skNames.left, false);
        strafeKeys.put(RTS_ParseInput.skNames.right, false);
        strafeKeys.put(RTS_ParseInput.skNames.up, false);
        strafeKeys.put(RTS_ParseInput.skNames.down, false);
        init.put(RTS_ParseInput.stNames.strafeKeys, strafeKeys);
        init.put(RTS_ParseInput.stNames.RTSMode, (boolean)((HashMap<String, Object>)this.getState(RTSAssist.stNames.config))
                .get(RTSAssist.coNames.defaultModeIsRTS));
        init.put(RTS_ParseInput.stNames.pauseUnpause, (boolean)((HashMap<String, Object>)this.getState(RTSAssist.stNames.config))
                .get(RTSAssist.coNames.pauseUnpause));
//        init.put("inputManager", new RTSInputManager(this.returnState()));
        this.setState(init);
//        Binds buildHotkeys = new Binds(this.returnState());
//        buildHotkeys.createBinds((RTSInputManager)this.getState("inputManager"));
    }

    public void update (float amount, List<InputEventAPI> events) {
//        ((RTSInputManager)this.getState("inputManager")).listenAndExecute(amount, events);
        if (
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI().isShowingCommandUI()
                || ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isUIShowingDialog()
                || this.getState(RTS_ParseInput.stNames.playerShipHold) == null
        ) {
            this.setState(RTS_ParseInput.stNames.rightClickStore, null);
            this.setState(RTS_ParseInput.stNames.leftClickStore, null);
            return;
        }

        HashMap<String, Object> hotKeys = (HashMap<String, Object>)this.getState(RTSAssist.stNames.hotKeys);
        boolean DRMBFlag = false;
        this.setState(RTS_ParseInput.stNames.isZooming, 0);
        boolean altFlag = false;
        boolean ctrlFlag = false;
        boolean shiftFlag = false;
        HashMap<String, Boolean> strafeKeys = (HashMap<String, Boolean>)this.getState(RTS_ParseInput.stNames.strafeKeys);
        for (InputEventAPI x: events) {
            if (x.isAltDown()) altFlag = true;
            this.setState(RTS_ParseInput.stNames.isAltDown, altFlag);
            if (x.isCtrlDown()) ctrlFlag = true;
            this.setState(RTS_ParseInput.stNames.isCtrlDown, ctrlFlag);
            if (x.isShiftDown()) shiftFlag = true;
            this.setState(RTS_ParseInput.stNames.isShiftDown, shiftFlag);
            if (x.isConsumed()) continue;
            if (x.isKeyDownEvent())
                if ((hotKeys.get(RTSAssist.hoNames.enable_RTSMode) == null && x.getEventValue() == 58)
                        || (hotKeys.get(RTSAssist.hoNames.enable_RTSMode) != null
                        && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.enable_RTSMode)
                        || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.enable_RTSMode) + 32)))
                ) {
                    ((RTS_ModeManager)this.getState(RTSAssist.stNames.modeManager)).playerChangesMode();
                    x.consume();
                    break;
                }
            if (x.isMouseMoveEvent()) {
                this.setState(RTS_ParseInput.stNames.screenSpace, new Vector2f((float)x.getX(), (float)x.getY()));
                this.setState(
                        RTS_ParseInput.stNames.worldSpace,
                        CombatUtils.toWorldCoordinates((Vector2f)this.getState(RTS_ParseInput.stNames.screenSpace))
                );
            }
            else if (x.isMouseScrollEvent()) {
                this.setState(RTS_ParseInput.stNames.isZooming, x.getEventValue() > 0 ? 1 : -1);
                x.consume();
            }
            else if ((Boolean)this.getState(RTS_ParseInput.stNames.RTSMode)) {
                if (x.isKeyboardEvent()) {
                    if ((boolean)this.getState(RTSAssist.stNames.inDevelopment)
                            && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.param1)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.param1) + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState(RTSAssist.stNames.testSuite)).deleteShips(
                                (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
                        );
                        x.consume();
                    } else if ((boolean)this.getState(RTSAssist.stNames.inDevelopment)
                            && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.param2)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.param2) + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState(RTSAssist.stNames.testSuite)).soutShipDetails(
                                (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
                        );
                        x.consume();
                    } else if ((boolean)this.getState(RTSAssist.stNames.inDevelopment)
                            && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.param3)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.param3) + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState(RTSAssist.stNames.testSuite)).spawnShipIter(
                                (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace)
                        );
                        x.consume();
                    } else if ((boolean)this.getState(RTSAssist.stNames.inDevelopment)
                            && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.param4)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.param4) + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState(RTSAssist.stNames.testSuite)).addShipsToCommons(
                                (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
                        );
                        x.consume();
                    } else if ((boolean)this.getState(RTSAssist.stNames.inDevelopment)
                            && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.param5)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.param5) + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState(RTSAssist.stNames.testSuite)).removeShipsFromCommons(
                                (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
                        );
                        x.consume();
                    } else if ((boolean)this.getState(RTSAssist.stNames.inDevelopment)
                            && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.param6)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.param6) + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState(RTSAssist.stNames.testSuite)).spawnCommonsShipIter(
                                (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace)
                        );
                        x.consume();
                    } else if ((boolean)this.getState(RTSAssist.stNames.inDevelopment)
                            && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.param7)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.param7) + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState(RTSAssist.stNames.testSuite)).spawnTestShip(
                                (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace)
                        );
                        x.consume();
                    } else if ((boolean)this.getState(RTSAssist.stNames.inDevelopment)
                            && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.param8)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.param8) + 32))
                    ) {
                        ((RTS_ShipTestSuite)this.getState(RTSAssist.stNames.testSuite)).setTestShip(
                                (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
                        );
                        x.consume();
                    } else if (x.getEventValue() == 30
                    ) {
                        strafeKeys.put(RTS_ParseInput.skNames.left, !x.isKeyUpEvent());
                        x.consume();
                    } else if (x.getEventValue() == 32
                    ) {
                        strafeKeys.put(RTS_ParseInput.skNames.right, !x.isKeyUpEvent());
                        x.consume();
                     } else if (x.getEventValue() == 17
                    ) {
                        strafeKeys.put(RTS_ParseInput.skNames.up, !x.isKeyUpEvent());
                        x.consume();
                    } else if (x.getEventValue() == 31
                    ) {
                        strafeKeys.put(RTS_ParseInput.skNames.down, !x.isKeyUpEvent());
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.saveLayout)
                            || x.getEventChar() == (char)((char) hotKeys.get(RTSAssist.hoNames.saveLayout) + 32)
                    ) {
                        ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).saveAssignments();
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.loadLayout)
                            || x.getEventChar() == (char)((char) hotKeys.get(RTSAssist.hoNames.loadLayout) + 32)
                    ) {
                        ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).
                                loadAssignments((Vector2f) this.getState(RTS_ParseInput.stNames.worldSpace));
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.vent)
                            || x.getEventChar() == (char)((char) hotKeys.get(RTSAssist.hoNames.vent) + 32)
                    ) {
                        if (this.getState(RTS_ParseInput.stNames.currentSelection) != null)
                            for(ShipAPI ship: (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection))
                                ((RTS_VentManager)this.getState(RTS_TaskManager.stNames.ventManager)).ventShip(ship);
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.useSystem)
                            || x.getEventChar() == (char)((char) hotKeys.get(RTSAssist.hoNames.useSystem) + 32)
                    ) {
                        if (this.getState(RTS_ParseInput.stNames.currentSelection) != null)
                            for(ShipAPI ship: (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection))
                                if (ship.getSystem().canBeActivated())
                                    ((RTS_SystemManager)this.getState(RTS_TaskManager.stNames.systemManager))
                                            .queueSystemActivation(ship);
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.moveTogether)
                            || x.getEventChar() == (char)((char) hotKeys.get(RTSAssist.hoNames.moveTogether) + 32)
                    ) {
                        ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).addNewTogaInstance(
                                (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
                        );
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.attackMove)
                            || x.getEventChar() == (char)((char) hotKeys.get(RTSAssist.hoNames.attackMove) + 32)
                    ) {
                        if (this.getState(RTS_ParseInput.stNames.currentSelection) != null)
                            for(ShipAPI ship: (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)) {
                                if (!((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager))
                                        .convertAssignmentOrQueueToAttackMove(
                                                ship,
                                                (boolean)this.getState(RTS_ParseInput.stNames.isShiftDown)
                                        )
                                ) {
                                    if (!(boolean)this.getState(RTS_ParseInput.stNames.isShiftDown))
                                        ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).createAssignments(
                                                null,
                                                Arrays.asList(ship),
                                                (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                                null,
                                                true,
                                                true,
                                                true,
                                                false
                                        );
                                    else
                                        ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).queueAssignments(
                                                Arrays.asList(ship),
                                                (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                                null,
                                                true,
                                                true
                                        );
                                }
                            }
                        x.consume();
                    } else if (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.deleteAssignments)
                            || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.deleteAssignments) + 32)
                    ) {
                        if (!(boolean)this.getState(RTS_ParseInput.stNames.isShiftDown))
                            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).deleteAssignments(
                                    (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                                    true
                            );
                        else
                            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).popAssignmentQueue(
                                    (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
                            );
                        x.consume();
                    } else if (x.getEventValue() == 45) { // ?
                        ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).editPriorityEnemy(
                                (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                                (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                false
                        );
                        x.consume();
                    } else if (x.getEventValue() == 29) { // left control
                        if ((boolean)this.getState(RTS_ParseInput.stNames.isAltDown)) {
                            if (this.getState(RTS_ParseInput.stNames.leftClickStoreSec) == null) {
                                this.setState(
                                        RTS_ParseInput.stNames.leftClickStoreSec,
                                        this.getState(RTS_ParseInput.stNames.worldSpace)
                                );
                                this.setState(RTS_ParseInput.stNames.isCtrlDown, true);
                            }
                        }
                        x.consume();
                    } else if (x.getEventValue() >= 2 && x.getEventValue() <= 6 && x.isKeyDownEvent()) { // number keys
                        if ((boolean)this.getState(RTS_ParseInput.stNames.isCtrlDown))
                            ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener))
                                    .controlGroupHandler((Integer)x.getEventValue(), true);
                        else {
                            ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener))
                                    .controlGroupHandler((Integer)x.getEventValue(), false);
                            if (this.getState(RTS_ParseInput.stNames.controlGroupStore) != null) {
                                if (((Vector2f)this.getState(RTS_ParseInput.stNames.controlGroupStore)).getX() == (float)x.getEventValue()
                                        && ((float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start))
                                                - ((Vector2f)this.getState(RTS_ParseInput.stNames.controlGroupStore)).getY() < 0.2f))
                                    ((RTS_CameraRework)this.getState(RTSAssist.stNames.cameraRework))
                                            .zoomToControlGroup((Integer)x.getEventValue());
                            }
                            if (this.getState(RTS_ParseInput.stNames.controlGroupStore) == null)
                                this.setState(RTS_ParseInput.stNames.controlGroupStore, new Vector2f());
                            ((Vector2f)this.getState(RTS_ParseInput.stNames.controlGroupStore)).set(
                                    x.getEventValue(),
                                    (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start))
                            );
                        }
                        x.consume();
                    }
                }
                if (!(boolean)this.getState(RTS_ParseInput.stNames.isAltDown)) {
                    this.setState(RTS_ParseInput.stNames.leftClickStore, null);
                    this.setState(RTS_ParseInput.stNames.leftClickStoreSec, null);
                    if (this.getState(RTS_TaskManager.stNames.translateStore) != null) {
                        ((HashMap<?, ?>)this.getState(RTS_TaskManager.stNames.translateStore)).clear(); // Taskmanager State
                        ((HashMap<?, ?>)this.getState(RTS_TaskManager.stNames.rotationStore)).clear(); // Taskmanager State
                    }
                }
                if (!(boolean)this.getState(RTS_ParseInput.stNames.isCtrlDown))
                    this.setState(RTS_ParseInput.stNames.leftClickStoreSec, null);
                if ((boolean)this.getState(RTS_ParseInput.stNames.isAltDown)) {
                    ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).prematurelyEndEvent();
//                    this.setState(RTS_ParseInput.stNames.rightClickStore, null);
                    if (x.isLMBDownEvent()) {
                        this.setState(RTS_ParseInput.stNames.leftClickStore, this.getState(RTS_ParseInput.stNames.worldSpace));
                        x.consume();
                    }
                    else if(x.isLMBUpEvent()) {
                        if ((boolean)this.getState(RTS_ParseInput.stNames.isAltDown)
                                && !(boolean)this.getState(RTS_ParseInput.stNames.isCtrlDown))
                            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).translateAssignments(
                                    (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.leftClickStore),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                    false,
                                    (boolean)this.getState(RTS_ParseInput.stNames.isShiftDown)
                            );
                        else if ((boolean)this.getState(RTS_ParseInput.stNames.isAltDown)
                                && (boolean)this.getState(RTS_ParseInput.stNames.isCtrlDown))
                            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).rotateAssignments(
                                    (List <ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.leftClickStoreSec),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                    false,
                                    (boolean)this.getState(RTS_ParseInput.stNames.isShiftDown)
                            );
                        this.setState(RTS_ParseInput.stNames.leftClickStore, null);
                        x.consume();
                    }
                }
                else if ((boolean)this.getState(RTS_ParseInput.stNames.isCtrlDown)) {
                    if (x.isLMBDownEvent()) {
                        ((RTS_TaskManager) this.getState(RTSAssist.stNames.taskManager)).editPriorityEnemy(
                                (List<ShipAPI>) this.getState(RTS_ParseInput.stNames.currentSelection),
                                (Vector2f) this.getState(RTS_ParseInput.stNames.worldSpace),
                                true
                        );
                        x.consume();
                    }
                }
                else if (x.isLMBDownEvent()) {
                    ((RTS_SelectionListener) this.getState(RTSAssist.stNames.selectionListener)).beginEvent(x);
                    x.consume();
                }
                else if (x.isLMBUpEvent()) {
                    this.setState(
                            RTS_ParseInput.stNames.currentSelection,
                            ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).createSelectionEvent(
                                    (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
                            ));
                    x.consume();
                }
                if (x.isRMBDownEvent()) {
                    if (x.isDoubleClick()) DRMBFlag = true;
                    if (this.getState(RTS_ParseInput.stNames.currentSelection) != null
                            && (((boolean)this.getState(RTS_TaskManager.stNames.combatStarted))
                            || ((List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)).size() == 1)
                    ) {
                        if (!(boolean)this.getState(RTS_ParseInput.stNames.isShiftDown))
                            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).createAssignments(
                                    null,
                                    (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                    null,
                                    !DRMBFlag,
                                    false,
                                    false,
                                    false
                            );
                        else
                            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).queueAssignments(
                                    (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                    null,
                                    !DRMBFlag,
                                    false
                            );
                    }
                    this.setState(RTS_ParseInput.stNames.rightClickStore, this.getState(RTS_ParseInput.stNames.worldSpace));
                    this.setDeepState(
                            Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.rightClickDelta),
                            this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start))
                    );
                    x.consume();
                }
                else if (x.isRMBUpEvent() && this.getState(RTS_ParseInput.stNames.rightClickStore) != null) {
                    if (this.getState(RTS_ParseInput.stNames.currentSelection) != null &&
                            ((List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)).size() > 1)
                        System.out.println("RTSAssist: You attempted to RClick and drag multiple ships. This is not ready yet");
                    else if ((MathUtils.getDistance(
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.rightClickStore)
                            ) > 100f)
                            &&
                            ((float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start))
                            - (
                                    this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.rightClickDelta)) == null
                                    ? 0f : (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.rightClickDelta))
                            ) > 0.2f)
                    ) {
                        if (!(boolean)this.getState(RTS_ParseInput.stNames.isShiftDown)) {
                            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).createAssignments(
                                    null,
                                    (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.rightClickStore),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                    DRMBFlag,
                                    false,
                                    false,
                                     false
                            );
                        }
                        else {
                            ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).queueAssignments(
                                    (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.rightClickStore),
                                    (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                                    DRMBFlag,
                                    false
                            );
                        }
                    }
                    this.setState(RTS_ParseInput.stNames.rightClickStore, null);
                    this.setDeepState(Arrays.asList(RTSAssist.stNames.config, RTSAssist.amNames.rightClickDelta), null);
                    x.consume();
                }
            }
        }
    }

    public void tempInterface() {
        if (this.getState(RTS_ParseInput.stNames.leftClickStore) == null && this.getState(RTS_ParseInput.stNames.rightClickStore) == null)
            return;
        if (this.getState(RTS_ParseInput.stNames.rightClickStore) != null)
            ((RTS_Draw)this.getState(RTSAssist.stNames.draw)).rudeDrawLine(
                    CombatUtils.toScreenCoordinates((Vector2f)this.getState(RTS_ParseInput.stNames.rightClickStore)),
                    (Vector2f)this.getState(RTS_ParseInput.stNames.screenSpace)
            );
        if (this.getState(RTS_ParseInput.stNames.leftClickStore) != null) {
            if ((boolean)this.getState(RTS_ParseInput.stNames.isAltDown) && !(boolean)this.getState(RTS_ParseInput.stNames.isCtrlDown))
                ((RTS_TaskManager) this.getState(RTSAssist.stNames.taskManager)).translateAssignments(
                        (List<ShipAPI>) this.getState(RTS_ParseInput.stNames.currentSelection),
                        (Vector2f) this.getState(RTS_ParseInput.stNames.leftClickStore),
                        (Vector2f) this.getState(RTS_ParseInput.stNames.worldSpace),
                        true,
                        false
                );
            else if ((boolean)this.getState(RTS_ParseInput.stNames.isAltDown) && (boolean)this.getState(RTS_ParseInput.stNames.isCtrlDown))
                ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).rotateAssignments(
                        (List <ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                        (Vector2f)this.getState(RTS_ParseInput.stNames.leftClickStoreSec),
                        (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                        true,
                        false
                );
            else
                ((RTS_Draw)this.getState(RTSAssist.stNames.draw)).rudeDrawLine(
                        CombatUtils.toScreenCoordinates((Vector2f) this.getState(RTS_ParseInput.stNames.leftClickStore)),
                        (Vector2f) this.getState(RTS_ParseInput.stNames.screenSpace)
                );
        }
    }
}