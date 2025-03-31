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

import com.fs.starfarer.api.combat.ShipCommand;
import data.scripts.plugins.Utils.RTS_Context;
import data.scripts.plugins.Utils.RTS_Draw;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

import java.util.HashMap;

public class RTS_VanillaSystems {

    private HashMap<String, RTS_BlockSystemPlugin> blockedSystems = new HashMap<>();

    public RTS_VanillaSystems () {
        this.blockedSystems.put("alwaysTrue", this._alwaysTrue_());
        this.blockedSystems.put("burndrive", this._burndrive_());
        this.blockedSystems.put("displacer", this._displacer_());
        this.blockedSystems.put("displacer_degraded", this._displacer_degraded_());
        this.blockedSystems.put("microburn", this._microburn_());
        this.blockedSystems.put("nova_burst", this._nova_burst_());
        this.blockedSystems.put("orion_device", this._orion_device_());
        this.blockedSystems.put("phaseteleporter", this._phaseteleporter_());
    }

    public HashMap<String, RTS_BlockSystemPlugin> getBlockedSystems() {
        return(this.blockedSystems);
    }

    private RTS_BlockSystemPlugin _alwaysTrue_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "alwaystrue";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return true;
            }
        });
    }

    private RTS_BlockSystemPlugin __ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return false;
            }
        });
    }

    private RTS_BlockSystemPlugin _burndrive_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "burndrive";
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
                return (flag);
            }
        });
    }
    private RTS_BlockSystemPlugin _displacer_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "displacer";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow((context.getShip().getShieldRadiusEvenIfNoShield() * 3) , 2))
                    flag = true;
                if (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getAngle(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ),
                        VectorUtils.getFacing(context.getShip().getVelocity())
                )) > 10f)
                    flag = true;
                return flag;
            }
        });
    }

    private RTS_BlockSystemPlugin _displacer_degraded_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "displacer_degraded";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow((context.getShip().getShieldRadiusEvenIfNoShield() * 3) , 2))
                    flag = true;
                if (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getAngle(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ),
                        VectorUtils.getFacing(context.getShip().getVelocity())
                )) > 10f)
                    flag = true;
                return flag;
            }
        });
    }

    private RTS_BlockSystemPlugin _microburn_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "microburn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow((context.getShip().getShieldRadiusEvenIfNoShield() * 2) , 2))
                    flag = true;
                if (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getAngle(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ),
                        context.getShip().getFacing()
                )) > 20f)
                    flag = true;
                return flag;
            }
        });
    }

    private RTS_BlockSystemPlugin _nova_burst_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "nova_burst";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow((context.getShip().getShieldRadiusEvenIfNoShield() * 4) , 2))
                    flag = true;
                if (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getAngle(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ),
                        context.getShip().getFacing()
                )) > 40f)
                    flag = true;
                return flag;
            }
        });
    }

    private RTS_BlockSystemPlugin _orion_device_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "orion_device";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow((context.getShip().getShieldRadiusEvenIfNoShield() * 4) , 2))
                    flag = true;
                if (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getAngle(
                                context.getShip().getLocation(),
                                context.getModDest()
                        ),
                        context.getShip().getFacing()
                )) > 40f)
                    flag = true;
                return flag;
            }
        });
    }

    private RTS_BlockSystemPlugin _phaseteleporter_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "phaseteleporter";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                ((RTS_Draw)context.getState().get("draw")).drawPrimaryDest(
                        context.getModDest(),
                        (float)context.getState().get("targetZoom"),
                        true
                );
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow((context.getShip().getShieldRadiusEvenIfNoShield() * 8) , 2))
                    flag = true;
                if (flag)
                    return (flag);
                else {
                    context.getShip().giveCommand(ShipCommand.USE_SYSTEM, context.getModDest(), 0);
                    return (false);
                }
            }
        });
    }

}