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

public class RTS_IronShellSystems {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "eis_jump";
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
                return "eis_rampagedrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 4;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (withinDistanceOfModDest(variantCalc) && !context.getShip().getSystem().isActive())
                    flag = true;
                if (this.notFacingModDest(20f))
                    flag = true;
                if (context.getTargetEnemy() == null)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                if (approachingModDest())
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
                return "eis_microburn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (withinDistanceOfModDest(variantCalc) && !context.getShip().getSystem().isActive())
                    flag = true;
                if (this.notFacingModDest(20f))
                    flag = true;
                if (context.getTargetEnemy() == null)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                if (approachingModDest())
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
                return "eis_zandatsu";
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
                return "eis_rampagedrive2";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 4;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (withinDistanceOfModDest(variantCalc) && !context.getShip().getSystem().isActive())
                    flag = true;
                if (this.notFacingModDest(20f))
                    flag = true;
                if (context.getTargetEnemy() == null)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                if (approachingModDest())
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }
}