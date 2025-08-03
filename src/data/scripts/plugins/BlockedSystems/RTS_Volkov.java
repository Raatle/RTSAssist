package data.scripts.plugins.BlockedSystems;

import com.fs.starfarer.api.Global;
import data.scripts.plugins.Utils.RTS_Context;

public class RTS_Volkov {

    /*
    *
    * */

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "vic_quantumlunge";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                return (true);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "vic_pirateBurn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2;
                this.addCoolDownOnCleanup();
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
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "vic_hunterDrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2;
                this.addCoolDownOnCleanup();
                if (withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(25f))
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                if (this.notFacingModDest(50f))
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
                return "vic_zealotDrive";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = false;
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2;
                this.addCoolDownOnCleanup();
                if (withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notFacingModDest(25f))
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                if (this.notFacingModDest(50f))
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
                return "vic_adaptiveassault";
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
                return "vic_DriftBoost";
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
                return "vic_defenceSuppressor";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                if (this.hasEnemy())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "vic_quantumteleporter";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 3;
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