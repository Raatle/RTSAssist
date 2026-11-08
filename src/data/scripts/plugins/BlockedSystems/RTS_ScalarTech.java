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

import com.fs.starfarer.api.Global;
import data.scripts.plugins.Utils.RTS_Context;

public class RTS_ScalarTech {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "tahlan_pulsedrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 0.8f;
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
                return "tahlan_supercruise";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.notFacingModDest(80f))
                    flag = true;
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "tahlan_flickerdrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = 400f;
                this.addCoolDownOnCleanup();
                if (this.conserveAmmo())
                    flag = true;
                if (this.engageDiveTactics(variantCalc, 2f))
                    flag = false;
                if (this.hadEnemy())
                    flag = false;
                if (withinDistanceOfModDest(variantCalc / 2))
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
            String targetID;

            @Override
            public String getSystemName() {
                return "tahlan_emsuppression";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                if (this.hasEnemy()) {
                    this.targetID = context.getTargetEnemy() != null
                            ? context.getTargetEnemy().getId()
                            : context.getPriorityEnemy().getId();
                    if (Global.getCombatEngine().getCustomData().get(this.targetID + "_acausal_target") == null)
                        flag = false;
                }
                if (context.getShip().getFluxLevel() > 0.7)
                    flag = true;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "tahlan_capoverdrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.isAttackWeaponfireing())
                    flag = false;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "tahlan_limitrelease";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.isAttackWeaponfireing())
                    flag = false;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "tahlan_powersurge";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.isAttackWeaponfireing())
                    flag = false;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "tahlan_cycleaccel";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.isAttackWeaponfireing())
                    flag = false;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "tahlan_berserk";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.isAttackWeaponfireing())
                    flag = false;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "tahlan_loaderjets";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.approachingModDest())
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
                return "tahlan_chainskimmer";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2f;
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
                return "tahlan_overboost";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = 350f;
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
    }
}