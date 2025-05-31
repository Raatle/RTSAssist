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
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import data.scripts.plugins.Utils.RTS_Context;
import data.scripts.plugins.Utils.RTS_Event;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

import java.util.HashMap;

public class RTS_Indies_Expansion_Pack {
    public RTS_Indies_Expansion_Pack () {
        this.blockedSystems.put("acs_advburndrive", this._acs_advburndrive_());
        this.blockedSystems.put("acs_raiderdriveSafe", this._acs_raiderdriveSafe_());
        this.blockedSystems.put("acs_phaseteleporter", this._acs_phaseteleporter_());
    }

    private HashMap<String, RTS_BlockSystemPlugin> blockedSystems = new HashMap<>();
    public HashMap<String, RTS_BlockSystemPlugin> getBlockedSystems() {
        return(this.blockedSystems);
    }

    private RTS_BlockSystemPlugin _acs_advburndrive_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "acs_advburndrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                if (context.getShip().getSystem().isActive())
                    return (false);
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(context.getShip().getShieldRadiusEvenIfNoShield() , 2))
                    flag = true;
                return (flag);
            }
        });
    }

    private RTS_BlockSystemPlugin _acs_raiderdriveSafe_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "acs_raiderdriveSafe";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
                if (MathUtils.getDistanceSquared(
                        context.getShip().getLocation(),
                        context.getModDest()
                ) < Math.pow(context.getShip().getShieldRadiusEvenIfNoShield() , 2))
                    flag = true;
                return (flag);
            }
        });
    }

    private RTS_BlockSystemPlugin _acs_phaseteleporter_ () {
        return (new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "acs_phaseteleporter";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                boolean flag = false;
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