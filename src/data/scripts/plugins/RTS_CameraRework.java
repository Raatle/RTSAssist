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

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import data.scripts.plugins.Utils.RTS_AssortedFunctions;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.Global;

import org.lazywizard.lazylib.MathUtils;

import data.scripts.plugins.Utils.RTS_StatefulClasses;

public class RTS_CameraRework extends RTS_StatefulClasses {

    public static class classidentifiers {
        public String sDimensions = RTS_StatefulClasses.getUniqueIdentifier();
        public String mDimensions = RTS_StatefulClasses.getUniqueIdentifier();
        public String targetLLVec = RTS_StatefulClasses.getUniqueIdentifier();
        public String LLVec = RTS_StatefulClasses.getUniqueIdentifier();
        public String targetZoom = RTS_StatefulClasses.getUniqueIdentifier();
        public String viewPort = RTS_StatefulClasses.getUniqueIdentifier();
        public String zoom = RTS_StatefulClasses.getUniqueIdentifier();
        public String zoomVelocity = RTS_StatefulClasses.getUniqueIdentifier();
        public String RTSModeTracker = RTS_StatefulClasses.getUniqueIdentifier();
        public String zoomSwitchHold = RTS_StatefulClasses.getUniqueIdentifier();
        public String playerIsPanning = RTS_StatefulClasses.getUniqueIdentifier();
        public String panVelocity = RTS_StatefulClasses.getUniqueIdentifier();
        public String panVelocityKeyboard = RTS_StatefulClasses.getUniqueIdentifier();
        public String scrollSmoothing = RTS_StatefulClasses.getUniqueIdentifier();
        public String scrollSmoothingKeyboard = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static classidentifiers stNames = new classidentifiers();

    public RTS_CameraRework(Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put(RTS_CameraRework.stNames.sDimensions, (Vector2f)null);
            put(RTS_CameraRework.stNames.mDimensions, new Vector2f(
                    Global.getSettings().getScreenWidth(),
                    Global.getSettings().getScreenHeight())
            );
            put(RTS_CameraRework.stNames.targetLLVec, (Vector2f)null);
            put(RTS_CameraRework.stNames.LLVec, (Vector2f)null);
            put(RTS_CameraRework.stNames.targetZoom, 1f);
            put(RTS_CameraRework.stNames.viewPort, null);
            put(RTS_CameraRework.stNames.zoom, 1f); // Is this unused??
            put(RTS_CameraRework.stNames.zoomVelocity, 0.1); // is this unused??
            put(RTS_CameraRework.stNames.RTSModeTracker, false);
            put(RTS_CameraRework.stNames.zoomSwitchHold, new Vector2f(1f,1f));
            put(RTS_CameraRework.stNames.playerIsPanning, false);
            put(RTS_CameraRework.stNames.panVelocity, null);
            put(RTS_CameraRework.stNames.panVelocityKeyboard, null);
            put(RTS_CameraRework.stNames.scrollSmoothing, null);
            put(RTS_CameraRework.stNames.scrollSmoothingKeyboard, null);
        }};
        init.put(RTS_CameraRework.stNames.panVelocity, MathUtils.clamp((float)((HashMap<String, Object>)this.getState(RTSAssist.stNames.config))
                .get(RTSAssist.coNames.scrollSpeed), 1f, 100f));
        init.put(RTS_CameraRework.stNames.scrollSmoothing, MathUtils.clamp((float)((HashMap<String, Object>)this.getState(RTSAssist.stNames.config))
                .get(RTSAssist.coNames.scrollSmoothing), 1f, 50f));
        init.put(RTS_CameraRework.stNames.panVelocityKeyboard, MathUtils.clamp((float)((HashMap<String, Object>)this.getState(RTSAssist.stNames.config))
                .get(RTSAssist.coNames.scrollSpeedKeyboard), 1f, 100f));
        init.put(RTS_CameraRework.stNames.scrollSmoothingKeyboard, MathUtils.clamp((float)((HashMap<String, Object>)this.getState(RTSAssist.stNames.config))
                .get(RTSAssist.coNames.scrollSmoothingKeyboard), 1f, 50f));
        init.put(RTS_CameraRework.stNames.viewPort, ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getViewport());
        this.setState(init);
    }

    public int _sn = 4;
    private class easeBuffer {
        float hold = 0f;
    }

    public void update (Vector2f screenSpace, int isZooming) {
        if (((boolean)this.getDeepState(Arrays.asList(RTSAssist.stNames.config, RTSAssist.coNames.rememberZoom))))
            if ((boolean)this.getState(RTS_ParseInput.stNames.RTSMode) != (boolean)this.getState(RTS_CameraRework.stNames.RTSModeTracker)) {
                this.setState(RTS_CameraRework.stNames.RTSModeTracker, (boolean)this.getState(RTS_ParseInput.stNames.RTSMode));
                if ((boolean)this.getState(RTS_ParseInput.stNames.RTSMode)) {
                    ((Vector2f)this.getState(RTS_CameraRework.stNames.zoomSwitchHold)).setX(
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom) + (this.zoomEaseBuffer.hold / 100000000f) // Current zoom + any delta still left over
                    );
                    this.zoomEaseBuffer.hold = (((Vector2f)this.getState(RTS_CameraRework.stNames.zoomSwitchHold)).getY()
                            - (float)this.getState(RTS_CameraRework.stNames.targetZoom)) * 100000000f; // Difference between what is stored and what is current times a million times a 100.
                }
                else {
                    ((Vector2f)this.getState(RTS_CameraRework.stNames.zoomSwitchHold)).setY(
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom) + (this.zoomEaseBuffer.hold / 100000000f)
                    );
                    this.zoomEaseBuffer.hold = (((Vector2f)this.getState(RTS_CameraRework.stNames.zoomSwitchHold)).getX()
                            - (float)this.getState(RTS_CameraRework.stNames.targetZoom)) * 100000000f;
                }
            }
        if (screenSpace == null) return;
        if (this.getState(RTS_CameraRework.stNames.sDimensions) == null)
            this.setState(
                    RTS_CameraRework.stNames.sDimensions,
                    new Vector2f(
                            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getViewport().getVisibleWidth(),
                            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getViewport().getVisibleHeight()
                    )
            );
        if (this.getState(RTS_CameraRework.stNames.LLVec) == null &&
                !((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI().isShowingCommandUI()) {
            initCameraXY();
        }
        if (!((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI().isShowingCommandUI()) {
            this.zoomManager(isZooming);
            if ((boolean)this.getState(RTS_ParseInput.stNames.RTSMode)) {
                this.strafeManager((HashMap<String, Boolean>)this.getState(RTS_ParseInput.stNames.strafeKeys));
                this.panManager(screenSpace);
                this.setState(
                        RTS_CameraRework.stNames.playerIsPanning,
                        (boolean)this.getState(RTS_CameraRework.stNames.playerIsPanning)
                                && (this.panXSmooth.hold != 0f || this.panYSmooth.hold != 0f)
                );
            }
            else if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip() != null) {
                    if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI().getEntityToFollowV2() != null)
                        this.playerIsFocusingEnemy(
                                screenSpace,
                                ((Vector2f)this.getState(RTS_ParseInput.stNames.worldSpace)),
                                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine))
                                        .getPlayerShip().getLocation(),
                                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine))
                                        .getCombatUI().getEntityToFollowV2().getLocation()
                        );
                    else
                        this.playerCameraManager(
                                screenSpace,
                                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine))
                                        .getPlayerShip().getLocation(),
                                1.3f,
                                1.7f
                        );
                }
        }
        this.render();
    }

    private void initCameraXY () {
        Vector2f init = null;
        for (ShipAPI ship : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips())
            if (ship.isStation() && ship.getOriginalOwner() == 0) {
                init = new Vector2f(0f, -4000f);
                break;
            }
        if (init == null) {
            List<ShipAPI> shipList = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips();
            float enemyLowest = 1000000;
            for (ShipAPI ship : shipList) {
                if ((ship.getOriginalOwner() != 0 && !ship.isAlly()) && ship.getLocation().getY() < enemyLowest)
                    enemyLowest = ship.getLocation().getY();
            }
            List<Vector2f> shipGroup = new ArrayList<>();
            for (ShipAPI ship : shipList) {
                if (
                        ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).dontSelect(ship)
                                || ship.getLocation().getY() > enemyLowest
                )
                    continue;
                shipGroup.add(ship.getLocation());
            }
            if (!shipGroup.isEmpty()) {
                shipGroup.sort(RTS_AssortedFunctions.Vector2fComparator('y'));
                init = new Vector2f(
                        0f,
                        shipGroup.get(shipGroup.size() - 1).getY()
                );
            }
            else
                init = new Vector2f(
                        0f,
                        -3500f
                );
        }
        Vector2f sDimensions = ((Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions));
        this.setState(RTS_CameraRework.stNames.LLVec, new Vector2f(
                init.getX() - (sDimensions.getX() / 2),
                init.getY() - (sDimensions.getY() / 2)
        ));
        this.setState(RTS_CameraRework.stNames.targetLLVec, new Vector2f(
                init.getX() - (sDimensions.getX() / 2),
                init.getY() - (sDimensions.getY() / 2)
        ));
        ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getViewport().setExternalControl(true);
    }

    private void strafeManager(HashMap<String, Boolean> strafeKeys) {
        if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isUIShowingDialog())
            return;
        Vector2f hold = (Vector2f)this.getState(RTS_CameraRework.stNames.targetLLVec);
        if (hold == null)
            return;
        float delta = (float)this.getState(RTS_CameraRework.stNames.panVelocityKeyboard) * (float)this.getState(RTS_CameraRework.stNames.targetZoom);
        Vector2f pan = new Vector2f();
        if (strafeKeys.get(RTS_ParseInput.skNames.left) && !strafeKeys.get(RTS_ParseInput.skNames.right))
            pan.setX(-delta);
        else if (strafeKeys.get(RTS_ParseInput.skNames.right) && !strafeKeys.get(RTS_ParseInput.skNames.left))
            pan.setX(delta);
        else { pan.setX(0f); }
        if (strafeKeys.get(RTS_ParseInput.skNames.up) && !strafeKeys.get(RTS_ParseInput.skNames.down))
            pan.setY(delta);
        else if (strafeKeys.get(RTS_ParseInput.skNames.down) && !strafeKeys.get(RTS_ParseInput.skNames.up))
            pan.setY(-delta);
        else { pan.setY(0f); }
        if (pan.getX() != 0f || pan.getY() != 0f)
            this.setState(RTS_CameraRework.stNames.playerIsPanning, true);
        else if (!(boolean)this.getState(RTS_CameraRework.stNames.playerIsPanning))
            return;
        float smoothStopper = (float)this.getState(RTS_CameraRework.stNames.panVelocityKeyboard) / 2f;
        Vector2f deltaTotal = new Vector2f(
                this.easeMachine(
                        this.panXSmooth,
                        pan.getX() / smoothStopper,
                        (float)this.getState(RTS_CameraRework.stNames.scrollSmoothingKeyboard)
                ) * smoothStopper,
                this.easeMachine(
                        this.panYSmooth,
                        pan.getY() / smoothStopper,
                        (float)this.getState(RTS_CameraRework.stNames.scrollSmoothingKeyboard)
                ) * smoothStopper
        );
        hold.set(
                hold.getX() + deltaTotal.getX(),
                hold.getY() + deltaTotal.getY()
        );
        this.setState(RTS_CameraRework.stNames.targetLLVec, hold);
        this.setState(RTS_CameraRework.stNames.LLVec, hold);
    }

    private void panManager (Vector2f screenSpace) {
        if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isUIShowingDialog()
                || (boolean)this.getState(RTS_CameraRework.stNames.playerIsPanning)
                || this.mouseOutOfBounds()
        )
            return;
        Vector2f hold = (Vector2f)this.getState(RTS_CameraRework.stNames.targetLLVec);
        if (hold == null)
            return;
        float delta = (float)this.getState(RTS_CameraRework.stNames.panVelocity)
                * (float)this.getState(RTS_CameraRework.stNames.targetZoom);
        Vector2f pan = new Vector2f();
        if (screenSpace.getX() <= 6f && screenSpace.getX() >= 0f)
            pan.setX(-delta);
        else if (screenSpace.getX() >= (((Vector2f)this.getState(RTS_CameraRework.stNames.mDimensions)).getX() - 6f)
                && screenSpace.getX() <= ((Vector2f)this.getState(RTS_CameraRework.stNames.mDimensions)).getX())
            pan.setX(delta);
        else { pan.setX(0f); }
        if (screenSpace.getY() <= 6f && screenSpace.getY() >= 0f)
            pan.setY(-delta);
        else if (screenSpace.getY() >= (((Vector2f)this.getState(RTS_CameraRework.stNames.mDimensions)).getY() - 6f)
                && screenSpace.getY() <= ((Vector2f)this.getState(RTS_CameraRework.stNames.mDimensions)).getY())
            pan.setY(delta);
        else { pan.setY(0f); }
        float smoothStopper = (float)this.getState(RTS_CameraRework.stNames.panVelocity) / 2f;
        Vector2f deltaTotal = new Vector2f(
                this.easeMachine(
                        this.panXSmooth,
                        pan.getX() / smoothStopper,
                        (float)this.getState(RTS_CameraRework.stNames.scrollSmoothing)
                ) * smoothStopper,
                this.easeMachine(
                        this.panYSmooth,
                        pan.getY() / smoothStopper,
                        (float)this.getState(RTS_CameraRework.stNames.scrollSmoothing)
                ) * smoothStopper
        );
        hold.set(
                hold.getX() + deltaTotal.getX(),
                hold.getY() + deltaTotal.getY()
        );
        this.setState(RTS_CameraRework.stNames.targetLLVec, hold);
        this.setState(RTS_CameraRework.stNames.LLVec, hold);
    }
    easeBuffer panXSmooth = new easeBuffer();
    easeBuffer panYSmooth = new easeBuffer();

    private void playerCameraManager(Vector2f screenSpace, Vector2f playerPosition, float deltaFromTarget, float smoothness) {
        Vector2f playerPositionInScreen = CombatUtils.toScreenCoordinates(playerPosition);
        float zoom = (float)this.getState(RTS_CameraRework.stNames.targetZoom);
        Vector2f sDimensions = ((Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions));
        Vector2f hold = new Vector2f(
                playerPosition.getX() - (sDimensions.getX() / 2),
                playerPosition.getY() - (sDimensions.getY() / 2)
        );
        hold.set(
                hold.getX() - (((playerPositionInScreen.getX() - screenSpace.getX()) * deltaFromTarget) / 2f * zoom),
                hold.getY() - (((playerPositionInScreen.getY() - screenSpace.getY()) * deltaFromTarget) / 2f * zoom)
        );
        Vector2f delta = new Vector2f(
                hold.getX() - ((Vector2f)this.getState(RTS_CameraRework.stNames.targetLLVec)).getX(),
                hold.getY() - ((Vector2f)this.getState(RTS_CameraRework.stNames.targetLLVec)).getY()
        );
        this.playerCameraXSmooth.hold = delta.getX() * 1000000f;
        this.playerCameraYSmooth.hold = delta.getY() * 1000000f;
        Vector2f finalVec = new Vector2f(
                ((Vector2f)this.getState(RTS_CameraRework.stNames.targetLLVec)).getX() + this.easeMachine(
                        this.playerCameraXSmooth,
                        0f,
                        10f * smoothness),
                ((Vector2f)this.getState(RTS_CameraRework.stNames.targetLLVec)).getY() + this.easeMachine(
                        this.playerCameraYSmooth,
                        0f,
                        10f * smoothness)
        );
        finalVec.set(
                Math.round(finalVec.getX() * 10f) /10f,
                Math.round(finalVec.getY() * 10f) /10f
        );
        this.setState(RTS_CameraRework.stNames.targetLLVec, finalVec);
        this.setState(RTS_CameraRework.stNames.LLVec, finalVec);
    }
    easeBuffer playerCameraXSmooth = new easeBuffer();
    easeBuffer playerCameraYSmooth = new easeBuffer();

    private void playerIsFocusingEnemy(
            Vector2f screenSpace,
            Vector2f worldSpace,
            Vector2f playerPosition,
            Vector2f enemyPosition
    ) {
        float nullSpaceAngle = VectorUtils.getAngle(playerPosition, enemyPosition);
        Vector2f rotatedWorldSpace = new Vector2f(worldSpace);
        VectorUtils.rotateAroundPivot(rotatedWorldSpace, playerPosition, 90f - nullSpaceAngle);
        Vector2f rotatedEnemyPosition = new Vector2f(enemyPosition);
        VectorUtils.rotateAroundPivot(rotatedEnemyPosition, playerPosition, 90f - nullSpaceAngle);
        if (rotatedWorldSpace.getY() > rotatedEnemyPosition.getY())
            this.playerCameraManager(CombatUtils.toScreenCoordinates(enemyPosition), enemyPosition, 1f, 5f);
        else
            this.playerCameraManager(screenSpace, enemyPosition, 0.8f, 3f);
    }

    private void zoomManager (int zoom) {
        if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI().isShowingCommandUI() ||
                ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isUIShowingDialog())
            return;
        float tZoom = (float)this.getState(RTS_CameraRework.stNames.targetZoom);
        float increment = (float)(((HashMap<String, Object>)this.getState(RTSAssist.stNames.config))
                .get(RTSAssist.coNames.maxZoom)) / 12f;
        tZoom = tZoom + (this.easeMachine(
                this.zoomEaseBuffer,
                zoom == 0
                ? 0f
                : zoom == -1 ? (increment * 100f) : (-increment * 100f),
                15f
        ) / 100f);
        this.setState(
                RTS_CameraRework.stNames.targetZoom,
                MathUtils.clamp(
                        tZoom,
                        1f,
                        (float)(((HashMap<String, Object>)this.getState(RTSAssist.stNames.config))
                                .get(RTSAssist.coNames.maxZoom))
                )
        );
    }
    easeBuffer zoomEaseBuffer = new easeBuffer();

    public void zoomToControlGroup (Integer group) {
        List<ShipAPI> members = ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener))
                .getControlGroupList(group);
        if (members == null)
            return;
        Vector2f sDimensions = (Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions);
        Vector2f totals = new Vector2f();
        for (ShipAPI ship : members)
            totals.set(
                    totals.getX() + ship.getLocation().getX(),
                    totals.getY() + ship.getLocation().getY()
            );
        totals.set(
                (totals.getX() / members.size()) - (sDimensions.getX() / 2),
                (totals.getY() / members.size()) - (sDimensions.getY() / 2) + 500f
        );

        this.setState(RTS_CameraRework.stNames.targetLLVec, totals);
        this.setState(RTS_CameraRework.stNames.LLVec, totals);
        this.panXSmooth.hold = 0f;
        this.panYSmooth.hold = 0f;

    }

    private float easeMachine(easeBuffer buffer, float increment, float delta) {
        float multi = (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.frameMult)) *
                (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.dilation));
        increment = increment / multi;
        increment *= 1000000f;
        buffer.hold += increment;
        float bufferHold = buffer.hold;
        if (buffer.hold > 0f) {
            buffer.hold += (buffer.hold / delta) * -1;
            if (buffer.hold < (1000000f / multi) && buffer.hold > (-1000000f / multi)) {
                buffer.hold = 0f;
                return (0f);
            }
            return (bufferHold / delta / 1000000f);
        }
        else if (buffer.hold < 0f) {
            buffer.hold += (buffer.hold / delta) * -1;
            if (buffer.hold < (1000000f / multi) && buffer.hold > -(1000000f / multi)) {
                buffer.hold = 0f;
                return (0f);
            }
            return (bufferHold / delta / 1000000f);
        }
        else
            return (0f);
    }

    private boolean mouseOutOfBounds () {
        Vector2f screenSpace = (Vector2f)this.getState(RTS_ParseInput.stNames.screenSpace);
        Vector2f mFinal = new Vector2f(
                ((Double)MouseInfo.getPointerInfo().getLocation().getX()).floatValue(),
                ((Double)MouseInfo.getPointerInfo().getLocation().getY()).floatValue()
        );
        if (OOBHoldInst.screenSpace != null && OOBHoldInst.screenSpace.equals(screenSpace)) {
            if (OOBHoldInst.mFinal != null && !OOBHoldInst.mFinal.equals(mFinal))
                OOBHoldInst.blocking = true;
        }
        else
            OOBHoldInst.blocking = false;
        OOBHoldInst.screenSpace = screenSpace;
        OOBHoldInst.mFinal = mFinal;
        if (OOBHoldInst.blocking && OOBHoldInst.iter < 5)
            OOBHoldInst.iter++;
        if (!OOBHoldInst.blocking)
            OOBHoldInst.iter = 0;
        return (OOBHoldInst.iter > 4);
    }
    private class OOBHold {
            Vector2f screenSpace = null;
            Vector2f mFinal = null;
            boolean blocking = false;
            int iter = 0;
    }
    OOBHold OOBHoldInst = new OOBHold();

    private void render() {
        if (this.getState(RTS_CameraRework.stNames.LLVec) == null) return;
        ((ViewportAPI)this.getState(RTS_CameraRework.stNames.viewPort)).set(
                ((Vector2f)this.getState(RTS_CameraRework.stNames.LLVec)).getX()
                        - (((Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions)).getX() * ((float)this.getState(RTS_CameraRework.stNames.targetZoom) / 2f))
                        + (((Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions)).getX() / 2f),
                ((Vector2f)this.getState(RTS_CameraRework.stNames.LLVec)).getY()
                        - (((Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions)).getY() * ((float)this.getState(RTS_CameraRework.stNames.targetZoom) / 2f))
                        + (((Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions)).getY() / 2f),
                ((Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions)).getX() * (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                ((Vector2f)this.getState(RTS_CameraRework.stNames.sDimensions)).getY() * (float)this.getState(RTS_CameraRework.stNames.targetZoom)
        );
    }
}