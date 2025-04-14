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


import data.scripts.plugins.Utils.RTS_Context;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

import java.util.HashMap;

public class RTS_IronShellSystems {

    private HashMap<String, RTS_BlockSystemPlugin> blockedSystems = new HashMap<>();

    public RTS_IronShellSystems () {
        this.blockedSystems.put("eis_jump", this._eis_jump_());
        this.blockedSystems.put("eis_rampagedrive", this._eis_rampagedrive_());
        this.blockedSystems.put("eis_microburn", this._eis_microburn_());
        this.blockedSystems.put("eis_zandatsu", this._eis_zandatsu_());
        this.blockedSystems.put("eis_rampagedrive2", this._eis_rampagedrive2_());

    }

    public HashMap<String, RTS_BlockSystemPlugin> getBlockedSystems() {
        return(this.blockedSystems);
    }

    private RTS_BlockSystemPlugin _eis_jump_ () {
        return(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "eis_jump";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return (true);
            }
        });
    }

    private RTS_BlockSystemPlugin _eis_rampagedrive_ () {
        return(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "eis_rampagedrive";
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



    private RTS_BlockSystemPlugin _eis_microburn_ () {
        return(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "eis_microburn";
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

    private RTS_BlockSystemPlugin _eis_zandatsu_ () {
        return(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "eis_zandatsu";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return (true);
            }
        });
    }

    private RTS_BlockSystemPlugin _eis_rampagedrive2_ () {
        return(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "eis_rampagedrive2";
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
}