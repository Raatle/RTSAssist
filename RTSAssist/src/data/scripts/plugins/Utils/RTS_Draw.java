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

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.Global;

import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.opengl.DrawUtils;

public class RTS_Draw extends RTS_StatefulClasses {

    public RTS_Draw(Object state) {
        super(state);
    }

    boolean drawTrue = true;

    public void stopDrawing(boolean draw) {this.drawTrue = draw;}

    public void checkIfShouldDraw() {
        this.drawTrue = !(((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI() ||
                ((CombatEngineAPI)this.getState("engine")).isUIShowingDialog());
    }

    public void open () {
        if (!this.drawTrue)
            return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glViewport(
                0,
                0,
                (int)Global.getCombatEngine().getViewport().getVisibleWidth(),
                (int)Global.getCombatEngine().getViewport().getVisibleHeight()
        );
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(
                0,
                (int)Global.getCombatEngine().getViewport().getVisibleWidth(),
                0,
                (int)Global.getCombatEngine().getViewport().getVisibleHeight(),
                -1,
                1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);
    }

    public void rudeDrawLine (Vector2f start, Vector2f end) {
        if (!this.drawTrue)
            return;
        GL11.glColor4ub((byte)155,(byte)189, (byte)0, (byte)150);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2d(
                start.getX(),
                start.getY()
        );
        GL11.glVertex2d(
                end.getX(),
                end.getY()
        );
        GL11.glEnd();
    }

    public void drawSelectionCircle (ShipAPI ship, float zoom) {
        if (!this.drawTrue)
            return;
        GL11.glColor4ub((byte)155,(byte)189, (byte)0, (byte)220);
        DrawUtils.drawCircle(
                CombatUtils.toScreenCoordinates(ship.getLocation()).getX(),
                CombatUtils.toScreenCoordinates(ship.getLocation()).getY(),
                (ship.getCollisionRadius() / zoom),
                100,
                false
        );
    }

    public void drawPrimaryDest (Vector2f coOrd, float zoom, boolean moveAndRelease) {
        if (!this.drawTrue)
            return;
        GL11.glColor4ub((byte)155,(byte)189, (byte)0, (byte)220);
        DrawUtils.drawCircle(
                CombatUtils.toScreenCoordinates(coOrd).getX(),
                CombatUtils.toScreenCoordinates(coOrd).getY(),
                (30 / zoom),
                100,
                !moveAndRelease
        );
        if (moveAndRelease)
            DrawUtils.drawCircle(
                    CombatUtils.toScreenCoordinates(coOrd).getX(),
                    CombatUtils.toScreenCoordinates(coOrd).getY(),
                    (15 / zoom),
                    100,
                    false
            );
    }

    public void drawSelectedEnemy (Vector2f coOrd, float zoom, boolean moveAndRelease) {
        if (!this.drawTrue)
            return;
        GL11.glColor4ub((byte)155,(byte)5, (byte)0, (byte)220);
        DrawUtils.drawCircle(
                CombatUtils.toScreenCoordinates(coOrd).getX(),
                CombatUtils.toScreenCoordinates(coOrd).getY(),
                (30 / zoom),
                100,
                false
        );
        if (moveAndRelease)
            DrawUtils.drawCircle(
                    CombatUtils.toScreenCoordinates(coOrd).getX(),
                    CombatUtils.toScreenCoordinates(coOrd).getY(),
                    (15 / zoom),
                    100,
                    false
            );
    }

    public void drawSecondaryDest (Vector2f coOrd, float zoom) {
        if (!this.drawTrue)
            return;
        GL11.glColor4ub((byte)155,(byte)189, (byte)0, (byte)220);
        DrawUtils.drawCircle(
                CombatUtils.toScreenCoordinates(coOrd).getX(),
                CombatUtils.toScreenCoordinates(coOrd).getY(),
                (30 / zoom),
                100,
                false
        );
    }

    public void close () {
        if (!this.drawTrue)
            return;
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}