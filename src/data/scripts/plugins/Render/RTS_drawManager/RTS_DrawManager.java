/*
  **********************************************************************************************************
  * RTSAssist version 0.2.04exp
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

package data.scripts.plugins.Render.RTS_drawManager;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.Render.RTS_drawManager.RTS_FBO.RTS_FBOManager;
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class RTS_DrawManager extends RTS_StatefulClasses {
    public RTS_DrawManager (Object state) {
        super(state);
        if (state != null && ((float)this.getDeepState(Arrays.asList(RTSAssist.stNames.config, RTSAssist.coNames.screenScaling)) != 100f))
            this.UIS = (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.config, RTSAssist.coNames.screenScaling)) / 100f;
        else
            this.UIS = Global.getSettings().getScreenScaleMult();
        RTS_DrawManager.vanillaDrawIDs = 0;
    }

    public static class viewPortClass {
        public int x;
        public int y;
        public int width;
        public int height;
        public void set(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    };
    public static viewPortClass viewPort = new viewPortClass();
    public static viewPortClass adjustedViewPort = new viewPortClass();

    private float UIS;
    private Integer drawIds = 0;
    private static Integer vanillaDrawIDs = 0;
    private List<Integer> vanillaDrawIDStore = new ArrayList<>();

    private HashMap<Integer, HashMap<Integer, RTS_DrawCall>> queuedDrawCalls = new HashMap<>();
    private HashMap<Integer, Integer> callRegister = new HashMap<>();


    public void draw() {
        this.doDraw();
        this.drawIds = 0;
        this.callRegister.replaceAll((key, value) -> null);
        this.queuedDrawCalls.forEach((k,v) -> v.clear());
        RTS_ShaderManager.clearProgram();
    }

    private void doDraw() {
        for (int i = 0; i <= 100; i++) {
            if (this.queuedDrawCalls.containsKey(i)) {
                for (Map.Entry<Integer, RTS_DrawCall> val : this.queuedDrawCalls.get(i).entrySet())
                    val.getValue().call();
            }
        }
    }

    public static Integer getVanillaDrawID () {
        return (RTS_DrawManager.vanillaDrawIDs++);
    }

    public void registerVanillaDrawCall (Integer vanillaDrawID, CombatLayeredRenderingPlugin call) {
        if (!this.vanillaDrawIDStore.contains(vanillaDrawID)) {
            Global.getCombatEngine().addLayeredRenderingPlugin(call);
            this.vanillaDrawIDStore.add(vanillaDrawID);
        }
    }

    public Integer registerDrawCall (RTS_DrawCall call) {
        this.drawIds++;
        if (this.queuedDrawCalls.containsKey(call.zIndex()))
            this.queuedDrawCalls.get(call.zIndex()).put(this.drawIds, call);
        else {
            this.queuedDrawCalls.put(call.zIndex(), new HashMap<>());
            this.queuedDrawCalls.get(call.zIndex()).put(this.drawIds, call);
        }
        this.callRegister.put(this.drawIds, call.zIndex());
        return (this.drawIds);
    }

    public boolean deregisterDrawCall (Integer ID) {
        if (!this.callRegister.containsKey(ID) || this.callRegister.get(ID) == null)
            return (false);
        this.queuedDrawCalls.get(this.callRegister.get(ID)).put(ID, null);
        this.callRegister.put(ID, null);
        return (true);
    }

    public void open () {
        RTS_FBOManager.prepareBufferWrapper();
        RTS_GenericDrawMeth.resetTextureDebouncer();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        RTS_DrawManager.viewPort.set(
                0,
                0,
                (int)(Global.getSettings().getScreenWidth() * this.UIS),
                (int)(Global.getSettings().getScreenHeight() * this.UIS)
        );
        RTS_DrawManager.adjustedViewPort.set(
                0,
                0,
                (int)(Global.getSettings().getScreenWidth() * this.UIS),
                (int)(Global.getSettings().getScreenHeight() * this.UIS)
        );
        GL11.glViewport(
                RTS_DrawManager.viewPort.x,
                RTS_DrawManager.viewPort.y,
                RTS_DrawManager.viewPort.width,
                RTS_DrawManager.viewPort.height
        );
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(
                0,
                (int)(Global.getSettings().getScreenWidth() * this.UIS),
                0,
                (int)(Global.getSettings().getScreenHeight() * this.UIS),
                -1,
                1
        );
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

    }

    public void close () {
        RTS_GenericDrawMeth.unbindTexture();
        RTS_ShaderManager.clearProgram();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static void adjustViewPort (int x, int y, int width, int height) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, 0, height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        RTS_DrawManager.adjustedViewPort.set(
                x,
                y,
                width,
                height
        );
        GL11.glViewport(
                x,
                y,
                width,
                height
        );
    }

    public static void resetViewPort () {
        RTS_DrawManager.adjustedViewPort.set(
                RTS_DrawManager.viewPort.x,
                RTS_DrawManager.viewPort.y,
                RTS_DrawManager.viewPort.width,
                RTS_DrawManager.viewPort.height
        );
        GL11.glViewport(
                RTS_DrawManager.viewPort.x,
                RTS_DrawManager.viewPort.y,
                RTS_DrawManager.viewPort.width,
                RTS_DrawManager.viewPort.height
        );
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, RTS_DrawManager.viewPort.width, 0, RTS_DrawManager.viewPort.height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    public static Vector2f adjustForVP (Vector2f point) {
        return (new Vector2f(
                point.getX() + RTS_DrawManager.adjustedViewPort.x,
                point.getY() + RTS_DrawManager.adjustedViewPort.y
        ));
    }

    public static void adjustForVPunSafe (Vector2f point) {
        point.set(
                point.getX() + RTS_DrawManager.adjustedViewPort.x,
                point.getY() + RTS_DrawManager.adjustedViewPort.y
        );
    }
}
