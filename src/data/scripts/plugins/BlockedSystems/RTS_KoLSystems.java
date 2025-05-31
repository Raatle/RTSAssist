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
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.Utils.RTS_Context;
import data.scripts.plugins.Utils.RTS_Event;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

import java.util.HashMap;
import java.util.List;

public class RTS_KoLSystems {

    public RTS_KoLSystems () {
        this.blockedSystems.put("zea_dusk_phase_skip", this._zea_dusk_phase_skip_());
        this.blockedSystems.put("kol_onion", this._kol_onion_());
        this.blockedSystems.put("kol_lunge", this._kol_lunge_());
        this.blockedSystems.put("kol_lidar", this._kol_lidar_());
    }

    private HashMap<String, RTS_BlockSystemPlugin> blockedSystems = new HashMap<>();
    public HashMap<String, RTS_BlockSystemPlugin> getBlockedSystems() {
        return(this.blockedSystems);
    }

    private RTS_BlockSystemPlugin _zea_dusk_phase_skip_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "zea_dusk_phase_skip";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(context.getShip().getShieldRadiusEvenIfNoShield() * 2f , 2))
                    flag = true;
                if (context.getShip().getSystem().isActive())
                    flag = false;
                if (!flag)
                    if (context.getShip().getSystem().canBeActivated())
                        context.getShip().useSystem();
                return (flag);
            }
        });
    }

    private RTS_BlockSystemPlugin _kol_lidar_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "kol_lidar";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                if (context.getTargetEnemy() == null && context.getPriorityEnemy() == null)
                    return (true);
                boolean flag = true;
                for (WeaponAPI set : context.getShip().getUsableWeapons()){
                    if (set.getType().compareTo(WeaponAPI.WeaponType.MISSILE) == 0
                            || set.getOriginalSpec().getPrimaryRoleStr() == null
                            || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense")
                            || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense (Area)"))
                        continue;
                    if (set.isFiring())
                        flag = false;
                }
                if (context.getShip().getSystem().isActive())
                    flag = false;
                if (!flag)
                    if (context.getShip().getSystem().canBeActivated())
                        context.getShip().useSystem();
                return (flag);
            }
        });
    }

    private RTS_BlockSystemPlugin _kol_onion_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "kol_onion";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(context.getShip().getShieldRadiusEvenIfNoShield() , 2))
                    flag = true;
                if (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getAngle(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ),
                        context.getShip().getFacing()
                )) > 20f)
                    flag = true;
                if (context.getShip().getSystem().isActive())
                    flag = false;
                if (!flag)
                    if (context.getShip().getSystem().canBeActivated())
                        context.getShip().useSystem();
                return (flag);
            }
        });
    }

    private class kol_lunge_staticClass {
        boolean init = false;
        boolean firstFrameInit = false;
        float cooldownAtCleanup = 0f;
        float cleanUpDelta = 0f;
        float firstFrameDelta = 0f;
        boolean hadEnemy = false;
        String eventHold = null;
        HashMap<String, Object> variantDistance = new HashMap<>() {{
            put("kol_larkspur", 130f);
        }};
    }

    private RTS_BlockSystemPlugin _kol_lunge_ () {
        return (new RTS_BlockSystemPlugin() {
            private kol_lunge_staticClass kol_lunge_static = new kol_lunge_staticClass();

            @Override
            public String getSystemName() {
                return "kol_lunge";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                if (context.getNeighboursFast().findEnemyShipNeighbours(context.getShip(), 1500f, false).isEmpty())
                    kol_lunge_static.hadEnemy = false;
                if (!kol_lunge_static.init) {
                    if (kol_lunge_static.eventHold != null) {
                        context.getEventManager().deleteEvent(kol_lunge_static.eventHold);
                        kol_lunge_static.eventHold = null;
                    }
                    if (kol_lunge_static.firstFrameDelta != 0f
                            && context.getShip().getSystem().getCooldownRemaining() > 0f
                            && !context.getShip().getSystem().isActive()
                    ) {
                        context.getShip().getSystem().setCooldownRemaining(MathUtils.clamp(
                                kol_lunge_static.cooldownAtCleanup
                                        - (context.getTimePassedIngame()
                                        - kol_lunge_static.cleanUpDelta),
                                0.1f,
                                255f
                        ));
                    }
                    kol_lunge_static.firstFrameDelta = context.getTimePassedIngame();
                    context.getShip().getSystem().getSpecAPI().setAccelerateAllowed(false);
                    kol_lunge_static.init = true;
                }
                if (context.getShip().getSystem().isActive()) {
                    if (!kol_lunge_static.firstFrameInit) {
                        context.getShip().getVelocity().set(
                                VectorUtils.resize(
                                        VectorUtils.getDirectionalVector(
                                                context.getShip().getLocation(),
                                                context.getModDest()
                                        ),
                                        context.getShip().getMutableStats().getMaxSpeed().modified * 0.7f
                                )
                        );
                        kol_lunge_static.firstFrameInit = true;
                    }
                }
                else
                    kol_lunge_static.firstFrameInit = false;
                boolean flag = false;
                if (context.getShip().getSystem().getAmmo() <= (context.getShip().getSystem().getMaxAmmo() - 1))
                    flag = true;
                if (context.getTargetEnemy() != null
                        && context.getShip().getSystem().getAmmo() >= 1
                ){
                    if (MathUtils.getDistanceSquared(
                            context.getShip().getLocation(),
                            context.getModDest()
                    ) < Math.pow(((float)kol_lunge_static.variantDistance
                            .get(context.getShip().getHullSpec().getBaseHullId()) * 4f), 2))
                        flag = false;
                }
                if (context.getTargetEnemy() == null && kol_lunge_static.hadEnemy)
                    flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(((float)kol_lunge_static.variantDistance
                        .get(context.getShip().getHullSpec().getBaseHullId()) * 3f), 2))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (context.getShip().getSystem().isActive())
                    flag = false;
                if (!flag)
                    if (context.getShip().getSystem().canBeActivated() && !context.getShip().getSystem().isActive())
                        context.getShip().useSystem();
                kol_lunge_static.hadEnemy = context.getTargetEnemy() != null
                        ? true
                        : (kol_lunge_static.hadEnemy && context.getShip().getSystem().isActive())
                        || (MathUtils.getDistanceSquared(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ) < Math.pow(100f, 2))
                        ? false
                        : kol_lunge_static.hadEnemy
                        ? true
                        : false;
                return (flag);
            }

            @Override
            public boolean cleanUp(RTS_Context context) {
                kol_lunge_static.cooldownAtCleanup = context.getShip().getSystem().getCooldownRemaining();
                kol_lunge_static.cleanUpDelta = context.getTimePassedIngame();
                kol_lunge_static.eventHold = context.getEventManager().addEvent(new RTS_Event() {
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
                kol_lunge_static.init = false;
                return (true);
            }
        });
    }
}



























