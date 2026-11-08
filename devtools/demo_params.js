'use strict';
/* =====================================================================================
 * demo_params.js — tunable parameters and constants
 * Mirrors RTS_MiniMapRenderer in the mod; once tuned, demo_panel.js auto-generates the Java migration snippet.
 * ===================================================================================*/

const P = {
  // ---- Sweep timing ----
  cycleMs: 1000,          // cycle period = data refresh period (mod: miniMapRefreshMs)
  sweepMs: 300,           // time to sweep all units within one phase (mod: miniMapSweepMs)
  orderMode: 'random',    // 'random' random order | 'flagship' nearest-to-flagship first (pulse wave)

  // ---- Opacity-time curve (highlight = refresh = stamp) ----
  // On a sweep hit, "stamp" the unit's current position: it appears at the curve's left-end brightness, then decays [in place] along the curve.
  // Each element [t, a]: t = time since the stamp (fraction of 3 cycles, 0..1), a = opacity 0..1.
  // Stamps are kept for 3 cycles; a unit has at most 3 stamps at different positions at once (motion trail).
  curve: [[0, 1], [0.28, 0.62], [0.6, 0.28], [1, 0.05]],
  curveSteps: 16,         // lookup-table sample segments when migrating to the mod (table length = steps + 1)

  // ---- Units and lines ----
  shipSpeedLinePct: 10,   // speed line length as a percentage of map width at 250su/s (mod: shipSpeedLineScale = pct/100/250)
  fighterLineFactor: 0.45,// fighter speed line reduction factor (mod: fighterSpeedLineScale)

  nShipsPerSide: 9,
  nFightersPerSide: 14,
  showVel: true, showObj: true, showVP: true,
  paused: false,
};

// ---- Structural constants (usually left alone) ----
const SWEEP_PHASE_SPLIT = 0.5;   // ship phase [0,split), fighter phase [split,1)
const ICON = { FRIGATE: 14, DESTROYER: 19, CRUISER: 27, CAPITAL_SHIP: 42 };
const FIGHTER_ICON = 7;
const SHIP_T = 2, FI_T = 1.2;            // line width
const MAXLEN_MOD = 0.16;                 // max speed line length × map width

// Vanilla command-screen palette (settings.json: iconFriendColor / iconEnemyColor / iconNeutralShipColor)
const PALETTE = {
  friendly: { r: 0,   g: 255, b: 0,   a: 235 },
  enemy:    { r: 255, g: 0,   b: 0,   a: 235 },
  allied:   { r: 226, g: 196, b: 70,  a: 235 },
  neutral:  { r: 75,  g: 75,  b: 75,  a: 255 },
  viewPort: { r: 255, g: 255, b: 255 },
  RCMCore:  { r: 113, g: 234, b: 14 },
  RCMBorder:{ r: 2,   g: 255, b: 132 },
};

/* Curve evaluation: u ∈ [0,1] (time since the stamp as a fraction of 3 cycles), returns opacity 0..1 (piecewise linear) */
function evalCurve(u) {
  const pts = P.curve;
  if (u <= pts[0][0]) return pts[0][1];
  const last = pts[pts.length - 1];
  if (u >= last[0]) return last[1];
  for (let i = 1; i < pts.length; i++) {
    if (u <= pts[i][0]) {
      const x0 = pts[i-1][0], y0 = pts[i-1][1], x1 = pts[i][0], y1 = pts[i][1];
      const f = (u - x0) / ((x1 - x0) || 1e-6);
      return y0 + (y1 - y0) * f;
    }
  }
  return last[1];
}

const CURVE_DEFAULT = [[0, 1], [0.33, 0.45], [0.66, 0.18], [1, 0.04]];
const STAMP_LIFETIME_CYCLES = 3;   // number of cycles a stamp (afterglow) is kept
