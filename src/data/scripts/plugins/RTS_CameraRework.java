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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

    public RTS_CameraRework(Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put("sDimensions", (Vector2f)null);
            put("mDimensions", new Vector2f(Global.getSettings().getScreenWidth(), Global.getSettings().getScreenHeight()));
            put("targetLLVec", (Vector2f)null);
            put("LLVec", (Vector2f)null);
            put("targetZoom", 1f);
            put("zoom", 1f);
            put("zoomVelocity", 0.1);
            put("panVelocity", null);
            put("RTSModeTracker", false);
            put("zoomSwitchHold", new Vector2f(1f,1f));
            put("playerIsPanning", false);
        }};
        init.put("panVelocity", MathUtils.clamp((float)((HashMap<String, Object>)this.getState("config"))
                .get("scrollSpeed"), 1f, 100f));
        init.put("scrollSmoothing", MathUtils.clamp((float)((HashMap<String, Object>)this.getState("config"))
                .get("scrollSmoothing"), 1f, 50f));
        init.put("panVelocityKeyboard", MathUtils.clamp((float)((HashMap<String, Object>)this.getState("config"))
                .get("scrollSpeedKeyboard"), 1f, 100f));
        init.put("scrollSmoothingKeyboard", MathUtils.clamp((float)((HashMap<String, Object>)this.getState("config"))
                .get("scrollSmoothingKeyboard"), 1f, 50f));
        init.put("viewPort", ((CombatEngineAPI)this.getState("engine")).getViewport());
        this.setState(init);
    }

    private class easeBuffer {
        float hold = 0f;
    }

    public void update (Vector2f screenSpace, int isZooming) {
        if ((boolean)this.getState("RTSMode") != (boolean)this.getState("RTSModeTracker")) {
            this.setState("RTSModeTracker", (boolean)this.getState("RTSMode"));
            if ((boolean)this.getState("RTSMode")) {
                ((Vector2f)this.getState("zoomSwitchHold")).setX(
                        (float)this.getState("targetZoom") + (this.zoomEaseBuffer.hold / 100000000f) // Current zoom + any delta still left over
                );
                this.zoomEaseBuffer.hold = (((Vector2f)this.getState("zoomSwitchHold")).getY()
                        - (float)this.getState("targetZoom")) * 100000000f; // Difference between what is stored and what is current times a million times a 100.
            }
            else {
                ((Vector2f)this.getState("zoomSwitchHold")).setY(
                        (float)this.getState("targetZoom") + (this.zoomEaseBuffer.hold / 100000000f)
                );
                this.zoomEaseBuffer.hold = (((Vector2f)this.getState("zoomSwitchHold")).getX()
                        - (float)this.getState("targetZoom")) * 100000000f;
            }
        }
        if (screenSpace == null) return;
        if (this.getState("sDimensions") == null)
            this.setState(
                    "sDimensions",
                    new Vector2f(
                            ((CombatEngineAPI)this.getState("engine")).getViewport().getVisibleWidth(),
                            ((CombatEngineAPI)this.getState("engine")).getViewport().getVisibleHeight()
                    )
            );
        if (this.getState("LLVec") == null &&
                !((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI()) {
            initCameraXY();
        }
        if (!((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI()) {
            this.zoomManager(isZooming);
            if ((boolean)this.getState("RTSMode")) {
                this.strafeManager((HashMap<String, Boolean>)this.getState("strafeKeys"));
                this.panManager(screenSpace);
                this.setState("playerIsPanning",
                        (boolean)this.getState("playerIsPanning")
                        ? this.panXSmooth.hold != 0f || this.panYSmooth.hold != 0f
                        : false
                );
            }
            else if (((CombatEngineAPI)this.getState("engine")).getPlayerShip() != null) {
                    if (((CombatEngineAPI)this.getState("engine")).getCombatUI().getEntityToFollowV2() != null)
                        this.playerIsFocusingEnemy(
                                screenSpace,
                                ((Vector2f)this.getState("worldSpace")),
                                ((CombatEngineAPI)this.getState("engine"))
                                        .getPlayerShip().getLocation(),
                                ((CombatEngineAPI)this.getState("engine"))
                                        .getCombatUI().getEntityToFollowV2().getLocation()
                        );
                    else
                        this.playerCameraManager(
                                screenSpace,
                                ((CombatEngineAPI)this.getState("engine"))
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
        int flag = 0;
        for (ShipAPI ship : ((CombatEngineAPI)this.getState("engine")).getShips())
            if (ship.isStation() && ship.getOriginalOwner() == 0)
                flag = 1;
        if (flag == 1)
            init = new Vector2f(0f, -4000f);
        else
            for (ShipAPI x: ((CombatEngineAPI)this.getState("engine")).getShips()) {
                if (x.getOriginalOwner() != 1) {
                    init = x.getLocation();
                    break;
                }
            }
        Vector2f sDimensions = ((Vector2f)this.getState("sDimensions"));
        if (init != null) {
            this.setState("LLVec", new Vector2f(
                    init.getX() - (sDimensions.getX() / 2),
                    init.getY() - (sDimensions.getY() / 2)
            ));
            this.setState("targetLLVec", new Vector2f(
                    init.getX() - (sDimensions.getX() / 2),
                    init.getY() - (sDimensions.getY() / 2)
            ));
        }
        ((CombatEngineAPI)this.getState("engine")).getViewport().setExternalControl(true);
    }

    private void strafeManager(HashMap<String, Boolean> strafeKeys) {
        if (((CombatEngineAPI)this.getState("engine")).isUIShowingDialog())
            return;
        Vector2f hold = (Vector2f)this.getState("targetLLVec");
        if (hold == null)
            return;
        float delta = (float)this.getState("panVelocityKeyboard") * (float)this.getState("targetZoom");
        Vector2f pan = new Vector2f();
        if (strafeKeys.get("left") && !strafeKeys.get("right"))
            pan.setX(-delta);
        else if (strafeKeys.get("right") && !strafeKeys.get("left"))
            pan.setX(delta);
        else { pan.setX(0f); }
        if (strafeKeys.get("up") && !strafeKeys.get("down"))
            pan.setY(delta);
        else if (strafeKeys.get("down") && !strafeKeys.get("up"))
            pan.setY(-delta);
        else { pan.setY(0f); }
        if (pan.getX() != 0f || pan.getY() != 0f)
            this.setState("playerIsPanning", true);
        else if (!(boolean)this.getState("playerIsPanning"))
            return;
        float smoothStopper = (float)this.getState("panVelocityKeyboard") / 2f;
        Vector2f deltaTotal = new Vector2f(
                this.easeMachine(
                        this.panXSmooth,
                        pan.getX() / smoothStopper,
                        (float)this.getState("scrollSmoothingKeyboard")
                ) * smoothStopper,
                this.easeMachine(
                        this.panYSmooth,
                        pan.getY() / smoothStopper,
                        (float)this.getState("scrollSmoothingKeyboard")
                ) * smoothStopper
        );
        hold.set(
                hold.getX() + deltaTotal.getX(),
                hold.getY() + deltaTotal.getY()
        );
        this.setState("targetLLVec", hold);
        this.setState("LLVec", hold);
    }

    easeBuffer panXSmooth = new easeBuffer();
    easeBuffer panYSmooth = new easeBuffer();
    private void panManager (Vector2f screenSpace) {
        if (((CombatEngineAPI)this.getState("engine")).isUIShowingDialog()
                || (boolean)this.getState("playerIsPanning")
        )
            return;
        Vector2f hold = (Vector2f)this.getState("targetLLVec");
        if (hold == null)
            return;
        float delta = (float)this.getState("panVelocity") * (float)this.getState("targetZoom");
        Vector2f pan = new Vector2f();
        if (screenSpace.getX() <= 6f && screenSpace.getX() >= 0f)
            pan.setX(-delta);
        else if (screenSpace.getX() >= (((Vector2f)this.getState("mDimensions")).getX() - 6f)
                && screenSpace.getX() <= ((Vector2f)this.getState("mDimensions")).getX())
            pan.setX(delta);
        else { pan.setX(0f); }
        if (screenSpace.getY() <= 6f && screenSpace.getY() >= 0f)
            pan.setY(-delta);
        else if (screenSpace.getY() >= (((Vector2f)this.getState("mDimensions")).getY() - 6f)
                && screenSpace.getY() <= ((Vector2f)this.getState("mDimensions")).getY())
            pan.setY(delta);
        else { pan.setY(0f); }
        float smoothStopper = (float)this.getState("panVelocity") / 2f;
        Vector2f deltaTotal = new Vector2f(
                this.easeMachine(
                        this.panXSmooth,
                        pan.getX() / smoothStopper,
                        (float)this.getState("scrollSmoothing")
                ) * smoothStopper,
                this.easeMachine(
                        this.panYSmooth,
                        pan.getY() / smoothStopper,
                        (float)this.getState("scrollSmoothing")
                ) * smoothStopper
        );
        hold.set(
                hold.getX() + deltaTotal.getX(),
                hold.getY() + deltaTotal.getY()
        );
        this.setState("targetLLVec", hold);
        this.setState("LLVec", hold);
    }

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

    easeBuffer playerCameraXSmooth = new easeBuffer();
    easeBuffer playerCameraYSmooth = new easeBuffer();
    private void playerCameraManager(Vector2f screenSpace, Vector2f playerPosition, float deltaFromTarget, float smoothness) {
        Vector2f playerPositionInScreen = CombatUtils.toScreenCoordinates(playerPosition);
        float zoom = (float)this.getState("targetZoom");
        Vector2f sDimensions = ((Vector2f)this.getState("sDimensions"));
        Vector2f hold = new Vector2f(
                playerPosition.getX() - (sDimensions.getX() / 2),
                playerPosition.getY() - (sDimensions.getY() / 2)
        );
        hold.set(
                hold.getX() - (((playerPositionInScreen.getX() - screenSpace.getX()) * deltaFromTarget) / 2f * zoom),
                hold.getY() - (((playerPositionInScreen.getY() - screenSpace.getY()) * deltaFromTarget) / 2f * zoom)
        );
        Vector2f delta = new Vector2f(
                hold.getX() - ((Vector2f)this.getState("targetLLVec")).getX(),
                hold.getY() - ((Vector2f)this.getState("targetLLVec")).getY()
        );
        this.playerCameraXSmooth.hold = delta.getX() * 1000000f;
        this.playerCameraYSmooth.hold = delta.getY() * 1000000f;
        Vector2f finalVec = new Vector2f(
                ((Vector2f)this.getState("targetLLVec")).getX() + this.easeMachine(
                        this.playerCameraXSmooth,
                        0f,
                        10f * smoothness),
                ((Vector2f)this.getState("targetLLVec")).getY() + this.easeMachine(
                        this.playerCameraYSmooth,
                        0f,
                        10f * smoothness)
        );
        finalVec.set(
                Math.round(finalVec.getX() * 10f) /10f,
                Math.round(finalVec.getY() * 10f) /10f
        );
        this.setState("targetLLVec", finalVec);
        this.setState("LLVec", finalVec);
    }

    easeBuffer zoomEaseBuffer = new easeBuffer();
    private void zoomManager (int zoom) {
        if (((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI() ||
                ((CombatEngineAPI)this.getState("engine")).isUIShowingDialog())
            return;
        float tZoom = (float)this.getState("targetZoom");
        float increment = (float)(((HashMap<String, Object>)this.getState("config")).get("maxZoom")) / 12f;
        tZoom = tZoom + (this.easeMachine(
                this.zoomEaseBuffer,
                zoom == 0
                ? 0f
                : zoom == -1 ? (increment * 100f) : (-increment * 100f),
                15f
        ) / 100f);
        this.setState("targetZoom", MathUtils.clamp(
                        tZoom,
                        1f,
                        (float)(((HashMap<String, Object>)this.getState("config")).get("maxZoom"))
                )
        );
    }

    private float easeMachine(easeBuffer buffer, float increment, float delta) {
        float multi = (float)this.getDeepState(Arrays.asList("amount", "frameMult")) *
                (float)this.getDeepState(Arrays.asList("amount", "dilation"));
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

    public void zoomToControlGroup (Integer group) {
        List<ShipAPI> members = ((RTS_SelectionListener)this.getState("selectionListener"))
                .getControlGroupList(group);
        if (members == null)
            return;
        Vector2f sDimensions = (Vector2f)this.getState("sDimensions");
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

        this.setState("targetLLVec", totals);
        this.setState("LLVec", totals);
        this.panXSmooth.hold = 0f;
        this.panYSmooth.hold = 0f;

    }

    private void render() {
        if (this.getState("LLVec") == null) return;
        ((ViewportAPI)this.getState("viewPort")).set(
                ((Vector2f)this.getState("LLVec")).getX()
                        - (((Vector2f)this.getState("sDimensions")).getX() * ((float)this.getState("targetZoom") / 2f))
                        + (((Vector2f)this.getState("sDimensions")).getX() / 2f),
                ((Vector2f)this.getState("LLVec")).getY()
                        - (((Vector2f)this.getState("sDimensions")).getY() * ((float)this.getState("targetZoom") / 2f))
                        + (((Vector2f)this.getState("sDimensions")).getY() / 2f),
                ((Vector2f)this.getState("sDimensions")).getX() * (float)this.getState("targetZoom"),
                ((Vector2f)this.getState("sDimensions")).getY() * (float)this.getState("targetZoom")
        );
    }
}