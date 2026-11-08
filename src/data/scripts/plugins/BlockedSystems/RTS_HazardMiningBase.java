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

import data.scripts.plugins.Utils.RTS_Context;

public class RTS_HazardMiningBase {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "hmi_runaway";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = 1200f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.notFacingModDest(10f))
                    flag = true;
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                if (!flag && context.getShip().getSystem().canBeActivated())
                    this.overrideFacing((Integer)((int)this.getHeadingInQuad(false).getX()));
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "hmi_creep_drive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.keepChargeInReserve())
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.hadEnemy()) {
                    flag = false;
                    if (context.getShip().getSystem().getAmmo() == 1f)
                        this.useOnCoolDown(flag);
                }
                if (this.ifOnStayOn())
                    flag = false;
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "hmi_drill_attack";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                if (!this.hasEnemy())
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
                return "hmi_runaway_exodus";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = 2700f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.notFacingModDest(10f))
                    flag = true;
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                if (!flag && context.getShip().getSystem().canBeActivated())
                    this.overrideFacing((Integer)((int)this.getHeadingInQuad(false).getX()));
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "hmi_ammodrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.approachingModDest())
                    flag = true;
                if (this.isAttackWeaponfireing())
                    flag = false;
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
                return "hmi_runaway_ons";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = 1400f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.notFacingModDest(10f))
                    flag = true;
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                if (!flag && context.getShip().getSystem().canBeActivated())
                    this.overrideFacing((Integer)((int)this.getHeadingInQuad(false).getX()));
                return (flag);
            }
        });
    }
}