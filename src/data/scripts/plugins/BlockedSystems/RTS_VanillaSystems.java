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

public class RTS_VanillaSystems {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "alwaystrue";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return true;
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "maneuveringjets";
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
                return "plasmajets";
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
                return "burndrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2;
                this.addCoolDownOnCleanup();
                if (withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(10f))
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                if (this.notFacingModDest(40f))
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
                return "displacer";
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
                return "displacer_degraded";
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
                return "microburn";
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
                return "nova_burst";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 4;
                this.addCoolDownOnCleanup();
                if (withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(10f))
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
                return "orion_device";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2;
                this.addCoolDownOnCleanup();
                if (withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(10f))
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
                return "phaseteleporter";
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
                return "combat_burn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2;
                if (withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(20f))
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                if (this.notFacingModDest(40f))
                    flag = true;
                if (this.approachingModDest())
                    flag = true;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }
}