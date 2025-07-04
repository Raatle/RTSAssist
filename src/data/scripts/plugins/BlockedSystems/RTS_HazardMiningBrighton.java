package data.scripts.plugins.BlockedSystems;

import data.scripts.plugins.Utils.RTS_Context;

public class RTS_HazardMiningBrighton {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "hmi_lidararray";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                if (!hasEnemy())
                    return (true);
                boolean flag = true;
                if (this.isAttackWeaponfireing())
                    flag = false;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }
}