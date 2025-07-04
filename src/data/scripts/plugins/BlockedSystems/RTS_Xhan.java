package data.scripts.plugins.BlockedSystems;

import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.plugins.Utils.RTS_Context;

public class RTS_Xhan {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "xhan_maneuvering";
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
                return "xhan_skipdrive";
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
                return "xhan_microburn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = 800f;
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
                return "XHAN_TorchDrive";
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
            ShipAPI enemyPointer = null;
            @Override
            public String getSystemName() {
                return "XHAN_TemporalLock";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.hasEnemy()) {
                    enemyPointer = context.getTargetEnemy() != null ? context.getTargetEnemy() : context.getPriorityEnemy();
                    if (!context.getEngine().getCustomData().containsKey(enemyPointer.getId() + "_XHAN_TemporalLock_target"))
                        flag = false;
                }
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
                return "XHAN_TurboBurn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.notFacingModDest(40f))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "XHAN_MindControl";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                this.addCoolDownOnCleanup();
                if (this.hasEnemy()) {
                    flag = false;
                }
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
                return "XHAN_PhaseHop";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 3f;
                boolean flag = false;
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
                return "PAMED_Teleport";
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
    }
}