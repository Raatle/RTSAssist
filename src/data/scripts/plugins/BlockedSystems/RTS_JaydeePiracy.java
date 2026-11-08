/*
  **********************************************************************************************************
  * RTSAssist version 0.2.04exp
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

package data.scripts.plugins.BlockedSystems;

import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.Utils.RTS_Context;

public class RTS_JaydeePiracy {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "jdp_lidararray";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
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
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "jdp_temporalteleporter";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.notVectoringToModDest(20f))
                    flag = true;
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "jdp_quickdrawlidararray";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
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
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "jdp_combatmasterlidararray";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
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
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }
}