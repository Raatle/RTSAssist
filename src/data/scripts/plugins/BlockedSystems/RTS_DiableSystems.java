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

package data.scripts.plugins.BlockedSystems;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import data.scripts.plugins.Utils.RTS_Context;
import data.scripts.plugins.Utils.RTS_Event;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

import java.util.HashMap;

public class RTS_DiableSystems {

    public RTS_DiableSystems () {
        this.blockedSystems.put("diableavionics_drift", this._diableavionics_drift_());
        this.blockedSystems.put("diableavionics_evasion", this._diableavionics_evasion_());
        this.blockedSystems.put("diableavionics_flicker", this._diableavionics_flicker_());
        this.blockedSystems.put("diableavionics_damperwave", this._diableavionics_damperwave_());
        this.blockedSystems.put("diableavionics_heavyflicker", this._diableavionics_heavyflicker_());
    }

    private HashMap<String, RTS_BlockSystemPlugin> blockedSystems = new HashMap<>();
    public HashMap<String, RTS_BlockSystemPlugin> getBlockedSystems() {
        return(this.blockedSystems);
    }

    /* Riptide */

    private class diableavionics_drift_staticClass {
        boolean init = false;
        boolean firstFrameInit = false;
        float cooldownAtCleanup = 0f;
        float cleanUpDelta = 0f;
        float firstFrameDelta = 0f;
        boolean hadEnemy = false;
        String eventHold = null;
        HashMap<String, Object> variantDistance = new HashMap<>() {{
            put("diableavionics_pandemonium", 50f);
            put("diable_riptide", 137f);
            put("diableavionics_maelstrom", 60f);
        }};
    }

    private RTS_BlockSystemPlugin _diableavionics_drift_ () {
        return (new RTS_BlockSystemPlugin() {
            private diableavionics_drift_staticClass diableavionics_drift_static = new diableavionics_drift_staticClass();

            @Override
            public String getSystemName() {
                return "diableavionics_drift";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                if (context.getNeighboursFast().findEnemyShipNeighbours(context.getShip(), 1500f, false).isEmpty())
                    diableavionics_drift_static.hadEnemy = false;
                if (!diableavionics_drift_static.init) {
                    if (diableavionics_drift_static.eventHold != null) {
                        context.getEventManager().deleteEvent(diableavionics_drift_static.eventHold);
                        diableavionics_drift_static.eventHold = null;
                    }
                    if (diableavionics_drift_static.firstFrameDelta != 0f
                            && context.getShip().getSystem().getCooldownRemaining() > 0f
                            && !context.getShip().getSystem().isActive()
                    ) {
                            context.getShip().getSystem().setCooldownRemaining(MathUtils.clamp(
                                    diableavionics_drift_static.cooldownAtCleanup
                                            - (context.getTimePassedIngame()
                                            - diableavionics_drift_static.cleanUpDelta),
                                    0.1f,
                                    255f
                            ));
                    }
                    diableavionics_drift_static.firstFrameDelta = context.getTimePassedIngame();
                    context.getShip().getSystem().getSpecAPI().setAccelerateAllowed(false);
                    diableavionics_drift_static.init = true;
                }
                if (context.getShip().getSystem().isActive()) {
                    if (!diableavionics_drift_static.firstFrameInit) {
                        context.getShip().getVelocity().set(
                                VectorUtils.resize(
                                        VectorUtils.getDirectionalVector(
                                                context.getShip().getLocation(),
                                                context.getModDest()
                                        ),
                                        context.getShip().getMutableStats().getMaxSpeed().modified
                                )
                        );
                        diableavionics_drift_static.firstFrameInit = true;
                    }
                }
                else
                    diableavionics_drift_static.firstFrameInit = false;
                boolean flag = false;
                if (context.getShip().getSystem().getAmmo() <= (context.getShip().getSystem().getMaxAmmo() - 1))
                    flag = true;
                if (context.getTargetEnemy() != null
                        && context.getShip().getSystem().getAmmo() >= 2
                ){
                    if (MathUtils.getDistanceSquared(
                            context.getShip().getLocation(),
                            context.getModDest()
                    ) < Math.pow(((float)diableavionics_drift_static.variantDistance
                            .get(context.getShip().getHullSpec().getBaseHullId()) * 5), 2))
                        flag = false;
                }
                if (context.getTargetEnemy() == null && diableavionics_drift_static.hadEnemy)
                    flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(((float)diableavionics_drift_static.variantDistance
                        .get(context.getShip().getHullSpec().getBaseHullId()) * 3), 2))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (context.getShip().getSystem().isActive())
                    flag = false;
                diableavionics_drift_static.hadEnemy = context.getTargetEnemy() != null
                        ? true
                        : (diableavionics_drift_static.hadEnemy && context.getShip().getSystem().isActive())
                                || (MathUtils.getDistanceSquared(
                                        context.getShip().getLocation(),
                                        context.getModDest()
                                ) < Math.pow(100f, 2))
                        ? false
                        : diableavionics_drift_static.hadEnemy
                        ? true
                        : false;

                return (flag);
            }

            @Override
            public boolean cleanUp(RTS_Context context) {
                diableavionics_drift_static.cooldownAtCleanup = context.getShip().getSystem().getCooldownRemaining();
                diableavionics_drift_static.cleanUpDelta = context.getTimePassedIngame();
                diableavionics_drift_static.eventHold = context.getEventManager().addEvent(new RTS_Event() {
                    ShipAPI ship = context.getShip();

                    @Override
                    public boolean shouldExecute(Object state) {
                        return(!this.ship.getSystem().isActive());
                    }

                    @Override
                    public void run() {
                        this.ship.getSystem().setCooldownRemaining(MathUtils.clamp(
                                2f - this.ship.getSystem().getCooldownRemaining(), 0.1f, 2f)
                        );
                    }
                });
                context.getShip().getSystem().getSpecAPI().setAccelerateAllowed(true);
                diableavionics_drift_static.init = false;
                return (true);
            }
        });
    }

    /* Vapor */

    private RTS_BlockSystemPlugin _diableavionics_evasion_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_evasion";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(500f, 2))
                    flag = true;
                if (context.getShip().getSystem().isActive())
                    flag = false;
                return (flag);
            }
        });
    }

    /* Coanda */

    private RTS_BlockSystemPlugin _diableavionics_damperwave_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_damperwave";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(800f, 2))
                    flag = true;
                if (context.getShip().getSystem().isActive())
                    flag = false;
                if (!flag)
                    if (context.getShip().getSystem().canBeActivated() && context.getShip().getSystem().getAmmo() >= 1)
                        context.getShip().useSystem();
                return (flag);
            }
        });
    }

    private RTS_BlockSystemPlugin _diableavionics_heavyflicker_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_heavyflicker";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(context.getShip().getShieldRadiusEvenIfNoShield() * 3, 2))
                    flag = true;
                if (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getAngle(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ),
                        VectorUtils.getFacing(context.getShip().getVelocity())
                )) > 20f)
                    flag = true;
                if (context.getShip().getSystem().isActive())
                    flag = false;
                if (!flag)
                    if (context.getShip().getSystem().canBeActivated() && context.getShip().getSystem().getAmmo() >= 1)
                        context.getShip().useSystem();
                return (flag);
            }
        });
    }

    private RTS_BlockSystemPlugin _diableavionics_flicker_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_flicker";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(context.getShip().getShieldRadiusEvenIfNoShield() * 5, 2))
                    flag = true;
                if (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getAngle(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ),
                        VectorUtils.getFacing(context.getShip().getVelocity())
                )) > 10f)
                    flag = true;
                if (context.getShip().getSystem().isActive())
                    flag = false;
                if (!flag)
                    if (context.getShip().getSystem().canBeActivated() && context.getShip().getSystem().getAmmo() >= 1)
                        context.getShip().useSystem();
                return (flag);
            }
        });
    }

}

