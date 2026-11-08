'use strict';
/* =====================================================================================
 * demo_sweep.js — sweep state machine and rendering (core effect; change the effect mainly here)
 *
 * Stamp model, mirroring RTS_MiniMapRenderer in the mod:
 *
 *   When the sweep hits a unit, it does one and only one thing:
 *     place a stamp at the unit's [current real position] — it appears at the
 *     curve's left-end brightness; position refresh and highlight are the same instant,
 *     then that stamp [stays in place] and decays along the opacity-time curve.
 *   Stamps are kept for STAMP_LIFETIME_CYCLES cycles; a unit has at most 3 stamps
 *   at different positions at once = motion trail (the afterglow is decoupled from the unit's position and only fades out where it was stamped).
 *   Cycle boundaries do only two things: reshuffle the sweep order and switch phase scheduling. No positions are batch-refreshed.
 * ===================================================================================*/

window.__sweepStage = 'sweep-file-loaded';
window.addEventListener('error', e => {
  window.__sweepErr = (e.error && e.error.stack) || e.message + ' @' + e.filename + ':' + e.lineno;
});
let cycleStart = null, sessionT0 = null, firstCycle = true, lastCycleMs = P.cycleMs;
let cycleIdx = 0;   // simTime is owned by demo_sim.js; not re-declared here
let markers = [];      // click order markers

function sideColor(side, ally) {
  return ally ? PALETTE.allied : side === 0 ? PALETTE.friendly : PALETTE.enemy;
}

/* Per-unit sweep state: stamp queue + slot within the phase + cycle number of the last stamp */
function ensureState(u) {
  if (!u._st) u._st = { stamps: [], offset: 0, sweptCycle: -1 };
  return u._st;
}

/* Cycle boundary: reshuffle each phase's sweep order (scheduling only, no position data touched) */
function reshuffle() {
  assignOffsets(ships, flagship);
  assignOffsets(fighters, flagship);
}
function assignOffsets(list, flagshipRef) {
  const n = list.length, idx = [...list.keys()];
  if (P.orderMode === 'flagship' && flagshipRef) {
    const fx = flagshipRef.x, fy = flagshipRef.y;
    idx.sort((a, b) => Math.hypot(list[a].x-fx, list[a].y-fy) - Math.hypot(list[b].x-fx, list[b].y-fy));
  } else {
    for (let i = n-1; i > 0; i--) { const j = (Math.random()*(i+1))|0; [idx[i], idx[j]] = [idx[j], idx[i]]; }
  }
  for (let r = 0; r < n; r++) ensureState(list[idx[r]]).offset = n <= 1 ? 0 : r/n;
}

/* ---- Canvas ---- */
const cv = document.getElementById('cv'), ctx = cv.getContext('2d');
let W = 0, H = 0, dpr = 1;
function resize() {
  dpr = window.devicePixelRatio || 1;
  W = window.innerWidth; H = window.innerHeight;
  cv.width = W*dpr; cv.height = H*dpr;
  cv.style.width = W+'px'; cv.style.height = H+'px';
}
window.addEventListener('resize', resize); resize();

const PANEL_W = 352;
function minimapLayout() {
  const dimX = Math.min(H/3, W/3);
  return { dimX, px: W - PANEL_W - dimX - 30, top: H - 30 - dimX };
}

const stars = Array.from({length: 240}, () => ({ x: Math.random(), y: Math.random(), r: Math.random()*1.1+0.2, a: Math.random()*0.5+0.1 }));
const dbgEl = document.getElementById('dbg');

let lastT = performance.now();
function frame(now) {
  const dt = Math.min(0.05, (now-lastT)/1000); lastT = now;
  if (!P.paused) { simTime += dt; stepSim(dt); }

  /* ---- Cycle state machine: boundaries only reshuffle order + switch scheduling, no position refresh ---- */
  const cycleMs = P.cycleMs;
  if (sessionT0 === null) sessionT0 = now;
  if (cycleStart === null) { cycleStart = now; reshuffle(); }
  else if (now - cycleStart >= cycleMs) {
    if (firstCycle) firstCycle = false;
    cycleStart = now;
    cycleIdx++;
    if (lastCycleMs !== cycleMs) { firstCycle = true; lastCycleMs = cycleMs; }
    reshuffle();
  }

  //------------------------------------------------ Sweep scheduling: whoever's turn it is gets stamped (position refresh + highlight at the same instant)
  const sweep = Math.min(P.sweepMs, cycleMs/2);
  trySweepGroup(ships, 0, now, cycleMs, sweep, cycleIdx, false);
  trySweepGroup(fighters, SWEEP_PHASE_SPLIT, now, cycleMs, sweep, cycleIdx, true);

  //------------------------------------------------ Drawing
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  ctx.fillStyle = '#05070c'; ctx.fillRect(0, 0, W, H);
  ctx.fillStyle = '#9fb4c8';
  for (const st of stars) { ctx.globalAlpha = st.a; ctx.fillRect(st.x*W, st.y*H, st.r, st.r); }
  ctx.globalAlpha = 1;

  const L = minimapLayout();
  const dimX = L.dimX;
  const mx = x => L.px + dimX/2 + (x/MAP.w)*dimX;
  const my = y => L.top + dimX/2 + (y/MAP.h)*dimX;

  const rs = drawGroup(ships, mx, my, dimX);
  const rf = drawGroup(fighters, mx, my, dimX);

  if (P.showObj) for (const o of objectives) {
    const c = o.owner===100 ? PALETTE.neutral : o.owner===1 ? PALETTE.enemy : PALETTE.friendly;
    const col = `rgba(${c.r},${c.g},${c.b},${(c.a*0.55/255).toFixed(3)})`;
    const cx0 = mx(o.x), cy0 = my(o.y), sz = 9;
    ctx.strokeStyle = col; ctx.lineWidth = 1.4;
    ctx.beginPath(); ctx.moveTo(cx0, cy0-sz); ctx.lineTo(cx0+sz, cy0);
    ctx.lineTo(cx0, cy0+sz); ctx.lineTo(cx0-sz, cy0); ctx.closePath(); ctx.stroke();
  }

  if (P.showVP) { // view frame: follows the flagship
    const vw = 7000, vh = 5000, cx0 = flagship ? flagship.x : 0, cy0 = flagship ? flagship.y : 0;
    ctx.strokeStyle = 'rgba(255,255,255,0.8)'; ctx.lineWidth = 1;
    ctx.strokeRect(mx(cx0 - vw/2), my(cy0 - vh/2), vw/MAP.w*dimX, vh/MAP.h*dimX);
  }

  // Click markers (right-click order marker colors)
  markers = markers.filter(m => now - m.t0 < 400);
  for (const m of markers) {
    const d = (now-m.t0)/400*100, size = 10 + d/2;
    let a = 1; if (d < 20) a = d/20; else if (d > 80) a = (100-d)/20;
    a = Math.max(0, Math.min(1, a)) - 0.7*(d/100); a = Math.max(0, a);
    const c1 = PALETTE.RCMCore, c2 = PALETTE.RCMBorder;
    ctx.strokeStyle = `rgba(${c1.r},${c1.g},${c1.b},${a})`; ctx.lineWidth = Math.min(5, size*0.2);
    ctx.beginPath(); ctx.arc(m.x, m.y, size/2, 0, Math.PI*2); ctx.stroke();
    ctx.strokeStyle = `rgba(${c2.r},${c2.g},${c2.b},${a/3})`; ctx.lineWidth = 5;
    ctx.beginPath(); ctx.arc(m.x, m.y, size/2, 0, Math.PI*2); ctx.stroke();
  }

  // Timeline (spans 3 cycles) + debug readout
  const pos = ((now - cycleStart) % cycleMs) / cycleMs;
  const span = cycleMs * 3;
  const cursorU = sessionT0 === null ? 0 : ((now - sessionT0) % span) / span;
  document.getElementById('cursor').style.left = (cursorU*100)+'%';
  const segs = document.querySelectorAll('#timeline .seg');
  const sw = Math.min(P.sweepMs, cycleMs/2), hold = cycleMs/2 - sw;
  for (let c = 0; c < 3; c++) {
    segs[c*4+0].style.flex = sw; segs[c*4+1].style.flex = hold;
    segs[c*4+2].style.flex = sw; segs[c*4+3].style.flex = hold;
  }
  dbgEl.textContent = `Stamps this frame: ships ${rs.n}/${rs.stamps} · fighters ${rf.n}/${rf.stamps} · phase ${(pos*100).toFixed(0)}%`;

  // The curve editor redraws along with the main loop
  if (window.CurveEditor && window.CurveEditor.render) window.CurveEditor.render();

  // Exposed for external reads (automated verification)
  window.__sweep = { now, pos, drawnS: rs.n, drawnF: rf.n, stampS: rs.stamps, stampF: rf.stamps };

  requestAnimationFrame(frame);
}

/* Stamps a unit when its turn comes: freezes the current real position/facing/speed into a new stamp (refresh + highlight at the same instant) */
function trySweepGroup(list, phaseStart, now, cycleMs, sweep, cycleIdx, fighters) {
  const phaseStartMs = phaseStart * cycleMs;
  for (const u of list) {
    const st = ensureState(u);
    if (st.sweptCycle === cycleIdx)
      continue;                                   // already stamped this cycle
    const sweptAt = cycleStart + phaseStartMs + st.offset * sweep;
    if (now < sweptAt)
      continue;                                   // not its turn yet
    st.sweptCycle = cycleIdx;
    const sp = u.speed || 0;
    st.stamps.push({
      x: u.x, y: u.y,
      vx: Math.cos(u.heading) * sp, vy: Math.sin(u.heading) * sp,
      facing: u.heading * 180 / Math.PI,
      size: fighters ? FIGHTER_ICON : ICON[u.cls],
      t: fighters ? FI_T : SHIP_T,
      lineScale: (P.shipSpeedLinePct/100)/250 * (fighters ? P.fighterLineFactor : 1),
      color: sideColor(u.side, fighters ? false : u.ally),
      born: now,
    });
    while (st.stamps.length > STAMP_LIFETIME_CYCLES)
      st.stamps.shift();                          // keep only the last 3 cycles of stamps
  }
}

/* Draws all live stamps of a unit group: old→new in order (brightest last); speed lines only on the newest stamp */
function drawGroup(list, mx, my, dimX) {
  let drawn = 0, total = 0;
  const now = performance.now();
  for (const u of list) {
    const st = ensureState(u);
    total += st.stamps.length;
    for (let i = 0; i < st.stamps.length; i++) {
      const s = st.stamps[i];
      const u3 = (now - s.born) / (P.cycleMs * STAMP_LIFETIME_CYCLES);
      if (u3 >= 1)
        continue;                                 // past its retention window (defensive; normally already shifted)
      const bright = evalCurve(u3);
      if (bright < P.sweepCutoff)
        continue;                                 // afterglow burned out: skip drawing
      const a = Math.min(255, s.color.a * bright);
      const col = `rgba(${s.color.r},${s.color.g},${s.color.b},${(a/255).toFixed(3)})`;
      drawTriangle(mx(s.x), my(s.y), s.facing, s.size, s.t, col);
      if (P.showVel && i === st.stamps.length - 1)
        drawVelLine(s, col, dimX, mx, my);
      drawn++;
    }
  }
  return { n: drawn, stamps: total };
}

function drawTriangle(cx0, cy0, facingDeg, size, thickness, col) {
  const rad = facingDeg*Math.PI/180;
  const c = Math.cos(rad), s = Math.sin(rad);
  const nx = cx0 + c*size*0.62, ny = cy0 + s*size*0.62;
  const tx = cx0 - c*size*0.38, ty = cy0 - s*size*0.38;
  const pxn = -s, pyn = c, hw = size*0.48;
  ctx.strokeStyle = col; ctx.lineWidth = thickness; ctx.lineJoin = 'round';
  ctx.beginPath();
  ctx.moveTo(nx, ny); ctx.lineTo(tx+pxn*hw, ty+pyn*hw);
  ctx.lineTo(tx-pxn*hw, ty-pyn*hw); ctx.closePath(); ctx.stroke();
}

function drawVelLine(b, col, dimX, mx_, my_) {
  const sp = Math.hypot(b.vx, b.vy); if (sp < 1) return;
  const len = Math.min(sp*b.lineScale*dimX, dimX*MAXLEN_MOD); if (len < 1.5) return;
  ctx.strokeStyle = col; ctx.lineWidth = b.t;
  ctx.beginPath(); ctx.moveTo(mx_(b.x), my_(b.y));
  ctx.lineTo(mx_(b.x) + b.vx/sp*len, my_(b.y) + b.vy/sp*len); ctx.stroke();
}

function addMarker(x, y) { markers.push({ x, y, t0: performance.now() }); }

requestAnimationFrame(frame);   // start the main loop
