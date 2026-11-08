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

package data.scripts.plugins.Render.Components.RTS_fogOfWar;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.combat.CombatViewport;
import data.scripts.plugins.Render.JXDOM.Props.RTS_P_Height;
import data.scripts.plugins.Render.JXDOM.Props.RTS_P_Inert;
import data.scripts.plugins.Render.JXDOM.Props.RTS_P_Left;
import data.scripts.plugins.Render.JXDOM.Props.RTS_P_Top;
import data.scripts.plugins.Render.JXDOM.Props.RTS_P_Width;
import data.scripts.plugins.Render.RTS_RenderManager;
import data.scripts.plugins.Render.RTS_Root;
import data.scripts.plugins.Render.RTS_drawManager.*;
import data.scripts.plugins.Utils.RTS_AssortedFunctions;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RTS_FogOfWarRenderer {

    public RTS_FogOfWarRenderer (RTS_DrawManager drawManager) {
        RTS_GenericDrawMeth.quadToCircleArrayUnion.rebuildShaders();// remove!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        this.drawManager = drawManager;
        drawFogOfWar.modify();
    }

    private static int FOWZIndex = RTS_RenderManager.ziNames.fogOfWar;
    private static int startCullResolution = 20;
    private static Vector2f rMods = new Vector2f(
            0.925f,
            1.2f
    );

    private RTS_DrawManager drawManager;

    private Vector2f pos = new Vector2f();
    private Vector2f posRef = new Vector2f();
    private Vector2f dim = new Vector2f();
    float radRat;

    private List<ShipAPI> shipsWithVision = new ArrayList<>();

    /* The vanilla draw plugin is attached to the combat engine once and keeps rendering on its
     * own, so the fog toggle has to be honoured inside its render pass, not at registration. */
    private boolean disabled = false;

    private List<MapCell> cellList = new ArrayList<>();
    private class MapCell {
        Vector2f bottomLeft = new Vector2f();
        Vector2f cellDim = new Vector2f();
        Vector2f topRight = new Vector2f();
        Vector2f centre = new Vector2f();
        int visState = 0; // 0: Not Visible; 1: Partially visible; 2: Fully visible
    }

    public static int drawID = RTS_DrawManager.getVanillaDrawID();

    public void render (RTS_Root.shipList listOfShips, boolean enabled) {
        this.disabled = !enabled;
        this.buildShipLists(listOfShips);
//        this.drawManager.registerDrawCall(drawFogOfWar);
        this.drawManager.registerVanillaDrawCall(drawID, vanillaDraw);
    }

    public void update (HashMap<String, Object> props, Map<String, Object> rawProps) {
        if (props.get(RTS_P_Inert.ID()) instanceof Boolean && (Boolean)props.get(RTS_P_Inert.ID()))
            return;
        this.pos.set((float)props.get(RTS_P_Left.ID()), RTS_Root.screenDim.getY() - (float)props.get(RTS_P_Top.ID()));
        this.dim.set((float)props.get(RTS_P_Width.ID()), (float)props.get(RTS_P_Height.ID()));
        this.posRef.set(this.pos.getX(), this.pos.getY() - this.dim.getY());
        this.drawFogOfWar.modify();
        this.generateCellList();
    }

    private void buildShipLists (RTS_Root.shipList listOfShips) {
        this.shipsWithVision.clear();
        this.shipsWithVision.addAll(listOfShips.friendlyShips);
        this.shipsWithVision.addAll(listOfShips.alliedShips);
    }

    RTS_DrawCall drawFogOfWar = new RTS_DrawCall() {
        RTS_DrawQuad.quadCall quadBuilder = new RTS_DrawQuad.quadCall();
        RTS_GenericDrawMeth.quadToCircleArrayUnion unionShader = new RTS_GenericDrawMeth.quadToCircleArrayUnion();

        Color black = new Color(0,0,0, 255);

        @Override
        public void modify () {
            unionShader
                    .aliasing(3f)
                    .color(black);
        }

        @Override
        public Integer zIndex () {
            return (RTS_FogOfWarRenderer.FOWZIndex);
        }

        @Override
        public void call () {
            radRat = dim.getX() / Global.getCombatEngine().getViewport().getVisibleWidth();
            unionShader.clearCircles();
            calculateCellVisibility(unionShader);
            float blend = 20000f * radRat * radRat;
            float alias = 1200f * radRat * radRat;
            for (MapCell cell : cellList) {
                if (cell.visState == 2) {
                    cell.visState = 0;
                    continue;
                }
                if (cell.visState == 1)
                    // work here
                    quadBuilder
                            .filter(unionShader)
                            .size(cell.cellDim)
                            .pos(cell.bottomLeft)
                            .blend(blend)
                            .aliasing(alias);
                else
                    quadBuilder.unSetFilter();
                quadBuilder
                        .size(cell.cellDim)
                        .pos(cell.centre)
                        .color(black)
                        .push();
                cell.visState = 0;
            }
            quadBuilder.render();
        }
    };

    CombatLayeredRenderingPlugin vanillaDraw = new CombatLayeredRenderingPlugin() {
        @Override
        public void init (CombatEntityAPI entity) {}
        @Override
        public void cleanup () {}
        @Override
        public boolean isExpired () { return (false); }
        @Override
        public void advance (float amount) {}

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers () {
            return (EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS));
        }

        @Override
        public float getRenderRadius () {
            return (1000000f);
        }

        @Override
        public void render (CombatEngineLayers layer, ViewportAPI viewport) {
            if (disabled)
                return;
            drawManager.open();
            drawFogOfWar.call();
            RTS_ShaderManager.clearProgram();
            drawManager.close();
        }
    };

    private void calculateCellVisibility (RTS_GenericDrawMeth.quadToCircleArrayUnion unionShader) {
        ViewportAPI viewportAPI = Global.getCombatEngine().getViewport();
        float rPointer;
        Vector2f shipScrPoi = new Vector2f();
        List<MapCell> squeezyMapList = new ArrayList<>(this.cellList);
        List<MapCell> trimList = new ArrayList<>(squeezyMapList.size());
        float zoomMod;
        for (ShipAPI ship : this.shipsWithVision) {
            shipScrPoi.set(
                    viewportAPI.convertWorldXtoScreenX(ship.getLocation().getX()),
                    viewportAPI.convertWorldYtoScreenY(ship.getLocation().getY())
            );
            rPointer = ship.getMutableStats().getSightRadiusMod().computeEffective(3000f) * this.radRat;
            unionShader.addCircle(shipScrPoi, rPointer);
            rPointer = rPointer * rMods.getY();
            zoomMod = this.dim.getX() / (rPointer * 3f);
            zoomMod = MathUtils.clamp(zoomMod, 1f, Math.max(1.3f * (zoomMod / 3f), 1.3f));
            rPointer = rPointer * zoomMod;
            rPointer = rPointer * rPointer;
            for (MapCell cell : squeezyMapList)
                if (cell.visState == 0 && RTS_AssortedFunctions.getDistanceSquared(shipScrPoi, cell.centre) < rPointer)
                    cell.visState = 3;
            rPointer = ship.getMutableStats().getSightRadiusMod().computeEffective(3000f) * this.radRat * rMods.getX();
            rPointer = rPointer / zoomMod;
            rPointer = rPointer * rPointer;
            for (MapCell cell : this.cellList) {
                if (cell.visState == 0)
                    continue;
                else if (cell.visState == 2) {
                    trimList.add(cell);
                    continue;
                }
                cell.visState = RTS_AssortedFunctions.getDistanceSquared(shipScrPoi, cell.centre) < rPointer ? 2 : 1;
            }
            squeezyMapList.removeAll(trimList);
            trimList.clear();
        }
    }

    private void generateCellList () {
        this.cellList.clear();
        this.adjustCullRes();
        Vector2f dim = new Vector2f(this.dim.getX() / startCullResolution, this.dim.getY() / startCullResolution);
        MapCell cellPoi;
        for (int y = 0; y < startCullResolution; y++) {
            for (int x = 0; x < startCullResolution; x++) {
                cellPoi = new MapCell();
                cellPoi.bottomLeft.set(x * dim.getX(), y * dim.getY());
                cellPoi.cellDim.set(dim);
                cellPoi.centre.set(
                        cellPoi.bottomLeft.getX() + (dim.getX() / 2f),
                        cellPoi.bottomLeft.getY() + (dim.getY() / 2f)
                );
                cellPoi.topRight.set(
                        cellPoi.bottomLeft.getX() + dim.getX(),
                        cellPoi.bottomLeft.getY() + dim.getY()
                );
                this.cellList.add(cellPoi);
            }
        }
    }

    private void adjustCullRes () {
        int delta = 0;
        int calculatedDelta;
        while (true) {
            calculatedDelta = startCullResolution + delta;
            if (
                    this.dim.getX() % calculatedDelta == 0
                    && this.dim.getY() % calculatedDelta == 0
            )
                break;
            calculatedDelta = startCullResolution - delta;
            if (calculatedDelta == 0)
                throw (new RuntimeException("RTSAssist: Your games present resolution is not supported."));
            if (
                    this.pos.getX() % calculatedDelta == 0
                    && this.pos.getY() % calculatedDelta == 0
            )
                break;
            delta++;
        }
        startCullResolution = calculatedDelta;
    }
}
