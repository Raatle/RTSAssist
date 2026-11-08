/*
  **********************************************************************************************************
  * RTSAssist version 0.2.15exp
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
import com.fs.starfarer.api.combat.*;
import data.scripts.plugins.Render.RTS_drawManager.*;
import data.scripts.plugins.Render.RTS_drawManager.RTS_animator.RTS_AnimationController;
import data.scripts.plugins.Render.RTS_drawManager.RTS_animator.RTS_Animator;
import data.scripts.plugins.Render.JXDOM.Props.*;
import data.scripts.plugins.Render.RTS_RenderManager;
import data.scripts.plugins.Render.RTS_Root;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;

import org.lwjgl.opengl.GL11;

public class RTS_MiniMapRenderer {

    //------------------------------------------------------------------------------------------------------------------

    public RTS_MiniMapRenderer (RTS_DrawManager drawManager, RTS_Animator animator) {
        this.drawManager = drawManager;
        this.animator = animator;
        this.aniStore = new HashMap<>(5);
        for (int i = 0; i < 5; i++)
            this.aniStore.put(i, new markerStore());
    }

    public static final class palette {
        /* Vanilla command-page colours, from starsector-core/data/config/settings.json:
         *   iconFriendColor [0,255,0]  iconEnemyColor [255,0,0]  iconNeutralShipColor [75,75,75].
         * Allies have no vanilla key; the gold matches the vanilla ally look. */
        public static Color friendly = new Color(0, 255, 0, 235);
        public static Color enemy = new Color(255, 0, 0, 235);
        public static Color allied = new Color(226, 196, 70, 235);
        public static Color neutral = new Color(75, 75, 75, 255);
        public static Color viewPortLine = new Color(255, 255, 255, 255);
        public static Color RCMCore = new Color(113, 234, 14, 255);
        public static Color RCMBorder = new Color(2, 255, 255, 132);
    }
    public static final class layers {
        public static int miniMap = RTS_RenderManager.ziNames.UIBase;
        public static int viewPortBox = miniMap + 7;
        public static int rightClick = miniMap + 8;
    }
    private static final HashMap<String, Float> iconSizes = new HashMap<>() {{
        put("FRIGATE", 14f);
        put("DESTROYER", 19f);
        put("CRUISER", 27f);
        put("CAPITAL_SHIP", 42f);
    }};
    private static final float fighterIconSize = 7f;
    /* Velocity streaks: map-width fraction per 250su/s; fighters are faster, so their attenuation factor is smaller */
    private static final float shipSpeedLineScale = 0.10f / 250f;
    private static final float fighterSpeedLineFactor = 0.45f;
    private static final float speedLineMaxLenMod = 0.16f;
    private static final float shipLineThickness = 2f;
    private static final float fighterLineThickness = 1.2f;
    private static final float rightClickMarkerDuration = 0.4f;
    private static final int STAMP_LIFETIME_CYCLES = 3;   // number of cycles a stamp (afterglow) is kept
    private static final float SWEEP_PHASE_SPLIT = 0.5f;  // ship phase [0,split), fighter phase [split,1)

    /* Alpha-vs-time curve (1.0 at the instant of stamping, decays in place per the table; time base = 3 cycles).
     * Exported from the demo page devtools/minimap_demo.html: index i = stamp age of 3 cycles × i/STEPS. */
    private static final int SWEEP_CURVE_STEPS = 16;
    private static final float[] SWEEP_CURVE = {
            1.000f, 0.904f, 0.809f, 0.713f, 0.617f, 0.521f,
            0.451f, 0.389f, 0.328f, 0.267f, 0.206f, 0.161f,
            0.135f, 0.109f, 0.083f, 0.058f, 0.040f,
    };

    /* Look up the curve by stamp age (piecewise-linear interpolation) */
    private static float sweepCurveAt (float ageMs, float cycleMs) {
        float u = ageMs / (3f * cycleMs);
        if (u < 0f) u = 0f; else if (u > 1f) u = 1f;
        float f = u * SWEEP_CURVE_STEPS;
        int i = (int) f;
        if (i > SWEEP_CURVE_STEPS - 1) i = SWEEP_CURVE_STEPS - 1;
        return SWEEP_CURVE[i] + (SWEEP_CURVE[i + 1] - SWEEP_CURVE[i]) * (f - i);
    }

    private RTS_DrawManager drawManager;
    public RTS_Animator animator;
    /* Data refresh period = sweep cycle period (ms); sweep duration = time to complete one phase (ms).
     * Injected by RTS_Root via RTS_Minimap (adjustable via Config.ini / Luna). */
    public float refreshMs = 1010f;
    public float sweepMs = 200f;
    public RTS_Root.camera camera;

    private long cycleStart = 0;
    private boolean cycleValid = false;
    private int cycleIdx = 0;

    private boolean init;
    private Vector2f pos = new Vector2f();
    private Vector2f posRef = new Vector2f();
    private Vector2f dim = new Vector2f();
    private boolean disabled = false;
    private float opacity = 1f;
    private Integer nextMarker = 0;

    private class markerStore {
        public markerStore () {
            this.aniCont.setAnimateDuringPause(true);
        }
        boolean queueAnimation = false;
        boolean removeAnimation = false;
        Vector2f location = new Vector2f();
        RTS_AnimationController aniCont = new RTS_AnimationController(rightClickMarkerDuration);
    }
    private HashMap<Integer, markerStore> aniStore;

    /* Stamp: position/facing/velocity readings frozen at the sweep hit, kept in place for 3 cycles */
    private static final class Stamp {
        final Vector2f loc = new Vector2f();
        final Vector2f vel = new Vector2f();
        float facing;
        float size;
        float t;
        float lineScale;
        Color color;
        long born;
    }
    private static final class SweepState {
        final ArrayList<Stamp> stamps = new ArrayList<>();
        float offset;          // appearance order within a phase [0,1)
        int sweptCycle = -1;
    }
    private final HashMap<ShipAPI, SweepState> states = new HashMap<>();

    //------------------------------------------------------------------------------------------------------------------

    public void render(
            RTS_Root.camera camera,
            RTS_Root.shipList listOfShips
    ) {
        if (disabled || !this.init)
            return;
        this.camera = camera;
        this.updateAnimationControllers();
        this.drawManager.registerDrawCall(this.sweepLayerCall);
        this.drawManager.registerDrawCall(this.viewPortBox);
        this.drawManager.registerDrawCall(this.rightClickMarker);
    }

    protected void update (HashMap<String, Object> props, Map<String, Object> rawProps) {
        this.disabled = (boolean)props.get(RTS_P_Inert.ID());
        this.opacity = (float)props.get(RTS_P_Opacity.ID());
        this.pos.set((float)props.get(RTS_P_Left.ID()), RTS_Root.screenDim.getY() - (float)props.get(RTS_P_Top.ID()));
        this.dim.set((float)props.get(RTS_P_Width.ID()), (float)props.get(RTS_P_Height.ID()));
        this.posRef.set(this.pos.getX(), this.pos.getY() - this.dim.getY());
        this.init = true;
    }

    //------------------------------------------------------------------------------------------------------------------

    /* Each frame: advance the sweep cycle; when a unit's turn comes, stamp its current position (refresh + highlight
     * at the same instant), then draw all live stamps along the curve. Cycle bounds only reshuffle order, no bulk refresh. */
    private RTS_DrawCall sweepLayerCall = new RTS_DrawCall() {
        @Override
        public Integer zIndex() {
            return (layers.miniMap);
        }

        @Override
        public void call() {
            long now = System.currentTimeMillis();
            long cycleMs = (long) refreshMs;
            if (!cycleValid || now - cycleStart >= cycleMs) {
                if (cycleValid) cycleIdx++;
                cycleStart = now;
                cycleValid = true;
                reshuffle(now);
            }
            long sweep = (long) Math.min(sweepMs, cycleMs / 2f);
            trySweepGroup(false, now, cycleMs, sweep, 0);
            trySweepGroup(true, now, cycleMs, sweep, (long) (SWEEP_PHASE_SPLIT * cycleMs));

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            drawAllStamps(now, cycleMs);
            drawObjectives();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    };

    /* Cycle boundary: retire departed units, reshuffle the appearance order of the remaining ones */
    private void reshuffle (long now) {
        java.util.List<ShipAPI> live = Global.getCombatEngine().getShips();
        states.keySet().removeIf(ship -> !live.contains(ship));
        ArrayList<ShipAPI> ships = new ArrayList<>();
        ArrayList<ShipAPI> fighters = new ArrayList<>();
        for (ShipAPI ship : live) {
            if (ship.isShuttlePod() || ship.getHullSize() == null || ship.getOriginalOwner() == 100)
                continue;
            if (isFighter(ship)) { fighters.add(ship); continue; }
            if (ship.isHulk()) continue;   // hulks get no new stamps; existing ones fade out naturally
            if (!ship.isAlly() && ship.getOriginalOwner() != 0 && ship.getOriginalOwner() != 1)
                continue;
            ships.add(ship);
        }
        assignOffsets(ships);
        assignOffsets(fighters);
    }

    private void assignOffsets (ArrayList<ShipAPI> list) {
        int n = list.size();
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Collections.shuffle(Arrays.asList(order));
        for (int r = 0; r < n; r++)
            ensureState(list.get(order[r])).offset = n <= 1 ? 0f : (float) r / (float) n;
    }

    private SweepState ensureState (ShipAPI ship) {
        SweepState st = states.get(ship);
        if (st == null) {
            st = new SweepState();
            states.put(ship, st);
        }
        return st;
    }

    private boolean isFighter (ShipAPI ship) {
        return !ship.isHulk()
                && (ship.getHullSize().name().equals("FIGHTER") || ship.isFighter());
    }

    private Color factionColor (ShipAPI ship) {
        if (ship.isAlly()) return palette.allied;
        if (ship.getOriginalOwner() == 0) return palette.friendly;
        if (ship.getOriginalOwner() == 1) return palette.enemy;
        return null;
    }

    /* Stamp a unit when its turn comes: freeze its true position/facing/velocity as a new stamp (refresh + highlight at once) */
    private void trySweepGroup (boolean fighters, long now, long cycleMs, long sweep, long phaseStartMs) {
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (isFighter(ship) != fighters)
                continue;
            SweepState st = ensureState(ship);
            if (st.sweptCycle == cycleIdx)
                continue;                                 // already stamped this cycle
            long sweptAt = cycleStart + phaseStartMs + (long) (st.offset * sweep);
            if (now < sweptAt)
                continue;                                 // not its turn yet
            st.sweptCycle = cycleIdx;
            Color color = factionColor(ship);
            if (color == null)
                continue;
            Stamp s = new Stamp();
            s.loc.set(ship.getLocation());
            s.vel.set(ship.getVelocity());
            s.facing = ship.getFacing();
            s.size = fighters ? fighterIconSize : getShipIconSize(ship);
            s.t = fighters ? fighterLineThickness : shipLineThickness;
            s.lineScale = shipSpeedLineScale * (fighters ? fighterSpeedLineFactor : 1f);
            s.color = color;
            s.born = now;
            st.stamps.add(s);
            while (st.stamps.size() > STAMP_LIFETIME_CYCLES)
                st.stamps.remove(0);                      // keep only the last 3 cycles of stamps
        }
    }

    /* Draw all live stamps: oldest to newest (brightest last); velocity streak only on the newest stamp */
    private void drawAllStamps (long now, long cycleMs) {
        for (Map.Entry<ShipAPI, SweepState> e : states.entrySet()) {
            SweepState st = e.getValue();
            if (st.stamps.isEmpty())
                continue;
            Iterator<Stamp> it = st.stamps.iterator();
            while (it.hasNext())
                if (now - it.next().born > (long) STAMP_LIFETIME_CYCLES * cycleMs)
                    it.remove();                          // lifetime exceeded, remove
            for (int i = 0; i < st.stamps.size(); i++) {
                Stamp s = st.stamps.get(i);
                float bright = sweepCurveAt(now - s.born, cycleMs);
                if (bright < 0.02f)
                    continue;                             // phosphor has died out
                int a = Math.min(255, (int) (s.color.getAlpha() * opacity * bright));
                Color col = new Color(s.color.getRed(), s.color.getGreen(), s.color.getBlue(), a);
                Vector2f loc = getVectorLoc(s.loc);
                drawTriangle(loc, s.facing, s.size, s.t, col);
                if (i == st.stamps.size() - 1)
                    drawVelLine(s, col, loc);
            }
        }
    }

    /* Hollow triangle: three thin quad segments (not limited by glLineWidth)*/
    private void drawTriangle (Vector2f centre, float facingDeg, float size, float thickness, Color color) {
        double rad = Math.toRadians(facingDeg);
        double nose = size * 0.62;
        double tail = size * 0.38;
        double halfW = size * 0.48;
        float nx = centre.getX() + ((float) (Math.cos(rad) * nose));
        float ny = centre.getY() + ((float) (Math.sin(rad) * nose));
        float tx = centre.getX() - ((float) (Math.cos(rad) * tail));
        float ty = centre.getY() - ((float) (Math.sin(rad) * tail));
        float px = -((float) Math.sin(rad));
        float py = ((float) Math.cos(rad));
        drawSegment(
                new Vector2f(nx, ny),
                new Vector2f(tx + px * (float) halfW, ty + py * (float) halfW),
                thickness, color
        );
        drawSegment(
                new Vector2f(tx + px * (float) halfW, ty + py * (float) halfW),
                new Vector2f(tx - px * (float) halfW, ty - py * (float) halfW),
                thickness, color
        );
        drawSegment(
                new Vector2f(tx - px * (float) halfW, ty - py * (float) halfW),
                new Vector2f(nx, ny),
                thickness, color
        );
    }

    private void drawVelLine (Stamp s, Color color, Vector2f mapLoc) {
        float speed = (float) Math.sqrt(s.vel.getX() * s.vel.getX() + s.vel.getY() * s.vel.getY());
        if (speed < 1f)
            return;
        float len = Math.min(speed * s.lineScale * dim.getX(), dim.getX() * speedLineMaxLenMod);
        if (len < 1.5f)
            return;
        drawSegment(
                mapLoc,
                new Vector2f(
                        mapLoc.getX() + (s.vel.getX() / speed) * len,
                        mapLoc.getY() + (s.vel.getY() / speed) * len
                ),
                s.t, color
        );
    }

    /* Objectives: hollow diamond, coloured by owner (vanilla neutral grey)*/
    private void drawObjectives () {
        float size = 9f;
        float thickness = 1.4f;
        for (BattleObjectiveAPI obj : Global.getCombatEngine().getObjectives()) {
            Color color = obj.getOwner() == 100
                    ? palette.neutral
                    : obj.getOwner() == 1
                    ? palette.enemy
                    : palette.friendly;
            color = new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    ((Float) (color.getAlpha() * opacity * 0.55f)).intValue()
            );
            Vector2f loc = getVectorLoc(obj.getLocation());
            drawSegment(new Vector2f(loc.getX(), loc.getY() + size), new Vector2f(loc.getX() + size, loc.getY()), thickness, color);
            drawSegment(new Vector2f(loc.getX() + size, loc.getY()), new Vector2f(loc.getX(), loc.getY() - size), thickness, color);
            drawSegment(new Vector2f(loc.getX(), loc.getY() - size), new Vector2f(loc.getX() - size, loc.getY()), thickness, color);
            drawSegment(new Vector2f(loc.getX() - size, loc.getY()), new Vector2f(loc.getX(), loc.getY() + size), thickness, color);
        }
    }

    /* Thin quad segment: width not limited by glLineWidth */
    private void drawSegment (Vector2f from, Vector2f to, float thickness, Color color) {
        float dx = to.getX() - from.getX();
        float dy = to.getY() - from.getY();
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.01f)
            return;
        float nx = (-dy / len) * (thickness / 2f);
        float ny = (dx / len) * (thickness / 2f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4ub(
                (byte) color.getRed(),
                (byte) color.getGreen(),
                (byte) color.getBlue(),
                (byte) color.getAlpha()
        );
        GL11.glVertex2f(from.getX() + nx, from.getY() + ny);
        GL11.glVertex2f(to.getX() + nx, to.getY() + ny);
        GL11.glVertex2f(to.getX() - nx, to.getY() - ny);
        GL11.glVertex2f(from.getX() - nx, from.getY() - ny);
        GL11.glEnd();
    }

    private RTS_DrawCall viewPortBox = new RTS_DrawCall() {

        @Override
        public Integer zIndex() {
            return (layers.viewPortBox);
        }

        @Override
        public void call() {
            if (RTS_MiniMapRenderer.this.camera == null || !RTS_MiniMapRenderer.this.camera.init)
                return;
            RTS_GenericDrawMeth.viewPortBoxOutline_LEGACY(
                    new RTS_GenericDrawMeth.quadRec(
                            this.getCameraLocation(RTS_MiniMapRenderer.this.camera.pos, RTS_MiniMapRenderer.this.camera.visableDim),
                            this.getViewPortSize(),
                            palette.viewPortLine
                    ),
                    0.5f,
                    0.8f * opacity,
                    0.3f,
                    this.getConstraint()
            );
        }

        private Vector2f getCameraLocation (Vector2f camerPos, Vector2f visibleVec) {
            Vector2f location = new Vector2f();
            float battleW = Global.getCombatEngine().getMapWidth();
            float battleH = Global.getCombatEngine().getMapHeight();
            location.setX(
                      pos.getX()
                    + (dim.getX() / 2f)
                    + ((camerPos.getX() / battleW) * dim.getX())
                    + (((visibleVec.getX() / battleW) * dim.getX()) / 2f)
            );
            location.setY(
                      pos.getY()
                    - (dim.getY() / 2f)
                    + ((camerPos.getY() / battleH) * dim.getY())
                    + (((visibleVec.getY() / battleH) * dim.getY()) / 2f)
            );
            return (location);
        }

        private Vector2f getViewPortSize () {
            CombatEngineAPI eng = Global.getCombatEngine();
            return (new Vector2f(
                    (eng.getViewport().getVisibleWidth() / eng.getMapWidth()) * dim.getX(),
                    (eng.getViewport().getVisibleHeight() / eng.getMapHeight()) * dim.getY()
            ));
        }

        private RTS_GenericDrawMeth.quadConstraint getConstraint () {
            return (
                    new RTS_GenericDrawMeth.quadConstraint(
                            pos.getX(),
                            pos.getX() + dim.getX(),
                            pos.getY() - dim.getY(),
                            pos.getY()
                    )
            );
        }
    };

    private RTS_DrawCall rightClickMarker = new RTS_DrawCall() {
        RTS_DrawQuad.quadCall quadBuilder = new RTS_DrawQuad.quadCall();
        RTS_GenericDrawMeth.quadToCircleLineShader circleShader = new RTS_GenericDrawMeth.quadToCircleLineShader();
        Color coreColor = palette.RCMCore;
        Color fadeColor = palette.RCMBorder;

        @Override
        public Integer zIndex() {
            return (layers.rightClick);
        }

        @Override
        public void call() {
            for (Map.Entry<Integer, markerStore> entry : aniStore.entrySet()) {
                if (entry.getValue().aniCont.ID == null)
                    continue;
                this.drawMarker(entry.getValue().location, entry.getValue().aniCont.getCalculatedDelta());
            }
        }

        public void drawMarker (Vector2f location, float delta) {
            float size = 10f + (delta / 2f);
            float alpha = 1f;
            if (delta < 20f)
                alpha = delta / 20f;
            else if (delta > 80f)
                alpha = (100f - delta) / 20f;
            alpha = clamp(alpha, 0f, 1f);
            alpha = alpha - (0.7f * (delta / 100f));
            alpha = clamp(alpha, 0f, 1f);
            Color main = new Color(
                    coreColor.getRed(),
                    coreColor.getGreen(),
                    coreColor.getBlue(),
                    ((Float) (alpha * 255f)).intValue()
            );
            Color fade = new Color(
                    fadeColor.getRed(),
                    fadeColor.getGreen(),
                    fadeColor.getBlue(),
                    ((Float) (alpha * 255f)).intValue()
            );
            quadBuilder
                    .pos(location)
                    .size(size)
                    .color(main)
                    .filter(circleShader)
                        .thickness(Math.min(5f, size * (2f/10f)))
                        .fadeThickness(5f)
                        .colorFade(fade)
                        .fitVsEncircle(true)
                    .set()
                    .render();
        }
    };

    private static float clamp (float value, float min, float max) {
        return (value < min ? min : value > max ? max : value);
    }

    //------------------------------------------------------------------------------------------------------------------

    public void updateAnimationControllers () {
        for (Map.Entry<Integer, markerStore> entry : aniStore.entrySet()) {
            markerStore hold = entry.getValue();
            if (hold.queueAnimation) {
                hold.queueAnimation = false;
                hold.aniCont.reset();
                hold.aniCont.setPlay(true);
                animator.queueAnimation(hold.aniCont);
            }
            if (hold.removeAnimation) {
                hold.removeAnimation = false;
                animator.removeAnimation(hold.aniCont.ID);
            }
        }
    }

    private float toMapX (float worldX, float battleW) {
        return (this.pos.getX() + this.dim.getX() / 2f + ((worldX / battleW) * this.dim.getX()));
    }

    private float toMapY (float worldY, float battleH) {
        return (this.pos.getY() - this.dim.getY() / 2f + ((worldY / battleH) * this.dim.getY()));
    }

    private Vector2f getVectorLoc (Vector2f loc) {
        float battleW = Global.getCombatEngine().getMapWidth();
        float battleH = Global.getCombatEngine().getMapHeight();
        return (new Vector2f(toMapX(loc.getX(), battleW), toMapY(loc.getY(), battleH)));
    }

    private Float getShipIconSize (ShipAPI ship) {
        // HullSize.DEFAULT (some vanilla/mod units) and unknown sizes fall back to frigate, avoiding an NPE when stamping
        Float size = this.iconSizes.get(ship.getHullSize().name());
        if (size == null)
            size = this.iconSizes.get("FRIGATE");
        return (size);
    }

    public int registerMarker (Vector2f location) {
        int place = this.nextMarker;
        markerStore hold = this.aniStore.get(place);
        hold.queueAnimation = true;
        hold.location = location;
        this.nextMarker++;
        this.nextMarker = this.nextMarker == 5 ? 0 : this.nextMarker;
        return (place);
    }

    public void deregisterMarker (int ID) {
        this.aniStore.get(ID).removeAnimation = true;
    }

    //------------------------------------------------------------------------------------------------------------------
}
