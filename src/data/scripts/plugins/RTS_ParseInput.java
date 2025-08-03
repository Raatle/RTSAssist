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

import data.scripts.plugins.Setup.RTS_ParseInputKeyBinds;
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
        public String DRMBFlag = RTS_StatefulClasses.getUniqueIdentifier();
        public String controlGroupStore = RTS_StatefulClasses.getUniqueIdentifier();
        public String isAltDown = RTS_StatefulClasses.getUniqueIdentifier();
        public String isCtrlDown = RTS_StatefulClasses.getUniqueIdentifier();
        public String isShiftDown = RTS_StatefulClasses.getUniqueIdentifier();
        public String RTSMode = RTS_StatefulClasses.getUniqueIdentifier();
        public String pauseUnpause = RTS_StatefulClasses.getUniqueIdentifier();
        public String playerShipHold = RTS_StatefulClasses.getUniqueIdentifier();
        public String strafeKeys = RTS_StatefulClasses.getUniqueIdentifier();
        public String enemyStore = RTS_StatefulClasses.getUniqueIdentifier();

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
            put (RTS_ParseInput.stNames.enemyStore, null);
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
        this.bindsEngine = new RTS_ParseInputKeyBinds(this.returnState());
        this.setState(init);
    }

    RTS_ParseInputKeyBinds bindsEngine;
    HashMap<String, Object> bindsCheckList = new HashMap<>(50);

    private void processKeyboardEvents(InputEventAPI x) {
        this.bindsCheckList.clear();
        this.bindsCheckList.put(RTS_ParseInputKeyBinds.kiNames.eventChar, x.getEventChar());
        this.bindsCheckList.put(
                RTS_ParseInputKeyBinds.kiNames.inDevelopment,
                this.getState(RTSAssist.stNames.inDevelopment
        ));
        this.bindsCheckList.put(RTS_ParseInputKeyBinds.kiNames.eventValue, x.getEventValue());
        this.bindsEngine.setEventAPI(x);
        this.bindsEngine.execBinds(this.bindsCheckList);
    }

    private boolean manageModes (InputEventAPI x) {
        if (x.isKeyDownEvent()) {
            HashMap<String, Object> hotKeys = (HashMap<String, Object>)this.getState(RTSAssist.stNames.hotKeys);
            if ((hotKeys.get(RTSAssist.hoNames.enable_RTSMode) == null && x.getEventValue() == 58)
                    || (hotKeys.get(RTSAssist.hoNames.enable_RTSMode) != null
                    && (x.getEventChar() == (char)hotKeys.get(RTSAssist.hoNames.enable_RTSMode)
                    || x.getEventChar() == (char)((char)hotKeys.get(RTSAssist.hoNames.enable_RTSMode) + 32)))
            ) {
                ((RTS_ModeManager)this.getState(RTSAssist.stNames.modeManager)).playerChangesMode();
                x.consume();
                return (true);
            }
        }
        return (false);
    }

    private boolean updateMouseLocData (InputEventAPI x) {
        if (x.isMouseMoveEvent()) {
            this.setState(RTS_ParseInput.stNames.screenSpace, new Vector2f((float)x.getX(), (float)x.getY()));
            this.setState(
                    RTS_ParseInput.stNames.worldSpace,
                    CombatUtils.toWorldCoordinates((Vector2f)this.getState(RTS_ParseInput.stNames.screenSpace))
            );
            return (true);
        }
        else if (x.isMouseScrollEvent()) {
            this.setState(RTS_ParseInput.stNames.isZooming, x.getEventValue() > 0 ? 1 : -1);
            x.consume();
            return (true);
        }
        return (false);
    }

    private boolean preLoop () {
        if (
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI().isShowingCommandUI()
                || ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isUIShowingDialog()
                || this.getState(RTS_ParseInput.stNames.playerShipHold) == null
        ) {
            this.setState(RTS_ParseInput.stNames.rightClickStore, null);
            this.setState(RTS_ParseInput.stNames.leftClickStore, null);
            return (false);
        }
        this.setState(RTS_ParseInput.stNames.DRMBFlag, false);
        this.setState(RTS_ParseInput.stNames.isZooming, 0);
        this.setState(RTS_ParseInput.stNames.isAltDown, false);
        this.setState(RTS_ParseInput.stNames.isCtrlDown, false);
        this.setState(RTS_ParseInput.stNames.isShiftDown, false);
        return (true);
    }

    private boolean loopInit (InputEventAPI x) {
        if (x.isAltDown()) this.setState(RTS_ParseInput.stNames.isAltDown,true);
        if (x.isCtrlDown()) this.setState(RTS_ParseInput.stNames.isCtrlDown, true);
        if (x.isShiftDown()) this.setState(RTS_ParseInput.stNames.isShiftDown, true);
        return (x.isConsumed());
    }

    private void modifierNegativeCheck () {
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
    }

    private void LMBInputIfAltDown (InputEventAPI x) {
        ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).prematurelyEndEvent();
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

    private void LMBInputIfCtrlDown (InputEventAPI x) {
        if (x.isLMBDownEvent()) {
            ((RTS_TaskManager) this.getState(RTSAssist.stNames.taskManager)).editPriorityEnemy(
                    (List<ShipAPI>) this.getState(RTS_ParseInput.stNames.currentSelection),
                    (Vector2f) this.getState(RTS_ParseInput.stNames.worldSpace),
                    true
            );
            x.consume();
        }
    }

    private void LMBInputNoModifiers (InputEventAPI x) {
        if (x.isLMBDownEvent()) {
            ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).beginEvent(x);
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
    }

    private void RMBInput (InputEventAPI x) {
        if (x.isRMBDownEvent()) {
            if (x.isDoubleClick()) this.setState(RTS_ParseInput.stNames.DRMBFlag, true);
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
                            !(boolean)this.getState(RTS_ParseInput.stNames.DRMBFlag),
                            false,
                            false,
                            false
                    );
                else
                    ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).queueAssignments(
                            (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                            (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                            null,
                            !(boolean)this.getState(RTS_ParseInput.stNames.DRMBFlag),
                            false
                    );
            }
            this.setState(RTS_ParseInput.stNames.rightClickStore, this.getState(RTS_ParseInput.stNames.worldSpace));
            this.setState(RTS_ParseInput.stNames.enemyStore, ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager))
                    .getEnemies("primary", (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace), null));
            this.setDeepState(
                    Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.rightClickDelta),
                    this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start))
            );
            x.consume();
        }
        else if (x.isRMBUpEvent() && this.getState(RTS_ParseInput.stNames.rightClickStore) != null) {
            if (this.getState(RTS_ParseInput.stNames.currentSelection) != null &&
                    ((List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)).size() > 1) {}
//                System.out.println("RTSAssist: You attempted to RClick and drag multiple ships. This is not ready yet");
            else if ((MathUtils.getDistance(
                            (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                            (Vector2f)this.getState(RTS_ParseInput.stNames.rightClickStore)
                    ) > 100f)
                    && ((float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start))
                            - (this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.rightClickDelta)) == null
                                    ? 0f
                                    : (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.rightClickDelta))
                    ) > 0.2f)
            ) {
                if (!(boolean)this.getState(RTS_ParseInput.stNames.isShiftDown)) {
                    ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).createAssignments(
                            null,
                            (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                            this.getState(RTS_ParseInput.stNames.enemyStore) == null
                                    ? (Vector2f)this.getState(RTS_ParseInput.stNames.rightClickStore)
                                    : ((ShipAPI)this.getState(RTS_ParseInput.stNames.enemyStore)).getLocation(),
                            (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                            (boolean)this.getState(RTS_ParseInput.stNames.DRMBFlag),
                            false,
                            false,
                            false
                    );
                }
                else {
                    ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).queueAssignments(
                            (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection),
                            this.getState(RTS_ParseInput.stNames.enemyStore) == null
                                    ? (Vector2f)this.getState(RTS_ParseInput.stNames.rightClickStore)
                                    : ((ShipAPI)this.getState(RTS_ParseInput.stNames.enemyStore)).getLocation(),
                            (Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace),
                            (boolean)this.getState(RTS_ParseInput.stNames.DRMBFlag),
                            false
                    );
                }
            }
            this.setState(RTS_ParseInput.stNames.rightClickStore, null);
            this.setState(RTS_ParseInput.stNames.enemyStore, null);
            this.setDeepState(Arrays.asList(RTSAssist.stNames.config, RTSAssist.amNames.rightClickDelta), null);
            x.consume();
        }
    }

    public void update (float amount, List<InputEventAPI> events) {

//        List<ShipAPI> selection = (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection);
//        if (selection != null && !selection.isEmpty()) {
//            for (ShipAPI ship : selection) {
//                if (ship.getCustomData().get(RTSAssist.shipCNames.blockAi) != null)
//                    System.out.println("blocked");
//            }
//        }
        if (!this.preLoop())
            return;
        for (InputEventAPI x: events) {
            if (this.loopInit(x))
                continue;
            if (this.manageModes(x))
                break;
            if (this.updateMouseLocData(x))
                continue;
            if ((Boolean)this.getState(RTS_ParseInput.stNames.RTSMode)) {
                if (x.isKeyboardEvent())
                    this.processKeyboardEvents(x);
                this.modifierNegativeCheck();
                if ((boolean)this.getState(RTS_ParseInput.stNames.isAltDown))
                    this.LMBInputIfAltDown(x);
                else if ((boolean)this.getState(RTS_ParseInput.stNames.isCtrlDown))
                    this.LMBInputIfCtrlDown(x);
                else
                    this.LMBInputNoModifiers(x);
                this.RMBInput(x);
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