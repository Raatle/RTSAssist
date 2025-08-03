package data.scripts.plugins.BlockedSystems;

import data.scripts.plugins.Utils.RTS_Context;

public class RTS_Volantian {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "volantian_phaseslip";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                init(context);
                boolean flag = false;
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2f;
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
    }
}