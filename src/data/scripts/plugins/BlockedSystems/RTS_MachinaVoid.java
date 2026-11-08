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

public class RTS_MachinaVoid {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "expsp_abyssdash";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = 300f;
                this.addCoolDownOnCleanup();
                if (this.conserveAmmo())
                    flag = true;
                if (this.engageDiveTactics(variantCalc, context.getShip().getSystem().getMaxAmmo() == 2 ? 1 : 2))
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
            @Override
            public String getSystemName() {
                return "expsp_coreoverclock";
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
                return "expsp_cossackjets";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (!this.approachingModDest())
                    flag = true;
                if (this.hasEnemy())
                    flag = true;
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
                return "expsp_harmony";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (!this.approachingModDest())
                    flag = true;
                if (this.hasEnemy())
                    flag = true;
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
                return "expsp_hybridfocus";
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
                return "expsp_energizedcharge";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = 500f;
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
                return "expsp_beamovercharge";
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
                return "expsp_burstcore";
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
            String targetID;

            @Override
            public String getSystemName() {
                return "expsp_disruptor";
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
            String targetID;

            @Override
            public String getSystemName() {
                return "expsp_lock";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                if (this.hasEnemy()) {
                    this.targetID = context.getTargetEnemy() != null
                            ? context.getTargetEnemy().getId()
                            : context.getPriorityEnemy().getId();
                    if (Global.getCombatEngine().getCustomData().get(this.targetID + "_expsp_entropy_target_data") == null)
                        flag = false;
                }
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "expsp_bombardmentmode";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.isAttackWeaponfireing())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "expsp_pursuitburn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = 700f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(30f))
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
                return "expsp_energizedram_sys";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.hasEnemy())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }
}