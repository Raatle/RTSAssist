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

/* KiyoHime behaves a little strangely.*/

public class RTS_KoLSystems {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "zea_dawn_targeting_beam";
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
                return "zea_dawn_charge_drive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2.2f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                if (this.notFacingModDest(30f))
                    flag = true;
                if (this.approachingModDest())
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "zea_boss_relativisticdrift";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
                boolean flag = false;
                float variantCalc = this.getFloatBasedOnBaseSpeed(3);
                this.addCoolDownOnCleanup();
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                if (!flag && context.getShip().getSystem().canBeActivated())
                    this.overrideThrust(this.getHeadingInQuad(false));
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "zea_dusk_displacer";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 3f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.keepChargeInReserve())
                    flag = true;
                if (this.hadEnemy())
                    flag = false;
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
                return "zea_edf_minijump";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
                boolean flag = false;
                float variantCalc = this.getFloatBasedOnBaseSpeed(3);
                this.addCoolDownOnCleanup();
                if (this.keepChargeInReserve())
                    flag = true;
                if (this.hadEnemy())
                    flag = false;
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                if (!flag && context.getShip().getSystem().canBeActivated())
                    this.overrideThrust(this.getHeadingInQuad(false));
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "zea_dawn_flare_burn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2.2f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(20f))
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
                return "zea_dusk_phaseteleporter";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 8;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "zea_dusk_phase_skip";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = this.getFloatBasedOnBaseSpeed(6);
                boolean flag = false;
                this.addCoolDownOnCleanup();
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
                return "kol_lidar";
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
                return "kol_onion";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 1.4f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(20f))
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
                return "kol_lunge";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = 60f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.conserveAmmo())
                    flag = true;
                if (this.engageDiveTactics(variantCalc * 5, context.getShip().getSystem().getMaxAmmo() == 2 ? 1 : 2))
                    flag = false;
                if (this.hadEnemy())
                    flag = false;
                if (this.withinDistanceOfModDest(variantCalc * 3))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }
}



























