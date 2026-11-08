'use strict';
window.__panelStage = 'file-loaded';
window.addEventListener('error', e => {
  window.__panelErr = (e.error && e.error.stack) || e.message + ' @' + e.filename + ':' + e.lineno;
});
/* =====================================================================================
 * demo_panel.js — control bindings, opacity-time curve editor (PS-curve style), Java migration snippet generation
 * ===================================================================================*/

function bind(id, key, fmt, scale) {
  const el = document.getElementById(id), out = document.getElementById(id+'V');
  const apply = () => {
    let v = scale ? scale(+el.value) : +el.value;
    if (el.type === 'checkbox') v = el.checked;
    P[key] = v;
    if (out) out.textContent = fmt(el.type==='checkbox' ? (v?'ON':'OFF') : +el.value);
    updateJava();
  };
  el.addEventListener('input', apply); apply();
}

window.__panelStage = 'getting-sel';
const sel = document.getElementById('orderMode');
if (!sel) throw new Error('orderMode element missing');
sel.addEventListener('change', () => { P.orderMode = sel.value; reshuffle(); updateJava(); });
window.__panelStage = 'sel-done';

bind('cycleMs','cycleMs', v=>v);
bind('sweepMs','sweepMs', v=>v);
bind('shipLine','shipSpeedLinePct', v=>v+'%');
bind('fiLine','fighterLineFactor', v=>v.toFixed(2), v=>+v);
bind('nShip','nShipsPerSide', v=>v);
bind('nFi','nFightersPerSide', v=>v);
bind('showVel','showVel', v=>v);
bind('showObj','showObj', v=>v);
bind('showVP','showVP', v=>v);
window.__panelStage = 'binds-done';
document.getElementById('nShip').addEventListener('change', resetSim);
document.getElementById('nFi').addEventListener('change', resetSim);

document.getElementById('btnPause').onclick = e => {
  P.paused = !P.paused; e.target.textContent = P.paused ? 'Resume' : 'Pause';
};
document.getElementById('btnShuffle').onclick = () => reshuffle();
document.getElementById('btnReset').onclick = () => location.reload();
window.__panelStage = 'buttons-done';
cv.addEventListener('click', e => {
  const L = minimapLayout();
  if (e.clientX >= L.px && e.clientY >= L.top) addMarker(e.clientX, e.clientY);
});
window.__panelStage = 'cv-done';

/* =================================================================================
 * Opacity-time curve editor (PS-curve style)
 * Data source P.curve: [[t,a],...] where t ∈ [0,1] is time since the sweep hit
 * and a ∈ [0,1] is opacity. The first/last points have fixed x (0 and 1) and can
 * only be dragged vertically; middle points can be freely dragged/added/removed.
 * ===============================================================================*/
window.CurveEditor = (() => {
  try {
    window.__panelStage = 'iife-enter';
  const cvs = document.getElementById('curveEd');
  window.__panelStage = 'got-canvas';
  const g = cvs.getContext('2d');
  window.__panelStage = 'got-ctx';
  cvs.width = 640; cvs.height = 360;
  window.__panelStage = 'sized';
  let drag = -1;

  const toPx = (x, y) => ({ x: x * cvs.width, y: (1 - y) * cvs.height });
  const toVal = (px, py) => ({ x: px / cvs.width, y: 1 - py / cvs.height });

  function hitPoint(px, py) {
    let best = -1, bd = Infinity;
    P.curve.forEach(([x, y], i) => {
      const p = toPx(x, y);
      const d = (p.x - px) ** 2 + (p.y - py) ** 2;
      if (d < bd) { bd = d; best = i; }
    });
    const r = 12;
    return bd <= r * r ? best : -1;
  }

  function render() {
    const w = cvs.width, h = cvs.height;
    g.clearRect(0, 0, w, h);
    g.fillStyle = '#0a0f16'; g.fillRect(0, 0, w, h);
    g.strokeStyle = '#16202e'; g.lineWidth = 1;
    for (let i = 1; i < 4; i++) {
      g.beginPath(); g.moveTo(w*i/4, 0); g.lineTo(w*i/4, h); g.stroke();
      g.beginPath(); g.moveTo(0, h*i/4); g.lineTo(w, h*i/4); g.stroke();
    }
    g.strokeStyle = '#24344a'; g.strokeRect(0.5, 0.5, w-1, h-1);
    g.strokeStyle = 'rgba(127,212,255,0.15)';                       // diagonal reference line
    g.beginPath(); g.moveTo(0, h); g.lineTo(w, 0); g.stroke();

    g.strokeStyle = '#7fd4ff'; g.lineWidth = 2.5; g.beginPath();    // curve
    for (let i = 0; i <= 120; i++) {
      const u = i / 120, y = evalCurve(u);
      const p = toPx(u, y);
      i === 0 ? g.moveTo(p.x, p.y) : g.lineTo(p.x, p.y);
    }
    g.stroke();

    for (const [x, y] of P.curve) {                                 // control points
      const p = toPx(x, y);
      g.beginPath(); g.arc(p.x, p.y, 7, 0, Math.PI*2);
      g.fillStyle = '#0d1520'; g.fill();
      g.strokeStyle = '#7fd4ff'; g.lineWidth = 2; g.stroke();
    }
  }

  function clampPoints() {
    P.curve.sort((a, b) => a[0] - b[0]);
    P.curve[0][0] = 0;
    P.curve[P.curve.length-1][0] = 1;
    for (let i = 1; i < P.curve.length; i++) {
      const lo = P.curve[i-1][0] + 0.02, hi = (i+1 < P.curve.length ? P.curve[i+1][0] : 1) - 0.02;
      P.curve[i][0] = Math.max(lo, Math.min(hi, P.curve[i][0]));
    }
  }

  cvs.addEventListener('pointerdown', e => {
    const r = cvs.getBoundingClientRect();
    const px = (e.clientX - r.left) * cvs.width / r.width;
    const py = (e.clientY - r.top) * cvs.height / r.height;
    const { x, y } = toVal(px, py);
    let i = hitPoint(px, py);
    if (i === -1) {
      const np = [Math.max(0, Math.min(1, x)), Math.max(0, Math.min(1, y))];
      P.curve.push(np);
      clampPoints();
      i = P.curve.indexOf(np);   // recover the new point by object reference after clampPoints sorts
      if (i === -1) i = 0;
    }
    drag = i;
    cvs.setPointerCapture(e.pointerId);
    render(); updateJava();
  });
  cvs.addEventListener('pointermove', e => {
    if (drag < 0) return;
    const r = cvs.getBoundingClientRect();
    const { y } = toVal((e.clientX - r.left) * cvs.width / r.width,
                        (e.clientY - r.top) * cvs.height / r.height);
    P.curve[drag][1] = Math.max(0, Math.min(1, y));
    clampPoints();
    render(); updateJava();
  });
  cvs.addEventListener('pointerup', () => { drag = -1; });
  cvs.addEventListener('dblclick', e => {
    const r = cvs.getBoundingClientRect();
    const px = (e.clientX - r.left) * cvs.width / r.width;
    const py = (e.clientY - r.top) * cvs.height / r.height;
    const i = hitPoint(px, py);
    if (i > 0 && i < P.curve.length - 1) {   // endpoints cannot be deleted
      P.curve.splice(i, 1);
      render(); updateJava();
    }
  });

  function preset(pts) { P.curve = pts.map(p => p.slice()); render(); updateJava(); }
  document.getElementById('btnCurveReset').onclick =
      () => preset(CURVE_DEFAULT);
  document.getElementById('btnCurvePulse').onclick =
      () => preset([[0, 1], [0.15, 0.55], [0.45, 0.2], [1, 0]]);
  document.getElementById('btnCurveLinger').onclick =
      () => preset([[0, 1], [0.5, 0.75], [0.85, 0.4], [1, 0.25]]);

  render();
  window.__panelStage = 'rendered';
  return { render };
  } catch (e) {
    window.__panelErr = (e && e.stack) || String(e);
    window.__panelStage = 'iife-threw';
    return null;
  }
})();

/* Java migration snippet: the curve is sampled evenly into a lookup table with curveSteps segments; the mod interpolates linearly */
function updateJava() {
  const n = P.curveSteps, row = [];
  for (let i = 0; i <= n; i++) row.push(evalCurve(i / n));
  const table = row.map(v => v.toFixed(3) + 'f');
  const lines = [];
  for (let i = 0; i < table.length; i += 6) lines.push('        ' + table.slice(i, i+6).join(', ') + ',');
  const pts = P.curve.map(p => `(${p[0].toFixed(2)},${p[1].toFixed(2)})`).join(' ');
  document.getElementById('javaBox').textContent =
`// RTS_MiniMapRenderer.java — opacity-time curve (stamp instant = 1.0, decays in place per the table; time base = 3 cycles)
private static final int SWEEP_CURVE_STEPS = ${n};
// index i corresponds to stamp age = 3 cycles * i/STEPS; the last value = afterglow brightness at the end of cycle 3
private static final float[] SWEEP_CURVE = {
${lines.join('\n')}
};
// Usage:
//   float u = Math.min(sinceStampMs / (3f * cycleMs), 1f);
//   float f = u * SWEEP_CURVE_STEPS;
//   int   i = (int)f;
//   float bright = SWEEP_CURVE[i] + (SWEEP_CURVE[i + 1] - SWEEP_CURVE[i]) * (f - i);
// Control points (backup): ${pts}

// Config.ini / Luna
"miniMapRefreshMs": ${P.cycleMs},
"miniMapSweepMs": ${P.sweepMs},
Sweep order: ${P.orderMode === 'random' ? 'random order' : 'nearest-to-flagship first (pulse wave)'}`;
}
updateJava();
document.getElementById('btnCopy').onclick = () => {
  navigator.clipboard.writeText(document.getElementById('javaBox').textContent);
};
