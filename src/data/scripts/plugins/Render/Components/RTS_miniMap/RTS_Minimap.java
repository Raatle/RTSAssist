/*
  **********************************************************************************************************
  * RTSAssist version 0.2.12exp
  * Copyright (C) 2025-2026, Raatle

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
  **********************************************************************************************************
 */

package data.scripts.plugins.Render.Components.RTS_miniMap;

import com.fs.starfarer.api.Global;
import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.RTS_CameraRework;
import data.scripts.plugins.RTS_ParseInput;
import data.scripts.plugins.Render.RTS_drawManager.RTS_animator.RTS_Animator;
import data.scripts.plugins.Render.RTS_drawManager.RTS_DrawManager;
import data.scripts.plugins.Render.JXDOM.Props.*;
import data.scripts.plugins.Render.JXDOM.RTS_BaseInterface;
import data.scripts.plugins.Render.JXDOM.RTS_Node;
import data.scripts.plugins.Render.RTS_Root;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RTS_Minimap extends RTS_BaseInterface {

    class CAU_muStatState_CAU {
        static RTS_MiniMapRenderer renderer;
        static HashMap<String, Object> amendedProps = new HashMap<>();
    }

    default Integer miniMap (
            Map<String, Object> props,
            RTS_Root root
    ) {
        RTS_CameraRework cameraRework = (RTS_CameraRework)root.getState(RTSAssist.stNames.cameraRework);
        RTS_ParseInput inputManager = (RTS_ParseInput)root.getState(RTSAssist.stNames.parseInput);
        CAU_muStatState_CAU.amendedProps.clear();
        CAU_muStatState_CAU.amendedProps.putAll(props);
        /* An inert minimap must not intercept mouse input either. */
        boolean disabled = props.get("inert") instanceof Boolean && (Boolean)props.get("inert");
        if (!disabled && !(boolean)root.getState(RTS_ParseInput.stNames.isShiftDown)) {
            CAU_muStatState_CAU.amendedProps.putAll(Map.of(
                    "onDrag", leftClickHoldManager.getLeftClickHold(cameraRework),
                    "onRightClick", rightClickManager.getRightClick(inputManager),
                    "onHover", (RTS_Prop.propEvent)(p, r, c) -> {
                            inputManager.queueInterrupt(RTS_ParseInput.interrupt.LMB);
                            inputManager.queueInterrupt(RTS_ParseInput.interrupt.RMB);
                    }));
        }
        return (this.finalise(
                CAU_muStatState_CAU.amendedProps,
                null,
                this.initProps,
                this.render,
                this.everyFrame,
                this.getClass()
        ));
    }

    default void initMiniMap (RTS_DrawManager drawManager, RTS_Animator animator) {
        CAU_muStatState_CAU.renderer = new RTS_MiniMapRenderer(drawManager, animator);
    }

    static class leftClickHoldManagerClass {
        private RTS_CameraRework cameraRework;

        private RTS_Prop.propEvent leftClick = new RTS_Prop.propEvent() {
            Vector2f pos = new Vector2f();
            Vector2f dim = new Vector2f();
            Vector2f miniMapMod = new Vector2f();
            Vector2f arena = new Vector2f();
            Vector2f finalVec = new Vector2f();

            @Override
            public void onTrigger(
                    HashMap<String, Object> processedProps,
                    Map<String, Object> rawProps,
                    RTS_Node callingNode
            ) {
                this.arena.set(
                        Global.getCombatEngine().getMapWidth(),
                        Global.getCombatEngine().getMapHeight()
                );
                this.dim.set(
                        (float)processedProps.get(RTS_P_Width.ID()),
                        (float)processedProps.get(RTS_P_Height.ID())
                );
                this.pos.set(
                        (float)processedProps.get(RTS_P_Left.ID()),
                        RTS_Root.screenDim.getY() - ((float)processedProps.get(RTS_P_Top.ID()) + this.dim.getY())
                );
                this.miniMapMod.set(
                        (Mouse.getX() - this.pos.getX()) / this.dim.getX(),
                        (Mouse.getY() - this.pos.getY()) / this.dim.getY()
                );
                this.finalVec.set(
                        (this.arena.getX() * this.miniMapMod.getX()) - (this.arena.getX() / 2f),
                        (this.arena.getY() * this.miniMapMod.getY()) - (this.arena.getY() / 2f)
                );
                cameraRework.zoomToLocation(this.finalVec);
            }

            @Override
            public void alwaysTrigger (
                    HashMap<String, Object> processedProps,
                    Map<String, Object> rawProps,
                    RTS_Node callingNode
            ) {
                cameraRework = null;
            }
        };

        public RTS_Prop.propEvent getLeftClickHold (RTS_CameraRework cameraRework) {
            this.cameraRework = cameraRework;
            return (this.leftClick);
        }
    }
    static leftClickHoldManagerClass leftClickHoldManager = new leftClickHoldManagerClass();

    static class rightClickManagerClass {
        private RTS_ParseInput inputManager;

        private RTS_Prop.propEvent rightClick = new RTS_Prop.propEvent() {
            Vector2f pos = new Vector2f();
            Vector2f dim = new Vector2f();
            Vector2f miniMapMod = new Vector2f();
            Vector2f arena = new Vector2f();
            Vector2f finalVec = new Vector2f();

            @Override
            public void onTrigger(
                    HashMap<String, Object> processedProps,
                    Map<String, Object> rawProps,
                    RTS_Node callingNode
            ) {
                this.arena.set(
                        Global.getCombatEngine().getMapWidth(),
                        Global.getCombatEngine().getMapHeight()
                );
                this.dim.set(
                        (float)processedProps.get(RTS_P_Width.ID()),
                        (float)processedProps.get(RTS_P_Height.ID())
                );
                this.pos.set(
                        (float)processedProps.get(RTS_P_Left.ID()),
                        RTS_Root.screenDim.getY() - ((float)processedProps.get(RTS_P_Top.ID()) + this.dim.getY())
                );
                this.miniMapMod.set(
                        (Mouse.getX() - this.pos.getX()) / this.dim.getX(),
                        (Mouse.getY() - this.pos.getY()) / this.dim.getY()
                );
                this.finalVec.set(
                        (this.arena.getX() * this.miniMapMod.getX()) - (this.arena.getX() / 2f),
                        (this.arena.getY() * this.miniMapMod.getY()) - (this.arena.getY() / 2f)
                );
                inputManager.preBuiltMoveAssignment(this.finalVec);
                CAU_muStatState_CAU.renderer.registerMarker(new Vector2f(Mouse.getX(), Mouse.getY()));
            }

            @Override
            public void alwaysTrigger (
                    HashMap<String, Object> processedProps,
                    Map<String, Object> rawProps,
                    RTS_Node callingNode
            ) {
                inputManager = null;
            }
        };

        public RTS_Prop.propEvent getRightClick (RTS_ParseInput inputManager) {
            this.inputManager = inputManager;
            return (this.rightClick);
        }
    }
    static rightClickManagerClass rightClickManager = new rightClickManagerClass();

    static onRender render = new onRender() {
        @Override
        public void render(HashMap<String, Object> props, Map<String, Object> rawProps) {
            CAU_muStatState_CAU.renderer.update(props, rawProps);
        }
    };

    static everyFrameUpdate everyFrame = new everyFrameUpdate() {
        @Override
        public void everyFrame(HashMap<String, Object> props, Map<String, Object> rawProps) {
            if (rawProps != null && rawProps.get(RTS_Root.roNames.miniMapRefresh) instanceof Float)
                CAU_muStatState_CAU.renderer.refreshMs = (Float)rawProps.get(RTS_Root.roNames.miniMapRefresh);
            if (rawProps != null && rawProps.get(RTS_Root.roNames.miniMapSweep) instanceof Float)
                CAU_muStatState_CAU.renderer.sweepMs = (Float)rawProps.get(RTS_Root.roNames.miniMapSweep);
            CAU_muStatState_CAU.renderer.render(
                    (RTS_Root.camera)rawProps.get(RTS_Root.roNames.camera),
                    (RTS_Root.shipList)rawProps.get(RTS_Root.roNames.shipList)
            );
        }
    };

    static initProps initProps = new initProps() {
        @Override
        public HashMap<String, RTS_Prop> init() {
            return (RTS_Prop.registerProps(List.of(
                    new RTS_P_Height(),
                    new RTS_P_Width(),
                    new RTS_P_Top(),
                    new RTS_P_Left(),
                    new RTS_P_Hidden(),
                    new RTS_P_Inert(),
                    new RTS_P_Opacity(),
                    new RTS_P_OnDrag(),
                    new RTS_P_OnRightClick(),
                    new RTS_P_OnHover()
            )));
        }
    };
}
