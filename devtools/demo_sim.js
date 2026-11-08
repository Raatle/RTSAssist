'use strict';
/* =====================================================================================
 * demo_sim.js — simulated battlefield
 * Builds a moving fake battlefield (both sides' ships/fighters/objectives/flagship) so the minimap has something to draw.
 * No counterpart in the mod; purely a data source. Edit here to add unit behavior.
 * ===================================================================================*/

const MAP = { w: 32000, h: 22000 };
let ships = [], fighters = [], objectives = [], flagship = null;
let simTime = 0;

function rnd(a, b) { return a + Math.random() * (b - a); }

function mkShip(side, cls) {
  const dir = side === 0 ? 1 : -1;
  const speed = cls === 'CAPITAL_SHIP' ? rnd(40, 70) : cls === 'CRUISER' ? rnd(60, 95)
              : cls === 'DESTROYER' ? rnd(80, 115) : rnd(100, 150);
  return {
    side, cls,
    x: side === 0 ? rnd(-MAP.w*0.42, -MAP.w*0.18) : rnd(MAP.w*0.18, MAP.w*0.42),
    y: rnd(-MAP.h*0.35, MAP.h*0.35),
    heading: dir > 0 ? rnd(-0.5, 0.5) : Math.PI + rnd(-0.5, 0.5),
    speed, turn: 0, turnTimer: rnd(0, 3), ally: Math.random() < 0.2,
  };
}

function mkFighter(side) {
  const dir = side === 0 ? 1 : -1;
  return {
    side,
    x: (side===0?-1:1)*rnd(MAP.w*0.15, MAP.w*0.35), y: rnd(-MAP.h*0.3, MAP.h*0.3),
    heading: dir>0?rnd(-0.9,0.9):Math.PI+rnd(-0.9,0.9),
    speed: rnd(180, 320), turn: 0, turnTimer: rnd(0, 1),
  };
}

function resetSim() {
  ships = []; fighters = [];
  for (let s = 0; s < 2; s++) {
    for (let i = 0; i < P.nShipsPerSide; i++) {
      const cls = i < 2 ? 'CAPITAL_SHIP' : i < 6 ? 'CRUISER' : i < 11 ? 'DESTROYER' : 'FRIGATE';
      ships.push(mkShip(s, cls));
    }
    for (let i = 0; i < P.nFightersPerSide; i++) fighters.push(mkFighter(s));
  }
  flagship = ships.find(s => s.side === 0 && s.cls === 'CAPITAL_SHIP');
  objectives = [
    { x: 0, y: -MAP.h*0.22, owner: 100 }, { x: 0, y: MAP.h*0.22, owner: 100 },
    { x: -MAP.w*0.25, y: 0, owner: 0 }, { x: MAP.w*0.25, y: 0, owner: 1 },
  ];
}

function stepSim(dt) {
  for (const s of ships) {
    s.turnTimer -= dt;
    if (s.turnTimer <= 0) { s.turn = rnd(-0.25, 0.25); s.turnTimer = rnd(1.5, 4); }
    s.heading += s.turn * dt;
    if ((s.side===0 && s.x > MAP.w*0.42) || (s.side===1 && s.x < -MAP.w*0.42)) s.heading = Math.PI - s.heading;
    s.x += Math.cos(s.heading) * s.speed * dt; s.y += Math.sin(s.heading) * s.speed * dt;
    s.y = Math.max(-MAP.h*0.45, Math.min(MAP.h*0.45, s.y));
  }
  for (const f of fighters) {
    f.turnTimer -= dt;
    if (f.turnTimer <= 0) { f.turn = rnd(-2.2, 2.2); f.turnTimer = rnd(0.3, 1.2); }
    f.heading += f.turn * dt;
    if ((f.side===0 && f.x > MAP.w*0.45) || (f.side===1 && f.x < -MAP.w*0.45)) f.heading += Math.PI;
    f.x += Math.cos(f.heading) * f.speed * dt; f.y += Math.sin(f.heading) * f.speed * dt;
    f.y = Math.max(-MAP.h*0.45, Math.min(MAP.h*0.45, f.y));
  }
}

resetSim(); // generate the initial battlefield
